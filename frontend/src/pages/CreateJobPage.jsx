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
        latitude: '-0000001111',
        longitude: '-000002222'
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

        try{
            const dataToSend = {
                ...formData,
                billinRate: parseFloat(formData.billingRate),
                budget: formData.budget ? parseFloat(formData.budget) : null
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

    const handleAddressSelect = (address) => {
        setFormData(prevState => ({
            ...prevState,
            address: address
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

                {error && <p style={{ color: 'red' }}>{error}</p>}
                
                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Salvando...' : 'Salvar Trabalho'}
                </button>
            </form>
        </div>
    );
}

export default CreateJobPage;