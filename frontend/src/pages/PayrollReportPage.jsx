import React, { useState } from "react";
import api from '../services/api';
import { formatDecimalHours } from "../utils/formatters";

function PayrollReportPage(){

    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [reportData, setReportDate] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const lastHourOfDay = "T23:59:59.999Z";

    const handleGenerateReport = async () => {
        if (!startDate || !endDate){
            setError('Por favor, selecione as datas de início e fim.')
            return;
        }

        setError('');
        setLoading(true);
        setReportDate(null);

        try {
            const startISO = new Date(startDate).toISOString();
            const endISO = new Date(endDate + lastHourOfDay).toISOString();

            const response = await api.get(`reports/payroll?start=${startISO}&end=${endISO}`);
            setReportDate(response.data);
        } catch(err) {
            console.error('Erro ao gerar relatório', err);
            setError(err.response?.data?.message || 'Falha ao gerar o relatório.');
        }finally{
            setLoading(false);
        }
    }

    return (
        <div>
            <h2>Relatório de Folha de Pagamento</h2>
            <div style={{ marginBottom:'20px', display:'flex', alignItems:'center', gap:'10px'}}>
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

            { reportData && (
                <div>
                    <h3>Resumo do Período</h3>
                    <p><strong>Total de Horas:</strong> {formatDecimalHours(reportData.periodTotals.totalHours)} </p>
                    <p><strong>Valor Total a Pagar:</strong> $ {reportData.periodTotals.totalAmount.toFixed(2)} </p>

                    <h3 style={{ marginTop:'30px'}}>Detalhamento por Funcionário</h3>

                    <table border="1" style={{width:'100%', borderCollapse: 'collapse'}}>
                        <thead>
                            <tr>
                                <th>Funcionário</th>
                                <th>Total de Horas</th>
                                <th>Valor a Pagar</th>
                            </tr>
                        </thead>
                        <tbody>
                            {reportData.employeePayrolls.map( data => (
                                <tr key={data.employeeId}>
                                    <td>{data.employeeName}</td>
                                    <td>{formatDecimalHours(data.totalHours)}</td>
                                    <td>$ {data.totalAmount.toFixed(2)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default PayrollReportPage;
