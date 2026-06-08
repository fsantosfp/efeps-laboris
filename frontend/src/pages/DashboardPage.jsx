import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";
import "./DashboardPage.css";

function DashboardPage(){
    const [jobs, setJobs] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const STATUS = {
        'PENDING': 'A fazer', 
        'IN_PROGRESS': 'Em andamento', 
        'COMPLETED': 'Concluído'
    };

    useEffect(() => {
        const fetchJobs = async () => {
            try {
                const response = await api.get('/jobs');
                setJobs(response.data);
            } catch (err) {
                console.error("Erro ao buscar trabalhos:", err);
                setError('Não foi possível carregar a lista de trabalhos. Tente novamente mais tarde.');
            } finally {
                setLoading(false);
            }
        };

        fetchJobs();
    }, []);

    const filteredJobs = jobs.filter(job => 
        job.address && job.address.toLowerCase().includes(searchQuery.toLowerCase())
    );

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
                <span>Carregando trabalhos...</span>
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <div className="dashboard-title-group">
                    <h2 className="dashboard-title">Trabalhos</h2>
                    <p className="dashboard-subtitle">
                        {jobs.length === 0 
                            ? "Nenhum trabalho cadastrado no momento." 
                            : `Visualizando ${jobs.length} trabalho(s) ativo(s) na plataforma.`}
                    </p>
                </div>
                <Link to="/jobs/new" className="create-job-btn">
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                        <line x1="12" y1="5" x2="12" y2="19"/>
                        <line x1="5" y1="12" x2="19" y2="12"/>
                    </svg>
                    Novo Trabalho
                </Link>
            </header>

            {jobs.length > 0 && (
                <div className="dashboard-search-container">
                    <div className="search-input-wrapper">
                        <svg className="search-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <circle cx="11" cy="11" r="8"/>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                        </svg>
                        <input 
                            type="text" 
                            className="search-input" 
                            placeholder="Buscar trabalhos por endereço..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                        />
                    </div>
                </div>
            )}

            {error && <div className="login-error-alert" style={{ marginBottom: '24px' }}>{error}</div>}

            {jobs.length === 0 && !error ? (
                <div className="empty-dashboard-card">
                    <p style={{ margin: 0, fontSize: '16px', fontWeight: '500' }}>Você ainda não cadastrou nenhum trabalho.</p>
                    <p style={{ margin: '8px 0 0 0', fontSize: '14px', color: '#94a3b8' }}>Clique em "Novo Trabalho" para começar.</p>
                </div>
            ) : filteredJobs.length === 0 ? (
                <div className="empty-dashboard-card">
                    <p style={{ margin: 0, fontSize: '16px', fontWeight: '500' }}>Nenhum trabalho encontrado para o endereço digitado.</p>
                    <p style={{ margin: '8px 0 0 0', fontSize: '14px', color: '#94a3b8' }}>Tente ajustar os termos da sua busca.</p>
                </div>
            ) : (
                <div className="jobs-grid">
                    {filteredJobs.map(job => {
                        return (
                            <div className="job-card" key={job.id}>
                                <div>
                                    <div className="job-card-header">
                                        <span className={`status-badge ${job.status.toLowerCase()}`}>
                                            {STATUS[job.status]}
                                        </span>
                                    </div>

                                    <div className="job-card-body">
                                        <h3 className="job-card-title">{job.clientName || "Trabalho Sem Cliente"}</h3>
                                        <p className="job-card-address">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                                <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/>
                                                <circle cx="12" cy="10" r="3"/>
                                            </svg>
                                            {job.address}
                                        </p>
                                    </div>
                                </div>

                                <div>
                                    <hr className="job-card-divider" />
                                    
                                    <div className="job-card-meta">
                                        <div className="meta-item">
                                            <span className="meta-label">Início</span>
                                            <span className="meta-value">{job.startDate}</span>
                                        </div>
                                        <div className="meta-item">
                                            <span className="meta-label">Responsável</span>
                                            <span className="meta-value" title={job.responsibleName}>
                                                {job.responsibleName}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="job-card-actions">
                                        <Link to={`/jobs/${job.id}`} className="view-details-link">
                                            Ver Detalhes
                                        </Link>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default DashboardPage;