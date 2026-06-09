import React, { useState } from "react";
import axios from 'axios';
import { useAuth } from "../context/AuthContext";
import "./LoginPage.css";

const AUTHORIZED_ROLES = ['MANAGER', 'SAAS_OWNER'];

function parseJwtRole(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.role || null;
    } catch {
        return null;
    }
}

function LoginPage(){
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await axios.post('http://localhost:8080/api/v1/auth/login',{
                email: email,
                password: password
            });

            const token = response.data.token;
            const role = parseJwtRole(token);

            if (!AUTHORIZED_ROLES.includes(role)) {
                setError('Acesso não autorizado. Esta plataforma é restrita a gestores e administradores.');
                return;
            }

            login(token, response.data.passwordResetRequired);

        } catch (err) {
            console.error("Erro no login: ", err);
            if (err.response && err.response.status === 401) {
                setError('E-mail ou senha inválidos. Por favor, tente novamente.');
            } else {
                setError('Não foi possível conectar ao servidor. Tente novamente.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-page-container">
            <div className="login-card card-surface">
                <div className="login-logo-container">
                    <svg className="login-logo-svg" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="32" cy="32" r="28" stroke="#0A52C6" strokeWidth="3.5" fill="none" />
                        <circle cx="32" cy="32" r="22" stroke="#0A52C6" strokeWidth="1.5" strokeDasharray="3 3" fill="none" />
                        <path d="M22 32L28 38L42 24" stroke="#0A52C6" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" fill="none" />
                    </svg>
                    <h2 className="login-title">Laboris</h2>
                    <p className="login-subtitle">Acesso Mactron Solutions</p>
                </div>

                {error && <div className="alert alert-error">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label className="form-label">E-mail corporativo</label>
                        <div className="input-wrapper">
                            <input 
                                type="email"
                                className="form-input"
                                placeholder="nome@mactron.com"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                disabled={loading}
                            />
                        </div>
                    </div>
                    
                    <div className="form-group">
                        <label className="form-label">Senha</label>
                        <div className="input-wrapper">
                            <input
                                type={showPassword ? "text" : "password"}
                                className="form-input"
                                placeholder="••••••••"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                disabled={loading}
                            />
                            <button 
                                type="button" 
                                className="password-toggle-btn"
                                onClick={() => setShowPassword(!showPassword)}
                                title={showPassword ? "Ocultar senha" : "Exibir senha"}
                            >
                                {showPassword ? (
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                        <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"/>
                                        <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"/>
                                        <path d="M6.61 6.61A13.52 13.52 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"/>
                                        <line x1="2" y1="2" x2="22" y2="22"/>
                                    </svg>
                                ) : (
                                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                        <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/>
                                        <circle cx="12" cy="12" r="3"/>
                                    </svg>
                                )}
                            </button>
                        </div>
                    </div>

                    <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
                        {loading ? 'Entrando...' : 'Entrar'}
                    </button>
                </form>

                <a href="#forgot" className="forgot-password-link" onClick={(e) => { e.preventDefault(); alert("Entre em contato com o administrador do sistema para redefinir sua senha."); }}>
                    Esqueci minha senha
                </a>

                <div className="login-footer">
                    © 2024 Mactron Solutions. Todos os direitos reservados.
                </div>
            </div>
        </div>
    );
}

export default LoginPage;