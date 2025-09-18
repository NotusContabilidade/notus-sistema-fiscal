import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom'; 
import Modal from 'react-modal';
import { jwtDecode } from 'jwt-decode'; // Biblioteca para ler o token
import api from '../services/api';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import { Edit, X, PlusCircle, Search, Trash2, UserCog } from 'lucide-react';
import '../styles/pages/Dashboard.css';

// --- Lógica para pegar a Role do usuário a partir do Token ---
const getUserRole = () => {
  const token = localStorage.getItem("token");
  if (!token) return null;
  try {
    const decodedToken = jwtDecode(token);
    return decodedToken.role; // Assumindo que a role está no token como "role"
  } catch (error) {
    console.error("Erro ao decodificar token:", error);
    return null;
  }
};

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
  
  // --- Novos estados para as funcionalidades de Admin ---
  const [userRole, setUserRole] = useState(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editedClient, setEditedClient] = useState({ razaoSocial: '', cnpj: '', email: '', regimeTributario: '' });

  const fetchCliente = useCallback(async () => {
    try {
      const response = await api.get(`/clientes/id/${clienteId}`);
      setCliente(response.data);
      if (response.data.parametros) {
        setEditedParams({
          rbt12: response.data.parametros.rbt12Atual,
          folha12m: response.data.parametros.folhaPagamento12mAtual
        });
      }
      // Prepara os dados para o modal de edição
      setEditedClient({
        razaoSocial: response.data.cliente.razaoSocial,
        cnpj: response.data.cliente.cnpj,
        email: response.data.cliente.email,
        regimeTributario: response.data.cliente.regimeTributario
      });
    } catch (error) {
      toast.error("Não foi possível carregar os dados do cliente.");
      navigate('/clientes/busca');
    } finally {
      setIsLoading(false);
    }
  }, [clienteId, navigate]);

  useEffect(() => {
    setUserRole(getUserRole()); // Pega a role do usuário ao carregar o componente
    fetchCliente();
  }, [fetchCliente]);

  const handleUpdateParams = async () => {
    setIsLoading(true);
    try {
      const payload = {
        rbt12: parseFloat(editedParams.rbt12),
        folha12m: parseFloat(editedParams.folha12m)
      };
      await api.put(`/clientes/${cliente.cliente.id}/parametros`, payload);
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
      const response = await api.get(`/calculos/historico/${cliente.cliente.id}`);
      setHistorico(response.data);
      setHistoricoVisivel(true);
    } catch (error) {
      toast.error('Não foi possível carregar o histórico de cálculos.');
    } finally {
      setIsLoadingHistorico(false);
    }
  };

  const handleUpdateClient = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await api.put(`/clientes/${cliente.cliente.id}/dados-gerais`, editedClient);
      toast.success("Dados do cliente atualizados com sucesso!");
      setIsEditModalOpen(false);
      await fetchCliente();
    } catch (error) {
      toast.error(error.response?.data?.erro || 'Falha ao atualizar o cliente.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteClient = async () => {
    setIsLoading(true);
    try {
      await api.delete(`/clientes/${cliente.cliente.id}`);
      toast.success("Cliente e todos os seus dados foram excluídos!");
      setIsDeleteModalOpen(false);
      navigate('/clientes/busca');
    } catch (error) {
      toast.error(error.response?.data?.erro || 'Falha ao excluir o cliente.');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return <div className="view-container card"><Spinner /></div>;
  }

  if (!cliente || !cliente.cliente) {
    return <div className="view-container card"><p>Dados do cliente não encontrados.</p></div>;
  }

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Dashboard do Cliente</h1></div>
      <div className="card">
        <div className="dashboard-grid">
          <div className="dashboard-info-card">
            <h4>Dados da Empresa</h4>
            <p><strong>Razão Social:</strong> {cliente.cliente.razaoSocial}</p>
            <p><strong>CNPJ:</strong> {cliente.cliente.cnpj}</p>
            <p><strong>E-mail:</strong> {cliente.cliente.email}</p>
            <p><strong>Regime:</strong> {cliente.cliente.regimeTributario.replace('_', ' ')}</p>
          </div>
          <div className="dashboard-info-card">
            <h4>Parâmetros Fiscais (Simples Nacional)</h4>
            {isEditingParams ? (
              <>
                <div className="form-group"><label>RBT12:</label><input type="number" step="0.01" value={editedParams.rbt12} onFocus={e => e.target.select()} onChange={e => setEditedParams({...editedParams, rbt12: e.target.value})} /></div>
                <div className="form-group"><label>Folha (12m):</label><input type="number" step="0.01" value={editedParams.folha12m} onFocus={e => e.target.select()} onChange={e => setEditedParams({...editedParams, folha12m: e.target.value})} /></div>
              </>
            ) : (
              cliente.parametros ? (
                <>
                  <p><strong>RBT12:</strong> {Number(cliente.parametros.rbt12Atual).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</p>
                  <p><strong>Folha (12m):</strong> {Number(cliente.parametros.folhaPagamento12mAtual).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</p>
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
            <button type="button" className="btn-primario" onClick={handleUpdateParams} disabled={isLoading}>{isLoading ? <Spinner /> : 'Salvar Parâmetros'}</button> :
            <>
              {userRole === 'ROLE_ADMIN' && (
                <>
                  <button type="button" className="btn-admin" onClick={() => setIsEditModalOpen(true)}><UserCog size={16}/> Editar Dados</button>
                  <button type="button" className="btn-admin danger" onClick={() => setIsDeleteModalOpen(true)}><Trash2 size={16}/> Excluir Cliente</button>
                </>
              )}
              <button type="button" className="btn-primario" onClick={handleBuscarHistorico} disabled={isLoadingHistorico}>{isLoadingHistorico ? <Spinner/> : (historicoVisivel ? 'Ocultar Histórico' : 'Ver Histórico')}</button>
              <Link to={`/clientes/${clienteId}/calculo`} className="btn-primario"><PlusCircle size={16}/> Novo Cálculo</Link>
            </>
          }
        </div>
      </div>

      {/* --- INÍCIO DO BLOCO CORRIGIDO --- */}
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
                    <td>{Number(calc.dasTotal).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</td>
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
      {/* --- FIM DO BLOCO CORRIGIDO --- */}

      {/* --- MODAL DE EDIÇÃO DE CLIENTE (ADMIN) --- */}
      <Modal isOpen={isEditModalOpen} onRequestClose={() => setIsEditModalOpen(false)} style={{ content: { maxWidth: '500px', margin: 'auto', height: 'fit-content' } }}>
        <div className="modal-header"><h3>Editar Dados Cadastrais</h3><button className="btn-close-modal" onClick={() => setIsEditModalOpen(false)}><X/></button></div>
        <form onSubmit={handleUpdateClient} className="modal-body">
          <div className="form-group"><label>Razão Social</label><input type="text" value={editedClient.razaoSocial} onChange={e => setEditedClient({...editedClient, razaoSocial: e.target.value})} required/></div>
          <div className="form-group"><label>CNPJ</label><input type="text" value={editedClient.cnpj} onChange={e => setEditedClient({...editedClient, cnpj: e.target.value})} required/></div>
          <div className="form-group"><label>E-mail</label><input type="email" value={editedClient.email} onChange={e => setEditedClient({...editedClient, email: e.target.value})} required/></div>
          <div className="form-group"><label>Regime Tributário</label>
            <select value={editedClient.regimeTributario} onChange={e => setEditedClient({...editedClient, regimeTributario: e.target.value})} required>
              <option value="SIMPLES_NACIONAL">Simples Nacional</option><option value="LUCRO_PRESUMIDO">Lucro Presumido</option><option value="LUCRO_REAL">Lucro Real</option>
            </select>
          </div>
          <div className="modal-actions">
            <button type="button" className="btn-secundario" onClick={() => setIsEditModalOpen(false)}>Cancelar</button>
            <button type="submit" className="btn-primario" disabled={isLoading}>{isLoading ? <Spinner /> : 'Salvar Alterações'}</button>
          </div>
        </form>
      </Modal>

      {/* --- MODAL DE EXCLUSÃO DE CLIENTE (ADMIN) --- */}
      <Modal isOpen={isDeleteModalOpen} onRequestClose={() => setIsDeleteModalOpen(false)} style={{ content: { maxWidth: '500px', margin: 'auto', height: 'fit-content' } }}>
        <div className="modal-header"><h3>Confirmar Exclusão</h3><button className="btn-close-modal" onClick={() => setIsDeleteModalOpen(false)}><X/></button></div>
        <div className="modal-body">
          <p>Você tem certeza que deseja excluir o cliente <strong>{cliente.cliente.razaoSocial}</strong>?</p>
          <p style={{ color: 'var(--cor-erro)', fontWeight: 'bold' }}>Atenção: Esta ação é irreversível e todos os seus dados associados (cálculos, documentos, tarefas, etc.) serão permanentemente removidos.</p>
        </div>
        <div className="modal-actions">
          <button type="button" className="btn-secundario" onClick={() => setIsDeleteModalOpen(false)}>Cancelar</button>
          <button type="button" className="btn-primario danger" onClick={handleDeleteClient} disabled={isLoading}>{isLoading ? <Spinner /> : 'Sim, Excluir Cliente'}</button>
        </div>
      </Modal>
    </div>
  );
}

export default Dashboard;