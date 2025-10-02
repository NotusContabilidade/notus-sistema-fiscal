import React, { useState, useEffect, useMemo, useCallback } from "react";
import api from "../services/api";
import Spinner from "../components/Spinner";
import { toast } from 'react-toastify';
import Modal from 'react-modal';
import { CheckSquare, Clock, AlertTriangle, Plus, X, Activity, Filter, Bell, Users, Search, Send, History, Globe } from 'lucide-react';
import WorkflowCard from '../components/WorkflowCard';
import DetalheTarefaModal from '../components/DetalheTarefaModal';
import ClienteSearchableSelect from '../components/ClienteSearchableSelect';
import '../styles/pages/PainelControle.css';

const StatCard = ({ icon, title, value, color }) => (
  <div className="stat-card-painel" style={{ borderLeftColor: color }}>
    <div className="stat-icon">{icon}</div>
    <div className="stat-info">
      <span className="stat-value">{value}</span>
      <span className="stat-title">{title}</span>
    </div>
  </div>
);

const initialStateNovaTarefa = {
  clienteId: '',
  titulo: '',
  descricao: '',
  responsavel: '',
  prazo: '',
  categoria: 'OBRIGACAO_ACESSORIA'
};

// --- Componente para o Formulário de Comunicados (Lógica Sincronizada e Visual Repaginado) ---
const ComunicadoSender = ({ initialClienteId, key }) => {
    const [selectedClienteId, setSelectedClienteId] = useState(initialClienteId || "ALL");
    const [titulo, setTitulo] = useState('');
    const [mensagem, setMensagem] = useState('');
    const [loading, setLoading] = useState(false);
    const [recentes, setRecentes] = useState([]);
    const [loadingRecentes, setLoadingRecentes] = useState(true);

    // Sincroniza o destinatário com o filtro principal do painel
    useEffect(() => {
        setSelectedClienteId(initialClienteId || "ALL");
    }, [initialClienteId]);

    const fetchRecentes = useCallback(async () => {
        setLoadingRecentes(true);
        try {
            const response = await api.get('/comunicados/recentes');
            setRecentes(response.data);
        } catch (error) {
            toast.error("Erro ao buscar comunicados recentes.");
        } finally {
            setLoadingRecentes(false);
        }
    }, []);

    useEffect(() => {
        fetchRecentes();
    }, [fetchRecentes]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!titulo.trim() || !mensagem.trim()) {
            toast.warn("Título e mensagem são obrigatórios.");
            return;
        }
        setLoading(true);
        try {
            const payload = { titulo, mensagem };
            if (selectedClienteId === "ALL") {
                await api.post('/comunicados/broadcast', payload);
                toast.success(`Comunicado enviado para TODOS os clientes!`);
            } else {
                await api.post(`/comunicados/cliente/${selectedClienteId}`, payload);
                toast.success(`Comunicado enviado com sucesso!`);
            }
            // Limpa o formulário, mas mantém a seleção do filtro principal
            setTitulo('');
            setMensagem('');
            fetchRecentes(); // Atualiza a lista de recentes
        } catch (error) {
            toast.error("Falha ao enviar o comunicado. Tente novamente.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="comunicado-module-grid" key={key}>
            <div className="comunicado-sender-container anim-glass-edge">
                <div className="comunicado-header">
                    <Send size={20} />
                    <h4>Criar Novo Comunicado</h4>
                </div>
                <p className="card-subtitle">Envie uma mensagem para um cliente específico ou para todos de uma vez.</p>
                <form onSubmit={handleSubmit} className="comunicado-form">
                    <div className="form-group">
                        <label>Destinatário</label>
                        <ClienteSearchableSelect
                            value={selectedClienteId}
                            onChange={setSelectedClienteId}
                        />
                    </div>

                    <div className="form-group floating-label-group">
                        <input 
                            type="text" 
                            id="comunicado-titulo"
                            value={titulo} 
                            onChange={e => setTitulo(e.target.value)} 
                            required 
                            placeholder=" " 
                        />
                        <label htmlFor="comunicado-titulo">Título do Comunicado</label>
                    </div>
                    
                    <div className="form-group floating-label-group">
                        <textarea 
                            id="comunicado-mensagem"
                            rows="5" 
                            value={mensagem} 
                            onChange={e => setMensagem(e.target.value)} 
                            required 
                            placeholder=" " 
                        />
                        <label htmlFor="comunicado-mensagem">Mensagem</label>
                    </div>

                    <button type="submit" className="btn-primario btn-enviar-comunicado" disabled={loading}>
                        {loading ? <Spinner /> : <><Send size={16} /> Enviar Comunicado</>}
                    </button>
                </form>
            </div>
            <div className="comunicado-history-container anim-glass-edge-delay">
                <div className="comunicado-header">
                    <History size={20} />
                    <h4>Últimos Enviados</h4>
                </div>
                <div className="history-list">
                    {loadingRecentes ? <Spinner /> : recentes.length > 0 ? recentes.map(r => (
                        <div key={r.id} className="history-item">
                            <div className="history-item-header">
                                <strong>{r.titulo}</strong>
                                <span className="history-item-date">{new Date(r.dataCriacao).toLocaleDateString('pt-BR', {day: '2-digit', month: '2-digit', year: 'numeric'})}</span>
                            </div>
                            <div className="history-item-recipient">
                                {r.clienteRazaoSocial ? (
                                    <>
                                        <Users size={14} />
                                        <span>{r.clienteRazaoSocial}</span>
                                    </>
                                ) : (
                                    <>
                                        <Globe size={14} />
                                        <span>TODOS OS CLIENTES</span>
                                    </>
                                )}
                            </div>
                            <p className="history-item-message">{r.mensagem}</p>
                        </div>
                    )) : <p className="empty-message">Nenhum comunicado recente.</p>}
                </div>
            </div>
        </div>
    );
};

export default function PainelControle() {
  const [filtroClienteId, setFiltroClienteId] = useState("ALL");
  const [workflowItems, setWorkflowItems] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [novaTarefa, setNovaTarefa] = useState(initialStateNovaTarefa);

  const [itemSelecionado, setItemSelecionado] = useState(null);
  const [activeTab, setActiveTab] = useState('comunicados');

  const fetchWorkflowItems = useCallback(() => {
    setIsLoading(true);
    const params = filtroClienteId !== "ALL" ? { clienteId: filtroClienteId } : {};
    api.get("/painel-controle/workflow", { params })
      .then(res => setWorkflowItems(res.data))
      .catch(() => toast.error("Erro ao carregar o fluxo de trabalho."))
      .finally(() => setIsLoading(false));
  }, [filtroClienteId]);

  useEffect(() => {
    fetchWorkflowItems();
  }, [fetchWorkflowItems]);
  
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNovaTarefa(prev => ({...prev, [name]: value}));
  };
  
  const handleCriarTarefa = async (e) => {
    e.preventDefault();
    if (!novaTarefa.clienteId || !novaTarefa.titulo) {
      toast.warn("Por favor, selecione um cliente e preencha o título.");
      return;
    }
    setIsLoading(true);
    try {
      await api.post("/tasks", novaTarefa);
      toast.success("Nova demanda criada com sucesso!");
      setIsModalOpen(false);
      setNovaTarefa(initialStateNovaTarefa);
      fetchWorkflowItems();
    } catch(err) {
      toast.error("Erro ao criar a demanda.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleStatusChange = async (taskId, newStatus) => {
    try {
      await api.put(`/tasks/${taskId}/status`, { status: newStatus });
      toast.success("Status da demanda atualizado!");
      fetchWorkflowItems();
    } catch (error) {
      toast.error("Erro ao atualizar status.");
    }
  };

  const handleCardClick = (item) => {
    if (item.tipo === 'TAREFA') {
      setItemSelecionado(item);
    } else {
      toast.info("Visualização de detalhes de documentos ainda não implementada.");
    }
  };

  const colunas = useMemo(() => {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);

    const atrasados = workflowItems.filter(item => item.prazo && new Date(item.prazo) < hoje && item.status?.toUpperCase() !== 'CONCLUIDO');
    const idsAtrasados = new Set(atrasados.map(item => item.id));

    const pendentes = workflowItems.filter(item => item.status?.toUpperCase() === 'PENDENTE' && !idsAtrasados.has(item.id));
    const emAndamento = workflowItems.filter(item => item.status?.toUpperCase() === 'EM_ANDAMENTO' && !idsAtrasados.has(item.id));
    const concluidos = workflowItems.filter(item => item.status?.toUpperCase() === 'CONCLUIDO');

    return { pendentes, emAndamento, concluidos, atrasados };
  }, [workflowItems]);

  const renderContent = () => {
    switch (activeTab) {
      case 'usuarios':
        return <div className="empty-message">Gerenciamento de Usuários (em breve)</div>;
      case 'comunicados':
        return (
          <ComunicadoSender initialClienteId={filtroClienteId} key={filtroClienteId} />
        );
      default:
        return null;
    }
  };

  return (
    <div className="view-container painel-workflow anim-fade-in">
      <div className="page-header">
        <h1 className="page-title">Painel de Controle de Workflow</h1>
        <button className="btn-primario" onClick={() => setIsModalOpen(true)}>
          <Plus size={18} /> Adicionar Demanda
        </button>
      </div>
      
      <div className="filtros-card">
        <div className="filtro-header">
          <Filter size={20} className="filtro-icon" />
          <label htmlFor="filtro-cliente" className="filtro-label">Filtrar por Cliente</label>
        </div>
        <ClienteSearchableSelect
          id="filtro-cliente"
          value={filtroClienteId}
          onChange={setFiltroClienteId}
          placeholder="Pesquisar todos ou um cliente específico..."
        />
      </div>

      <div className="stats-grid-painel four-stats">
        <StatCard icon={<Clock size={28} />} title="Pendentes" value={colunas.pendentes.length} color="#f59e0b" />
        <StatCard icon={<Activity size={28} />} title="Em Andamento" value={colunas.emAndamento.length} color="#7c3aed" />
        <StatCard icon={<AlertTriangle size={28} />} title="Atrasadas" value={colunas.atrasados.length} color="#ef4444" />
        <StatCard icon={<CheckSquare size={28} />} title="Concluídas" value={colunas.concluidos.length} color="#10b981" />
      </div>
      
      <div className="workflow-board four-columns">
        {isLoading && workflowItems.length === 0 ? <Spinner /> : (
          <>
            {Object.entries({atrasadas: colunas.atrasados, pendentes: colunas.pendentes, emAndamento: colunas.emAndamento, concluidos: colunas.concluidos}).map(([key, colunaItems]) => (
              <div key={key} className={`workflow-coluna coluna-${key}`}>
                <h3>{key.charAt(0).toUpperCase() + key.slice(1)}</h3>
                <div className="coluna-content">
                  {colunaItems.map(item => 
                    <WorkflowCard key={`${item.tipo}-${item.id}`} item={item} onStatusChange={handleStatusChange} onClick={handleCardClick} />
                  )}
                  {colunaItems.length === 0 && <p className="coluna-vazia">Nenhuma demanda aqui.</p>}
                </div>
              </div>
            ))}
          </>
        )}
      </div>
      
      <Modal 
        isOpen={isModalOpen} 
        onRequestClose={() => setIsModalOpen(false)} 
        className="ReactModal__Content"
        overlayClassName="ReactModal__Overlay"
        style={{ content: { maxWidth: '600px', margin: 'auto', height: 'fit-content' } }}
      >
        <div className="modal-header"><h3>Adicionar Nova Demanda</h3><button className="btn-close-modal" onClick={() => setIsModalOpen(false)}><X/></button></div>
        <form onSubmit={handleCriarTarefa} className="modal-body form-nova-demanda">
          <div className="form-group">
            <label>Cliente*</label>
            <ClienteSearchableSelect
              value={novaTarefa.clienteId}
              onChange={(id) => handleInputChange({ target: { name: 'clienteId', value: id } })}
            />
          </div>
          <div className="form-group"><label>Título da Demanda*</label><input type="text" name="titulo" value={novaTarefa.titulo} onChange={handleInputChange} placeholder="Ex: Enviar DAS Ref. 09/2025" required /></div>
          <div className="form-group"><label>Categoria</label><select name="categoria" value={novaTarefa.categoria} onChange={handleInputChange}><option value="OBRIGACAO_ACESSORIA">Obrigação Acessória</option><option value="IMPOSTO">Imposto</option><option value="DOCUMENTO_CLIENTE">Documento do Cliente</option><option value="INTERNO">Interno</option></select></div>
          <div className="form-row"><div className="form-group"><label>Responsável</label><input type="text" name="responsavel" value={novaTarefa.responsavel} onChange={handleInputChange} placeholder="Nome do membro da equipe" /></div><div className="form-group"><label>Prazo Final</label><input type="date" name="prazo" value={novaTarefa.prazo} onChange={handleInputChange} /></div></div>
          <div className="form-group"><label>Descrição / Detalhes</label><textarea name="descricao" value={novaTarefa.descricao} onChange={handleInputChange} rows="3"></textarea></div>
          <div className="modal-actions"><button type="button" className="btn-secundario" onClick={() => setIsModalOpen(false)}>Cancelar</button><button type="submit" className="btn-primario" disabled={isLoading}>{isLoading ? <Spinner /> : 'Criar Demanda'}</button></div>
        </form>
      </Modal>

      <Modal 
          isOpen={!!itemSelecionado} 
          onRequestClose={() => setItemSelecionado(null)} 
          className="ReactModal__Content"
          overlayClassName="ReactModal__Overlay"
          style={{ content: { 
            width: '90vw', 
            maxWidth: '1200px', 
            height: 'auto',
            maxHeight: '85vh',
            padding: '0' 
          } }}
      >
          <div className="modal-header">
              <h3>Detalhes da Demanda</h3>
              <button className="btn-close-modal" onClick={() => setItemSelecionado(null)}><X/></button>
          </div>
          {itemSelecionado && (
              <DetalheTarefaModal 
                  item={itemSelecionado} 
                  onRequestClose={() => setItemSelecionado(null)}
                  onActionSuccess={fetchWorkflowItems}
              />
          )}
      </Modal>

      <div className="painel-controle-card">
        <nav className="painel-controle-tabs">
          <button onClick={() => setActiveTab('comunicados')} className={activeTab === 'comunicados' ? 'active' : ''}>
            <Bell size={16} /> Comunicados
          </button>
          <button onClick={() => setActiveTab('usuarios')} className={activeTab === 'usuarios' ? 'active' : ''}>
            <Users size={16} /> Usuários
          </button>
        </nav>
        <div className="painel-controle-content">
          {renderContent()}
        </div>
      </div>
    </div>
  );
}