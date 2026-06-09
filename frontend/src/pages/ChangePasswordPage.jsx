import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";
import "./ChangePasswordPage.css";

function ChangePasswordPage() {
    const { logout } = useAuth();
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);
    const [countdown, setCountdown] = useState(3);

    // Validation checks
    const isMinLength = newPassword.length >= 8;
    const isMatching = newPassword !== "" && newPassword === confirmPassword;
    const isValid = isMinLength && isMatching;

    // Countdown effect on success
    useEffect(() => {
        if (!success) return;

        const timer = setInterval(() => {
            setCountdown((prev) => {
                if (prev <= 1) {
                    clearInterval(timer);
                    logout();
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(timer);
    }, [success, logout]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!isValid) return;

        setLoading(true);
        setError("");

        try {
            await api.put("/me/password", {
                newPassword: newPassword
            });
            setSuccess(true);
        } catch (err) {
            console.error("Erro ao alterar senha:", err);
            const message = err.response?.data?.message || 
                            "Ocorreu um erro ao tentar alterar sua senha. Por favor, tente novamente.";
            setError(message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="change-password-container">
            <div className="glow-orb orb-1"></div>
            <div className="glow-orb orb-2"></div>

            <div className="glass-card card-surface">
                <h2>Segurança da Conta</h2>
                <p className="card-subtitle">Defina uma nova senha definitiva para acessar a plataforma.</p>

                {error && <div className="alert alert-error">{error}</div>}

                {success ? (
                    <div className="alert alert-success">
                        <p style={{ fontWeight: '600', margin: '0 0 10px 0', fontSize: '16px' }}>
                            ✓ Senha atualizada com sucesso!
                        </p>
                        <p style={{ margin: 0, fontSize: '14px' }}>
                            Redirecionando para o login em <span className="pulse-count">{countdown}</span> segundos...
                        </p>
                    </div>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label className="form-label">Nova Senha</label>
                            <div className="input-container">
                                <input
                                    type="password"
                                    className="form-input"
                                    placeholder="Digite a nova senha segura"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    required
                                    disabled={loading}
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Confirmar Nova Senha</label>
                            <div className="input-container">
                                <input
                                    type="password"
                                    className="form-input"
                                    placeholder="Repita a nova senha"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                    disabled={loading}
                                />
                            </div>
                        </div>

                        <div className="requirements-container">
                            <div className={`requirement-item ${isMinLength ? "valid" : ""}`}>
                                <div className="requirement-bullet"></div>
                                Mínimo de 8 caracteres
                            </div>
                            <div className={`requirement-item ${isMatching ? "valid" : ""}`}>
                                <div className="requirement-bullet"></div>
                                As senhas digitadas devem ser iguais
                            </div>
                        </div>

                        <button
                            type="submit"
                            className="btn btn-primary"
                            style={{ width: '100%' }}
                            disabled={!isValid || loading}
                        >
                            {loading ? (
                                <>
                                    <span className="loader-spinner"></span>
                                    Salvando...
                                </>
                            ) : (
                                "Confirmar Nova Senha"
                            )}
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
}

export default ChangePasswordPage;
