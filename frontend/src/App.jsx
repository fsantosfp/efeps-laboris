import React from "react";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import { Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from './components/ProtectedRoute';

function App(){
  return (
    <Routes>
      <Route path="/" element={ <Navigate to="/dashboard" /> } />
      <Route path="/login" element={ <LoginPage/> } />
      <Route path="/dashboard" element={<ProtectedRoute><DashboardPage/></ProtectedRoute>} />
    </Routes>
  )
}

export default App;