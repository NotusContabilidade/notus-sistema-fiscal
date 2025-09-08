import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import BuscaCliente from './pages/BuscaCliente';
import Dashboard from './pages/Dashboard';
import NovoCliente from './pages/NovoCliente';
import Calculo from './pages/Calculo';
import Resultado from './pages/Resultado';
import DashboardGeral from './pages/DashboardGeral';
import TodosClientes from './pages/TodosClientes';
import Vencimentos from './pages/Vencimentos';
import Login from './pages/Login';
import Tasks from './pages/Tasks';
import ProtectedRoute from './components/ProtectedRoute';
import './styles/global/BigCalendarDark.css';

function App() {
  const [dark, setDark] = useState(() => {
    return localStorage.getItem('darkMode') === 'true';
  });

  useEffect(() => {
    document.body.classList.toggle('dark', dark);
    localStorage.setItem('darkMode', dark);
  }, [dark]);

  return (
    <Layout dark={dark} setDark={setDark}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardGeral />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard-geral"
          element={
            <ProtectedRoute>
              <DashboardGeral />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tasks"
          element={
            <ProtectedRoute>
              <Tasks />
            </ProtectedRoute>
          }
        />
        <Route
          path="/clientes"
          element={
            <ProtectedRoute>
              <TodosClientes />
            </ProtectedRoute>
          }
        />
        <Route
          path="/clientes/novo"
          element={
            <ProtectedRoute>
              <NovoCliente />
            </ProtectedRoute>
          }
        />
        <Route
          path="/clientes/busca"
          element={
            <ProtectedRoute>
              <BuscaCliente />
            </ProtectedRoute>
          }
        />
        <Route
          path="/calculo"
          element={
            <ProtectedRoute>
              <Calculo />
            </ProtectedRoute>
          }
        />
        <Route
          path="/resultado"
          element={
            <ProtectedRoute>
              <Resultado />
            </ProtectedRoute>
          }
        />
        <Route
          path="/vencimentos"
          element={
            <ProtectedRoute>
              <Vencimentos />
            </ProtectedRoute>
          }
        />
        <Route
          path="*"
          element={
            <div className="view-container">
              <div className="card">
                <h3>Erro 404 - Página Não Encontrada</h3>
              </div>
            </div>
          }
        />
      </Routes>
    </Layout>
  );
}

export default App;