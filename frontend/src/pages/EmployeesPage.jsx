import React, { useState, useEffect, useRef } from "react";
import api from "../services/api";
import "./EmployeesPage.css";

function EmployeesPage() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showInactive, setShowInactive] = useState(false);

  // Modal Dialog Refs
  const createDialogRef = useRef(null);
  const salaryDialogRef = useRef(null);
  const deactivateDialogRef = useRef(null);

  // Selected Employee & Salaries
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [salaries, setSalaries] = useState([]);
  const [salariesLoading, setSalariesLoading] = useState(false);
  const [salariesError, setSalariesError] = useState("");

  // Create Form State
  const [createForm, setCreateForm] = useState({
    name: "",
    email: "",
    hourlyRate: "",
    effectiveDate: new Date().toISOString().split("T")[0],
  });
  const [createLoading, setCreateLoading] = useState(false);
  const [createError, setCreateError] = useState("");

  // Salary Form State
  const [salaryForm, setSalaryForm] = useState({
    hourlyRate: "",
    effectiveDate: new Date().toISOString().split("T")[0],
  });
  const [salaryFormLoading, setSalaryFormLoading] = useState(false);
  const [salaryFormError, setSalaryFormError] = useState("");

  // Deactivate State
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [deactivateError, setDeactivateError] = useState("");

  // Activate Ref & State
  const activateDialogRef = useRef(null);
  const [activateLoading, setActivateLoading] = useState(false);
  const [activateError, setActivateError] = useState("");

  // Fetch employees
  const fetchEmployees = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await api.get("/employees");
      console.log("Employees data from API:", response.data);
      setEmployees(response.data);
    } catch (err) {
      console.error("Erro ao buscar funcionários:", err);
      setError("Não foi possível carregar a lista de funcionários.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEmployees();
  }, []);

  // Helper to bind light-dismiss fallback
  const bindLightDismiss = (dialogRef, onClose) => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    const handleOutsideClick = (event) => {
      if (event.target !== dialog) return;
      const rect = dialog.getBoundingClientRect();
      const isDialogContent = (
        rect.top <= event.clientY &&
        event.clientY <= rect.top + rect.height &&
        rect.left <= event.clientX &&
        event.clientX <= rect.left + rect.width
      );
      if (!isDialogContent) {
        dialog.close();
        if (onClose) onClose();
      }
    };

    if (!("closedBy" in HTMLDialogElement.prototype)) {
      dialog.addEventListener("click", handleOutsideClick);
      return () => dialog.removeEventListener("click", handleOutsideClick);
    }
  };

  useEffect(() => {
    const cleanupCreate = bindLightDismiss(createDialogRef, handleCloseCreate);
    const cleanupSalary = bindLightDismiss(salaryDialogRef, handleCloseSalary);
    const cleanupDeactivate = bindLightDismiss(deactivateDialogRef, handleCloseDeactivate);
    const cleanupActivate = bindLightDismiss(activateDialogRef, handleCloseActivate);

    return () => {
      if (cleanupCreate) cleanupCreate();
      if (cleanupSalary) cleanupSalary();
      if (cleanupDeactivate) cleanupDeactivate();
      if (cleanupActivate) cleanupActivate();
    };
  }, [selectedEmployee]);

  // Open / Close Handlers
  const handleOpenCreate = () => {
    setCreateError("");
    setCreateForm({
      name: "",
      email: "",
      hourlyRate: "",
      effectiveDate: new Date().toISOString().split("T")[0],
    });
    createDialogRef.current.showModal();
  };

  const handleCloseCreate = () => {
    createDialogRef.current.close();
  };

  const handleOpenSalary = async (employee) => {
    setSelectedEmployee(employee);
    setSalariesError("");
    setSalaryFormError("");
    setSalaryForm({
      hourlyRate: "",
      effectiveDate: new Date().toISOString().split("T")[0],
    });
    salaryDialogRef.current.showModal();

    setSalariesLoading(true);
    try {
      const response = await api.get(`/employees/${employee.id}/salaries`);
      setSalaries(response.data);
    } catch (err) {
      console.error("Erro ao buscar histórico salarial:", err);
      setSalariesError("Não foi possível carregar o histórico de salários.");
    } finally {
      setSalariesLoading(false);
    }
  };

  const handleCloseSalary = () => {
    setSelectedEmployee(null);
    setSalaries([]);
    salaryDialogRef.current.close();
  };

  const handleOpenDeactivate = (employee) => {
    setSelectedEmployee(employee);
    setDeactivateError("");
    deactivateDialogRef.current.showModal();
  };

  const handleCloseDeactivate = () => {
    setSelectedEmployee(null);
    deactivateDialogRef.current.close();
  };

  const handleOpenActivate = (employee) => {
    setSelectedEmployee(employee);
    setActivateError("");
    activateDialogRef.current.showModal();
  };

  const handleCloseActivate = () => {
    setSelectedEmployee(null);
    activateDialogRef.current.close();
  };

  // Submit Actions
  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setCreateError("");
    setCreateLoading(true);

    try {
      await api.post("/employees", {
        name: createForm.name,
        email: createForm.email,
        hourlyRate: parseFloat(createForm.hourlyRate),
        effectiveDate: createForm.effectiveDate,
      });
      handleCloseCreate();
      await fetchEmployees();
    } catch (err) {
      console.error("Erro ao cadastrar funcionário:", err);
      const errMsg = err.response?.data?.message || "Erro ao cadastrar funcionário. Verifique os dados.";
      setCreateError(errMsg);
    } finally {
      setCreateLoading(false);
    }
  };

  const handleSalarySubmit = async (e) => {
    e.preventDefault();
    setSalaryFormError("");
    setSalaryFormLoading(true);

    try {
      await api.post(`/employees/${selectedEmployee.id}/salaries`, {
        hourlyRate: parseFloat(salaryForm.hourlyRate),
        effectiveDate: salaryForm.effectiveDate,
      });
      
      // Reload timeline and employees list
      const response = await api.get(`/employees/${selectedEmployee.id}/salaries`);
      setSalaries(response.data);
      
      setSalaryForm({
        hourlyRate: "",
        effectiveDate: new Date().toISOString().split("T")[0],
      });
      
      await fetchEmployees();
    } catch (err) {
      console.error("Erro ao adicionar taxa salarial:", err);
      const errMsg = err.response?.data?.message || "Erro ao adicionar nova taxa salarial.";
      setSalaryFormError(errMsg);
    } finally {
      setSalaryFormLoading(false);
    }
  };

  const handleDeactivateSubmit = async () => {
    setDeactivateError("");
    setDeactivateLoading(true);

    try {
      await api.delete(`/employees/${selectedEmployee.id}`);
      handleCloseDeactivate();
      await fetchEmployees();
    } catch (err) {
      console.error("Erro ao inativar funcionário:", err);
      const errMsg = err.response?.data?.message || "Não foi possível inativar o funcionário.";
      setDeactivateError(errMsg);
    } finally {
      setDeactivateLoading(false);
    }
  };

  const handleActivateSubmit = async () => {
    setActivateError("");
    setActivateLoading(true);

    try {
      await api.post(`/employees/${selectedEmployee.id}/activate`);
      handleCloseActivate();
      await fetchEmployees();
    } catch (err) {
      console.error("Erro ao ativar funcionário:", err);
      const errMsg = err.response?.data?.message || "Não foi possível reativar o funcionário.";
      setActivateError(errMsg);
    } finally {
      setActivateLoading(false);
    }
  };

  // Filtering
  const filteredEmployees = employees.filter((emp) => {
    if (showInactive) return true;
    return emp.status === "ACTIVE";
  });

  return (
    <div className="employees-page-wrapper">
      {/* Background Glowing Orbs */}
      <div className="employees-glow-orb orb-indigo"></div>
      <div className="employees-glow-orb orb-teal"></div>

      <div className="employees-header">
        <h2>Equipe da Empresa</h2>
        <div className="header-actions">
          <div
            className={`toggle-container ${showInactive ? "active" : ""}`}
            onClick={() => setShowInactive(!showInactive)}
          >
            <div className="toggle-switch"></div>
            <span>Mostrar Inativos</span>
          </div>
          <button className="btn btn-primary" onClick={handleOpenCreate}>
            + Cadastrar Funcionário
          </button>
        </div>
      </div>

      {error && <p className="alert alert-error" style={{ marginBottom: "20px" }}>{error}</p>}

      {loading ? (
        <div className="loading-spinner"></div>
      ) : (
        <div className="table-container">
          {filteredEmployees.length === 0 ? (
            <div className="empty-state">
              <p>Nenhum funcionário encontrado.</p>
            </div>
          ) : (
            <table className="modern-table">
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>E-mail</th>
                  <th>Status</th>
                  <th>Taxa Atual</th>
                  <th>Vigência</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {filteredEmployees.map((emp) => (
                  <tr key={emp.id} className={emp.status === "INACTIVE" ? "row-inactive" : ""}>
                    <td style={{ fontWeight: 600 }}>{emp.name}</td>
                    <td>{emp.email}</td>
                    <td>
                      <span className={`badge ${emp.status === "ACTIVE" ? "badge-active" : "badge-inactive"}`}>
                        {emp.status === "ACTIVE" ? "Ativo" : "Inativo"}
                      </span>
                    </td>
                    <td>
                      {emp.hourlyRate != null
                        ? `R$ ${parseFloat(emp.hourlyRate).toFixed(2)}/h`
                        : "Não definida"}
                    </td>
                    <td>
                      {emp.effectiveDate
                        ? new Date(emp.effectiveDate + "T00:00:00").toLocaleDateString("pt-BR")
                        : "-"}
                    </td>
                    <td>
                      <div className="action-buttons-group">
                        <button
                          className="btn-action btn-action-primary"
                          onClick={() => handleOpenSalary(emp)}
                        >
                          Histórico Salarial
                        </button>
                        {emp.status === "ACTIVE" && (
                          <button
                            className="btn-action btn-action-danger"
                            onClick={() => handleOpenDeactivate(emp)}
                          >
                            Inativar
                          </button>
                        )}
                        {emp.status === "INACTIVE" && (
                          <button
                            className="btn-action btn-action-success"
                            onClick={() => handleOpenActivate(emp)}
                          >
                            Reativar
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      <dialog ref={createDialogRef} className="glass-dialog" closedby="any">
        <div className="dialog-header">
          <h3>Cadastrar Novo Funcionário</h3>
          <button className="btn-close" onClick={handleCloseCreate}>
            &times;
          </button>
        </div>
        {createError && <p className="alert alert-error" style={{ marginBottom: "15px" }}>{createError}</p>}
        <form onSubmit={handleCreateSubmit} className="dialog-form">
          <div className="form-group">
            <label className="form-label">Nome Completo</label>
            <input
              type="text"
              required
              className="form-input"
              value={createForm.name}
              onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
              placeholder="Ex: Felipe Silva"
            />
          </div>
          <div className="form-group">
            <label className="form-label">E-mail Corporativo</label>
            <input
              type="email"
              required
              className="form-input"
              value={createForm.email}
              onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
              placeholder="Ex: felipe@empresa.com"
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Taxa Horária Inicial (R$)</label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                required
                className="form-input"
                value={createForm.hourlyRate}
                onChange={(e) => setCreateForm({ ...createForm, hourlyRate: e.target.value })}
                placeholder="Ex: 25.50"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Vigência Inicial</label>
              <input
                type="date"
                required
                className="form-input"
                value={createForm.effectiveDate}
                onChange={(e) => setCreateForm({ ...createForm, effectiveDate: e.target.value })}
              />
            </div>
          </div>
          <div className="dialog-actions">
            <button type="button" className="btn btn-secondary" onClick={handleCloseCreate}>
              Cancelar
            </button>
            <button type="submit" className="btn btn-primary" disabled={createLoading}>
              {createLoading ? "Salvando..." : "Cadastrar"}
            </button>
          </div>
        </form>
      </dialog>

      {/* MODAL 2: Histórico Salarial e Nova Vigência */}
      <dialog ref={salaryDialogRef} className="glass-dialog" closedby="any" style={{ maxWidth: "600px" }}>
        <div className="dialog-header">
          <h3>Histórico Salarial</h3>
          <button className="btn-close" onClick={handleCloseSalary}>
            &times;
          </button>
        </div>
        
        {selectedEmployee && (
          <p style={{ color: "var(--text-muted)", marginTop: "-15px", marginBottom: "20px", fontSize: "14px" }}>
            Funcionário(a): <span style={{ color: "var(--color-primary)", fontWeight: 600 }}>{selectedEmployee.name}</span>
          </p>
        )}

        {salariesError && <p className="alert alert-error" style={{ marginBottom: "15px" }}>{salariesError}</p>}

        {salariesLoading ? (
          <div className="loading-spinner" style={{ margin: "20px auto" }}></div>
        ) : (
          <div className="timeline-container">
            {salaries.length === 0 ? (
              <p style={{ textAlign: "center", color: "var(--text-muted)" }}>Nenhum salário registrado.</p>
            ) : (
              <ul className="timeline">
                {salaries.map((sal, idx) => (
                  <li key={sal.id} className="timeline-item">
                    <div className="timeline-dot"></div>
                    <div className="timeline-content">
                      <div className="timeline-header">
                        <span className="timeline-rate">R$ {parseFloat(sal.hourlyRate).toFixed(2)} / hora</span>
                        {idx === 0 && <span className="timeline-badge">Atual</span>}
                      </div>
                      <div className="timeline-date">
                        Vigência: {new Date(sal.effectiveDate + "T00:00:00").toLocaleDateString("pt-BR")}
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}

        {selectedEmployee && selectedEmployee.status === "ACTIVE" && (
          <div style={{ borderTop: "1px solid var(--border-subtle)", paddingTop: "20px" }}>
            <h4 style={{ margin: "0 0 15px 0", fontSize: "16px", color: "var(--text-main)" }}>Adicionar Nova Vigência</h4>
            {salaryFormError && <p className="alert alert-error" style={{ marginBottom: "15px" }}>{salaryFormError}</p>}
            <form onSubmit={handleSalarySubmit} className="dialog-form">
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Nova Taxa Horária (R$)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    required
                    className="form-input"
                    value={salaryForm.hourlyRate}
                    onChange={(e) => setSalaryForm({ ...salaryForm, hourlyRate: e.target.value })}
                    placeholder="Ex: 30.00"
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Data de Vigência</label>
                  <input
                    type="date"
                    required
                    className="form-input"
                    value={salaryForm.effectiveDate}
                    onChange={(e) => setSalaryForm({ ...salaryForm, effectiveDate: e.target.value })}
                  />
                </div>
              </div>
              <div className="dialog-actions">
                <button type="submit" className="btn btn-primary" disabled={salaryFormLoading}>
                  {salaryFormLoading ? "Adicionando..." : "Adicionar Taxa"}
                </button>
              </div>
            </form>
          </div>
        )}
      </dialog>

      <dialog ref={deactivateDialogRef} className="glass-dialog" closedby="any">
        <div className="dialog-header">
          <h3>Confirmar Inativação</h3>
          <button className="btn-close" onClick={handleCloseDeactivate}>
            &times;
          </button>
        </div>
        {deactivateError && <p className="alert alert-error" style={{ marginBottom: "15px" }}>{deactivateError}</p>}
        {selectedEmployee && (
          <div style={{ marginBottom: "25px" }}>
            <p style={{ margin: "0 0 10px 0" }}>
              Tem certeza que deseja inativar o(a) funcionário(a) <strong>{selectedEmployee.name}</strong>?
            </p>
            <p style={{ color: "var(--color-danger)", fontSize: "13px", margin: 0 }}>
              Aviso: Esta ação impedirá que ele(a) acesse o sistema ou registre novos pontos de presença.
            </p>
          </div>
        )}
        <div className="dialog-actions">
          <button className="btn btn-secondary" onClick={handleCloseDeactivate}>
            Cancelar
          </button>
          <button className="btn btn-danger" onClick={handleDeactivateSubmit} disabled={deactivateLoading}>
            {deactivateLoading ? "Inativando..." : "Confirmar Inativação"}
          </button>
        </div>
      </dialog>

      <dialog ref={activateDialogRef} className="glass-dialog" closedby="any">
        <div className="dialog-header">
          <h3>Confirmar Reativação</h3>
          <button className="btn-close" onClick={handleCloseActivate}>
            &times;
          </button>
        </div>
        {activateError && <p className="alert alert-error" style={{ marginBottom: "15px" }}>{activateError}</p>}
        {selectedEmployee && (
          <div style={{ marginBottom: "25px" }}>
            <p style={{ margin: "0 0 10px 0" }}>
              Tem certeza que deseja reativar o(a) funcionário(a) <strong>{selectedEmployee.name}</strong>?
            </p>
            <p style={{ color: "var(--color-success)", fontSize: "13px", margin: 0 }}>
              Aviso: Esta ação reabilitará o acesso do funcionário ao sistema e permitirá que ele registre ponto.
            </p>
          </div>
        )}
        <div className="dialog-actions">
          <button className="btn btn-secondary" onClick={handleCloseActivate}>
            Cancelar
          </button>
          <button className="btn btn-primary" onClick={handleActivateSubmit} disabled={activateLoading}>
            {activateLoading ? "Ativando..." : "Confirmar Reativação"}
          </button>
        </div>
      </dialog>
    </div>
  );
}

export default EmployeesPage;
