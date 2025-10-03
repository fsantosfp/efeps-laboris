import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom'; 
import api from '../services/api';

function JobDetailPage(){

    const { jobId } = useParams();

    const [job, setJob] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchJobDetails = async () => { 
            try {
                const response = await api.get(`/jobs/${jobId}`);
                setJob(response.data);
            } catch (err) {
                console.error("Erro ao buscar detalhes do trabalho:", err);
                setError('Trabalho não encontrado ou falha ao carregar os dados.');
            } finally {
                setLoading(false);
            }
        };

        fetchJobDetails();
    },[jobId]);

    if(loading) return <div> Carregando detalhes do trabalho... </div>

    if(error) return <div style={{ color: 'red' }}>{error}</div>;

    return (
        <div>
            <Link to="/dashboard">{ "< Voltar para o Dashboard" }</Link>
            <h1>Detalhe do trabalho</h1>
            { job && (
                <div>
                    <p><strong>Endereço:</strong> { job.address }</p>
                    <p><strong>Contratante:</strong> { job.clientName }</p>
                    <p><strong>Status:</strong> { job.status }</p>
                    <p><strong>Valor/Hora (Faturamento):</strong> { job.billingRate }</p>
                    <p><strong>Data Inicio:</strong> { job.startDate }</p>

                    <h3>Equipe Designada</h3>
                    {job.assignedTeam && job.assignedTeam.length > 0 ? (
                        <ul>
                            { job.assignedTeam.map(employee => (<li key="{employee.id}">{employee.name}</li>)) }
                        </ul>
                    ):( 
                        <p>Nenhum funcionário designado para este trabalho ainda.</p> 
                    )}
                </div>
            )}
        </div>
    );
}

export default JobDetailPage;