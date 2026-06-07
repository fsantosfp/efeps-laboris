import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const ProtectedRoute = ({children, allowResetPending = false}) => {
    const { token, passwordResetRequired } = useAuth();

    if(!token) return <Navigate to="/login" />

    if(passwordResetRequired && !allowResetPending) return <Navigate to="/change-password" />

    return children
};

export default ProtectedRoute;