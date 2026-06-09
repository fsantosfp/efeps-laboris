import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, Image } from 'react-native';
import api from '../services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { theme } from '../styles/theme';
import { globalStyles } from '../styles/globalStyles';

const LoginScreen = ({ onLoginSucess }) => {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleLogin = async () => {
        try {
            const response = await api.post('/auth/login', {email, password});
            const { token, passwordResetRequired } = response.data;
            await AsyncStorage.setItem('authToken', token);
            await AsyncStorage.setItem('passwordResetRequired', String(passwordResetRequired));

            Alert.alert("Sucesso", "Login realizado com sucesso!");
            onLoginSucess(passwordResetRequired);
        } catch (error) {
            Alert.alert("Erro no Login", "E-mail ou senha inválidos.");
        }
    }

    const styles = StyleSheet.create({
        container: { 
            flex: 1, 
            justifyContent: 'center', 
            padding: theme.spacing.sm,
            backgroundColor: theme.colors.canvas
        },
        card: {
            ...globalStyles.card,
            padding: theme.spacing.md,
        },
        logo: {
            width: 48,
            height: 48,
            alignSelf: 'center',
            marginBottom: theme.spacing.xs,
        },
        title: { 
            fontSize: 24, 
            fontWeight: 'bold', 
            textAlign: 'center', 
            color: theme.colors.textMain,
            marginBottom: theme.spacing.xs 
        },
        subtitle: {
            fontSize: 14,
            color: theme.colors.textMuted,
            textAlign: 'center',
            marginBottom: theme.spacing.md,
        },
        label: {
            fontSize: 12,
            fontWeight: 'bold',
            color: theme.colors.textMuted,
            marginBottom: 6,
            textTransform: 'uppercase',
            letterSpacing: 0.5,
        },
        input: {
            ...globalStyles.input,
        }
    });

    return (
        <View style={ styles.container }>
            <View style={ styles.card }>
                <Image 
                    source={require('../assets/mactronLogo.png')} 
                    style={styles.logo} 
                    resizeMode="contain"
                />
                <Text style={ styles.title }>Laboris</Text>
                <Text style={ styles.subtitle }>Acesso Colaborador</Text>
                
                <Text style={ styles.label }>E-mail</Text>
                <TextInput
                    style={ styles.input }
                    placeholder='nome@mactron.com'
                    placeholderTextColor={theme.colors.textMuted}
                    value={email}
                    onChangeText={setEmail}
                    keyboardType='email-address'
                    autoCapitalize='none'
                />
    
                <Text style={ styles.label }>Senha</Text>
                <TextInput
                    style={ styles.input }
                    placeholder='Digite sua senha'
                    placeholderTextColor={theme.colors.textMuted}
                    value={password}
                    onChangeText={setPassword}
                    secureTextEntry
                />
    
                <TouchableOpacity 
                    style={[globalStyles.btn, globalStyles.btnPrimary, { marginTop: theme.spacing.sm }]} 
                    onPress={handleLogin}
                >
                    <Text style={[globalStyles.btnText, globalStyles.btnPrimaryText]}>Entrar</Text>
                </TouchableOpacity>
            </View>
        </View>
    )
}

export default LoginScreen;