import React from "react";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import { Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from './components/ProtectedRoute';
import JobDetailPage from "./pages/JobDetailPage";
import CreateJobPage from "./pages/CreateJobPage";
import Layout from "./components/Layout";

function App(){
  return (
    <Routes>
      <Route path="/login" element={ <LoginPage/> } />
      <Route path="/" element={<ProtectedRoute><Layout/></ProtectedRoute>}>
        <Route index element={ <Navigate to="/dashboard" /> } />
        <Route path="/dashboard" element={<ProtectedRoute><DashboardPage/></ProtectedRoute>} />
        <Route path="/jobs/new" element={<ProtectedRoute><CreateJobPage/></ProtectedRoute>}></Route>
        <Route path="/jobs/:jobId" element={<ProtectedRoute><JobDetailPage/></ProtectedRoute>}></Route>
      </Route>
    </Routes>
  )
}

export default App;