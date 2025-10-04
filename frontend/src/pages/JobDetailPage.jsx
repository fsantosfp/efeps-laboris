import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link, Navigate, useNavigate } from 'react-router-dom'; 
import api from '../services/api';
import ManageTeamModal from '../components/ManageTeamModal';

function JobDetailPage(){

    const { jobId } = useParams();
    const navigate = useNavigate();

    const [job, setJob] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);

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

    useEffect(() => {
        fetchJobDetails();
    },[fetchJobDetails]);

    const handleStatusChange = async (newStatus) => {
        try{
            const response = await api.patch(`/jobs/${jobId}`, {status: newStatus});
            alert('Status atualizado com sucesso');
            fetchJobDetails()
        } catch (err){
            console.error("Erro ao atualizar status:", err);
            alert(err.response?.data?.message || 'Falha ao atualizar o status.');
        }
    }

    const handleCancelJob = async () => {
        if(window.confirm("Você tem certeza que deseja cancelar este trabalho? Esta ação não pode ser desfeita.")){
            try{
                await api.delete(`/jobs/${jobId}`);
                alert('Trabalho cancelado com sucesso!');
                navigate('/dashboard');
            } catch(err){
                console.error("Erro ao cancelar trabalho:", err);
                alert(err.response?.data?.message || 'Falha ao cancelar o trabalho.');
            }
        }
    }

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
                    <div style={{ margin:'20px 0'}}>
                        <strong>Status: </strong>
                        <select value={job.status} onChange={(e) => {handleStatusChange(e.target.value)}} >
                            <option value="PENDING">A fazer</option>
                            <option value="IN_PROGRESS">Em Andamento</option>
                            <option values="COMPLETED">Concluído</option>
                            {job.status === 'DELETED' && <option value="DELETED">Cancelado</option>}
                        </select>
                    </div>
                    <p><strong>Valor/Hora (Faturamento):</strong> { job.billingRate }</p>
                    <p><strong>Orçamento:</strong> { job.budget}</p>
                    <p><strong>Data Inicio:</strong> { job.startDate }</p>

                    <h3>Equipe Designada</h3>
                    <button onClick={ () => setIsModalOpen(true) }>Gerenciar Equipe</button>
                    {job.assignedTeam && job.assignedTeam.length > 0 ? (
                        <ul>
                            { job.assignedTeam.map(employee => (<li key={employee.id}>{employee.name}</li>)) }
                        </ul>
                    ):( 
                        <p>Nenhum funcionário designado para este trabalho ainda.</p> 
                    )}

                    <hr style={{ margin: '20px 0' }} />
                    {job.status === 'PENDING' && (
                        <div>
                            <h4>Ação Perigosa</h4>
                            <button onClick={handleCancelJob} style={{ background:'red', color:'white'}}> Cancelar Trabalho</button>
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
        </div>
    );
}

export default JobDetailPage;