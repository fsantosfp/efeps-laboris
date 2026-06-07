import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, Button, StyleSheet, ActivityIndicator, Alert, TouchableOpacity } from 'react-native';
import Geolocation from '@react-native-community/geolocation';
import api from '../services/api';

const MainScreen = ({ onLogout: handleLogout }) => {
    const [loading, setLoading] = useState(true);
    const [currentJob, setCurrentJob] = useState(null);
    const [errorMessage, setErrorMessage] = useState('');
    const [lastEntry, setLastEntry] = useState(null);
    const [currentPosition, setCurrentPosition] = useState(null);
    const [assignments, setAssignments] = useState([]);
    const [activeDisplacement, setActiveDisplacement] = useState(null);
    const [selectedDestinationJobId, setSelectedDestinationJobId] = useState('');

    const fetchData = useCallback(() => {
        setLoading(true);
        setErrorMessage('');
        setCurrentJob(null);

        Geolocation.getCurrentPosition(
            async (position) => {
                const {latitude, longitude} = position.coords;
                setCurrentPosition(position.coords);
                console.log(`[Developer Log] Dispositivo coordenadas: Lat: ${latitude}, Lon: ${longitude}`);

                try{
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

                    const foundJob = assignmentsRes.data.find( job => {
                        const latDiff = Math.abs(job.latitude - latitude);
                        const lonDiff = Math.abs(job.longitude - longitude);
                        return latDiff < 0.1 && lonDiff < 0.1;
                    });

                    if(foundJob){
                        setCurrentJob(foundJob);
                    } else {
                        if (assignmentsRes.data.length > 0) {
                            const firstJob = assignmentsRes.data[0];
                            setErrorMessage(`Você não parece estar perto do local de trabalho designado.\n\nSua posição atual:\nLat: ${latitude.toFixed(4)}, Lon: ${longitude.toFixed(4)}\n\nCoordenadas do trabalho:\nLat: ${firstJob.latitude.toFixed(4)}, Lon: ${firstJob.longitude.toFixed(4)}\n\n(Aproxime-se a menos de 0.1 graus ou configure seu emulador com estas coordenadas).`);
                        } else {
                            setErrorMessage('Você não possui nenhum trabalho ativo designado.');
                        }
                    }
                }
                catch (error) {
                    setErrorMessage('Falha ao buscar seus dados de trabalho.');
                }finally{
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

    const handlePunch = async (entryType) => {
        if(!currentJob) return;
        
        setLoading(true);
        Geolocation.getCurrentPosition(
            async (position) => {
                const { latitude, longitude } = position.coords;
                setCurrentPosition(position.coords);
                try {
                    const response = await api.post('/time-entries', {
                        jobId: currentJob.jobId,
                        entryType: entryType,
                        latitude,
                        longitude,
                        isManual: false
                    });

                    setLastEntry(response.data);
                    Alert.alert("Sucesso!", `Ponto '${entryType}' registrado.`);
                } catch(error){
                    Alert.alert("Erro ao Bater o Ponto", error.response?.data?.message || "Ocorreu um erro.");
                }finally{
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
                setCurrentPosition(position.coords);
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
                setCurrentPosition(position.coords);
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

                    Alert.alert("Deslocamento Finalizado", "Deslocamento concluído e Entrada (IN) registrada com sucesso.");
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

    const renderActionButtons = () => {
        if(!currentJob) return null;

        if (!lastEntry || lastEntry.entryType === 'OUT') {
            return <Button title="Registrar Entrada (IN)" onPress={() => handlePunch('IN')} />;
        } else { 
            return <Button title="Registrar Saída (OUT)" onPress={() => handlePunch('OUT')} />;
        }
    };

    const styles = StyleSheet.create({
        container: { flex:1, justifyContent:'center', alignItems: 'center', padding: 20, backgroundColor: '#f5f6fa' },
        card: { width: '100%', padding: 20, borderRadius: 10, backgroundColor: '#ffffff', alignItems: 'center', shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.1, shadowRadius: 8, elevation: 5, marginBottom: 20 },
        title: { fontSize:24, fontWeight: 'bold', marginBottom:30, color: '#2c3e50' },
        statusText: { fontSize:16, color:'gray', textAlign:'center'},
        jobContainer: { alignItems: 'center', width: '100%' },
        jobLabel: { fontSize:14, color:'#7f8c8d', fontWeight: '600' },
        jobAddress: { fontSize:18, fontWeight:'bold', marginVertical:10, textAlign:'center', color: '#2c3e50' },
        errorContainer: { alignItems: 'center', width: '100%' },
        errorText: { fontSize: 14, color: '#e74c3c', textAlign: 'center', marginBottom: 20 },
        displacementContainer: { width: '100%', alignItems: 'center' },
        displacementTitle: { fontSize: 18, fontWeight: 'bold', color: '#d35400', marginBottom: 15 },
        jobItem: { width: '100%', padding: 15, borderRadius: 8, borderWidth: 1, borderColor: '#bdc3c7', marginVertical: 5, backgroundColor: '#fff' },
        jobItemSelected: { borderColor: '#d35400', backgroundColor: '#fef5e7' },
        jobItemText: { fontSize: 14, color: '#2c3e50', fontWeight: '500' },
        buttonContainer: { width: '100%', marginTop: 20 },
        startDisplacementButton: { backgroundColor: '#e67e22', padding: 15, borderRadius: 8, width: '100%', alignItems: 'center', marginTop: 10 },
        startDisplacementButtonText: { color: '#fff', fontSize: 16, fontWeight: 'bold' },
        logoutButton: { position: 'absolute', bottom: 50, width: '100%' }
    });

    return (
        <View style={ styles.container }>
            <Text style={ styles.title }>Meu Ponto</Text>

            { loading && <ActivityIndicator size="large" color="#0000ff" /> }

            { !loading && (
                <View style={ styles.card }>
                    { activeDisplacement ? (
                        <View style={ styles.displacementContainer }>
                            <Text style={ styles.displacementTitle }>🚀 Em Deslocamento</Text>
                            <Text style={{ fontSize: 14, color: '#7f8c8d', marginBottom: 15, textAlign: 'center' }}>
                                Selecione o Trabalho de Destino para finalizar o deslocamento e bater a entrada:
                            </Text>
                            { assignments.map(job => {
                                const isSelected = selectedDestinationJobId === job.jobId;
                                return (
                                    <TouchableOpacity
                                        key={job.jobId}
                                        style={[styles.jobItem, isSelected && styles.jobItemSelected]}
                                        onPress={() => setSelectedDestinationJobId(job.jobId)}
                                    >
                                        <Text style={styles.jobItemText}>{job.address}</Text>
                                    </TouchableOpacity>
                                );
                            })}
                            <View style={styles.buttonContainer}>
                                <Button
                                    title="Finalizar Deslocamento & Entrar"
                                    color="#e67e22"
                                    onPress={handleEndDisplacementAndClockIn}
                                    disabled={!selectedDestinationJobId}
                                />
                            </View>
                        </View>
                    ) : (
                        <View style={{ width: '100%', alignItems: 'center' }}>
                            { currentJob ? (
                                <View style={ styles.jobContainer } >
                                    <Text style={ styles.jobLabel } > Trabalho Atual:</Text>
                                    <Text style={ styles.jobAddress} > { currentJob.address }</Text>
                                    <View style={styles.buttonContainer}>
                                        {renderActionButtons()}
                                    </View>
                                </View>
                            ) : (
                                <View style={styles.errorContainer}>
                                    <Text style={styles.errorText}>{errorMessage}</Text>
                                    <Button title="Atualizar Localização" onPress={fetchData} />
                                </View>
                            )}
                        </View>
                    )}
                </View>
            )}

            {/* Iniciar Deslocamento Button */}
            { !loading && !activeDisplacement && assignments.length >= 2 && (!lastEntry || lastEntry.entryType === 'OUT') && (
                <TouchableOpacity style={styles.startDisplacementButton} onPress={handleStartDisplacement}>
                    <Text style={styles.startDisplacementButtonText}>Iniciar Deslocamento (Translado)</Text>
                </TouchableOpacity>
            )}

            <View style={ styles.logoutButton }>
                <Button title="Sair" onPress={handleLogout} color="red" />
            </View>
        </View>
    );
};

export default MainScreen;