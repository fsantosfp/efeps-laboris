import React from "react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const Layout = () => {

    const { logout } = useAuth();

    const navLinkStyle = ({isActive}) => ({
        margin: '0 10px',
        color: isActive ? 'blue' : 'black',
        textDecoration: 'none',
        fontWeight: isActive ? 'bold' : 'normal'
    });

    return (
        <div style={{padding:'20px'}}>
            <header style={{display:'flex',justifyContent:'space-between',alignItems:'center',borderBottom: '1px solid #ccc',paddingBottom: '10px'}}>
                <h1>Laboris</h1>
                <nav>
                    <NavLink to="/dashboard" style={navLinkStyle}>Dashboard</NavLink>
                    <NavLink to="/employees" style={navLinkStyle}>Equipe</NavLink>
                    <NavLink to="/reports" style={navLinkStyle}>Relatórios</NavLink>
                </nav>
                <button onClick={logout}>Sair</button>
            </header>
            <main style={{margin:'20px'}}>
                <Outlet></Outlet>
            </main>
        </div>
    )
}

export default Layout;