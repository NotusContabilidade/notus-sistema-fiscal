import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import BuscaCliente from './pages/BuscaCliente.jsx';
import Dashboard from './pages/Dashboard.jsx';
import NovoCliente from './pages/NovoCliente.jsx';
import Calculo from './pages/Calculo.jsx';
import Resultado from './pages/Resultado.jsx';
import DashboardGeral from './pages/DashboardGeral.jsx';
import TodosClientes from './pages/TodosClientes.jsx';
import Vencimentos from './pages/Vencimentos.jsx';
import TarefasModelo from './pages/TarefasModelo.jsx'; // ✅ 1. IMPORTE A NOVA PÁGINA

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<DashboardGeral />} />
        
        <Route path="clientes" element={<Navigate to="/clientes/todos" replace />} />

        <Route path="clientes/busca" element={<BuscaCliente />} />
        <Route path="clientes/todos" element={<TodosClientes />} />
        <Route path="clientes/novo" element={<NovoCliente />} />
        <Route path="clientes/:clienteId/dashboard" element={<Dashboard />} />
        <Route path="clientes/:clienteId/calculo" element={<Calculo />} />
        <Route path="clientes/:clienteId/resultado/:calculoId" element={<Resultado />} />
        <Route path="vencimentos" element={<Vencimentos />} />
        
        {/* ✅ 2. ADICIONE A NOVA ROTA */}
        <Route path="tarefas-modelo" element={<TarefasModelo />} />
        
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