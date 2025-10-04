import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { useAuth } from '../context/AuthContext';
import api from "../services/api";

function DashboardPage(){
    const { logout } = useAuth();

    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const STATUS = {'PENDING':'A fazer', 'IN_PROGRESS':'Em andamento', 'COMPLETED': 'Concluído'}

    useEffect(() => {
        const fetchJobs = async () => {
            try {
                const response = await api.get('/jobs');
                setJobs(response.data)
            } catch (err) {
                console.error("Erro ao buscar trabalhos:", err);
                setError('Não foi possível carregar a lista de trabalhos. Tente novamente mais tarde.')
            } finally {
                setLoading(false);
            }
        };

        fetchJobs();
    }, []);

    if(loading) return <div>Carregando...</div>

    return (
        <div>
            <h2>Dashboard</h2>
            <button onClick={logout} style={{float:'right'}}>Sair</button>
            <hr style={{ clear: 'both', marginBottom: '20px' }}/>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3>Seus Trabalhos</h3>
                <Link to="/jobs/new"><button>+ Novo Trabalho</button></Link>
            </div>

            {error && <p style={{color:'red'}}>{error}</p>}
            { jobs.length === 0 && !error ? (<p>Você ainda não cadastrou nenhum trabalho.</p>) : (
                <table border="1" style={{width:'100%', borderCollapse:'collapse'}}>
                    <thead>
                        <tr>
                            <th style={{padding:'8px', textAlign:'left'}}>Endereço</th>
                            <th style={{padding:'8px', textAlign:'left'}}>Status</th>
                            <th style={{padding:'8px', textAlign:'left'}}>Data de Inicio</th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            jobs.map( job => (
                                <tr key={job.id}>
                                    <td style={{padding:'8px'}}><Link to={`/jobs/${job.id}`}>{job.address}</Link></td>
                                    <td style={{padding:'8px'}}>{STATUS[job.status]}</td>
                                    <td style={{padding:'8px'}}>{job.startDate}</td>
                                </tr>
                            ))
                        }
                    </tbody>
                </table>
            )}
        </div>
    );
}

export default DashboardPage;