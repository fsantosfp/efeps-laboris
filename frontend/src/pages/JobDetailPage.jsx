import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom'; 
import api from '../services/api';
import ManageTeamModal from '../components/ManageTeamModal';
import { formatDecimalHours } from "../utils/formatters";
import "./JobDetailPage.css";

function JobDetailPage(){

    const { jobId } = useParams();
    const navigate = useNavigate();

    const [job, setJob] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);

    // Estados para edição
    const [isEditing, setIsEditing] = useState(false);
    const [editData, setEditData] = useState({
        status: '',
        billingRate: '',
        budget: '',
        startDate: '',
        endDate: '',
        responsibleName: '',
        responsiblePhone: '',
        responsibleEmail: ''
    });

    // Estados para o Timesheet (Relatório de Presença)
    const [timesheetData, setTimesheetData] = useState(null);
    const [loadingTimesheet, setLoadingTimesheet] = useState(false);
    const [timesheetError, setTimesheetError] = useState('');
    const [timesheetStart, setTimesheetStart] = useState('');
    const [timesheetEnd, setTimesheetEnd] = useState('');

    // Estados para exclusão segura
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [confirmAddressInput, setConfirmAddressInput] = useState('');

    const fetchJobDetails = useCallback( async ()=>{
        setLoading(true);

        try {
            const response = await api.get(`/jobs/${jobId}`);
            setJob(response.data);
        } catch (err) {
            console.error("Erro ao buscar detalhes do trabalho:", err);
            setError('Trabalho não encontrado ou falha ao carregar os dados.');
        } finally {
            setLoading(false);
        }
         
    }, [jobId]);

    const fetchTimesheet = useCallback(async (start = '', end = '') => {
        setLoadingTimesheet(true);
        setTimesheetError('');
        try {
            let url = `/reports/jobs/${jobId}/timesheet`;
            const params = [];
            if (start) {
                params.push(`start=${new Date(start).toISOString()}`);
            }
            if (end) {
                params.push(`end=${new Date(end + 'T23:59:59.999Z').toISOString()}`);
            }
            if (params.length > 0) {
                url += `?${params.join('&')}`;
            }
            const response = await api.get(url);
            setTimesheetData(response.data);
        } catch (err) {
            console.error("Erro ao buscar timesheet do trabalho:", err);
            setTimesheetError('Não foi possível carregar o relatório de presença.');
        } finally {
            setLoadingTimesheet(false);
        }
    }, [jobId]);

    useEffect(() => {
        fetchJobDetails();
        fetchTimesheet();
    }, [fetchJobDetails, fetchTimesheet]);

    const handleStartEdit = () => {
        if (!job) return;
        setEditData({
            status: job.status,
            billingRate: job.billingRate !== null && job.billingRate !== undefined ? job.billingRate.toString() : '',
            budget: job.budget !== null && job.budget !== undefined ? job.budget.toString() : '',
            startDate: job.startDate || '',
            endDate: job.endDate || '',
            responsibleName: job.responsibleName || '',
            responsiblePhone: job.responsiblePhone || '',
            responsibleEmail: job.responsibleEmail || ''
        });
        setIsEditing(true);
    };

    const handleEditChange = (e) => {
        const { name, value } = e.target;
        setEditData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSaveEdit = async (e) => {
        e.preventDefault();
        try {
            const dataToSend = {
                status: editData.status,
                billingRate: parseFloat(editData.billingRate),
                budget: parseFloat(editData.budget),
                startDate: editData.startDate,
                endDate: editData.endDate || null,
                responsibleName: editData.responsibleName,
                responsiblePhone: editData.responsiblePhone,
                responsibleEmail: editData.responsibleEmail || null
            };

            await api.patch(`/jobs/${jobId}`, dataToSend);
            alert('Trabalho atualizado com sucesso!');
            setIsEditing(false);
            fetchJobDetails();
        } catch (err) {
            console.error("Erro ao atualizar trabalho:", err);
            alert(err.response?.data?.message || 'Falha ao atualizar o trabalho.');
        }
    };

    const handleConfirmDelete = async () => {
        if (confirmAddressInput !== job.address) {
            alert("Endereço incorreto. Deleção não autorizada.");
            return;
        }

        try {
            await api.delete(`/jobs/${jobId}`);
            alert('Trabalho excluído/cancelado com sucesso!');
            setIsDeleteModalOpen(false);
            setConfirmAddressInput('');
            navigate('/dashboard');
        } catch(err){
            console.error("Erro ao deletar trabalho:", err);
            alert(err.response?.data?.message || 'Falha ao deletar o trabalho.');
        }
    };

    if(loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
                <span>Carregando detalhes do trabalho...</span>
            </div>
        );
    }

    if(error) {
        return (
            <div className="login-error-alert" style={{ marginTop: '20px', textAlign: 'left' }}>
                {error}
            </div>
        );
    }

    return (
        <div className="job-detail-container">
            <header className="job-detail-header">
                <h2 className="job-title">Detalhe do Trabalho</h2>
                {job && !isEditing && (
                    <button className="btn btn-secondary" onClick={handleStartEdit}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"/>
                        </svg>
                        Editar Trabalho
                    </button>
                )}
            </header>

            { job && (
                <div>
                    <div className="job-detail-grid">
                        {/* Left Card: Info or Edit Form */}
                        {isEditing ? (
                            <form className="card-surface" onSubmit={handleSaveEdit}>
                                <h3>Editar Informações do Trabalho</h3>
                                
                                <div className="job-detail-edit-form-grid">
                                    <div className="form-group">
                                        <label className="form-label">Status:*</label>
                                        <select className="form-select" name="status" value={editData.status} onChange={handleEditChange} required>
                                            <option value="PENDING">A fazer</option>
                                            <option value="IN_PROGRESS">Em Andamento</option>
                                            <option value="COMPLETED">Concluído</option>
                                            {job.status === 'DELETED' && <option value="DELETED">Cancelado (Deletado)</option>}
                                        </select>
                                    </div>

                                    <div className="form-group">
                                        <label className="form-label">Valor/Hora (Faturamento):*</label>
                                        <input className="form-input" type="number" name="billingRate" value={editData.billingRate} onChange={handleEditChange} required step="0.01" />
                                    </div>

                                    <div className="form-group">
                                        <label className="form-label">Orçamento:*</label>
                                        <input className="form-input" type="number" name="budget" value={editData.budget} onChange={handleEditChange} required step="0.01" />
                                    </div>

                                    <div className="form-group">
                                        <label className="form-label">Data de Início:*</label>
                                        <input className="form-input" type="date" name="startDate" value={editData.startDate} onChange={handleEditChange} required />
                                    </div>

                                    <div className="form-group">
                                        <label className="form-label">Data de Término (Estimada):</label>
                                        <input className="form-input" type="date" name="endDate" value={editData.endDate} onChange={handleEditChange} />
                                    </div>
                                </div>

                                <h4 style={{ marginTop: '24px' }}>Dados do Responsável</h4>
                                <div className="job-detail-edit-form-grid">
                                    <div className="form-group">
                                        <label className="form-label">Nome do Responsável:*</label>
                                        <input className="form-input" type="text" name="responsibleName" value={editData.responsibleName} onChange={handleEditChange} required />
                                    </div>

                                    <div className="form-group">
                                        <label className="form-label">Telefone do Responsável:*</label>
                                        <input className="form-input" type="text" name="responsiblePhone" value={editData.responsiblePhone} onChange={handleEditChange} required />
                                    </div>

                                    <div className="form-group full-width">
                                        <label className="form-label">E-mail do Responsável (Opcional):</label>
                                        <input className="form-input" type="email" name="responsibleEmail" value={editData.responsibleEmail} onChange={handleEditChange} />
                                    </div>
                                </div>

                                <div className="form-actions">
                                    <button className="btn btn-primary" type="submit">Salvar Alterações</button>
                                    <button className="btn btn-secondary" type="button" onClick={() => setIsEditing(false)}>Cancelar</button>
                                </div>
                            </form>
                        ) : (
                            <div className="card-surface">
                                <h3>Informações Gerais</h3>
                                <ul className="detail-list">
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                                            <span className="detail-label">Contratante</span>
                                        </div>
                                        <div className="detail-value">{job.clientName || 'Não informado'}</div>
                                    </li>
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                                            <span className="detail-label">Endereço</span>
                                        </div>
                                        <div className="detail-value">{job.address}</div>
                                    </li>
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                                            <span className="detail-label">Status</span>
                                        </div>
                                        <div className="detail-value">
                                            <span className={`status-badge ${job.status.toLowerCase()}`}>
                                                {job.status === 'PENDING' ? 'A fazer' : 
                                                 job.status === 'IN_PROGRESS' ? 'Em andamento' : 
                                                 job.status === 'COMPLETED' ? 'Concluído' : 'Cancelado (Deletado)'}
                                            </span>
                                        </div>
                                    </li>
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                                            <span className="detail-label">Faturamento/Hora</span>
                                        </div>
                                        <div className="detail-value">R$ {job.billingRate !== null && job.billingRate !== undefined ? job.billingRate.toFixed(2) : '0.00'}</div>
                                    </li>
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="4" width="20" height="16" rx="2" ry="2"/><line x1="12" y1="18" x2="12" y2="18"/><line x1="12" y1="14" x2="12" y2="14"/><line x1="16" y1="14" x2="16" y2="14"/><line x1="8" y1="14" x2="8" y2="14"/><line x1="8" y1="10" x2="8" y2="10"/><line x1="12" y1="10" x2="12" y2="10"/><line x1="16" y1="10" x2="16" y2="10"/></svg>
                                            <span className="detail-label">Orçamento Total</span>
                                        </div>
                                        <div className="detail-value">R$ {job.budget !== null && job.budget !== undefined ? job.budget.toFixed(2) : '0.00'}</div>
                                    </li>
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                                            <span className="detail-label">Data de Início</span>
                                        </div>
                                        <div className="detail-value">{job.startDate}</div>
                                    </li>
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                                            <span className="detail-label">Data de Término (Est.)</span>
                                        </div>
                                        <div className="detail-value">{job.endDate || 'Não informada'}</div>
                                    </li>
                                </ul>
                            </div>
                        )}

                        {/* Right Cards: Responsible and Team */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                            <div className="card-surface">
                                <h3>Responsável</h3>
                                <ul className="detail-list">
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                                            <span className="detail-label">Nome do Responsável</span>
                                        </div>
                                        <div className="detail-value">{job.responsibleName}</div>
                                    </li>
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
                                            <span className="detail-label">Telefone</span>
                                        </div>
                                        <div className="detail-value">{job.responsiblePhone}</div>
                                    </li>
                                    <li className="detail-item">
                                        <div className="detail-label-row">
                                            <svg className="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                                            <span className="detail-label">E-mail</span>
                                        </div>
                                        <div className="detail-value">{job.responsibleEmail || 'Não informado'}</div>
                                    </li>
                                </ul>
                            </div>

                            <div className="card-surface">
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px', marginBottom: '16px' }}>
                                    <h3 style={{ borderBottom: 'none', paddingBottom: 0, margin: 0 }}>Equipe</h3>
                                    <button className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '12.5px' }} onClick={() => setIsModalOpen(true)}>
                                        Gerenciar
                                    </button>
                                </div>
                                {job.assignedTeam && job.assignedTeam.length > 0 ? (
                                    <ul className="team-list">
                                        {job.assignedTeam.map(employee => {
                                            const initials = employee.name ? employee.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() : 'EE';
                                            return (
                                                <li className="team-member-item" key={employee.id}>
                                                    <span className="team-member-avatar">{initials}</span>
                                                    <span>{employee.name}</span>
                                                </li>
                                            );
                                        })}
                                    </ul>
                                ) : (
                                    <p className="empty-team-text">Nenhum funcionário designado para este trabalho ainda.</p>
                                )}
                            </div>
                        </div>
                    </div>

                    <h3 className="timesheet-section-title">Relatório de Presença e Horas Diárias</h3>
                    <div className="timesheet-filters">
                        <div className="filter-group">
                            <label className="filter-label">Data de Início:</label>
                            <input 
                                className="filter-input"
                                type="date" 
                                value={timesheetStart} 
                                onChange={(e) => setTimesheetStart(e.target.value)} 
                            />
                        </div>
                        <div className="filter-group">
                            <label className="filter-label">Data de Término:</label>
                            <input 
                                className="filter-input"
                                type="date" 
                                value={timesheetEnd} 
                                onChange={(e) => setTimesheetEnd(e.target.value)} 
                            />
                        </div>
                        <button 
                            className="btn btn-primary"
                            onClick={() => fetchTimesheet(timesheetStart, timesheetEnd)}
                            disabled={loadingTimesheet}
                        >
                            Filtrar
                        </button>
                        <button 
                            className="btn btn-secondary"
                            onClick={() => {
                                setTimesheetStart('');
                                setTimesheetEnd('');
                                fetchTimesheet('', '');
                            }}
                            disabled={loadingTimesheet}
                        >
                            Limpar
                        </button>
                    </div>

                    {timesheetError && <p className="timesheet-error">{timesheetError}</p>}
                    
                    {loadingTimesheet ? (
                        <p style={{ textAlign: 'left', color: '#64748b' }}>Carregando dados do relatório...</p>
                    ) : (() => {
                        const flatRows = [];
                        if (timesheetData && timesheetData.employeeTimesheets) {
                            timesheetData.employeeTimesheets.forEach(empSheet => {
                                if (empSheet.dailyHours) {
                                    empSheet.dailyHours.forEach(day => {
                                        flatRows.push({
                                            date: day.date,
                                            employeeName: empSheet.employeeName,
                                            employeeId: empSheet.employeeId,
                                            start: day.start ? day.start.substring(0, 5) : '-',
                                            end: day.end ? day.end.substring(0, 5) : '-',
                                            hoursWorked: day.hoursWorked,
                                            displacement: day.displacement || '-',
                                            displacementHours: day.displacementHours || 0,
                                            interval: day.interval
                                        });
                                    });
                                }
                            });
                        }

                        if (flatRows.length === 0) {
                            return <p style={{ margin: 0, color: '#64748b', textAlign: 'left', fontStyle: 'italic' }}>Não há registros de ponto para este trabalho no período selecionado.</p>;
                        }

                        flatRows.sort((a, b) => {
                            const dateCompare = b.date.localeCompare(a.date);
                            if (dateCompare !== 0) return dateCompare;
                            return a.employeeName.localeCompare(b.employeeName);
                        });

                        return (
                            <div className="table-wrapper">
                                <table className="timesheet-table">
                                    <thead>
                                        <tr>
                                            <th>Data</th>
                                            <th>Funcionário</th>
                                            <th>Entrada</th>
                                            <th>Saída</th>
                                            <th className="text-right">Deslocamento (h)</th>
                                            <th>Deslocamento (Partida)</th>
                                            <th className="text-right">Intervalo (h)</th>
                                            <th className="text-right">Total Trabalhado (h)</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {flatRows.map((row, idx) => (
                                            <tr key={idx}>
                                                <td>{row.date}</td>
                                                <td style={{ fontWeight: 600 }}>{row.employeeName}</td>
                                                <td>{row.start}</td>
                                                <td>{row.end}</td>
                                                <td className="text-right">{row.displacementHours > 0 ? formatDecimalHours(row.displacementHours) : '-'}</td>
                                                <td>{row.displacement}</td>
                                                <td className="text-right">{formatDecimalHours(row.interval)}</td>
                                                <td className="text-right" style={{ fontWeight: 600, color: '#0a52c6' }}>{formatDecimalHours(row.hoursWorked)}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        );
                    })()}

                    {job.status === 'PENDING' && !isEditing && (
                        <div className="danger-card">
                            <h4 className="danger-title">Zona de Ação Perigosa</h4>
                            <p className="danger-desc">
                                Esta ação irá deletar o trabalho permanentemente. Esta operação só é permitida enquanto o trabalho estiver com status "A fazer".
                            </p>
                            <button className="btn btn-danger" onClick={() => setIsDeleteModalOpen(true)}>
                                Deletar Trabalho
                            </button>
                        </div>
                    )}
                </div>
            )}

            {isModalOpen && (
                <ManageTeamModal 
                    jobId={jobId}
                    currentTeam={job.assignedTeam}
                    onClose={() => setIsModalOpen(false)}
                    onTeamUpdate={fetchJobDetails}
                />
            )}

            {/* Modal de exclusão segura */}
            {isDeleteModalOpen && (
                <div className="modal-backdrop">
                    <div className="modal-card">
                        <h3 className="modal-title">Confirmar Exclusão</h3>
                        <p className="modal-desc">Esta ação é permanente. Para prosseguir, digite exatamente o endereço do trabalho abaixo:</p>
                        <div className="modal-highlight-box">
                            {job.address}
                        </div>
                        <input 
                            type="text" 
                            className="modal-confirm-input"
                            value={confirmAddressInput} 
                            onChange={(e) => setConfirmAddressInput(e.target.value)} 
                            placeholder="Digite o endereço exato do trabalho"
                        />
                        <div className="modal-actions">
                            <button className="btn btn-secondary" onClick={() => { setIsDeleteModalOpen(false); setConfirmAddressInput(''); }}>
                                Cancelar
                            </button>
                            <button 
                                className="btn btn-danger"
                                onClick={handleConfirmDelete} 
                                disabled={confirmAddressInput !== job.address}
                                style={{ 
                                    opacity: confirmAddressInput === job.address ? 1 : 0.5,
                                    cursor: confirmAddressInput === job.address ? 'pointer' : 'not-allowed'
                                }}
                            >
                                Confirmar Exclusão
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default JobDetailPage;