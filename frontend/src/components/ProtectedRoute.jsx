import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const AUTHORIZED_ROLES = ['MANAGER', 'SAAS_OWNER'];

function parseJwtRole(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.role || null;
    } catch {
        return null;
    }
}

const ProtectedRoute = ({children, allowResetPending = false}) => {
    const { token, passwordResetRequired } = useAuth();

    if (!token) return <Navigate to="/login" />;

    const role = parseJwtRole(token);
    if (!AUTHORIZED_ROLES.includes(role)) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('passwordResetRequired');
        return <Navigate to="/login" />;
    }

    if (passwordResetRequired && !allowResetPending) return <Navigate to="/change-password" />;

    return children;
};

export default ProtectedRoute;