import { useEffect, useState } from 'react';
import { Route, Routes, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import BuscaCliente from './pages/BuscaCliente';
import Calculo from './pages/Calculo';
import Dashboard from './pages/Dashboard';
import DashboardGeral from './pages/DashboardGeral';
import Login from './pages/Login';
import NovoCliente from './pages/NovoCliente';
import PainelControle from './pages/PainelControle';
import PortalCliente from './pages/PortalCliente';
import Resultado from './pages/Resultado';
import TarefasRecorrentes from "./pages/TarefasRecorrentes";
import Tasks from './pages/Tasks';
import TodosClientes from './pages/TodosClientes';
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
        {/* Login */}
        <Route path="/login" element={<Login />} />

        {/* Rota de correção para o link do menu "Portal do Cliente" */}
        <Route path="/portal-cliente" element={<Navigate to="/clientes/busca" replace />} />

        {/* Dashboard Geral */}
        <Route path="/" element={<ProtectedRoute><DashboardGeral /></ProtectedRoute>} />
        <Route path="/dashboard" element={<ProtectedRoute><DashboardGeral /></ProtectedRoute>} />

        {/* Dashboard do Cliente */}
        <Route path="/clientes/:clienteId/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />

        {/* Listagem de todos os clientes */}
        <Route path="/clientes" element={<ProtectedRoute><TodosClientes /></ProtectedRoute>} />
        {/* Busca de clientes */}
        <Route path="/clientes/busca" element={<ProtectedRoute><BuscaCliente /></ProtectedRoute>} />
        {/* Cadastro de novo cliente */}
        <Route path="/clientes/novo" element={<ProtectedRoute><NovoCliente /></ProtectedRoute>} />

        {/* Calculo para cliente específico */}
        <Route path="/clientes/:clienteId/calculo" element={<ProtectedRoute><Calculo /></ProtectedRoute>} />

        {/* Resultado do cálculo para cliente específico */}
        <Route path="/clientes/:clienteId/resultado/:calculoId" element={<ProtectedRoute><Resultado /></ProtectedRoute>} />

        {/* Tasks (página antiga, pode ser removida/reaproveitada depois) */}
        <Route path="/tasks" element={<ProtectedRoute><Tasks /></ProtectedRoute>} />

        {/* Ferramentas Principais */}
        <Route path="/painel-controle" element={<ProtectedRoute><PainelControle /></ProtectedRoute>} />
        <Route path="/recorrencias" element={<ProtectedRoute><TarefasRecorrentes /></ProtectedRoute>} />

        {/* Rota correta do Portal do Cliente (acessada pelo dashboard) */}
        <Route path="/clientes/:clienteId/portal" element={<ProtectedRoute><PortalCliente /></ProtectedRoute>} />

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