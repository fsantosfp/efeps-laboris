import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    ActivityIndicator,
    Alert,
    BackHandler,
    KeyboardAvoidingView,
    Platform,
    ScrollView
} from 'react-native';
import api from '../services/api';

const ChangePasswordScreen = ({ onPasswordChanged }) => {
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);

    const isMinLength = newPassword.length >= 8;
    const isMatching = newPassword !== '' && newPassword === confirmPassword;
    const isValid = isMinLength && isMatching;

    // Disable Android Back Button
    useEffect(() => {
        const backAction = () => {
            // Return true to prevent default back action
            return true;
        };

        const backHandler = BackHandler.addEventListener(
            'hardwareBackPress',
            backAction
        );

        return () => backHandler.remove();
    }, []);

    const handleChangePassword = async () => {
        if (!isValid) return;

        setLoading(true);

        try {
            await api.put('/me/password', { newPassword });
            onPasswordChanged();
        } catch (error) {
            console.error('Erro ao redefinir senha no mobile:', error);
            const message = error.response?.data?.message || 
                            'Não foi possível atualizar a senha. Tente novamente.';
            Alert.alert('Erro', message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <KeyboardAvoidingView
            style={styles.keyboardContainer}
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        >
            <ScrollView contentContainerStyle={styles.scrollContainer} keyboardShouldPersistTaps="handled">
                <View style={styles.card}>
                    <Text style={styles.title}>Segurança da Conta</Text>
                    <Text style={styles.subtitle}>
                        Defina uma senha definitiva para continuar acessando o Laboris.
                    </Text>

                    <Text style={styles.label}>Nova Senha</Text>
                    <TextInput
                        style={styles.input}
                        placeholder="Digite a nova senha"
                        placeholderTextColor="#6b7280"
                        value={newPassword}
                        onChangeText={setNewPassword}
                        secureTextEntry
                        autoCapitalize="none"
                        editable={!loading}
                    />

                    <Text style={styles.label}>Confirmar Nova Senha</Text>
                    <TextInput
                        style={styles.input}
                        placeholder="Confirme a nova senha"
                        placeholderTextColor="#6b7280"
                        value={confirmPassword}
                        onChangeText={setConfirmPassword}
                        secureTextEntry
                        autoCapitalize="none"
                        editable={!loading}
                    />

                    <View style={styles.requirementsBox}>
                        <View style={styles.requirementItem}>
                            <View style={[styles.bullet, isMinLength && styles.bulletValid]} />
                            <Text style={[styles.requirementText, isMinLength && styles.textValid]}>
                                Mínimo de 8 caracteres
                            </Text>
                        </View>
                        <View style={styles.requirementItem}>
                            <View style={[styles.bullet, isMatching && styles.bulletValid]} />
                            <Text style={[styles.requirementText, isMatching && styles.textValid]}>
                                As senhas devem ser iguais
                            </Text>
                        </View>
                    </View>

                    <TouchableOpacity
                        style={[styles.button, !isValid && styles.buttonDisabled]}
                        onPress={handleChangePassword}
                        disabled={!isValid || loading}
                    >
                        {loading ? (
                            <ActivityIndicator size="small" color="#ffffff" />
                        ) : (
                            <Text style={styles.buttonText}>Confirmar Nova Senha</Text>
                        )}
                    </TouchableOpacity>
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    );
};

const styles = StyleSheet.create({
    keyboardContainer: {
        flex: 1,
        backgroundColor: '#0b0c10',
    },
    scrollContainer: {
        flexGrow: 1,
        justifyContent: 'center',
        padding: 20,
    },
    card: {
        backgroundColor: '#111218',
        borderRadius: 24,
        padding: 24,
        borderWidth: 1,
        borderColor: 'rgba(255, 255, 255, 0.08)',
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 10 },
        shadowOpacity: 0.5,
        shadowRadius: 15,
        elevation: 10,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#ffffff',
        textAlign: 'center',
        marginBottom: 8,
    },
    subtitle: {
        fontSize: 14,
        color: '#9ca3af',
        textAlign: 'center',
        marginBottom: 28,
        lineHeight: 20,
    },
    label: {
        fontSize: 12,
        fontWeight: 'bold',
        color: '#a5b4fc',
        textTransform: 'uppercase',
        marginBottom: 8,
        letterSpacing: 0.5,
    },
    input: {
        backgroundColor: 'rgba(15, 17, 26, 0.8)',
        borderColor: 'rgba(255, 255, 255, 0.1)',
        borderWidth: 1.5,
        borderRadius: 12,
        height: 50,
        paddingHorizontal: 16,
        fontSize: 15,
        color: '#ffffff',
        marginBottom: 16,
    },
    requirementsBox: {
        backgroundColor: 'rgba(255, 255, 255, 0.03)',
        borderRadius: 12,
        padding: 12,
        marginBottom: 24,
        borderWidth: 1,
        borderColor: 'rgba(255, 255, 255, 0.05)',
    },
    requirementItem: {
        flexDirection: 'row',
        alignItems: 'center',
        marginVertical: 4,
    },
    bullet: {
        width: 6,
        height: 6,
        borderRadius: 3,
        backgroundColor: '#4b5563',
        marginRight: 8,
    },
    bulletValid: {
        backgroundColor: '#34d399',
    },
    requirementText: {
        fontSize: 13,
        color: '#9ca3af',
    },
    textValid: {
        color: '#34d399',
    },
    button: {
        backgroundColor: '#6366f1',
        height: 50,
        borderRadius: 12,
        justifyContent: 'center',
        alignItems: 'center',
        shadowColor: '#6366f1',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 10,
        elevation: 5,
    },
    buttonDisabled: {
        backgroundColor: 'rgba(99, 102, 241, 0.4)',
        shadowOpacity: 0,
        elevation: 0,
    },
    buttonText: {
        color: '#ffffff',
        fontSize: 16,
        fontWeight: 'bold',
    },
});

export default ChangePasswordScreen;
