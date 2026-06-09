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
    ScrollView,
    SafeAreaView
} from 'react-native';
import api from '../services/api';
import { theme } from '../styles/theme';
import { globalStyles } from '../styles/globalStyles';
import ScreenHeader from '../components/ScreenHeader';

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
        <SafeAreaView style={{ flex: 1, backgroundColor: theme.colors.canvas }}>
            <ScreenHeader title="Segurança" />
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
                            placeholderTextColor={theme.colors.textMuted}
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
                            placeholderTextColor={theme.colors.textMuted}
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
                                <Text style={[styles.buttonText, !isValid && styles.buttonTextDisabled]}>Confirmar Nova Senha</Text>
                            )}
                        </TouchableOpacity>
                    </View>
                </ScrollView>
            </KeyboardAvoidingView>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    keyboardContainer: {
        flex: 1,
        backgroundColor: theme.colors.canvas,
    },
    scrollContainer: {
        flexGrow: 1,
        justifyContent: 'center',
        padding: theme.spacing.sm,
    },
    card: {
        ...globalStyles.card,
        padding: theme.spacing.md,
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        color: theme.colors.textMain,
        textAlign: 'center',
        marginBottom: 8,
    },
    subtitle: {
        fontSize: 14.5,
        color: theme.colors.textMuted,
        textAlign: 'center',
        marginBottom: 24,
        lineHeight: 20,
    },
    label: {
        fontSize: 12,
        fontWeight: 'bold',
        color: theme.colors.textMuted,
        textTransform: 'uppercase',
        marginBottom: 8,
        letterSpacing: 0.5,
    },
    input: {
        ...globalStyles.input,
    },
    requirementsBox: {
        backgroundColor: theme.colors.surfaceLow,
        borderColor: theme.colors.borderSubtle,
        borderWidth: 1,
        borderRadius: theme.radius.card,
        padding: 12,
        marginBottom: 24,
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
        backgroundColor: theme.colors.textMuted,
        marginRight: 8,
    },
    bulletValid: {
        backgroundColor: theme.colors.success,
    },
    requirementText: {
        fontSize: 13,
        color: theme.colors.textMuted,
    },
    textValid: {
        color: theme.colors.success,
    },
    button: {
        ...globalStyles.btn,
        backgroundColor: theme.colors.primary,
    },
    buttonDisabled: {
        backgroundColor: theme.colors.surfaceContainer,
        elevation: 0,
    },
    buttonText: {
        ...globalStyles.btnText,
        color: '#ffffff',
    },
    buttonTextDisabled: {
        color: theme.colors.textMuted,
    },
});

export default ChangePasswordScreen;
