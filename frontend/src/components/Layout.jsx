import React from "react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Layout.css";

const Layout = () => {
    const { logout } = useAuth();

    return (
        <div className="layout-container">
            <aside className="sidebar">
                <div className="sidebar-brand">
                    <svg className="sidebar-logo" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="32" cy="32" r="28" stroke="#0A52C6" strokeWidth="4" fill="none" />
                        <circle cx="32" cy="32" r="22" stroke="#0A52C6" strokeWidth="1.5" strokeDasharray="3 3" fill="none" />
                        <path d="M22 32L28 38L42 24" stroke="#0A52C6" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" fill="none" />
                    </svg>
                    <h1 className="sidebar-title">Laboris</h1>
                </div>

                <nav className="sidebar-menu">
                    <NavLink to="/dashboard" className="sidebar-link">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <rect width="7" height="9" x="3" y="3" rx="1"/>
                            <rect width="7" height="5" x="14" y="3" rx="1"/>
                            <rect width="7" height="9" x="14" y="12" rx="1"/>
                            <rect width="7" height="5" x="3" y="16" rx="1"/>
                        </svg>
                        Dashboard
                    </NavLink>
                    <NavLink to="/employees" className="sidebar-link">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                            <circle cx="9" cy="7" r="4"/>
                            <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                        </svg>
                        Equipe
                    </NavLink>
                    <NavLink to="/reports" className="sidebar-link">
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <line x1="18" y1="20" x2="18" y2="10"/>
                            <line x1="12" y1="20" x2="12" y2="4"/>
                            <line x1="6" y1="20" x2="6" y2="14"/>
                        </svg>
                        Relatórios
                    </NavLink>
                </nav>

                <div className="sidebar-footer">
                    <div className="system-status-container">
                        <div className="status-indicator"></div>
                        <span>Status: Operacional</span>
                    </div>
                    <button className="logout-btn" onClick={logout}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                            <polyline points="16 17 21 12 16 7"/>
                            <line x1="21" y1="12" x2="9" y2="12"/>
                        </svg>
                        Sair
                    </button>
                </div>
            </aside>

            <main className="main-content">
                <Outlet />
            </main>
        </div>
    );
};

export default Layout;