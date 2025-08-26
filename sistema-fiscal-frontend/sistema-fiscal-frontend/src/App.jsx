import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import BuscaCliente from './pages/BuscaCliente.jsx';
import Dashboard from './pages/Dashboard.jsx';
import NovoCliente from './pages/NovoCliente.jsx';
import Calculo from './pages/Calculo.jsx';
import Resultado from './pages/Resultado.jsx';
import DashboardGeral from './pages/DashboardGeral.jsx';
import TodosClientes from './pages/TodosClientes.jsx';
import Vencimentos from './pages/Vencimentos.jsx'; 

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<DashboardGeral />} />
        <Route path="clientes/busca" element={<BuscaCliente />} />
                {/* ✅ 2. ROTA DA NOVA PÁGINA ADICIONADA */}
        <Route path="clientes/todos" element={<TodosClientes />} />
          <Route path="vencimentos" element={<Vencimentos />}/>
        <Route path="clientes/novo" element={<NovoCliente />} />
        <Route path="clientes/:clienteId/dashboard" element={<Dashboard />} />
        <Route path="clientes/:clienteId/calculo" element={<Calculo />} />
        <Route path="clientes/:clienteId/resultado/:calculoId" element={<Resultado />} />
        
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