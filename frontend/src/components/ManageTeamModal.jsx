import React, {useState, useEffect} from 'react';
import api from '../services/api';
import "./ManageTeamModal.css";

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

    return (
        <div className="manage-team-backdrop" onClick={onClose}>
            <div className="manage-team-card" onClick={(e) => e.stopPropagation()}>
                
                <button className="manage-team-close" onClick={onClose}>&times;</button>
                
                <h2 className="manage-team-title">Gerenciar Equipe</h2>
                
                {loading ? (
                    <div className="manage-team-loading">Carregando funcionários...</div>
                ) : error ? (
                    <div className="manage-team-error">{error}</div>
                ) : (
                    <div className="manage-team-list">
                        {allEmployees.map(employee => (
                            <label className="manage-team-item" key={employee.id} htmlFor={`emp-${employee.id}`}>
                                <input
                                    type="checkbox"
                                    id={`emp-${employee.id}`}
                                    className="manage-team-checkbox"
                                    checked={selectedEmployees.has(employee.id)}
                                    onChange={() => handleCheckboxChange(employee.id)}
                                />
                                <span className="manage-team-label">
                                    {employee.name}
                                </span>
                            </label>
                        ))}
                    </div>
                )}

                <div className="manage-team-actions">
                    <button className="manage-team-btn manage-team-btn-secondary" onClick={onClose}>Cancelar</button>
                    <button className="manage-team-btn manage-team-btn-primary" onClick={handleSave}>Salvar</button>
                </div>
            </div>
        </div>
    );
}

export default ManageTeamModal;