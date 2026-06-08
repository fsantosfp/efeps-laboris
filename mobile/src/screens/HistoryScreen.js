import React, { useState } from 'react';
import { View, Text, Button, StyleSheet, SectionList, TextInput, ActivityIndicator, Alert } from 'react-native';
import api from '../services/api';
import { calculateWorkedHours } from '../utils/calculateWorkedHours';
import { calculateBreakHours } from '../utils/calculateBreakHours';
import { formatDecimalHours } from '../utils/formatters';

const HistoryScreen = () => {

    const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
    const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
    const [sections, setSections] = useState([]);
    const [totalHours, setTotalHours] = useState(0);
    const [totalBreakHours, setTotalBreakHours] = useState(0);
    const [loading, setLoading] = useState(false);

    const fetchHistory = async () => {
        
        if(!startDate || !endDate) {
            Alert.alert("Erro", "Por favor, preencha as datas de ínicio e fim.");
            return;
        }

        setLoading(true);
        setSections([]);
        setTotalHours(0);
        setTotalBreakHours(0);

        try {

            const startISO = new Date(startDate + "T00:00:00.000Z").toISOString();
            const endISO = new Date(endDate + "T23:59:59.999Z").toISOString();

            const [entriesRes, displacementsRes] = await Promise.all([
                api.get(`/time-entries/me?start=${startISO}&end=${endISO}`),
                api.get(`/displacements/me?start=${startISO}&end=${endISO}`).catch(err => {
                    console.error("Erro ao buscar deslocamentos:", err);
                    return { data: [] };
                })
            ]);
            const entries = entriesRes.data;
            const displacements = displacementsRes.data;

            const mappedEntries = entries.map(e => ({
                id: e.id,
                entryType: e.entryType,
                timestamp: e.timestamp,
                jobAddress: e.jobAddress || ''
            }));

            const mappedDisplacements = displacements.map(d => ({
                id: d.id,
                entryType: 'DESLOCAMENTO',
                timestamp: d.startTimestamp,
                endTimestamp: d.endTimestamp,
                address: d.startAddress
            }));

            const combinedList = [...mappedEntries, ...mappedDisplacements];

            const groupedByDate = combinedList.reduce((acc, item) => {
                const date = new Date(item.timestamp).toISOString().split('T')[0];
                if(!acc[date]){
                    acc[date] = []
                }
                acc[date].push(item);
                return acc;
            }, {});

            const formattedSections = Object.keys(groupedByDate).map(date => {
                const sortedDayData = groupedByDate[date].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
                return {
                    title: new Date(date).toLocaleDateString('pt-BR', {timeZone: 'UTC'}),
                    data: sortedDayData
                };
            }).sort((a, b) => b.title.localeCompare(a.title));

            setSections(formattedSections);

            let total = entries.length > 0 ? calculateWorkedHours(entries) : 0;
            let displacementTotalHours = 0;
            displacements.forEach(d => {
                if (d.endTimestamp) {
                    const durationMs = new Date(d.endTimestamp) - new Date(d.startTimestamp);
                    displacementTotalHours += durationMs / 3600000;
                }
            });
            total += displacementTotalHours;
            setTotalHours(total);

            let breakTotal = entries.length > 0 ? calculateBreakHours(entries) : 0;
            setTotalBreakHours(breakTotal);

        } catch (error) {
            console.error("Erro ao buscar histórico:", error);
            Alert.alert("Erro", "Não foi possível buscar o histórico.");
        } finally {
            setLoading(false);
        }
    };

    const formatTime = (timestamp) => {
        return new Date(timestamp).toLocaleTimeString('pt-BR')
    }

    const style = StyleSheet.create({
        container: { flex: 1, padding: 20, paddingTop: 60, backgroundColor: '#f5f5f5' },
        title: { fontSize: 24, fontWeight: 'bold', textAlign: 'center', marginBottom: 20 },
        dateContainer: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 },
        input: { flex: 1, height: 40, borderColor: 'gray', borderWidth: 1, padding: 10, borderRadius: 5, marginRight: 10 },
        summaryContainer: { padding: 10, marginVertical: 10, backgroundColor: '#e0e0e0', borderRadius: 5, alignItems: 'center' },
        summaryText: { fontSize: 16, color: '#333' },
        summaryHours: { fontSize: 20, fontWeight: 'bold', color: '#000' },
        sectionHeader: { fontSize: 18, fontWeight: 'bold', backgroundColor: '#ddd', padding: 10, marginTop: 10 },
        entryItem: { flexDirection: 'row', justifyContent: 'space-between', padding: 15, borderBottomWidth: 1, borderBottomColor: '#eee', backgroundColor: '#fff' },
        entryType: { fontSize: 16, fontWeight: '500' },
        entryTime: { fontSize: 16, color: '#333' },
        emptyText: { textAlign: 'center', marginTop: 50, color: 'gray' }
    })

    return (
        <View style={style.container}>
            <Text style={style.title}> Histórico de Batidas </Text>
            <View style={style.dateContainer}>
                <TextInput
                    style={style.input}
                    placeholder='YYYT-MM-DD'
                    value={startDate}
                    onChangeText={setStartDate}
                />
                <TextInput
                    style={style.input}
                    placeholder='YYYT-MM-DD'
                    value={endDate}
                    onChangeText={setEndDate}
                />
            </View>
            <Button title='Buscar' onPress={fetchHistory} />

            { loading ? ( 
                <ActivityIndicator size="large" style={{ marginTop:20 }} />
            ) : (
                <>
                    {sections.length > 0 && (
                        <View style={style.summaryContainer}>
                            <View style={{ flexDirection: 'row', justifyContent: 'space-around', width: '100%' }}>
                                <View style={{ alignItems: 'center' }}>
                                    <Text style={style.summaryText}>Total Trabalhado:</Text>
                                    <Text style={style.summaryHours}>{formatDecimalHours(totalHours)}</Text>
                                </View>
                                <View style={{ alignItems: 'center' }}>
                                    <Text style={style.summaryText}>Total Intervalo:</Text>
                                    <Text style={style.summaryHours}>{formatDecimalHours(totalBreakHours)}</Text>
                                </View>
                            </View>
                        </View>
                    )}
                    <SectionList 
                        sections={sections}
                        keyExtractor={(item) => item.id}
                        renderItem={({item}) => (
                            <View style={style.entryItem}>
                                {item.entryType === 'DESLOCAMENTO' ? (
                                    <View style={{ flex: 1 }}>
                                        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <Text style={[style.entryType, { color: '#e67e22', fontWeight: 'bold' }]}>🚀 Translado (Deslocamento)</Text>
                                            <Text style={style.entryTime}>
                                                {formatTime(item.timestamp)} - {item.endTimestamp ? formatTime(item.endTimestamp) : 'Em andamento'}
                                            </Text>
                                        </View>
                                        {item.address ? (
                                            <Text style={{ fontSize: 12, color: 'gray', marginTop: 4 }}>
                                                Partida: {item.address}
                                            </Text>
                                        ) : null}
                                    </View>
                                ) : (
                                    <>
                                        <Text style={ style.entryType }> {item.entryType === 'IN' ? 'Entrada' : 'Saída'} </Text>
                                        <Text style={ style.entryTime}> {formatTime(item.timestamp)} </Text>
                                    </>
                                )}
                            </View>
                        )}
                        renderSectionHeader={({ section: {title} }) => (
                            <Text style={style.sectionHeader}>{title}</Text>
                        )}
                        ListEmptyComponent={<Text style={ style.emptyText}> Nenhuma batida encontrada neste período.</Text>}
                        style={{ width:'100%'}}
                    />
                </>
            )}
        </View>
    );
}

export default HistoryScreen;