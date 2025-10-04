import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from './components/ProtectedRoute';
import Layout from "./components/Layout";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import JobDetailPage from "./pages/JobDetailPage";
import CreateJobPage from "./pages/CreateJobPage";
import PayrollReportPage from "./pages/PayrollReportPage";

function App(){
  return (
    <Routes>
      <Route path="/login" element={ <LoginPage/> } />
      <Route path="/" element={<ProtectedRoute><Layout/></ProtectedRoute>}>
        <Route index element={ <Navigate to="/dashboard" /> } />
        <Route path="/dashboard" element={ <DashboardPage/> } />
        <Route path="/jobs/new" element={ <CreateJobPage/> } />
        <Route path="/jobs/:jobId" element={ <JobDetailPage/> } />
        <Route path="reports" element={ <PayrollReportPage /> } />
      </Route>
    </Routes>
  )
}

export default App;