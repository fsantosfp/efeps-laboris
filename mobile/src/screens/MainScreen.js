import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, Button, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import Geolocation from '@react-native-community/geolocation';
import api from '../services/api';

const MainScreen = ({ onLogout: handleLogout }) => {
    const [loading, setLoading] = useState(true);
    const [currentJob, setCurrentJob] = useState(null);
    const [errorMessage, setErrorMessage] = useState('');
    const [lastEntry, setLastEntry] = useState(null);
    const [currentPosition, setCurrentPosition] = useState(null);

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

                    const [assignmentsRes, lastEntryRes] = await Promise.all([
                        api.get('/my-assignments'),
                        api.get('/time-entries/me/last').catch(err => {
                            if (err.response && err.response.status === 404) {
                                return { data: null };
                            }
                            throw err;
                        })
                    ]);

                    setLastEntry(lastEntryRes.data);
                    console.log(`[Developer Log] Trabalhos designados:`, assignmentsRes.data);

                    const foundJob = assignmentsRes.data.find( job => {
                        const latDiff = Math.abs(job.latitude - latitude)
                        const lonDiff = Math.abs(job.longitude - longitude)
                        return latDiff < 0.1 && lonDiff < 0.1;
                    })

                    if(foundJob){
                        setCurrentJob(foundJob)
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
                    setLoading(false)
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

        if(!currentJob || !currentPosition) return;
        
        setLoading(true);
        try {
            const response = await api.post('/time-entries', {
                jobId: currentJob.jobId,
                entryType: entryType,
                latitude: currentPosition.latitude,
                longitude: currentPosition.longitude,
                isManual: false
            });

            setLastEntry(response.data);
            Alert.alert("Sucesso!", `Ponto '${entryType}' registrado.`);
        } catch(error){
            Alert.alert("Erro ao Bater o Ponto", error.response?.data?.message || "Ocorreu um erro.");
        }finally{
            setLoading(false);
        }
    }

    const renderActionButtons = () => {
            if(!currentJob) return null;

            if (!lastEntry || lastEntry.entryType === 'OUT') {
                return <Button title="Registrar Entrada (IN)" onPress={() => handlePunch('IN')} />;
            } else { 
                return <Button title="Registrar Saída (OUT)" onPress={() => handlePunch('OUT')} />;
            }
        }

    const styles = StyleSheet.create({
        container: { flex:1, justifyContent:'center', alignItems: 'center', padding: 20 },
        title: { fontSize:24, fontWeight: 'bold', marginBottom:30 },
        statusText: { fontSize:16, color:'gray', textAlign:'center'},
        jobContainer: { alignItems: 'center' },
        jobLabel: { fontSize:16, color:'gray' },
        jobAddress: { fontSize:18, fontWeight:'bold', marginVertical:10, textAlign:'center' },
        logoutButton: { position:'absolute', bottom:40 },
        errorContainer: { alignItems: 'center' },
        errorText: { fontSize: 16, color: 'red', textAlign: 'center', marginBottom: 20 },
        logoutButton: { position: 'absolute', bottom: 50 }
    })

    return (
        <View style={ styles.container }>
            <Text style={ styles.title }>Meu Ponto</Text>

            { loading && <ActivityIndicator size="large" color="#0000ff" /> }

            { !loading && currentJob && (
                <View style={ styles.jobContainer } >
                    <Text style={ styles.jobLabel } > Trabalho Atual:</Text>
                    <Text style={ styles.jobAddress} > { currentJob.address }</Text>
                    <View style={{ marginTop:20 }}>
                        {renderActionButtons()}
                    </View>
                </View>
            ) }

            { !loading && errorMessage && (
                <View style={styles.errorContainer}>
                    <Text style={styles.errorText}>{errorMessage}</Text>
                    <Button title="Tentar Novamente" onPress={fetchData} />
                </View>
            )}

            {
                <View style={ styles.logoutButton }>
                    <Button title="Sair" onPress={handleLogout} color="red" />
                </View>
            }
        </View>
    );
};

export default MainScreen;