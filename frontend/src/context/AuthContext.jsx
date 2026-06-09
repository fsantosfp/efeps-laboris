import React, { createContext, useState, useContext } from "react";
import { useNavigate } from "react-router-dom";

const AuthContext = createContext(null);

export const AuthProvider = ({children}) => {
    const navigate = useNavigate();
    const [token, setToken] = useState(localStorage.getItem('authToken'));
    const [passwordResetRequired, setPasswordResetRequired] = useState(
        localStorage.getItem('passwordResetRequired') === 'true'
    );

    const login = (newToken, resetRequired) => {
        localStorage.setItem('authToken', newToken);
        localStorage.setItem('passwordResetRequired', String(resetRequired));
        setToken(newToken);
        setPasswordResetRequired(resetRequired);
        if (resetRequired) {
            navigate('/change-password');
        } else {
            navigate('/dashboard');
        }
    }

    const logout = () => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('passwordResetRequired');
        setToken(null);
        setPasswordResetRequired(false);
        navigate('/login');
    }

    return (
        <AuthContext.Provider value={{token, passwordResetRequired, login, logout}}>
            { children }
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext);
