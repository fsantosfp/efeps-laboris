import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom'; 
import api from '../services/api';
import ManageTeamModal from '../components/ManageTeamModal';

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

    useEffect(() => {
        fetchJobDetails();
    },[fetchJobDetails]);

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

    if(loading) return <div> Carregando detalhes do trabalho... </div>

    if(error) return <div style={{ color: 'red' }}>{error}</div>;

    return (
        <div>
            <Link to="/dashboard">{ "< Voltar para o Dashboard" }</Link>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '10px' }}>
                <h1>Detalhe do Trabalho</h1>
                {job && !isEditing && (
                    <button onClick={handleStartEdit}>Editar Dados do Trabalho</button>
                )}
            </div>

            <div style={{ marginBottom:'20px'}}>
                <Link to={`/reports/jobs/${jobId}`}>
                    <button>Gerar Relatório de Custo</button>
                </Link>
            </div>

            { job && (
                <div>
                    {isEditing ? (
                        <form onSubmit={handleSaveEdit} style={{ background: '#f8f9fa', padding: '20px', borderRadius: '8px', marginBottom: '20px', border: '1px solid #ddd' }}>
                            <h3>Editar Informações do Trabalho</h3>
                            
                            <div style={{ marginBottom: '10px' }}>
                                <label>Status:*</label><br />
                                <select name="status" value={editData.status} onChange={handleEditChange} required>
                                    <option value="PENDING">A fazer</option>
                                    <option value="IN_PROGRESS">Em Andamento</option>
                                    <option value="COMPLETED">Concluído</option>
                                    {job.status === 'DELETED' && <option value="DELETED">Cancelado (Deletado)</option>}
                                </select>
                            </div>

                            <div style={{ marginBottom: '10px' }}>
                                <label>Valor/Hora (Faturamento):*</label><br />
                                <input type="number" name="billingRate" value={editData.billingRate} onChange={handleEditChange} required step="0.01" />
                            </div>

                            <div style={{ marginBottom: '10px' }}>
                                <label>Orçamento:*</label><br />
                                <input type="number" name="budget" value={editData.budget} onChange={handleEditChange} required step="0.01" />
                            </div>

                            <div style={{ marginBottom: '10px' }}>
                                <label>Data de Início:*</label><br />
                                <input type="date" name="startDate" value={editData.startDate} onChange={handleEditChange} required />
                            </div>

                            <div style={{ marginBottom: '10px' }}>
                                <label>Data de Término (Estimada):</label><br />
                                <input type="date" name="endDate" value={editData.endDate} onChange={handleEditChange} />
                            </div>

                            <hr style={{ margin: '15px 0', border: '0', borderTop: '1px solid #ccc' }} />
                            <h4>Dados do Responsável</h4>

                            <div style={{ marginBottom: '10px' }}>
                                <label>Nome do Responsável:*</label><br />
                                <input type="text" name="responsibleName" value={editData.responsibleName} onChange={handleEditChange} required style={{ width: '300px' }} />
                            </div>

                            <div style={{ marginBottom: '10px' }}>
                                <label>Telefone do Responsável:*</label><br />
                                <input type="text" name="responsiblePhone" value={editData.responsiblePhone} onChange={handleEditChange} required style={{ width: '300px' }} />
                            </div>

                            <div style={{ marginBottom: '10px' }}>
                                <label>E-mail do Responsável (Opcional):</label><br />
                                <input type="email" name="responsibleEmail" value={editData.responsibleEmail} onChange={handleEditChange} style={{ width: '300px' }} />
                            </div>

                            <div style={{ marginTop: '15px', display: 'flex', gap: '10px' }}>
                                <button type="submit">Salvar Alterações</button>
                                <button type="button" onClick={() => setIsEditing(false)} style={{ background: '#7f8c8d', color: 'white' }}>Cancelar</button>
                            </div>
                        </form>
                    ) : (
                        <div style={{ background: '#fdfdfd', padding: '20px', borderRadius: '8px', border: '1px solid #eee', marginBottom: '20px' }}>
                            <p><strong>Endereço:</strong> { job.address }</p>
                            <p><strong>Contratante:</strong> { job.clientName }</p>
                            <p><strong>Status:</strong> { 
                                job.status === 'PENDING' ? 'A fazer' : 
                                job.status === 'IN_PROGRESS' ? 'Em andamento' : 
                                job.status === 'COMPLETED' ? 'Concluído' : 'Cancelado (Deletado)' 
                            }</p>
                            <p><strong>Valor/Hora (Faturamento):</strong> R$ { job.billingRate }</p>
                            <p><strong>Orçamento:</strong> R$ { job.budget}</p>
                            <p><strong>Data de Início:</strong> { job.startDate }</p>
                            <p><strong>Data de Término (Estimada):</strong> { job.endDate || 'Não informada' }</p>

                            <hr style={{ margin: '15px 0', border: '0', borderTop: '1px solid #ccc' }} />
                            <h4>Dados do Responsável</h4>
                            <p><strong>Nome do Responsável:</strong> { job.responsibleName }</p>
                            <p><strong>Telefone do Responsável:</strong> { job.responsiblePhone }</p>
                            <p><strong>E-mail do Responsável:</strong> { job.responsibleEmail || 'Não informado' }</p>
                        </div>
                    )}

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
                    {job.status === 'PENDING' && !isEditing && (
                        <div>
                            <h4>Ação Perigosa</h4>
                            <button onClick={() => setIsDeleteModalOpen(true)} style={{ background:'red', color:'white'}}> Deletar Trabalho</button>
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
                <div style={{
                    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex',
                    justifyContent: 'center', alignItems: 'center', zIndex: 1000
                }}>
                    <div style={{
                        backgroundColor: '#fff', color: '#333', padding: '25px',
                        borderRadius: '8px', width: '450px', boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                        border: '1px solid #ccc'
                    }}>
                        <h3 style={{ marginTop: 0, color: '#c0392b' }}>Confirmar Exclusão do Trabalho</h3>
                        <p>Esta ação é permanente e não poderá ser desfeita. Para prosseguir, digite exatamente o endereço do trabalho abaixo para confirmar:</p>
                        <p style={{ fontStyle: 'italic', background: '#f5f5f5', padding: '10px', borderRadius: '4px', borderLeft: '4px solid #c0392b', wordBreak: 'break-all' }}>
                            {job.address}
                        </p>
                        <input 
                            type="text" 
                            value={confirmAddressInput} 
                            onChange={(e) => setConfirmAddressInput(e.target.value)} 
                            style={{ width: '100%', padding: '10px', marginBottom: '20px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px' }}
                            placeholder="Digite o endereço exato do trabalho"
                        />
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                            <button onClick={() => { setIsDeleteModalOpen(false); setConfirmAddressInput(''); }} style={{ background: '#7f8c8d', color: 'white', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer' }}>
                                Cancelar
                            </button>
                            <button 
                                onClick={handleConfirmDelete} 
                                disabled={confirmAddressInput !== job.address}
                                style={{ 
                                    background: confirmAddressInput === job.address ? '#d9534f' : '#ccc', 
                                    color: 'white', border: 'none', padding: '8px 16px', borderRadius: '4px', 
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