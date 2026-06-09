import React, { useState } from 'react';
import { View, Text, StyleSheet, SectionList, TextInput, ActivityIndicator, Alert, TouchableOpacity, SafeAreaView } from 'react-native';
import api from '../services/api';
import { calculateWorkedHours } from '../utils/calculateWorkedHours';
import { calculateBreakHours } from '../utils/calculateBreakHours';
import { formatDecimalHours } from '../utils/formatters';
import { theme } from '../styles/theme';
import { globalStyles } from '../styles/globalStyles';
import ScreenHeader from '../components/ScreenHeader';

const HistoryScreen = () => {

    const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
    const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
    const [sections, setSections] = useState([]);
    const [totalHours, setTotalHours] = useState(0);
    const [totalBreakHours, setTotalBreakHours] = useState(0);
    const [loading, setLoading] = useState(false);

    const fetchHistory = async () => {

        if (!startDate || !endDate) {
            Alert.alert("Erro", "Por favor, preencha as datas de ínicio e fim.");
            return;
        }

        setLoading(true);
        setSections([]);
        setTotalHours(0);
        setTotalBreakHours(0);

        try {

            const startISO = new Date(`${startDate}T00:00:00`).toLocaleString('en-US', { timeZone: 'America/New_York' });
            // Build ET-aware day boundaries using Intl offset calculation
            const buildETBoundary = (dateStr, hour, minute, second, ms) => {
                // Parse in local time of the machine, then adjust to NY offset
                const dt = new Date(`${dateStr}T${String(hour).padStart(2,'0')}:${String(minute).padStart(2,'0')}:${String(second).padStart(2,'0')}.${String(ms).padStart(3,'0')}`);
                const nyStr = dt.toLocaleString('en-US', { timeZone: 'America/New_York' });
                const localStr = dt.toLocaleString('en-US');
                const offsetMs = new Date(localStr) - new Date(nyStr);
                return new Date(dt.getTime() + offsetMs).toISOString();
            };
            const startISOStr = buildETBoundary(startDate, 0, 0, 0, 0);
            const endISOStr = buildETBoundary(endDate, 23, 59, 59, 999);

            const [entriesRes, displacementsRes] = await Promise.all([
                api.get(`/time-entries/me?start=${startISOStr}&end=${endISOStr}`),
                api.get(`/displacements/me?start=${startISOStr}&end=${endISOStr}`).catch(err => {
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
                // Group by local date in America/New_York timezone
                const date = new Date(item.timestamp).toLocaleDateString('en-US', {
                    timeZone: 'America/New_York',
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                });
                if (!acc[date]) {
                    acc[date] = [];
                }
                acc[date].push(item);
                return acc;
            }, {});

            const formattedSections = Object.keys(groupedByDate).map(date => {
                const sortedDayData = groupedByDate[date].sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
                return {
                    title: date, // already formatted as MM/DD/YYYY by Intl
                    data: sortedDayData
                };
            }).sort((a, b) => new Date(b.title) - new Date(a.title));

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
        return new Date(timestamp).toLocaleTimeString('en-US', {
            timeZone: 'America/New_York',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true,
        });
    };

    const style = StyleSheet.create({
        container: {
            flex: 1,
            padding: theme.spacing.sm,
            backgroundColor: theme.colors.canvas
        },
        dateContainer: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: theme.spacing.xs
        },
        inputLabel: {
            fontSize: 11,
            fontWeight: 'bold',
            color: theme.colors.textMuted,
            marginBottom: 4,
            textTransform: 'uppercase',
            letterSpacing: 0.5,
        },
        inputWrapper: {
            flex: 1,
            marginRight: 8,
        },
        input: {
            ...globalStyles.input,
            marginBottom: 0,
        },
        summaryContainer: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            marginVertical: theme.spacing.xs,
        },
        summaryCard: {
            ...globalStyles.card,
            flex: 1,
            marginHorizontal: 4,
            alignItems: 'center',
            paddingVertical: 12,
        },
        summaryText: {
            fontSize: 12,
            fontWeight: '600',
            color: theme.colors.textMuted,
            textTransform: 'uppercase',
            letterSpacing: 0.5,
            marginBottom: 4,
        },
        summaryHours: {
            fontSize: 20,
            fontWeight: 'bold',
            color: theme.colors.textMain
        },
        sectionHeader: {
            fontSize: 13,
            fontWeight: 'bold',
            backgroundColor: theme.colors.surfaceLow,
            color: theme.colors.textMuted,
            paddingVertical: 8,
            paddingHorizontal: 12,
            marginTop: theme.spacing.sm,
            borderRadius: theme.radius.control,
            textTransform: 'uppercase',
            letterSpacing: 0.5,
        },
        entryItem: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            paddingVertical: 12,
            paddingHorizontal: 16,
            borderBottomWidth: 1,
            borderBottomColor: theme.colors.borderSubtle,
            backgroundColor: theme.colors.surface
        },
        entryType: {
            fontSize: 15,
            fontWeight: '600',
            color: theme.colors.textMain,
        },
        entryTime: {
            fontSize: 14,
            color: theme.colors.textMain
        },
        emptyText: {
            textAlign: 'center',
            marginTop: 40,
            color: theme.colors.textMuted,
            fontSize: 14.5,
        }
    });

    return (
        <SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.canvas }}>
            <ScreenHeader title="Histórico" />
            <View style={style.container}>
                <View style={style.dateContainer}>
                    <View style={style.inputWrapper}>
                        <Text style={style.inputLabel}>Data Início</Text>
                        <TextInput
                            style={style.input}
                            placeholder='YYYY-MM-DD'
                            placeholderTextColor={theme.colors.textMuted}
                            value={startDate}
                            onChangeText={setStartDate}
                        />
                    </View>
                    <View style={[style.inputWrapper, { marginRight: 0 }]}>
                        <Text style={style.inputLabel}>Data Fim</Text>
                        <TextInput
                            style={style.input}
                            placeholder='YYYY-MM-DD'
                            placeholderTextColor={theme.colors.textMuted}
                            value={endDate}
                            onChangeText={setEndDate}
                        />
                    </View>
                </View>

                <TouchableOpacity
                    style={[globalStyles.btn, globalStyles.btnPrimary, { width: '100%', marginVertical: theme.spacing.sm }]}
                    onPress={fetchHistory}
                >
                    <Text style={[globalStyles.btnText, globalStyles.btnPrimaryText]}>Buscar</Text>
                </TouchableOpacity>

                {loading ? (
                    <ActivityIndicator size="large" style={{ marginTop: 20 }} color={theme.colors.primary} />
                ) : (
                    <>
                        {sections.length > 0 && (
                            <View style={style.summaryContainer}>
                                <View style={style.summaryCard}>
                                    <Text style={style.summaryText}>Total Trabalhado</Text>
                                    <Text style={style.summaryHours}>{formatDecimalHours(totalHours)}</Text>
                                </View>
                                <View style={style.summaryCard}>
                                    <Text style={style.summaryText}>Total Intervalo</Text>
                                    <Text style={style.summaryHours}>{formatDecimalHours(totalBreakHours)}</Text>
                                </View>
                            </View>
                        )}
                        <SectionList
                            sections={sections}
                            keyExtractor={(item) => item.id}
                            renderItem={({ item }) => (
                                <View style={style.entryItem}>
                                    {item.entryType === 'DESLOCAMENTO' ? (
                                        <View style={{ flex: 1 }}>
                                            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                                                <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                                                    <Text style={{ marginRight: 8, fontSize: 16 }}>🚗</Text>
                                                    <Text style={[style.entryType, { color: theme.colors.warning, fontWeight: 'bold' }]}>Deslocamento</Text>
                                                </View>
                                                <Text style={style.entryTime}>
                                                    {formatTime(item.timestamp)} - {item.endTimestamp ? formatTime(item.endTimestamp) : 'Em andamento'}
                                                </Text>
                                            </View>
                                            {item.address ? (
                                                <Text style={{ fontSize: 12, color: theme.colors.textMuted, marginTop: 4, marginLeft: 24 }}>
                                                    Partida: {item.address}
                                                </Text>
                                            ) : null}
                                        </View>
                                    ) : (
                                        <>
                                            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                                                <Text style={{ marginRight: 8, fontSize: 16 }}>💼</Text>
                                                <Text style={style.entryType}>{item.entryType === 'IN' ? 'Clock In' : 'Clock Out'}</Text>
                                            </View>
                                            <Text style={style.entryTime}>{formatTime(item.timestamp)}</Text>
                                        </>
                                    )}
                                </View>
                            )}
                            renderSectionHeader={({ section: { title } }) => (
                                <Text style={style.sectionHeader}>{title}</Text>
                            )}
                            ListEmptyComponent={<Text style={style.emptyText}>Nenhuma batida encontrada neste período.</Text>}
                            style={{ width: '100%' }}
                        />
                    </>
                )}
            </View>
        </SafeAreaView>
    );
}

export default HistoryScreen;