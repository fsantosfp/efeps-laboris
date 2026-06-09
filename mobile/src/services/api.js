import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Alert } from 'react-native';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'
});

api.interceptors.request.use(async (config) => {
    const token = await AsyncStorage.getItem('authToken');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
}, (error) => Promise.reject(error));

// Callback registrado pelo App.tsx para resetar o estado de autenticação
let sessionExpiredCallback = null;
let isAlertVisible = false;

export const setSessionExpiredCallback = (callback) => {
    sessionExpiredCallback = callback;
};

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response && error.response.status === 401) {
            if (!isAlertVisible) {
                isAlertVisible = true;
                Alert.alert(
                    'Sessão Expirada',
                    'Sua sessão expirou. Deseja fazer login novamente?',
                    [
                        {
                            text: 'Não',
                            style: 'cancel',
                            onPress: () => { isAlertVisible = false; },
                        },
                        {
                            text: 'Fazer Login',
                            onPress: async () => {
                                isAlertVisible = false;
                                await AsyncStorage.removeItem('authToken');
                                await AsyncStorage.removeItem('passwordResetRequired');
                                if (sessionExpiredCallback) sessionExpiredCallback();
                            },
                        },
                    ],
                    { cancelable: false }
                );
            }
        }
        return Promise.reject(error);
    }
);

export default api;