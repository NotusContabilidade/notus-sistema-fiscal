import React, { useState } from 'react';
import axios from 'axios';

// Todo o CSS necessário está aqui dentro, com os novos estilos para o cabeçalho e melhorias de layout.
const STYLES = `
:root {
  --cor-primaria: #a13751;
  --cor-fundo: #f8f8f8;
  --cor-texto: #333;
  --cor-borda: #e0e0e0;
  --cor-erro: #d9534f;
  --sombra-card: 0 4px 12px rgba(0, 0, 0, 0.08);
  --sombra-card-hover: 0 8px 25px rgba(0, 0, 0, 0.12);
}
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  background-color: var(--cor-fundo);
  color: var(--cor-texto);
}
/* --- MELHORIAS GERAIS --- */
.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.app-header {
  background-color: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo-link {
  display: flex;
  align-items: center;
  gap: 15px;
  text-decoration: none;
  cursor: pointer;
}
.logo {
  height: 50px;
  width: auto;
  border-radius: 8px;
  transition: transform 0.3s, box-shadow 0.3s;
}
.logo:hover {
  transform: scale(1.05);
  box-shadow: var(--sombra-card);
}
.header-title {
  color: var(--cor-primaria);
  font-size: 1.5rem;
  font-weight: bold;
}
.container {
  max-width: 900px;
  margin: 2rem auto;
  padding: 0 2rem;
  width: 100%;
}
.card {
  background-color: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: var(--sombra-card);
  margin-top: 2rem;
  border: 1px solid var(--cor-borda);
  text-align: center;
  transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
}
.card:hover {
  transform: translateY(-5px) scale(1.01);
  box-shadow: var(--sombra-card-hover);
}
.card h2, .card h3 {
  margin-top: 0;
  color: var(--cor-primaria);
  border-bottom: 1px solid var(--cor-borda);
  padding-bottom: 0.5rem;
  margin-bottom: 1.5rem;
}
.input-group {
  display: inline-flex;
  justify-content: center;
  gap: 1rem;
  width: auto;
}
.input-group input, .form-group input {
  padding: 0.75rem;
  border: 1px solid var(--cor-borda);
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s, box-shadow 0.3s;
}
.input-group input:focus, .form-group input:focus {
  outline: none;
  border-color: var(--cor-primaria);
  box-shadow: 0 0 0 3px rgba(161, 55, 81, 0.2);
}
.input-group input {
  flex-grow: 0;
  width: 250px;
  text-align: left;
}
.btn-primario, .iniciar-calculo-btn, .input-group button, .btn-secundario {
  padding: 0.75rem 1.5rem;
  background-color: var(--cor-primaria);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.2s;
}
.btn-primario:hover, .iniciar-calculo-btn:hover, .input-group button:hover, .btn-secundario:hover {
  background-color: #812c41;
  transform: translateY(-2px);
}
.btn-primario:active, .iniciar-calculo-btn:active, .input-group button:active, .btn-secundario:active {
  transform: translateY(1px);
}
.btn-primario:disabled, .iniciar-calculo-btn:disabled, .input-group button:disabled {
  background-color: #a0a0a0;
  cursor: not-allowed;
  transform: none;
}
.btn-secundario {
  background-color: #6c757d;
}
.btn-secundario:hover {
  background-color: #5a6268;
}
.erro-mensagem {
  color: var(--cor-erro);
  font-weight: bold;
  text-align: center;
  margin-top: 1.5rem;
}
.cliente-card p {
  line-height: 1.6;
  text-align: center;
}
.form-row {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-bottom: 1.5rem;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
  text-align: left;
}
.form-group label {
  font-weight: bold;
  font-size: 0.9rem;
  text-align: left;
}
.form-group input {
  text-align: left;
}
.anexo-bloco {
  border: 1px solid var(--cor-borda);
  border-radius: 5px;
  padding: 1.5rem;
  margin-top: 1.5rem;
  text-align: left;
}
.anexo-bloco h4 {
  margin-top: 0;
  color: var(--cor-primaria);
}
.botoes-acao {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  border-top: 1px solid var(--cor-borda);
  padding-top: 1.5rem;
}
.total-das {
  grid-column: 1 / -1;
  font-size: 1.3rem;
  font-weight: bold;
  color: var(--cor-primaria);
  text-align: center;
  margin-top: 1rem !important;
  background-color: #fceeee !important;
  border: 1px solid var(--cor-primaria);
  padding: 1rem;
  border-radius: 4px;
}
.selecao-anexos {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--cor-borda);
}
.btn-anexo {
  padding: 10px 15px;
  font-size: 0.9rem;
  border: 1px solid var(--cor-borda);
  background-color: #fff;
  color: #555;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-anexo.selecionado {
  background-color: var(--cor-primaria);
  color: white;
  border-color: var(--cor-primaria);
  font-weight: bold;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.view-container {
  animation: fadeIn 0.5s ease-in-out;
}
/* --- MELHORIAS: CENTRALIZAÇÃO DA TELA DE BUSCA --- */
.view-container > main {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  min-height: 70vh;
}
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  text-align: left;
}
.dashboard-info-card {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 5px;
}
.dashboard-info-card h4 {
  margin-top: 0;
  color: var(--cor-primaria);
}
.historico-tabela {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
  text-align: left;
}
.historico-tabela th, .historico-tabela td {
  padding: 0.75rem;
  border-bottom: 1px solid var(--cor-borda);
}
.historico-tabela th {
  background-color: #f1f1f1;
}
.detalhe-anexo-card {
  margin-top: 1.5rem;
  border: 1px solid var(--cor-borda);
  border-radius: 5px;
  padding: 1rem;
  text-align: left;
}
.detalhe-anexo-card h4 {
  margin-top: 0;
  color: var(--cor-primaria);
  font-size: 1.2rem;
}
.relatorio-tabela {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
}
.relatorio-tabela th, .relatorio-tabela td {
  padding: 0.5rem;
  border-bottom: 1px solid #eee;
}
.relatorio-tabela th {
  font-size: 0.9rem;
  color: #666;
}
.relatorio-tabela th:first-child, .relatorio-tabela td:first-child {
  text-align: left;
}
.relatorio-tabela th:not(:first-child), .relatorio-tabela td:not(:first-child) {
  text-align: right;
}
.relatorio-tabela .subtotal-row {
  font-weight: bold;
  border-top: 2px solid var(--cor-texto);
}
.info-extra-relatorio {
  text-align: right;
  font-size: 0.9rem;
  color: #555;
  margin-top: 0.5rem;
}
.secao-calculo {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--cor-borda);
}
.dropzone {
  border: 2px dashed var(--cor-borda);
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: background-color 0.3s, border-color 0.3s;
}
.dropzone.drag-over {
  background-color: #e7f3fe;
  border-color: var(--cor-primaria);
}
.dropzone p {
  margin: 0;
  font-size: 1rem;
  color: #666;
}
.dropzone-icon {
  width: 50px;
  height: 50px;
  margin-bottom: 1rem;
  opacity: 0.5;
}
.separador-ou {
  margin: 1.5rem auto;
  font-weight: bold;
  color: #999;
}
.info-relatorio-geral {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  margin-bottom: 1rem;
  border-bottom: 1px solid var(--cor-borda);
  font-size: 0.9rem;
  color: #555;
}
`;

