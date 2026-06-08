import React, { useState, useEffect } from "react";
import api from '../services/api';
import { formatDecimalHours } from "../utils/formatters";
import './ReportsPage.css';

const formatTime = (isoString) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
};

const formatDate = (isoString) => {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleDateString([], { day: '2-digit', month: '2-digit', year: 'numeric' });
};

function ReportsPage() {
    const [activeTab, setActiveTab] = useState('payroll'); // 'payroll', 'jobCosts' or 'journey'

    // Common Loading/Error states
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    // Tab 1: Payroll State
    const [payrollStart, setPayrollStart] = useState('');
    const [payrollEnd, setPayrollEnd] = useState('');
    const [payrollData, setPayrollData] = useState(null);

    // Tab 2: Job Costs State
    const [jobs, setJobs] = useState([]);
    const [selectedJobIds, setSelectedJobIds] = useState([]);
    const [costStart, setCostStart] = useState('');
    const [costEnd, setCostEnd] = useState('');
    const [jobsCostsData, setJobsCostsData] = useState([]);
    const [loadingJobs, setLoadingJobs] = useState(false);

    // Tab 3: Journey State
    const [employees, setEmployees] = useState([]);
    const [selectedEmployeeIds, setSelectedEmployeeIds] = useState([]);
    const [journeyStart, setJourneyStart] = useState('');
    const [journeyEnd, setJourneyEnd] = useState('');
    const [journeyData, setJourneyData] = useState([]);
    const [loadingEmployees, setLoadingEmployees] = useState(false);

    const lastHourOfDay = "T23:59:59.999Z";

    // Fetch jobs for multi-select on mount or tab change
    useEffect(() => {
        if (activeTab === 'jobCosts' && jobs.length === 0) {
            const fetchJobs = async () => {
                setLoadingJobs(true);
                try {
                    const response = await api.get('/jobs');
                    setJobs(response.data);
                } catch (err) {
                    console.error("Erro ao buscar trabalhos para o filtro:", err);
                    setError(err.response?.data?.message || err.message || 'Não foi possível carregar a lista de trabalhos.');
                } finally {
                    setLoadingJobs(false);
                }
            };
            fetchJobs();
        }
    }, [activeTab, jobs.length]);

    // Fetch employees for multi-select on mount or tab change
    useEffect(() => {
        if (activeTab === 'journey' && employees.length === 0) {
            const fetchEmployees = async () => {
                setLoadingEmployees(true);
                try {
                    const response = await api.get('/employees');
                    setEmployees(response.data);
                } catch (err) {
                    console.error("Erro ao buscar funcionários para o filtro:", err);
                    setError(err.response?.data?.message || err.message || 'Não foi possível carregar a lista de funcionários.');
                } finally {
                    setLoadingEmployees(false);
                }
            };
            fetchEmployees();
        }
    }, [activeTab, employees.length]);

    // Handle Payroll Report Generation
    const handleGeneratePayroll = async (e) => {
        e.preventDefault();
        if (!payrollStart || !payrollEnd) {
            setError('Por favor, selecione as datas de início e fim.');
            return;
        }

        setError('');
        setLoading(true);
        setPayrollData(null);

        try {
            const startISO = new Date(payrollStart).toISOString();
            const endISO = new Date(payrollEnd + lastHourOfDay).toISOString();

            const response = await api.get(`/reports/payroll?start=${startISO}&end=${endISO}`);
            setPayrollData(response.data);
        } catch (err) {
            console.error('Erro ao gerar relatório de folha', err);
            setError(err.response?.data?.message || 'Falha ao gerar o relatório.');
        } finally {
            setLoading(false);
        }
    };

    // Handle Job Cost Report Generation
    const handleGenerateJobCosts = async (e) => {
        e.preventDefault();
        if (selectedJobIds.length === 0) {
            setError('Por favor, selecione pelo menos um trabalho.');
            return;
        }
        if (!costStart || !costEnd) {
            setError('Por favor, selecione as datas de início e fim.');
            return;
        }

        setError('');
        setLoading(true);
        setJobsCostsData([]);

        try {
            const startISO = new Date(costStart).toISOString();
            const endISO = new Date(costEnd + lastHourOfDay).toISOString();

            const reports = await Promise.all(
                selectedJobIds.map(async (id) => {
                    const res = await api.get(`/reports/jobs/${id}?start=${startISO}&end=${endISO}`);
                    return res.data;
                })
            );
            setJobsCostsData(reports);
        } catch (err) {
            console.error("Erro ao gerar relatórios de custo:", err);
            setError(err.response?.data?.message || 'Falha ao gerar os relatórios de custo.');
        } finally {
            setLoading(false);
        }
    };

    // Toggle job selection
    const handleToggleJobSelection = (jobId) => {
        setSelectedJobIds(prev => 
            prev.includes(jobId) 
                ? prev.filter(id => id !== jobId) 
                : [...prev, jobId]
        );
    };

    // Toggle employee selection
    const handleToggleEmployeeSelection = (employeeId) => {
        setSelectedEmployeeIds(prev => 
            prev.includes(employeeId) 
                ? prev.filter(id => id !== employeeId) 
                : [...prev, employeeId]
        );
    };

    // Handle Journey Report Generation
    const handleGenerateJourney = async (e) => {
        e.preventDefault();
        if (selectedEmployeeIds.length === 0) {
            setError('Por favor, selecione pelo menos um funcionário.');
            return;
        }
        if (!journeyStart || !journeyEnd) {
            setError('Por favor, selecione as datas de início e fim.');
            return;
        }

        setError('');
        setLoading(true);
        setJourneyData([]);

        try {
            const startISO = new Date(journeyStart).toISOString();
            const endISO = new Date(journeyEnd + lastHourOfDay).toISOString();
            const empIdsParam = selectedEmployeeIds.join(',');

            const response = await api.get(`/reports/employee-journey?employeeIds=${empIdsParam}&start=${startISO}&end=${endISO}`);
            setJourneyData(response.data);
        } catch (err) {
            console.error("Erro ao gerar relatório de jornada:", err);
            setError(err.response?.data?.message || 'Falha ao gerar o relatório de jornada.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="reports-page-wrapper">
            <div className="reports-glow-orb orb-indigo"></div>
            <div className="reports-glow-orb orb-teal"></div>

            <div className="reports-header">
                <h2>Central de Relatórios</h2>
            </div>

            <div className="reports-tabs">
                <button 
                    className={`reports-tab-btn ${activeTab === 'payroll' ? 'active' : ''}`}
                    onClick={() => { setActiveTab('payroll'); setError(''); }}
                >
                    Folha de Pagamento
                </button>
                <button 
                    className={`reports-tab-btn ${activeTab === 'jobCosts' ? 'active' : ''}`}
                    onClick={() => { setActiveTab('jobCosts'); setError(''); }}
                >
                    Custos de Serviços
                </button>
                <button 
                    className={`reports-tab-btn ${activeTab === 'journey' ? 'active' : ''}`}
                    onClick={() => { setActiveTab('journey'); setError(''); }}
                >
                    Jornada de Funcionários
                </button>
            </div>

            {error && <div className="error-message">{error}</div>}

            {activeTab === 'payroll' && (
                /* Tab 1: Payroll */
                <div>
                    <form className="reports-form" onSubmit={handleGeneratePayroll}>
                        <div className="form-label">Período de Apuração</div>
                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Data de Início</label>
                                <input 
                                    type="date" 
                                    className="form-input" 
                                    value={payrollStart} 
                                    onChange={(e) => setPayrollStart(e.target.value)} 
                                    required 
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Data de Término</label>
                                <input 
                                    type="date" 
                                    className="form-input" 
                                    value={payrollEnd} 
                                    onChange={(e) => setPayrollEnd(e.target.value)} 
                                    required 
                                />
                            </div>
                        </div>
                        <button type="submit" className="btn-generate" disabled={loading}>
                            {loading ? 'Gerando...' : 'Gerar Relatório'}
                        </button>
                    </form>

                    {loading && (
                        <div className="spinner-container">
                            <div className="spinner"></div>
                            <p>Calculando folha de pagamento...</p>
                        </div>
                    )}

                    {payrollData && !loading && (
                        <div className="job-report-block">
                            <h3 className="job-report-title">Resumo do Período</h3>
                            <div className="summary-container">
                                <div className="summary-box">
                                    <span className="summary-label">Total de Horas Trabalhadas</span>
                                    <span className="summary-value">{formatDecimalHours(payrollData.periodTotals.totalHours)}</span>
                                </div>
                                <div className="summary-box">
                                    <span className="summary-label">Valor Total a Pagar</span>
                                    <span className="summary-value">$ {payrollData.periodTotals.totalAmount.toFixed(2)}</span>
                                </div>
                            </div>

                            <h3 className="job-report-title" style={{ marginTop: '30px' }}>Detalhamento por Funcionário</h3>
                            <div className="table-container">
                                <table className="reports-table">
                                    <thead>
                                        <tr>
                                            <th>Funcionário</th>
                                            <th className="text-right">Total de Horas</th>
                                            <th className="text-right">Valor a Pagar</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {payrollData.employeePayrolls.map(data => (
                                            <tr key={data.employeeId}>
                                                <td>{data.employeeName}</td>
                                                <td className="text-right">{formatDecimalHours(data.totalHours)}</td>
                                                <td className="text-right">$ {data.totalAmount.toFixed(2)}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}
                </div>
            )}

            {activeTab === 'jobCosts' && (
                /* Tab 2: Job Costs */
                <div>
                    <form className="reports-form" onSubmit={handleGenerateJobCosts}>
                        <div className="jobs-selector-container">
                            <label className="form-label">Selecione os Trabalhos (Multi-seleção)</label>
                            {loadingJobs ? (
                                <p style={{ color: 'var(--text-muted)' }}>Carregando trabalhos...</p>
                            ) : jobs.length === 0 ? (
                                <p style={{ color: 'var(--text-muted)' }}>Nenhum trabalho cadastrado.</p>
                            ) : (
                                <div className="reports-jobs-grid">
                                    {jobs.map(job => {
                                        const isSelected = selectedJobIds.includes(job.id);
                                        return (
                                            <div 
                                                key={job.id} 
                                                className={`job-selection-card ${isSelected ? 'selected' : ''}`}
                                                onClick={() => handleToggleJobSelection(job.id)}
                                            >
                                                <input 
                                                    type="checkbox" 
                                                    className="job-selection-checkbox" 
                                                    checked={isSelected}
                                                    onChange={() => {}} // Controlled by card onClick
                                                />
                                                <div className="job-card-details">
                                                    <span className="job-card-client">{job.clientName || 'Cliente sem nome'}</span>
                                                    <span className="job-card-address">{job.address}</span>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Data de Início</label>
                                <input 
                                    type="date" 
                                    className="form-input" 
                                    value={costStart} 
                                    onChange={(e) => setCostStart(e.target.value)} 
                                    required 
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Data de Término</label>
                                <input 
                                    type="date" 
                                    className="form-input" 
                                    value={costEnd} 
                                    onChange={(e) => setCostEnd(e.target.value)} 
                                    required 
                                />
                            </div>
                        </div>
                        <button type="submit" className="btn-generate" disabled={loading || selectedJobIds.length === 0}>
                            {loading ? 'Gerando...' : 'Gerar Relatório'}
                        </button>
                    </form>

                    {loading && (
                        <div className="spinner-container">
                            <div className="spinner"></div>
                            <p>Calculando custos dos serviços selecionados...</p>
                        </div>
                    )}

                    {jobsCostsData.length > 0 && !loading && (
                        <div className="reports-results-stack">
                            {jobsCostsData.map((report, idx) => (
                                <div key={report.jobInfo.jobId || idx} className="job-report-block">
                                    <h3 className="job-report-title">
                                        Relatório de Custos: {report.jobInfo.clientName || 'Sem Cliente'}
                                    </h3>
                                    
                                    <div className="job-info-grid">
                                        <div className="info-item">
                                            <span className="info-label">Endereço</span>
                                            <span className="info-value">{report.jobInfo.address}</span>
                                        </div>
                                        <div className="info-item">
                                            <span className="info-label">Valor/Hora Base</span>
                                            <span className="info-value">
                                                $ {report.jobInfo.billingRate !== null && report.jobInfo.billingRate !== undefined ? report.jobInfo.billingRate.toFixed(2) : '0.00'}
                                            </span>
                                        </div>
                                        <div className="info-item">
                                            <span className="info-label">Total de Horas</span>
                                            <span className="info-value">{formatDecimalHours(report.periodTotals.totalHours)}</span>
                                        </div>
                                        <div className="info-item">
                                            <span className="info-label">Custo Total</span>
                                            <span className="info-value">$ {report.periodTotals.totalAmount.toFixed(2)}</span>
                                        </div>
                                    </div>

                                    {report.dailyBreakdown.length === 0 ? (
                                        <div className="info-message">
                                            Não há registros de ponto para este trabalho no período selecionado.
                                        </div>
                                    ) : (
                                        <div className="table-container">
                                            <table className="reports-table">
                                                <thead>
                                                    <tr>
                                                        <th>Data</th>
                                                        <th>Entrada</th>
                                                        <th>Saída</th>
                                                        <th className="text-right">Nº Colaboradores</th>
                                                        <th className="text-right">Horas</th>
                                                        <th className="text-right">Valor a Faturar</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {report.dailyBreakdown.map((item, index) => (
                                                        <tr key={index}>
                                                            <td>{item.date}</td>
                                                            <td>{item.start} h</td>
                                                            <td>{item.end} h</td>
                                                            <td className="text-right">{item.employeesCount}</td>
                                                            <td className="text-right">{formatDecimalHours(item.hoursWorked)}</td>
                                                            <td className="text-right">$ {item.amountToBill.toFixed(2)}</td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}

            {activeTab === 'journey' && (
                /* Tab 3: Journey */
                <div>
                    <form className="reports-form" onSubmit={handleGenerateJourney}>
                        <div className="jobs-selector-container">
                            <label className="form-label">Selecione os Funcionários (Multi-seleção)</label>
                            {loadingEmployees ? (
                                <p style={{ color: 'var(--text-muted)' }}>Carregando funcionários...</p>
                            ) : employees.length === 0 ? (
                                <p style={{ color: 'var(--text-muted)' }}>Nenhum funcionário cadastrado.</p>
                            ) : (
                                <div className="reports-jobs-grid">
                                    {employees.map(emp => {
                                        const isSelected = selectedEmployeeIds.includes(emp.id);
                                        return (
                                            <div 
                                                key={emp.id} 
                                                className={`job-selection-card ${isSelected ? 'selected' : ''}`}
                                                onClick={() => handleToggleEmployeeSelection(emp.id)}
                                            >
                                                <input 
                                                    type="checkbox" 
                                                    className="job-selection-checkbox" 
                                                    checked={isSelected}
                                                    onChange={() => {}} // Controlled by card onClick
                                                />
                                                <div className="job-card-details">
                                                    <span className="job-card-client">{emp.name || 'Sem nome'}</span>
                                                    <span className="job-card-address">{emp.email}</span>
                                                </div>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Data de Início</label>
                                <input 
                                    type="date" 
                                    className="form-input" 
                                    value={journeyStart} 
                                    onChange={(e) => setJourneyStart(e.target.value)} 
                                    required 
                                />
                            </div>
                            <div className="form-group">
                                <label className="form-label">Data de Término</label>
                                <input 
                                    type="date" 
                                    className="form-input" 
                                    value={journeyEnd} 
                                    onChange={(e) => setJourneyEnd(e.target.value)} 
                                    required 
                                />
                            </div>
                        </div>
                        <button type="submit" className="btn-generate" disabled={loading || selectedEmployeeIds.length === 0}>
                            {loading ? 'Gerando...' : 'Gerar Relatório'}
                        </button>
                    </form>

                    {loading && (
                        <div className="spinner-container">
                            <div className="spinner"></div>
                            <p>Calculando jornada dos funcionários...</p>
                        </div>
                    )}

                    {journeyData.length > 0 && !loading && (
                        <div className="reports-results-stack">
                            {journeyData.map((employeeReport) => (
                                <div key={employeeReport.employeeId} className="job-report-block">
                                    <h3 className="job-report-title">
                                        Linha do Tempo: {employeeReport.employeeName}
                                    </h3>

                                    {employeeReport.events.length === 0 ? (
                                        <div className="info-message">
                                            Nenhuma atividade registrada (Trabalho, Intervalo ou Deslocamento) no período.
                                        </div>
                                    ) : (
                                        <div className="timeline-container">
                                            {employeeReport.events.map((event, eventIdx) => {
                                                const eventTime = `${formatTime(event.startTimestamp)} - ${formatTime(event.endTimestamp)}`;
                                                const eventDate = formatDate(event.startTimestamp);
                                                
                                                let eventBadgeClass = "";
                                                let eventTypeLabel = "";
                                                let eventIcon = "";
                                                let eventDetail = "";

                                                if (event.type === 'WORK') {
                                                    eventBadgeClass = "badge-work";
                                                    eventTypeLabel = "Trabalho";
                                                    eventIcon = "💼";
                                                    eventDetail = `Serviço: ${event.jobAddress || 'Endereço não informado'}`;
                                                } else if (event.type === 'BREAK') {
                                                    eventBadgeClass = "badge-break";
                                                    eventTypeLabel = "Intervalo";
                                                    eventIcon = "☕";
                                                    eventDetail = `Local: ${event.jobAddress || 'Endereço não informado'}`;
                                                } else if (event.type === 'DISPLACEMENT') {
                                                    eventBadgeClass = "badge-displacement";
                                                    eventTypeLabel = "Deslocamento";
                                                    eventIcon = "🚗";
                                                    eventDetail = `De: ${event.originAddress || 'N/A'} ➔ Para: ${event.jobAddress || 'N/A'}`;
                                                }

                                                return (
                                                    <div key={eventIdx} className="timeline-item">
                                                        <div className="timeline-meta">
                                                            <span className="timeline-date">{eventDate}</span>
                                                            <span className="timeline-time">{eventTime}</span>
                                                        </div>
                                                        <div className="timeline-marker">
                                                            <div className={`timeline-icon-container ${eventBadgeClass}`}>
                                                                {eventIcon}
                                                            </div>
                                                        </div>
                                                        <div className="timeline-content">
                                                            <div className="timeline-header-row">
                                                                <span className={`timeline-badge ${eventBadgeClass}`}>
                                                                    {eventTypeLabel}
                                                                </span>
                                                                <span className="timeline-duration">
                                                                    {formatDecimalHours(event.durationHours)}
                                                                </span>
                                                            </div>
                                                            <div className="timeline-details">
                                                                {eventDetail}
                                                            </div>
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default ReportsPage;
