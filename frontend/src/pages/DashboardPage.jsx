import React from "react";
import { useAuth } from '../context/AuthContext';

function DashboardPage(){
    const { logout } = useAuth();

    return (
        <div>
            <h2>Dashboard</h2>
            <p>Bem-vindo! Você está logado.</p>
            <button onClick={logout}>Sair (Logout)</button>
        </div>
    );
}

export default DashboardPage;