import React, {useState} from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../services/api";
import PlacesAutocompleteInput from "../components/PlacesAutoCompleteInput";

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
        <div>
            <Link to="/dashboard"> {"< Voltar para o Dashboard"} </Link>
            <h2>Criar Novo Trabalho</h2>
            <form onSubmit={handleSubmit}>
                <div style={{marginBottom:'10px'}}>
                    <label> Endereço:*</label><br/>
                    <PlacesAutocompleteInput onPlaceSelect={handleAddressSelect} />
                    {formData.latitude && formData.longitude && (
                        <span style={{ fontSize: '12px', color: 'green' }}>
                            Coordenadas obtidas: {formData.latitude}, {formData.longitude}
                        </span>
                    )}
                </div>
                <div style={{marginBottom:'10px'}}>
                    <label>Nome do Contratante:*</label><br/>
                    <input type="text" name="clientName" value={formData.clientName} onChange={handleChange} required style={{width:'300px'}}/>
                </div>
                <div style={{marginBottom:'10px'}}>
                    <label>Valor/Hora (Faturamento):*</label><br/>
                    <input type="number" name="billingRate" value={formData.billingRate} onChange={handleChange} required step="0.01"/>
                </div>
                <div style={{marginBottom:'10px'}}>
                    <label>Orçamento:*</label><br/>
                    <input type="number" name="budget" value={formData.budget} onChange={handleChange} required step="0.01"/>
                </div>
                <div style={{marginBottom:'10px'}}>
                    <label>Data de Inicio:*</label><br/>
                    <input type="date" name="startDate" value={formData.startDate} onChange={handleChange} required />
                </div>
                <div style={{marginBottom:'10px'}}>
                    <label>Data de Termino (Opcional):</label><br/>
                    <input type="date" name="endDate" value={formData.endDate} onChange={handleChange} />
                </div>

                <hr style={{ margin: '20px 0', border: '0', borderTop: '1px solid #ccc' }} />
                <h3>Dados do Responsável</h3>
                <div style={{marginBottom:'10px'}}>
                    <label>Nome do Responsável:*</label><br/>
                    <input type="text" name="responsibleName" value={formData.responsibleName} onChange={handleChange} required style={{width:'300px'}}/>
                </div>
                <div style={{marginBottom:'10px'}}>
                    <label>Telefone do Responsável:*</label><br/>
                    <input type="text" name="responsiblePhone" value={formData.responsiblePhone} onChange={handleChange} required style={{width:'300px'}}/>
                </div>
                <div style={{marginBottom:'10px'}}>
                    <label>E-mail do Responsável (Opcional):</label><br/>
                    <input type="email" name="responsibleEmail" value={formData.responsibleEmail} onChange={handleChange} style={{width:'300px'}}/>
                </div>

                {error && <p style={{ color: 'red' }}>{error}</p>}
                
                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Salvando...' : 'Salvar Trabalho'}
                </button>
            </form>
        </div>
    );
}

export default CreateJobPage;