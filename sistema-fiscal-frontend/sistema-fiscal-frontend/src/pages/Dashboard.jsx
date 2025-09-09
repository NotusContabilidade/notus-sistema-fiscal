import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom'; 
import api from '../services/api'
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import { Edit, X, PlusCircle, Search } from 'lucide-react';
import '../styles/pages/Dashboard.css';

// Componente para exibir um "esqueleto" enquanto os dados carregam
const SkeletonCard = () => (
  <div className="dashboard-indicador-card skeleton-card">
    <div className="dashboard-indicador-icone" style={{ background: "#e0e0e0", borderRadius: "50%", width: 48, height: 48 }}></div>
    <div className="dashboard-indicador-titulo" style={{ background: "#e0e0e0", height: 18, width: 120, borderRadius: 4, margin: "12px 0" }}></div>
    <div className="dashboard-indicador-valor" style={{ background: "#e0e0e0", height: 22, width: 60, borderRadius: 4 }}></div>
  </div>
);

function Dashboard() {
  const { clienteId } = useParams();
  const navigate = useNavigate();

  const [cliente, setCliente] = useState(null);
  const [isLoading, setIsLoading] = useState(true); 
  const [isEditingParams, setIsEditingParams] = useState(false);
  const [editedParams, setEditedParams] = useState({ rbt12: 0, folha12m: 0 });
  const [historico, setHistorico] = useState([]);
  const [historicoVisivel, setHistoricoVisivel] = useState(false);
  const [isLoadingHistorico, setIsLoadingHistorico] = useState(false);

  const fetchCliente = useCallback(async () => {
    try {
      const response = await api.get(`http://localhost:8080/api/clientes/id/${clienteId}`);
      setCliente(response.data);
      if (response.data.parametros) {
        setEditedParams({
          rbt12: response.data.parametros.rbt12Atual,
          folha12m: response.data.parametros.folhaPagamento12mAtual
        });
      }
    } catch (error) {
      toast.error("Não foi possível carregar os dados do cliente.");
      navigate('/clientes/busca');
    } finally {
      setIsLoading(false);
    }
  }, [clienteId, navigate]);

  useEffect(() => {
    fetchCliente();
  }, [fetchCliente]);

  const handleUpdateParams = async () => {
    setIsLoading(true);
    try {
      const payload = {
        rbt12: parseFloat(editedParams.rbt12),
        folha12m: parseFloat(editedParams.folha12m)
      };
      await api.put(`http://localhost:8080/api/clientes/${cliente.cliente.id}/parametros`, payload);
      toast.success("Parâmetros atualizados com sucesso!");
      setIsEditingParams(false);
      await fetchCliente();
    } catch (error) {
      toast.error('Falha ao atualizar os parâmetros.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleBuscarHistorico = async () => {
    if (historicoVisivel) {
      setHistoricoVisivel(false);
      return;
    }
    setIsLoadingHistorico(true);
    try {
      const response = await api.get(`http://localhost:8080/api/calculos/historico/${cliente.cliente.id}`);
      setHistorico(response.data);
      setHistoricoVisivel(true);
    } catch (error) {
      toast.error('Não foi possível carregar o histórico de cálculos.');
    } finally {
      setIsLoadingHistorico(false);
    }
  };
  
  if (isLoading) {
    return (
      <div className="view-container">
        <div className="page-header"><h1 className="page-title">Dashboard do Cliente</h1></div>
        <div className="dashboard-indicadores">
          <SkeletonCard />
          <SkeletonCard />
          <SkeletonCard />
        </div>
      </div>
    );
  }

  if (!cliente) return null;

  // Indicadores do dashboard (ajuste conforme seus dados reais)
  const totalClientes = cliente.totalClientes ?? 0;
  const dasMes = cliente.dasMes ?? 0;
  const clientesPendentes = cliente.clientesPendentes ?? 0;

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Dashboard do Cliente</h1></div>
      
      {/* Indicadores do dashboard */}
      <div className="dashboard-indicadores">
        <div className="dashboard-indicador-card">
          <div className="dashboard-indicador-icone" style={{ color: "#2563eb" }}>👥</div>
          <div className="dashboard-indicador-titulo">Total de Clientes</div>
          <div className="dashboard-indicador-valor">{totalClientes}</div>
        </div>
        <div className="dashboard-indicador-card">
          <div className="dashboard-indicador-icone" style={{ color: "#22c55e" }}>💲</div>
          <div className="dashboard-indicador-titulo">DAS Calculado no Mês</div>
          <div className="dashboard-indicador-valor">R$ {dasMes.toFixed(2)}</div>
        </div>
        <div className="dashboard-indicador-card">
          <div className="dashboard-indicador-icone" style={{ color: "#ef4444" }}>❗</div>
          <div className="dashboard-indicador-titulo">Clientes com Cálculo Pendente</div>
          <div className="dashboard-indicador-valor">{clientesPendentes}</div>
        </div>
      </div>

      {/* Informações detalhadas do cliente */}
      <div className="card">
        <div className="dashboard-grid">
          <div className="dashboard-info-card">
            <h4>Dados da Empresa</h4>
            <p><strong>Razão Social:</strong> {cliente.cliente.razaoSocial}</p>
            <p><strong>CNPJ:</strong> {cliente.cliente.cnpj}</p>
          </div>
          <div className="dashboard-info-card">
            <h4>Parâmetros Fiscais</h4>
            {isEditingParams ? (
              <>
                <div className="form-group"><label>RBT12:</label><input type="number" step="0.01" value={editedParams.rbt12} onFocus={e => e.target.select()} onChange={e => setEditedParams({...editedParams, rbt12: e.target.value})} /></div>
                <div className="form-group"><label>Folha (12m):</label><input type="number" step="0.01" value={editedParams.folha12m} onFocus={e => e.target.select()} onChange={e => setEditedParams({...editedParams, folha12m: e.target.value})} /></div>
              </>
            ) : (
              cliente.parametros ? (
                <>
                  <p><strong>RBT12:</strong> R$ {cliente.parametros.rbt12Atual.toFixed(2)}</p>
                  <p><strong>Folha (12m):</strong> R$ {cliente.parametros.folhaPagamento12mAtual.toFixed(2)}</p>
                </>
              ) : (
                <p>Parâmetros fiscais não cadastrados.</p>
              )
            )}
            <button type="button" className="btn-edit" onClick={() => setIsEditingParams(!isEditingParams)}>
              {isEditingParams ? <X size={20}/> : <Edit size={18}/>}
            </button>
          </div>
        </div>
        <div className="botoes-acao">
          <Link to="/clientes/busca" className="btn-secundario"><Search size={16}/> Nova Consulta</Link>
          {isEditingParams ? 
            <button type="button" className="btn-primario" onClick={handleUpdateParams} disabled={isLoading}>{isLoading ? <Spinner /> : 'Salvar'}</button> :
            <>
              <button type="button" className="btn-primario" onClick={handleBuscarHistorico} disabled={isLoadingHistorico}>{isLoadingHistorico ? <Spinner/> : (historicoVisivel ? 'Ocultar Histórico' : 'Ver Histórico')}</button>
              <Link to={`/clientes/${clienteId}/calculo`} className="btn-primario"><PlusCircle size={16}/> Novo Cálculo</Link>
            </>
          }
        </div>
      </div>
      {historicoVisivel && (
        <div className="card">
          <h3>Histórico de Cálculos</h3>
          {historico.length > 0 ? (
            <table className="historico-tabela">
              <thead><tr><th>Período</th><th>DAS Total</th><th>Ações</th></tr></thead>
              <tbody>
                {historico.map(calc => (
                  <tr key={calc.id}>
                    <td>{String(calc.mesReferencia).padStart(2, '0')}/{calc.anoReferencia}</td>
                    <td>R$ {calc.dasTotal.toFixed(2)}</td>
                    <td>
                      <button 
                        type="button" 
                        className="btn-primario" 
                        style={{padding: '0.5rem 1rem', fontSize: '0.9rem'}} 
                        onClick={() => navigate(`/clientes/${clienteId}/resultado/${calc.id}`)}>
                        Ver Detalhes
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <p>Nenhum cálculo encontrado no histórico para este cliente.</p>}
        </div>
      )}
    </div>
  );
}

export default Dashboard;