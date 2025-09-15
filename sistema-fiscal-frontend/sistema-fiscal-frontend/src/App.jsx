import React, { useState, useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import BuscaCliente from './pages/BuscaCliente';
import Dashboard from './pages/Dashboard';
import NovoCliente from './pages/NovoCliente';
import Calculo from './pages/Calculo';
import Resultado from './pages/Resultado';
import DashboardGeral from './pages/DashboardGeral';
import TodosClientes from './pages/TodosClientes';
import Login from './pages/Login';
import Tasks from './pages/Tasks';
import ProtectedRoute from './components/ProtectedRoute';
import './styles/global/BigCalendarDark.css';
import PortalCliente from "./pages/PortalCliente/PortalCliente";

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
        {/* Login */}
        <Route path="/login" element={<Login />} />

        {/* Dashboard Geral */}
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
              <DashboardGeral />
            </ProtectedRoute>
          }
        />

        {/* Dashboard do Cliente */}
        <Route
          path="/clientes/:clienteId/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        {/* Listagem de todos os clientes */}
        <Route
          path="/clientes"
          element={
            <ProtectedRoute>
              <TodosClientes />
            </ProtectedRoute>
          }
        />
        {/* Busca de clientes */}
        <Route
          path="/clientes/busca"
          element={
            <ProtectedRoute>
              <BuscaCliente />
            </ProtectedRoute>
          }
        />
        {/* Cadastro de novo cliente */}
        <Route
          path="/clientes/novo"
          element={
            <ProtectedRoute>
              <NovoCliente />
            </ProtectedRoute>
          }
        />

        {/* Calculo para cliente específico */}
        <Route
          path="/clientes/:clienteId/calculo"
          element={
            <ProtectedRoute>
              <Calculo />
            </ProtectedRoute>
          }
        />

        {/* Resultado do cálculo para cliente específico */}
        <Route
          path="/clientes/:clienteId/resultado/:calculoId"
          element={
            <ProtectedRoute>
              <Resultado />
            </ProtectedRoute>
          }
        />

        {/* Tasks */}
        <Route
          path="/tasks"
          element={
            <ProtectedRoute>
              <Tasks />
            </ProtectedRoute>
          }
        />

        {/* Portal do Cliente */}
        <Route path="/portal-cliente" element={<PortalCliente />} />

        {/* 404 */}
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