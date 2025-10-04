import React, {useState, useEffect} from 'react';
import api from '../services/api';

const ManageTeamModal = ({jobId, currentTeam, onClose, onTeamUpdate}) =>{
    
    const [allEmployees, setAllEmployees] = useState([]);
    const [selectedEmployees, setSelectedEmployees] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(()=>{
        const fetchAllEmployees = async () =>{
            try {

                const response = await api.get('/employees');
                setAllEmployees(response.data)

                const currentTeamId = new Set(currentTeam.map(employee => employee.id));
                setSelectedEmployees(currentTeamId);

            } catch (error){
                setError('Falha  ao carregar a lista de funcionarios.')
            } finally {
                setLoading(false);
            }
        };

        fetchAllEmployees();

    }, [currentTeam]);

    const handleCheckboxChange = (employeeId) => {
        
        const newSelection = new Set(selectedEmployees);

        if(newSelection.has(employeeId)){
            newSelection.delete(employeeId);
        } else {
            newSelection.add(employeeId);
        }

        setSelectedEmployees(newSelection)
    };

    const handleSave = async () => {

        const originalIds =  new Set(currentTeam.map(employee => employee.id));
        const finalIds = selectedEmployees;

        const toAdd = [...finalIds].filter(id => !originalIds.has(id));
        const toRemove = [...originalIds].filter(id => !finalIds.has(id));

        try {
            if(toAdd.length > 0 ) await api.post(`/jobs/${jobId}/assignments`, {employeeIds: toAdd});

            if(toRemove.length > 0) await api.delete(`/jobs/${jobId}/assignments`, {data: {employeeIds: toRemove}});

            onTeamUpdate();
            onClose();
        } catch (err){
            setError('Falha ao salvar as alterações.');
        }
    };

    // ===================================================================
    // ESTILOS DO MODAL (CSS em formato de objeto JavaScript)
    // ===================================================================
    const modalOverlayStyle = {
        position: 'fixed', // Fica fixo na tela, mesmo com scroll
        top: 0,
        left: 0,
        width: '100vw', // 100% da largura da tela
        height: '100vh', // 100% da altura da tela
        backgroundColor: 'rgba(0, 0, 0, 0.5)', // Fundo preto com 50% de transparência
        display: 'flex', // Usa flexbox para centralizar
        justifyContent: 'center', // Centraliza horizontalmente
        alignItems: 'center', // Centraliza verticalmente
        zIndex: 1000, // Garante que fique na frente de tudo
    };

    const modalContentStyle = {
        backgroundColor: '#fff',
        padding: '20px',
        borderRadius: '8px',
        minWidth: '300px',
        maxWidth: '500px',
        boxShadow: '0 4px 8px rgba(0,0,0,0.1)',
        position: 'relative', // Necessário para o botão de fechar
    };

    const closeButtonStyle = {
        position: 'absolute',
        top: '10px',
        right: '10px',
        border: 'none',
        background: 'transparent',
        fontSize: '1.5rem',
        cursor: 'pointer',
    };
    // ===================================================================

    return (
        // Aplica o estilo do fundo
        <div style={modalOverlayStyle} onClick={onClose}>
            {/* Aplica o estilo do conteúdo e impede que o clique feche o modal */}
            <div style={modalContentStyle} onClick={(e) => e.stopPropagation()}>
                
                {/* Botão de fechar */}
                <button style={closeButtonStyle} onClick={onClose}>&times;</button>
                
                <h2>Gerenciar Equipe</h2>
                
                {loading ? (
                    <p>Carregando funcionários...</p>
                ) : error ? (
                    <p style={{ color: 'red' }}>{error}</p>
                ) : (
                    <div style={{ maxHeight: '200px', overflowY: 'auto', border: '1px solid #ccc', padding: '10px', marginTop: '15px' }}>
                        {allEmployees.map(employee => (
                            <div key={employee.id}>
                                <input
                                    type="checkbox"
                                    id={`emp-${employee.id}`}
                                    checked={selectedEmployees.has(employee.id)}
                                    onChange={() => handleCheckboxChange(employee.id)}
                                />
                                <label htmlFor={`emp-${employee.id}`} style={{ marginLeft: '5px' }}>
                                    {employee.name}
                                </label>
                            </div>
                        ))}
                    </div>
                )}

                <div style={{ marginTop: '20px', textAlign: 'right' }}>
                    <button onClick={onClose}>Cancelar</button>
                    <button onClick={handleSave} style={{ marginLeft: '10px' }}>Salvar</button>
                </div>
            </div>
        </div>
    );
}

export default ManageTeamModal;