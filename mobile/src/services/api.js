import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
})

api.interceptors.request.use(async ( config ) => {
    const token = await AsyncStorage.getItem('authToken');

    if(token) config.headers.Authorization = `Bearer ${token}`;

    return config;
}, (error) => Promise.reject(error));

export default api;