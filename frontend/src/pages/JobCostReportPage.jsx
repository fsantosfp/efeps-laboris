import React, { useState } from "react";
import { useParams, Link } from 'react-router-dom';
import api from '../services/api';
import { formatDecimalHours } from "../utils/formatters";

function JobCostReportPage(){
    
    const { jobId } = useParams();
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [reportData, setReportData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const lastHourOfDay = 'T23:59:59.999Z';

    const handleGenerateReport = async () => {
        
        if(!startDate || !endDate){
            setError('Por favor, selecione as datas de início e fim.');
            return;
        }

        setError('');
        setLoading(true);
        setReportData(null);

        try {

            const startISO = new Date(startDate).toISOString();
            const endISO = new Date(endDate + lastHourOfDay).toISOString();

            const response = await api.get(`/reports/jobs/${jobId}?start=${startISO}&end=${endISO}`);
            setReportData(response.data);
        } catch(err){
            console.error("Erro ao gerar relatório de custo:", err);
            setError(err.response?.data?.message || 'Falha ao gerar o relatório.');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <Link to={`/jobs/${jobId}`}> {"< Voltar aos Detalhes do Trabaho"} </Link>
            <h2 style={{ marginTop:'20px'}}>Relatório de Custo por Serviço</h2>

            <div style={{ marginTop:'20px', display:'flex', alignItems:'center', gap: '10px'}}>
                <div>
                    <label>Data de Início: </label>
                    <input type="date" value={startDate} onChange={ (e) => setStartDate(e.target.value) } />
                </div>
                <div>
                    <label>Data de Término: </label>
                    <input type="date" value={endDate} onChange={ (e) => setEndDate(e.target.value) } />
                </div>
                <button onClick={handleGenerateReport} disabled={loading}>{ loading ? 'Gerando...' : 'Gerar Relatório' }</button>
            </div>
            
            {error && <p style={{ color: 'red' }}>{error}</p>}
                
            {loading && <p>Gerando relatório...</p>}

            { reportData && (
                <div>
                    <h3>Resumo do Trabalho</h3>
                    <p><strong>Endereço:</strong> { reportData.jobInfo.address } </p>
                    <p><strong>Cliente:</strong> { reportData.jobInfo.clientName } </p>
                    <p><strong>Valor/Hora Base:</strong> $ { reportData.jobInfo.billingRate && reportData.jobInfo.billingRate.toFixed(2) }</p>
                    
                    <hr />
                    
                    <h3>Resumo do Período</h3>
                    <p><strong>Total de Horas Faturáveis:</strong> { formatDecimalHours(reportData.periodTotals.totalHours) }</p>
                    <p><strong>Valor Total a Faturar:</strong> $ {reportData.periodTotals.totalAmount.toFixed(2) } </p>

                    <h3 style={{ mariginTop:'30px'}}> Detalhamento por Dia</h3>

                    <table border="1" style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr>
                                <th>Data</th>
                                <th>Entrada</th>
                                <th>Saída</th>
                                <th>Nº de Colaboradores</th>
                                <th>Horas</th>
                                <th>Valor a Faturar</th>
                            </tr>
                        </thead>
                        <tbody>
                            { reportData.dailyBreakdown.map((item, index)=>(
                                <tr key={ index }>
                                    <td>{ item.date }</td>
                                    <td style={{textAlign:'right'}}>{ item.start } h</td>
                                    <td style={{textAlign:'right'}}>{ item.end } h</td>
                                    <td style={{textAlign:'right'}}>{ item.employeesCount }</td>
                                    <td style={{textAlign:'right'}}>{ formatDecimalHours(item.hoursWorked)}</td>
                                    <td style={{textAlign:'right'}}>$ { item.amountToBill.toFixed(2)}</td>
                                </tr>
                            )) }
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default JobCostReportPage;

