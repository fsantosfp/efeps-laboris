import React, { useState, useEffect } from "react";
import api from '../services/api';
import { formatDecimalHours } from "../utils/formatters";
import './ReportsPage.css';

function ReportsPage() {
    const [activeTab, setActiveTab] = useState('payroll'); // 'payroll' or 'jobCosts'

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
                    setError('Não foi possível carregar a lista de trabalhos.');
                } finally {
                    setLoadingJobs(false);
                }
            };
            fetchJobs();
        }
    }, [activeTab, jobs.length]);

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
            </div>

            {error && <div className="error-message">{error}</div>}

            {activeTab === 'payroll' ? (
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
            ) : (
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
                                <div className="jobs-grid">
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
        </div>
    );
}

export default ReportsPage;
