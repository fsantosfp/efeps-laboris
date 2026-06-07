import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, Alert } from 'react-native';
import api from '../services/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

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
        container: { flex:1, justifyContent:'center', padding:20 },
        title: { fontSize:24, fontWeight:'bold', textAlign:'center', marginBottom:20 },
        input: { height:40, borderColor:'gray', borderWidth:1, marginBottom:10, padding:10, borderRadius:5 }
    })

    return (
        <View style={ styles.container }>
            <Text style={ styles.title }>Laboris Mobile</Text>
            <TextInput
                style={ styles.input }
                placeholder='E-mail'
                value={email}
                onChangeText={setEmail}
                keyboardType='email-address'
                autoCapitalize='none'
            />

            <TextInput
                style={ styles.input }
                placeholder='Senha'
                value={password}
                onChangeText={setPassword}
                secureTextEntry
            />

            <Button title='Entrar' onPress={handleLogin} />
        </View>
    )
}

export default LoginScreen;