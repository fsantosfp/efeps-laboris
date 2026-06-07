import React, { useState } from 'react';
import { View, Text, Button, StyleSheet, SectionList, TextInput, ActivityIndicator, Alert } from 'react-native';
import api from '../services/api';
import { calculateWorkedHours } from '../utils/calculateWorkedHours';
import { formatDecimalHours } from '../utils/formatters';

const HistoryScreen = () => {

    const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
    const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
    const [sections, setSections] = useState([]);
    const [totalHours, setTotalHours] = useState(0);
    const [loading, setLoading] = useState(false);

    const fetchHistory = async () => {
        
        if(!startDate || !endDate) {
            Alert.alert("Erro", "Por favor, preencha as datas de ínicio e fim.");
            return;
        }

        setLoading(true);
        setSections([]);
        setTotalHours(0);

        try {

            const startISO = new Date(startDate + "T00:00:00.000Z").toISOString();
            const endISO = new Date(endDate + "T23:59:59.999Z").toISOString();

            const response = await api.get(`/time-entries/me?start=${startISO}&end=${endISO}`);
            const entries = response.data;

            const groupedByDate = entries.reduce((acc, entry) => {
                const date = new Date(entry.timestamp).toISOString().split('T')[0];
                if(!acc[date]){
                    acc[date] = []
                }
                acc[date].push(entry);
                return acc;
            }, {});

            const formattedSections = Object.keys(groupedByDate).map(date => ({
                title: new Date(date).toLocaleDateString('pt-BR', {timeZone: 'UTC'}),
                data: groupedByDate[date]
            })).sort((a, b) => b.title.localeCompare(a.title));

            setSections(formattedSections);

            const total = entries.length > 0 ? calculateWorkedHours(entries) : 0;
            setTotalHours(total);

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
                            <Text style={style.summaryText}> Total de Horas no Período:</Text>
                            <Text style={style.summaryHours}> {formatDecimalHours(totalHours)} </Text>
                        </View>
                    )}
                    <SectionList 
                        sections={sections}
                        keyExtractor={(item) => item.id}
                        renderItem={({item}) => (
                            <View style={style.entryItem}>
                                <Text style={ style.entryType }> {item.entryType === 'IN' ? 'Entrada' : 'Saída'} </Text>
                                <Text style={ style.entryTime}> {formatTime(item.timestamp)} </Text>
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