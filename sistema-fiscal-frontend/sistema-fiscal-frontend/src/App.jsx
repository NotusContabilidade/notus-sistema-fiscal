import React, { useState } from 'react';
import axios from 'axios';
import './App.css'; // Importa os estilos específicos deste componente

function App() {
  const [view, setView] = useState('busca'); 
  const [cnpj, setCnpj] = useState('');
  const [cliente, setCliente] = useState(null);
  const [erro, setErro] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const [mesRef, setMesRef] = useState(new Date().getMonth());
  const [anoRef, setAnoRef] = useState(new Date().getFullYear());
  const [receitaAnexoI, setReceitaAnexoI] = useState(0);
  const [resultadoCalculo, setResultadoCalculo] = useState(null);

  const handleBuscarCliente = async () => {
    if (!cnpj) {
      setErro('Por favor, informe um CNPJ.');
      return;
    }
    setIsLoading(true);
    setErro('');
    setCliente(null);
    try {
      const response = await axios.get(`http://localhost:8080/api/clientes?cnpj=${cnpj}`);
      setCliente(response.data);
    } catch (error) {
      if (error.response && error.response.status === 404) {
        setErro('Cliente não encontrado. Verifique o CNPJ ou realize um novo cadastro.');
      } else {
        setErro('Erro ao conectar com o servidor. Verifique se o backend está em execução.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleExecutarCalculo = async () => {
    setIsLoading(true);
    setErro('');
    setResultadoCalculo(null);

    const receitas = {
      "anexoI_rpaNormal": receitaAnexoI,
    };

    const requestData = {
      clienteId: cliente.id,
      mesRef: parseInt(mesRef, 10),
      anoRef: parseInt(anoRef, 10),
      receitas: receitas
    };

    try {
      const response = await axios.post('http://localhost:8080/api/calculos', requestData);
      setResultadoCalculo(response.data);
      setView('resultado');
    } catch (error) {
      setErro('Ocorreu um erro ao executar o cálculo. Verifique os dados e tente novamente.');
      console.error("Erro no cálculo: ", error);
    } finally {
      setIsLoading(false);
    }
  };
  
  const irParaCalculo = () => {
    setView('calculo');
    setErro('');
  };

  const voltarParaBusca = () => {
    setView('busca');
    setCliente(null);
    setCnpj('');
    setErro('');
  };

  const renderHeader = () => (
    <header>
      <img src="/logo-notus.jpg" alt="Logotipo Nótus Contábil" className="logo" />
      <h1>Sistema Fiscal</h1>
    </header>
  );
  
  const renderContent = () => {
    if (view === 'busca') {
      return (
        <main>
          <div className="card">
            <h2>Consulta de Clientes</h2>
            <div className="input-group">
              <input type="text" value={cnpj} onChange={(e) => setCnpj(e.target.value)} placeholder="Digite o CNPJ para buscar" disabled={isLoading} />
              <button onClick={handleBuscarCliente} disabled={isLoading}>{isLoading ? 'Buscando...' : 'Buscar'}</button>
            </div>
          </div>
          {erro && <p className="erro-mensagem">{erro}</p>}
          {cliente && (
            <div className="card cliente-card">
              <h3>Dados do Cliente</h3>
              <p><strong>Razão Social:</strong> {cliente.razaoSocial}</p>
              <p><strong>CNPJ:</strong> {cliente.cnpj}</p>
              <button className="iniciar-calculo-btn" onClick={irParaCalculo}>Iniciar Cálculo</button>
            </div>
          )}
        </main>
      );
    }

    if (view === 'calculo') {
      return (
        <main>
          <div className="card">
            <h2>Lançamento de Receitas para <span style={{color: '#555'}}>{cliente.razaoSocial}</span></h2>
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="mesRef">Mês de Referência:</label>
                <input type="number" id="mesRef" value={mesRef} onChange={e => setMesRef(e.target.value)} min="1" max="12" />
              </div>
              <div className="form-group">
                <label htmlFor="anoRef">Ano de Referência:</label>
                <input type="number" id="anoRef" value={anoRef} onChange={e => setAnoRef(e.target.value)} min="2020" />
              </div>
            </div>
            <div className="anexo-bloco">
              <h4>Anexo I (Comércio)</h4>
              <div className="form-group" style={{alignItems: 'center'}}>
                <label htmlFor="anexoI">Receita Normal (R$):</label>
                <input type="number" id="anexoI" value={receitaAnexoI} onChange={e => setReceitaAnexoI(e.target.value)} step="0.01" style={{width: '150px'}}/>
              </div>
            </div>
            <div className="botoes-acao">
              <button className="btn-secundario" onClick={voltarParaBusca} disabled={isLoading}>Cancelar</button>
              <button className="btn-primario" onClick={handleExecutarCalculo} disabled={isLoading}>{isLoading ? 'Calculando...' : 'Executar Cálculo'}</button>
            </div>
          </div>
          {erro && <p className="erro-mensagem">{erro}</p>}
        </main>
      );
    }

    if (view === 'resultado') {
      return (
        <main className="card resultado-card">
          <h2>Relatório de Apuração para <span style={{color: '#555'}}>{cliente.razaoSocial}</span></h2>
          <div className="resultado-grid">
            <p><strong>Período:</strong> {String(resultadoCalculo.mesReferencia).padStart(2, '0')}/{resultadoCalculo.anoReferencia}</p>
            <p><strong>Anexo Aplicado:</strong> {resultadoCalculo.anexoAplicado}</p>
            <p><strong>Receita do Mês:</strong> R$ {resultadoCalculo.rpaTotal.toFixed(2)}</p>
            <p><strong>Alíquota Efetiva:</strong> {(resultadoCalculo.aliquotaEfetiva * 100).toFixed(4)}%</p>
          </div>
          <div className="total-das">
             <strong>Valor Total do DAS: R$ {resultadoCalculo.dasTotal.toFixed(2)}</strong>
          </div>
          <div className="botoes-acao">
            <button className="btn-primario" onClick={voltarParaBusca}>Novo Cálculo</button>
          </div>
        </main>
      );
    }
    return null;
  };

  return (
    <div className="container">
      {renderHeader()}
      {renderContent()}
    </div>
  );
}

export default App;