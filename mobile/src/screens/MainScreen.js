import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, Alert, TouchableOpacity, SafeAreaView } from 'react-native';
import Geolocation from '@react-native-community/geolocation';
import api from '../services/api';
import { theme } from '../styles/theme';
import { globalStyles } from '../styles/globalStyles';
import ScreenHeader from '../components/ScreenHeader';

const MainScreen = () => {
    const [loading, setLoading] = useState(true);
    const [currentJob, setCurrentJob] = useState(null);
    const [errorMessage, setErrorMessage] = useState('');
    const [lastEntry, setLastEntry] = useState(null);
    const [assignments, setAssignments] = useState([]);
    const [activeDisplacement, setActiveDisplacement] = useState(null);
    const [selectedDestinationJobId, setSelectedDestinationJobId] = useState('');
    const [secondsElapsed, setSecondsElapsed] = useState(0);

    const fetchData = useCallback(() => {
        setLoading(true);
        setErrorMessage('');
        setCurrentJob(null);

        Geolocation.getCurrentPosition(
            async (position) => {
                const { latitude, longitude } = position.coords;
                console.log(`[Developer Log] Dispositivo coordenadas: Lat: ${latitude}, Lon: ${longitude}`);

                try {
                    const [assignmentsRes, lastEntryRes, activeDisplacementRes] = await Promise.all([
                        api.get('/my-assignments'),
                        api.get('/time-entries/me/last').catch(err => {
                            if (err.response && err.response.status === 404) {
                                return { data: null };
                            }
                            throw err;
                        }),
                        api.get('/displacements/active').catch(err => {
                            if (err.response && (err.response.status === 404 || err.response.status === 204)) {
                                return { data: null };
                            }
                            throw err;
                        })
                    ]);

                    setAssignments(assignmentsRes.data);
                    setLastEntry(lastEntryRes.data);
                    setActiveDisplacement(activeDisplacementRes.data);
                    console.log(`[Developer Log] Trabalhos designados:`, assignmentsRes.data);

                    const foundJob = assignmentsRes.data.find(job => {
                        const latDiff = Math.abs(job.latitude - latitude);
                        const lonDiff = Math.abs(job.longitude - longitude);
                        return latDiff < 0.1 && lonDiff < 0.1;
                    });

                    if (foundJob) {
                        setCurrentJob(foundJob);
                    } else {
                        if (assignmentsRes.data.length > 0) {
                            setErrorMessage('Você não está no local de trabalho designado.');
                        } else {
                            setErrorMessage('Você não possui nenhum trabalho ativo designado.');
                        }
                    }
                }
                catch (error) {
                    setErrorMessage('Falha ao buscar seus dados de trabalho.');
                } finally {
                    setLoading(false);
                }
            },

            (error) => {
                setErrorMessage('Não foi possível obter sua localização.');
                setLoading(false);
            },
            { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 }
        );
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    // Live Activity Clock Timer
    useEffect(() => {
        let intervalId;
        if (lastEntry && lastEntry.entryType === 'IN') {
            const calculateElapsed = () => {
                const diffMs = new Date() - new Date(lastEntry.timestamp);
                setSecondsElapsed(Math.max(0, Math.floor(diffMs / 1000)));
            };
            calculateElapsed();
            intervalId = setInterval(calculateElapsed, 1000);
        } else {
            setSecondsElapsed(0);
        }
        return () => {
            if (intervalId) clearInterval(intervalId);
        };
    }, [lastEntry]);

    const formatElapsedTime = (sec) => {
        const hrs = Math.floor(sec / 3600);
        const mins = Math.floor((sec % 3600) / 60);
        const secs = sec % 60;
        return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    const handlePunch = async (entryType) => {
        if (!currentJob) return;

        setLoading(true);
        Geolocation.getCurrentPosition(
            async (position) => {
                const { latitude, longitude } = position.coords;
                try {
                    const response = await api.post('/time-entries', {
                        jobId: currentJob.jobId,
                        entryType: entryType,
                        latitude,
                        longitude,
                        isManual: false
                    });

                    setLastEntry(response.data);
                    Alert.alert("Sucesso!", `Ponto registrado.`);
                } catch (error) {
                    Alert.alert("Erro ao Bater o Ponto", error.response?.data?.message || "Ocorreu um erro.");
                } finally {
                    setLoading(false);
                }
            },
            (error) => {
                Alert.alert("Erro", "Não foi possível obter sua localização atual para registrar o ponto.");
                setLoading(false);
            },
            { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
        );
    };

    const handleStartDisplacement = async () => {
        setLoading(true);
        Geolocation.getCurrentPosition(
            async (position) => {
                const { latitude, longitude } = position.coords;
                try {
                    const response = await api.post('/displacements/start', {
                        latitude,
                        longitude
                    });
                    setActiveDisplacement(response.data);
                    Alert.alert("Deslocamento Iniciado", "Sua viagem começou. Quando chegar ao destino, escolha o trabalho de destino e finalize o deslocamento.");
                } catch (error) {
                    Alert.alert("Erro ao Iniciar Deslocamento", error.response?.data?.message || "Ocorreu um erro.");
                } finally {
                    setLoading(false);
                }
            },
            (error) => {
                Alert.alert("Erro", "Não foi possível obter sua localização atual para iniciar o deslocamento.");
                setLoading(false);
            },
            { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
        );
    };

    const handleEndDisplacementAndClockIn = async () => {
        if (!selectedDestinationJobId) {
            Alert.alert("Aviso", "Selecione o trabalho de destino.");
            return;
        }

        setLoading(true);
        Geolocation.getCurrentPosition(
            async (position) => {
                const { latitude, longitude } = position.coords;
                try {
                    await api.post('/displacements/end', {
                        latitude,
                        longitude,
                        destinationJobId: selectedDestinationJobId
                    });

                    const response = await api.post('/time-entries', {
                        jobId: selectedDestinationJobId,
                        entryType: 'IN',
                        latitude,
                        longitude,
                        isManual: false
                    });

                    setActiveDisplacement(null);
                    setSelectedDestinationJobId('');
                    setLastEntry(response.data);

                    const newJob = assignments.find(j => j.jobId === selectedDestinationJobId);
                    if (newJob) {
                        setCurrentJob(newJob);
                    }

                    Alert.alert("Deslocamento Finalizado", "Deslocamento concluído e Clock In registrado com sucesso.");
                } catch (error) {
                    Alert.alert("Erro ao Finalizar Deslocamento", error.response?.data?.message || "Ocorreu um erro.");
                } finally {
                    setLoading(false);
                }
            },
            (error) => {
                Alert.alert("Erro", "Não foi possível obter sua localização atual para finalizar o deslocamento.");
                setLoading(false);
            },
            { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
        );
    };

    const handleCancelDisplacement = async () => {
        Alert.alert(
            "Confirmar",
            "Deseja realmente cancelar o deslocamento? O período decorrido será desconsiderado.",
            [
                { text: "Não", style: "cancel" },
                {
                    text: "Sim",
                    onPress: async () => {
                        setLoading(true);
                        try {
                            await api.delete('/displacements/active');
                            setActiveDisplacement(null);
                            setSelectedDestinationJobId('');
                            Alert.alert("Sucesso", "Deslocamento cancelado.");
                        } catch (error) {
                            Alert.alert("Erro ao Cancelar", error.response?.data?.message || "Ocorreu um erro.");
                        } finally {
                            setLoading(false);
                        }
                    }
                }
            ]
        );
    };

    const renderActionButtons = () => {
        if (!currentJob) return null;

        if (!lastEntry || lastEntry.entryType === 'OUT') {
            return (
                <TouchableOpacity
                    style={[globalStyles.btn, globalStyles.btnSuccess, { width: '100%' }]}
                    onPress={() => handlePunch('IN')}
                >
                    <Text style={[globalStyles.btnText, globalStyles.btnSuccessText]}>Clock In</Text>
                </TouchableOpacity>
            );
        } else {
            return (
                <TouchableOpacity
                    style={[globalStyles.btn, {
                        backgroundColor: '#ffffff',
                        borderWidth: 2,
                        borderColor: theme.colors.success,
                        width: '100%'
                    }]}
                    onPress={() => handlePunch('OUT')}
                >
                    <Text style={[globalStyles.btnText, { color: theme.colors.success }]}>Clock Out</Text>
                </TouchableOpacity>
            );
        }
    };

    const styles = StyleSheet.create({
        container: {
            flex: 1,
            padding: theme.spacing.sm,
            backgroundColor: theme.colors.canvas,
            alignItems: 'center',
            justifyContent: 'center',
        },
        card: {
            ...globalStyles.card,
            width: '100%',
            padding: theme.spacing.md,
            alignItems: 'center',
            marginBottom: theme.spacing.sm
        },
        statusText: {
            fontSize: 16,
            color: theme.colors.textMuted,
            textAlign: 'center'
        },
        jobContainer: {
            alignItems: 'center',
            width: '100%'
        },
        jobLabel: {
            fontSize: 12,
            color: theme.colors.textMuted,
            fontWeight: '600',
            textTransform: 'uppercase',
            letterSpacing: 0.5,
        },
        jobAddress: {
            fontSize: 18,
            fontWeight: 'bold',
            marginVertical: theme.spacing.xs,
            textAlign: 'center',
            color: theme.colors.textMain
        },
        errorContainer: {
            alignItems: 'center',
            width: '100%'
        },
        errorCard: {
            backgroundColor: theme.colors.danger,
            borderRadius: theme.radius.card,
            padding: theme.spacing.sm,
            width: '100%',
            alignItems: 'center',
            marginBottom: theme.spacing.xs,
        },
        errorIcon: {
            fontSize: 24,
            color: '#ffffff',
            marginBottom: 4,
        },
        errorText: {
            fontSize: 14.5,
            color: '#ffffff',
            textAlign: 'center',
            fontWeight: '500',
        },
        displacementContainer: {
            width: '100%',
            alignItems: 'center'
        },
        displacementTitle: {
            fontSize: 18,
            fontWeight: 'bold',
            color: theme.colors.warning,
            marginBottom: 15
        },
        jobItem: {
            width: '100%',
            padding: 12,
            borderRadius: theme.radius.control,
            borderWidth: 1.5,
            borderColor: theme.colors.borderSubtle,
            marginVertical: 4,
            backgroundColor: theme.colors.surface
        },
        jobItemSelected: {
            borderColor: theme.colors.primary,
            backgroundColor: theme.colors.secondaryContainer
        },
        jobItemText: {
            fontSize: 14,
            color: theme.colors.textMain,
            fontWeight: '500'
        },
        jobItemTextSelected: {
            color: theme.colors.primary,
            fontWeight: '600',
        },
        buttonContainer: {
            width: '100%',
            marginTop: theme.spacing.sm
        },
        buttonDisabled: {
            backgroundColor: theme.colors.surfaceContainer,
            elevation: 0,
        },
        buttonTextDisabled: {
            color: theme.colors.textMuted,
        },
        clockContainer: {
            alignItems: 'center',
            marginVertical: theme.spacing.sm,
            padding: theme.spacing.sm,
            backgroundColor: theme.colors.surfaceLow,
            borderRadius: theme.radius.card,
            width: '100%',
        },
        clockLabel: {
            fontSize: 11,
            color: theme.colors.textMuted,
            fontWeight: '600',
            textTransform: 'uppercase',
            letterSpacing: 0.5,
            marginBottom: 4,
        },
        clockText: {
            fontSize: theme.typography.displayClock.fontSize,
            fontWeight: theme.typography.displayClock.fontWeight,
            color: theme.colors.textMain,
        }
    });

    return (
        <SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.canvas }}>
            <ScreenHeader title="Meu Ponto" />
            <View style={styles.container}>
                {loading && <ActivityIndicator size="large" color={theme.colors.primary} />}

                {!loading && (
                    <View style={styles.card}>
                        {activeDisplacement ? (
                            <View style={styles.displacementContainer}>
                                <Text style={styles.displacementTitle}>🚀 Em Deslocamento</Text>
                                <Text style={{ fontSize: 14.5, color: theme.colors.textMuted, marginBottom: 15, textAlign: 'center' }}>
                                    Selecione o Trabalho de Destino para finalizar o deslocamento e bater a entrada:
                                </Text>
                                {assignments
                                    .filter(job => !lastEntry || job.jobId !== lastEntry.jobId)
                                    .map(job => {
                                        const isSelected = selectedDestinationJobId === job.jobId;
                                        return (
                                            <TouchableOpacity
                                                key={job.jobId}
                                                style={[styles.jobItem, isSelected && styles.jobItemSelected]}
                                                onPress={() => setSelectedDestinationJobId(job.jobId)}
                                            >
                                                <Text style={[styles.jobItemText, isSelected && styles.jobItemTextSelected]}>{job.address}</Text>
                                            </TouchableOpacity>
                                        );
                                    })
                                }
                                <View style={styles.buttonContainer}>
                                    <TouchableOpacity
                                        style={[globalStyles.btn, globalStyles.btnPrimary, { width: '100%' }, !selectedDestinationJobId && styles.buttonDisabled]}
                                        onPress={handleEndDisplacementAndClockIn}
                                        disabled={!selectedDestinationJobId || loading}
                                    >
                                        <Text style={[globalStyles.btnText, globalStyles.btnPrimaryText, !selectedDestinationJobId && styles.buttonTextDisabled]}>Finalizar Deslocamento & Clock In</Text>
                                    </TouchableOpacity>
                                </View>
                                <View style={[styles.buttonContainer, { marginTop: 8 }]}>
                                    <TouchableOpacity
                                        style={[globalStyles.btn, globalStyles.btnDanger, { width: '100%' }]}
                                        onPress={handleCancelDisplacement}
                                        disabled={loading}
                                    >
                                        <Text style={[globalStyles.btnText, globalStyles.btnDangerText]}>Cancelar Deslocamento</Text>
                                    </TouchableOpacity>
                                </View>
                            </View>
                        ) : (
                            <View style={{ width: '100%', alignItems: 'center' }}>
                                {currentJob ? (
                                    <View style={styles.jobContainer} >
                                        <Text style={styles.jobLabel} >Trabalho Atual:</Text>
                                        <Text style={styles.jobAddress} >{currentJob.address}</Text>

                                        {lastEntry && lastEntry.entryType === 'IN' && (
                                            <View style={styles.clockContainer}>
                                                <Text style={styles.clockLabel}>Tempo em Atividade</Text>
                                                <Text style={styles.clockText}>{formatElapsedTime(secondsElapsed)}</Text>
                                            </View>
                                        )}

                                        <View style={styles.buttonContainer}>
                                            {renderActionButtons()}
                                        </View>
                                    </View>
                                ) : (
                                    <View style={styles.errorContainer}>
                                        <View style={styles.errorCard}>
                                            <Text style={styles.errorIcon}>⚠️</Text>
                                            <Text style={styles.errorText}>{errorMessage}</Text>
                                        </View>
                                        <TouchableOpacity
                                            style={[globalStyles.btn, globalStyles.btnPrimary, { width: '100%', marginTop: theme.spacing.sm }]}
                                            onPress={fetchData}
                                        >
                                            <Text style={[globalStyles.btnText, globalStyles.btnPrimaryText]}>Atualizar Localização</Text>
                                        </TouchableOpacity>
                                    </View>
                                )}
                            </View>
                        )}
                    </View>
                )}

                {/* Iniciar Deslocamento Button */}
                {!loading && !activeDisplacement && assignments.length >= 2 && (!lastEntry || lastEntry.entryType === 'OUT') && (
                    <TouchableOpacity
                        style={[globalStyles.btn, globalStyles.btnSecondary, { width: '100%' }]}
                        onPress={handleStartDisplacement}
                    >
                        <Text style={[globalStyles.btnText, globalStyles.btnSecondaryText]}>Iniciar Deslocamento</Text>
                    </TouchableOpacity>
                )}
            </View>
        </SafeAreaView>
    );
};

export default MainScreen;