function App() {
  const [view, setView] = useState('busca'); 
  const [cnpj, setCnpj] = useState('');
  const [cliente, setCliente] = useState(null);
  const [erro, setErro] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const [novoCliente, setNovoCliente] = useState({ razaoSocial: '', rbt12: '', folha12m: '' });

  const [mesRef, setMesRef] = useState(new Date().getMonth() + 1);
  const [anoRef, setAnoRef] = useState(new Date().getFullYear());
  const [resultadoCalculo, setResultadoCalculo] = useState(null);
  const [historico, setHistorico] = useState([]);
  const [historicoVisivel, setHistoricoVisivel] = useState(false);

  const [receitas, setReceitas] = useState({
    anexoI: { rpaNormal: 0, rpaSt: 0 },
    anexoII: { rpaNormal: 0, rpaSt: 0 },
    anexoIII: { rpaNormal: 0, rpaRetencao: 0 },
    anexoIV: { rpaNormal: 0, rpaRetencao: 0 },
    anexoV: { rpaNormal: 0, rpaRetencao: 0 },
  });

  const [anexosSelecionados, setAnexosSelecionados] = useState([]);
  const [isDragging, setIsDragging] = useState(false);
  const [pdfFile, setPdfFile] = useState(null);

  const handleToggleAnexo = (anexo) => {
    setAnexosSelecionados(prev => 
      prev.includes(anexo) 
        ? prev.filter(a => a !== anexo)
        : [...prev, anexo]
    );
  };

  const handleReceitaChange = (anexo, tipo, valor) => {
    setReceitas(prevState => ({
      ...prevState,
      [anexo]: {
        ...prevState[anexo],
        [tipo]: parseFloat(valor) || 0
      }
    }));
  };

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
      setView('dashboard');
    } catch (error) {
      if (error.response && error.response.status === 404) {
        setErro('Cliente não encontrado.');
      } else {
        setErro('Erro ao conectar com o servidor.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleSalvarNovoCliente = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setErro('');
    try {
      const payload = {
        cnpj: cnpj,
        razaoSocial: novoCliente.razaoSocial,
        rbt12: parseFloat(novoCliente.rbt12),
        folha12m: parseFloat(novoCliente.folha12m)
      };
      await axios.post('http://localhost:8080/api/clientes', payload);
      handleBuscarCliente();
    } catch (error) {
       setErro('Não foi possível salvar o cliente. Verifique os dados ou o servidor.');
    } finally {
       setIsLoading(false);
    }
  };

  const handleBuscarHistorico = async () => {
    if (historicoVisivel) {
      setHistoricoVisivel(false);
      return;
    }
    setIsLoading(true);
    setErro('');
    try {
      const response = await axios.get(`http://localhost:8080/api/calculos/historico/${cliente.cliente.id}`);
      setHistorico(response.data);
      setHistoricoVisivel(true);
    } catch (error) {
      setErro('Não foi possível carregar o histórico de cálculos.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleExecutarCalculo = async () => {
    setIsLoading(true);
    setErro('');
    setResultadoCalculo(null);
    const requestData = {
      clienteId: cliente.cliente.id,
      mesRef: parseInt(mesRef, 10),
      anoRef: parseInt(anoRef, 10),
      receitas: receitas
    };
    try {
      const response = await axios.post('http://localhost:8080/api/calculos', requestData);
      if (response.data && Array.isArray(response.data.detalhes)) {
        setResultadoCalculo(response.data);
        setView('resultado');
      } else {
        setErro("A resposta do servidor não continha os detalhes do cálculo.");
        setView('calculo');
      }
    } catch (error) {
      setErro('Ocorreu um erro ao executar o cálculo.');
      setView('calculo');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDragOver = (e) => { e.preventDefault(); setIsDragging(true); };
  const handleDragLeave = (e) => { e.preventDefault(); setIsDragging(false); };
  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files[0];
    if (file && file.type === "application/pdf") {
      setPdfFile(file);
      console.log("PDF recebido:", file.name);
    } else {
      setErro("Por favor, solte apenas um ficheiro PDF.");
    }
  };

  const irParaCalculo = () => { setView('calculo'); setErro(''); };
  const irParaCadastro = () => { setView('cadastro'); setErro(''); };
  const voltarParaBusca = () => { 
    setView('busca'); 
    setCliente(null); 
    setCnpj(''); 
    setErro(''); 
    setAnexosSelecionados([]);
    setHistorico([]);
    setHistoricoVisivel(false);
  };

  const renderContent = () => {
    switch (view) {
      case 'busca':
        return (
          <div key="busca" className="view-container">
            <main>
              <div className="card">
                <h2>Consulta de Clientes</h2>
                <div className="input-group">
                  <input type="text" value={cnpj} onChange={(e) => setCnpj(e.target.value)} placeholder="Digite o CNPJ para buscar" disabled={isLoading} />
                  <button onClick={handleBuscarCliente} disabled={isLoading}>{isLoading ? 'Buscando...' : 'Buscar'}</button>
                </div>
              </div>
              {erro && (
                <div className="card erro-mensagem">
                  <p style={{ margin: 0, marginBottom: '1rem' }}>{erro}</p>
                  {erro.includes('não encontrado') && 
                    <button onClick={irParaCadastro} className="btn-primario">Cadastrar Novo Cliente</button>
                  }
                </div>
              )}
            </main>
          </div>
        );
      case 'cadastro':
        return (
          <div key="cadastro" className="view-container">
            <main>
              <form className="card" onSubmit={handleSalvarNovoCliente}>
                <h2>Cadastro de Novo Cliente</h2>
                <div className="form-group">
                  <label>CNPJ</label>
                  <input type="text" value={cnpj} disabled />
                </div>
                <div className="form-group">
                  <label htmlFor="razaoSocial">Razão Social</label>
                  <input type="text" id="razaoSocial" value={novoCliente.razaoSocial} onChange={e => setNovoCliente({...novoCliente, razaoSocial: e.target.value})} required />
                </div>
                <div className="form-group">
                  <label htmlFor="rbt12">RBT12 (últimos 12 meses)</label>
                  <input type="number" id="rbt12" value={novoCliente.rbt12} onChange={e => setNovoCliente({...novoCliente, rbt12: e.target.value})} step="0.01" required />
                </div>
                <div className="form-group">
                  <label htmlFor="folha12m">Folha de Pagamento (12m)</label>
                  <input type="number" id="folha12m" value={novoCliente.folha12m} onChange={e => setNovoCliente({...novoCliente, folha12m: e.target.value})} step="0.01" required />
                </div>
                <div className="botoes-acao">
                  <button type="button" className="btn-secundario" onClick={voltarParaBusca} disabled={isLoading}>Cancelar</button>
                  <button type="submit" className="btn-primario" disabled={isLoading}>{isLoading ? 'Salvando...' : 'Salvar Cliente'}</button>
                </div>
              </form>
            </main>
          </div>
        );
      case 'dashboard':
        return (
          <div key="dashboard" className="view-container">
            <main>
              <div className="card">
                <h2>Dashboard do Cliente</h2>
                <div className="dashboard-grid">
                  <div className="dashboard-info-card">
                    <h4>Dados da Empresa</h4>
                    <p><strong>Razão Social:</strong> {cliente.cliente.razaoSocial}</p>
                    <p><strong>CNPJ:</strong> {cliente.cliente.cnpj}</p>
                  </div>
                  <div className="dashboard-info-card">
                    <h4>Parâmetros Fiscais</h4>
                    <p><strong>RBT12:</strong> R$ {cliente.parametros.rbt12Atual.toFixed(2)}</p>
                    <p><strong>Folha (12m):</strong> R$ {cliente.parametros.folhaPagamento12mAtual.toFixed(2)}</p>
                  </div>
                </div>
                <div className="botoes-acao">
                  <button className="btn-secundario" onClick={voltarParaBusca}>Nova Consulta</button>
                  <button className="btn-primario" onClick={handleBuscarHistorico} disabled={isLoading}>{isLoading ? 'Carregando...' : (historicoVisivel ? 'Ocultar Histórico' : 'Ver Histórico')}</button>
                  <button className="btn-primario" onClick={irParaCalculo}>Novo Cálculo</button>
                </div>
              </div>
              {historicoVisivel && (
                <div className="card view-container">
                  <h3>Histórico de Cálculos</h3>
                  {historico.length > 0 ? (
                    <table className="historico-tabela">
                      <thead>
                        <tr><th>Período</th><th>DAS Total</th><th>Ações</th></tr>
                      </thead>
                      <tbody>
                        {historico.map(calc => (
                          <tr key={calc.id}>
                            <td>{String(calc.mesReferencia).padStart(2, '0')}/{calc.anoReferencia}</td>
                            <td>R$ {calc.dasTotal.toFixed(2)}</td>
                            <td><button className="btn-primario" style={{padding: '0.5rem 1rem', fontSize: '0.9rem'}} onClick={() => { setResultadoCalculo(calc); setView('resultado'); }}>Ver Detalhes</button></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <p>Nenhum cálculo encontrado no histórico para este cliente.</p>
                  )}
                </div>
              )}
            </main>
          </div>
        );
      case 'calculo':
        return (
          <div key="calculo" className="view-container">
            <main>
              <div className="card">
                <h2>Lançamento de Receitas para <span style={{color: '#555'}}>{cliente.cliente.razaoSocial}</span></h2>
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
                
                <div className="secao-calculo">
                  <h3>Preenchimento Automático</h3>
                  <div 
                    className={`dropzone ${isDragging ? 'drag-over' : ''}`}
                    onDragOver={handleDragOver}
                    onDragLeave={handleDragLeave}
                    onDrop={handleDrop}
                  >
                    <svg className="dropzone-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"/></svg>
                    {pdfFile ? (
                      <p>Ficheiro carregado: <strong>{pdfFile.name}</strong></p>
                    ) : (
                      <p>Arraste e solte o extrato PGDAS-D em PDF aqui</p>
                    )}
                  </div>
                </div>

                <div className="separador-ou">OU</div>

                <div className="secao-calculo">
                  <h3>Preenchimento Manual</h3>
                  <div className="selecao-anexos">
                    <button className={`btn-anexo ${anexosSelecionados.includes('anexoI') && 'selecionado'}`} onClick={() => handleToggleAnexo('anexoI')}>Anexo I</button>
                    <button className={`btn-anexo ${anexosSelecionados.includes('anexoII') && 'selecionado'}`} onClick={() => handleToggleAnexo('anexoII')}>Anexo II</button>
                    <button className={`btn-anexo ${anexosSelecionados.includes('anexoIII') && 'selecionado'}`} onClick={() => handleToggleAnexo('anexoIII')}>Anexo III</button>
                    <button className={`btn-anexo ${anexosSelecionados.includes('anexoIV') && 'selecionado'}`} onClick={() => handleToggleAnexo('anexoIV')}>Anexo IV</button>
                    <button className={`btn-anexo ${anexosSelecionados.includes('anexoV') && 'selecionado'}`} onClick={() => handleToggleAnexo('anexoV')}>Anexo V</button>
                  </div>
                  {anexosSelecionados.map(anexoKey => (
                    <div key={anexoKey} className="anexo-bloco">
                      <h4>Anexo {anexoKey.replace('anexo', '')}</h4>
                      <div className="form-group"><label>Receita Normal (R$):</label><input type="number" value={receitas[anexoKey].rpaNormal} onChange={e => handleReceitaChange(anexoKey, 'rpaNormal', e.target.value)} step="0.01"/></div>
                      {(anexoKey === 'anexoI' || anexoKey === 'anexoII') && <div className="form-group"><label>Receita c/ ICMS-ST (R$):</label><input type="number" value={receitas[anexoKey].rpaSt} onChange={e => handleReceitaChange(anexoKey, 'rpaSt', e.target.value)} step="0.01"/></div>}
                      {(anexoKey === 'anexoIII' || anexoKey === 'anexoIV' || anexoKey === 'anexoV') && <div className="form-group"><label>Receita c/ Retenção de ISS (R$):</label><input type="number" value={receitas[anexoKey].rpaRetencao} onChange={e => handleReceitaChange(anexoKey, 'rpaRetencao', e.target.value)} step="0.01"/></div>}
                    </div>
                  ))}
                </div>

                <div className="botoes-acao">
                  <button className="btn-secundario" onClick={() => setView('dashboard')}>Cancelar</button>
                  <button className="btn-primario" onClick={handleExecutarCalculo} disabled={isLoading}>{isLoading ? 'Calculando...' : 'Executar Cálculo'}</button>
                </div>
              </div>
              {erro && <p className="erro-mensagem">{erro}</p>}
            </main>
          </div>
        );
      case 'resultado':
        if (isLoading || !resultadoCalculo || !Array.isArray(resultadoCalculo.detalhes)) {
          return (
            <div key="loading" className="view-container">
              <main className="card"><h2>A carregar resultado...</h2></main>
            </div>
          );
        }
        return (
          <div key="resultado" className="view-container">
            <main className="card resultado-card">
              <h2>Relatório de Apuração para <span style={{color: '#555'}}>{cliente.cliente.razaoSocial}</span></h2>
              <div className="info-relatorio-geral">
                <span><strong>Período de Apuração:</strong> {String(resultadoCalculo.mesReferencia).padStart(2, '0')}/{resultadoCalculo.anoReferencia}</span>
                <span><strong>Data do Cálculo:</strong> {resultadoCalculo.dataCalculo}</span>
              </div>
              <div className="total-das">
                 <strong>Valor Total do DAS: R$ {resultadoCalculo.dasTotal.toFixed(2)}</strong>
              </div>
              <h3>Detalhamento por Atividade</h3>
              {resultadoCalculo.detalhes.length > 0 ? (
                resultadoCalculo.detalhes.map((detalhe, index) => (
                  <div key={index} className="detalhe-anexo-card">
                    <h4>{detalhe.anexoAplicado.replace('Anexo', 'Anexo ')}</h4>
                    <table className="relatorio-tabela">
                      <tbody>
                        <tr><th>Descrição</th><th>Receita (R$)</th><th>Valor do DAS (R$)</th></tr>
                        <tr><td>Receita Normal</td><td>{detalhe.rpaNormal.toFixed(2)}</td><td>{detalhe.dasNormal.toFixed(2)}</td></tr>
                        {detalhe.rpaComRetencao > 0 && <tr><td>Receita c/ Retenção ISS</td><td>{detalhe.rpaComRetencao.toFixed(2)}</td><td>{detalhe.dasComRetencaoLiquido.toFixed(2)}</td></tr>}
                        {detalhe.rpaComRetencao > 0 && <tr><td><em>(ISS Retido na Fonte)</em></td><td>-</td><td><em>({detalhe.issRetido.toFixed(2)})</em></td></tr>}
                        {detalhe.rpaStICMS > 0 && <tr><td>Receita c/ ICMS-ST</td><td>{detalhe.rpaStICMS.toFixed(2)}</td><td>{detalhe.dasStICMS.toFixed(2)}</td></tr>}
                        <tr className="subtotal-row"><td>Subtotal (Anexo)</td><td>{detalhe.rpaTotal.toFixed(2)}</td><td>{detalhe.dasTotal.toFixed(2)}</td></tr>
                      </tbody>
                    </table>
                    <div className="info-extra-relatorio">
                      <span>Alíquota Efetiva: {(detalhe.aliquotaEfetivaTotal * 100).toFixed(4)}%</span>
                      {detalhe.fatorR != null && <span> | Fator R: {detalhe.fatorR.toFixed(2)}%</span>}
                    </div>
                  </div>
                ))
              ) : (
                <p>Não foram encontrados detalhes para este cálculo antigo.</p>
              )}
              <div className="botoes-acao">
                <button className="btn-secundario" onClick={() => setView('dashboard')}>Voltar ao Dashboard</button>
                <button className="btn-primario" onClick={voltarParaBusca}>Nova Consulta</button>
              </div>
            </main>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <>
      <style>{STYLES}</style>
      <div className="app-wrapper">
        <header className="app-header">
          <div className="logo-link" onClick={voltarParaBusca}>
            <img src="/logo-notus.jpg" alt="Logotipo Nótus Contábil" className="logo" />
            <span className="header-title">Nótus Sistema Fiscal</span>
          </div>
        </header>
        <div className="container">
          {renderContent()}
        </div>
      </div>
    </>
  );
}

export default App;