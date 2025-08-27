import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom'; // ✅ 1. Importar o Navigate
import Layout from './components/Layout.jsx';
import BuscaCliente from './pages/BuscaCliente.jsx';
import Dashboard from './pages/Dashboard.jsx';
import NovoCliente from './pages/NovoCliente.jsx';
import Calculo from './pages/Calculo.jsx';
import Resultado from './pages/Resultado.jsx';
import DashboardGeral from './pages/DashboardGeral.jsx';
import TodosClientes from './pages/TodosClientes.jsx';
import Vencimentos from './pages/Vencimentos.jsx'; // Garantindo que todos os imports estão aqui

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<DashboardGeral />} />
        
        {/* ✅ 2. ROTA DE REDIRECIONAMENTO ADICIONADA */}
        {/* Se alguém acessar /clientes, será automaticamente redirecionado para /clientes/todos */}
        <Route path="clientes" element={<Navigate to="/clientes/todos" replace />} />

        <Route path="clientes/busca" element={<BuscaCliente />} />
        <Route path="clientes/todos" element={<TodosClientes />} />
        <Route path="clientes/novo" element={<NovoCliente />} />
        <Route path="clientes/:clienteId/dashboard" element={<Dashboard />} />
        <Route path="clientes/:clienteId/calculo" element={<Calculo />} />
        <Route path="clientes/:clienteId/resultado/:calculoId" element={<Resultado />} />
        <Route path="vencimentos" element={<Vencimentos />} />
        
        <Route path="*" element={
            <div className="view-container">
                <div className="card">
                    <h3>Erro 404 - Página Não Encontrada</h3>
                </div>
            </div>
        } /> 
      </Route>
    </Routes>
  );
}

export default App;