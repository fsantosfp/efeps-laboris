import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, Button, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import Geolocation from '@react-native-community/geolocation';
import api from '../services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

const MainScreen = ({ onLogout: handleLogout }) => {
    const [loading, setLoading] = useState(true);
    const [currentJob, setCurrentJob] = useState(null);
    const [statusMessage, setStatusMessage] = useState('Buscando sua localização...');
    const [error, setError] = useState(false);

    const findCurrentJob = useCallback(() => {
        setLoading(true);
        setStatusMessage('');
        setCurrentJob(null);

        Geolocation.getCurrentPosition(

            async (position) => {
                setStatusMessage('Localização encontrada! Buscando trabalhos...');
                const { latitude, longitude } = position.coords;

                console.log(position.coords)
                
                try{
                    const response =  await api.get('/my-assignments');
                    const assignedJobs = response.data;

                    const foundJob = assignedJobs.find( job => {
                        const latDiff = Math.abs(job.latitude - latitude);
                        const lonDiff = Math.abs(job.longitude - longitude);
                        return latDiff < 0.1 && lonDiff < 0.1
                    })

                    if (foundJob){
                        setCurrentJob(foundJob);
                        setStatusMessage('');
                        setError(false)
                    } else {
                        setStatusMessage('Você não parece estar em um local de trabalho designado.');
                        setError(true)
                    }

                } catch(error) {
                    setStatusMessage('Falha ao buscar seus trabalhos.')
                    setError(true)
                } finally {
                    setLoading(false)
                }
            }, (error) => {
                console.error("Erro de geolocalização:", error);
                setStatusMessage('Não foi possível obter sua localização. Verifique as permissões.')
                setLoading(false)
                setError(true)
            },
            { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 }
        );
    }, []);

    useEffect(() => {
        findCurrentJob();
    }, [findCurrentJob]);

    const handleClockIn = async () => {
        if(!currentJob) return;

        Alert.alert("Ponto Registrado", `Entrada registrada para o trabalho: ${currentJob.address}`)
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

            { loading && <ActivityIndicator size="large" /> }

            { !loading && currentJob && (
                <View style={ styles.jobContainer } >
                    <Text style={ styles.jobLabel } > Trabalho Atual:</Text>
                    <Text style={ styles.jobAddress} > { currentJob.address }</Text>
                    <Button title="Registrar Entrada (CLOCK_IN)" onPress={handleClockIn} />
                </View>
            ) }

            { !loading && error && (
                <View style={styles.errorContainer}>
                    <Text style={styles.errorText}>{statusMessage}</Text>
                    <Button title="Tentar Novamente" onPress={findCurrentJob} />
                </View>
            )}



            {/* { loading ? (
                <View>
                    <ActivityIndicator size="large" />
                    <Text style={ styles.statusText }> { statusMessage } </Text>
                </View>
            ) : currentJob ? (
                <View style={ styles.jobContainer } >
                    <Text style={ styles.jobLabel } > Trabalho Atual:</Text>
                    <Text style={ styles.jobAddress} > { currentJob.address }</Text>
                    <Button title="Registrar Entrada (CLOCK_IN)" onPress={handleClockIn} />
                </View>
            ) : (
                <Text style={styles.statusText}>{statusMessage}</Text>
            )} */}

            <View style={ styles.logoutButton }>
                <Button title="Sair" onPress={handleLogout} color="red" />
            </View>
        </View>
    );
};

export default MainScreen;