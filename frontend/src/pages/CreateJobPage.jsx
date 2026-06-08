import React, {useState} from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../services/api";
import PlacesAutocompleteInput from "../components/PlacesAutoCompleteInput";
import "./CreateJobPage.css";

function CreateJobPage(){
    const [formData, setFormData] = useState({
        address: '',
        clientName: '',
        billingRate: '',
        startDate: '',
        endDate: '',
        budget: '',
        latitude: '',
        longitude: '',
        responsibleName: '',
        responsiblePhone: '',
        responsibleEmail: ''
    });

    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        const {name, value} = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setIsSubmitting(true);

        if (!formData.latitude || !formData.longitude) {
            setError('Por favor, selecione um endereço válido no campo para obter as coordenadas de geolocalização.');
            setIsSubmitting(false);
            return;
        }

        try{
            const dataToSend = {
                ...formData,
                billingRate: parseFloat(formData.billingRate),
                budget: parseFloat(formData.budget),
                latitude: parseFloat(formData.latitude),
                longitude: parseFloat(formData.longitude),
                responsibleEmail: formData.responsibleEmail || null
            }

            console.log(dataToSend);

            await api.post('/jobs', dataToSend);

            alert("Trabalho criado com sucesso");
            navigate('/dashboard');

        }catch(err){
            console.error("Erro ao criar trabalho:", err);
            const errorMsg = err.response?.data?.message || 'Falha ao criar o trabalho. Verifique os dados e tente novamente.';
            setError(errorMsg);
        } finally{
            setIsSubmitting(false);
        }
    }

    const handleAddressSelect = (address, lat, lng) => {
        setFormData(prevState => ({
            ...prevState,
            address: address,
            latitude: lat !== null && lat !== undefined ? lat.toString() : '',
            longitude: lng !== null && lng !== undefined ? lng.toString() : ''
        }));
    }

    return (
        <div className="job-create-container">
            <header className="job-create-header">
                <h2 className="job-create-title">Criar Novo Trabalho</h2>
            </header>

            <form onSubmit={handleSubmit} className="job-create-card">
                <h3>Informações Gerais</h3>
                <div className="job-create-grid">
                    <div className="job-create-form-group full-width">
                        <label className="job-create-label">Endereço:*</label>
                        <PlacesAutocompleteInput onPlaceSelect={handleAddressSelect} />
                    </div>

                    <div className="job-create-form-group">
                        <label className="job-create-label">Nome do Contratante:*</label>
                        <input 
                            type="text" 
                            name="clientName" 
                            className="job-create-input" 
                            value={formData.clientName} 
                            onChange={handleChange} 
                            required 
                            placeholder="Digite o nome do contratante"
                        />
                    </div>

                    <div className="job-create-form-group">
                        <label className="job-create-label">Valor/Hora (Faturamento):*</label>
                        <input 
                            type="number" 
                            name="billingRate" 
                            className="job-create-input" 
                            value={formData.billingRate} 
                            onChange={handleChange} 
                            required 
                            step="0.01" 
                            placeholder="0.00"
                        />
                    </div>

                    <div className="job-create-form-group">
                        <label className="job-create-label">Orçamento:*</label>
                        <input 
                            type="number" 
                            name="budget" 
                            className="job-create-input" 
                            value={formData.budget} 
                            onChange={handleChange} 
                            required 
                            step="0.01" 
                            placeholder="0.00"
                        />
                    </div>

                    <div className="job-create-form-group">
                        <label className="job-create-label">Data de Início:*</label>
                        <input 
                            type="date" 
                            name="startDate" 
                            className="job-create-input" 
                            value={formData.startDate} 
                            onChange={handleChange} 
                            required 
                        />
                    </div>

                    <div className="job-create-form-group full-width">
                        <label className="job-create-label">Data de Término (Opcional):</label>
                        <input 
                            type="date" 
                            name="endDate" 
                            className="job-create-input" 
                            value={formData.endDate} 
                            onChange={handleChange} 
                        />
                    </div>
                </div>

                <h3 style={{ marginTop: '32px' }}>Dados do Responsável</h3>
                <div className="job-create-grid">
                    <div className="job-create-form-group">
                        <label className="job-create-label">Nome do Responsável:*</label>
                        <input 
                            type="text" 
                            name="responsibleName" 
                            className="job-create-input" 
                            value={formData.responsibleName} 
                            onChange={handleChange} 
                            required 
                            placeholder="Digite o nome do responsável"
                        />
                    </div>

                    <div className="job-create-form-group">
                        <label className="job-create-label">Telefone do Responsável:*</label>
                        <input 
                            type="text" 
                            name="responsiblePhone" 
                            className="job-create-input" 
                            value={formData.responsiblePhone} 
                            onChange={handleChange} 
                            required 
                            placeholder="(00) 00000-0000"
                        />
                    </div>

                    <div className="job-create-form-group full-width">
                        <label className="job-create-label">E-mail do Responsável (Opcional):</label>
                        <input 
                            type="email" 
                            name="responsibleEmail" 
                            className="job-create-input" 
                            value={formData.responsibleEmail} 
                            onChange={handleChange} 
                            placeholder="email@exemplo.com"
                        />
                    </div>
                </div>

                {error && <div className="job-create-error">{error}</div>}

                <div className="job-create-actions">
                    <button type="submit" className="job-create-btn job-create-btn-primary" disabled={isSubmitting}>
                        {isSubmitting ? 'Salvando...' : 'Salvar Trabalho'}
                    </button>
                    <button type="button" className="job-create-btn job-create-btn-secondary" onClick={() => navigate('/dashboard')}>
                        Cancelar
                    </button>
                </div>
            </form>
        </div>
    );
}

export default CreateJobPage;