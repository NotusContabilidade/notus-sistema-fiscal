import React from 'react';
import { Routes, Route } from 'react-router-dom';

// Importações Corrigidas (com .jsx no final)
import Layout from './components/Layout.jsx';
import BuscaCliente from './pages/BuscaCliente.jsx';
import Dashboard from './pages/Dashboard.jsx';
import NovoCliente from './pages/NovoCliente.jsx';
import Calculo from './pages/Calculo.jsx';
import Resultado from './pages/Resultado.jsx';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<BuscaCliente />} />
        <Route path="clientes/novo" element={<NovoCliente />} />
        <Route path="clientes/:clienteId/dashboard" element={<Dashboard />} />
        <Route path="clientes/:clienteId/calculo" element={<Calculo />} />
        <Route path="clientes/:clienteId/resultado" element={<Resultado />} />
        <Route path="*" element={
          <div className="view-container">
            <div className="card">
              <h3>Erro 404 - Página Não Encontrada</h3>
              <p>A página que você está procurando não existe.</p>
            </div>
          </div>
        } />
      </Route>
    </Routes>
  );
}

export default App;