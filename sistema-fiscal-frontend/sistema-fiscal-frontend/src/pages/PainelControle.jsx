import React, { useState, useEffect, useMemo, useCallback } from "react";
import api from "../services/api";
import Spinner from "../components/Spinner";
import { toast } from 'react-toastify';
import Modal from 'react-modal';
import { CheckSquare, Clock, AlertTriangle, Plus, X, ChevronsUpDown, Activity } from 'lucide-react';
import WorkflowCard from '../components/WorkflowCard';
import DetalheTarefaModal from '../components/DetalheTarefaModal';
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

export default function PainelControle() {
  const [clientes, setClientes] = useState([]);
  const [filtroClienteId, setFiltroClienteId] = useState("ALL");
  const [workflowItems, setWorkflowItems] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [novaTarefa, setNovaTarefa] = useState(initialStateNovaTarefa);

  const [tarefaSelecionadaId, setTarefaSelecionadaId] = useState(null);
  const [isDetalheModalOpen, setIsDetalheModalOpen] = useState(false);

  const fetchWorkflowItems = useCallback(() => {
    setIsLoading(true);
    const params = filtroClienteId !== "ALL" ? { clienteId: filtroClienteId } : {};
    api.get("/painel-controle/workflow", { params })
      .then(res => setWorkflowItems(res.data))
      .catch(() => toast.error("Erro ao carregar o fluxo de trabalho."))
      .finally(() => setIsLoading(false));
  }, [filtroClienteId]);

  useEffect(() => {
    api.get("/clientes/todos")
      .then(res => setClientes(res.data))
      .catch(() => toast.error("Erro ao carregar lista de clientes."));
  }, []);

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
    // Otimisticamente atualiza a UI para feedback instantâneo
    const isConcluding = newStatus.toUpperCase() === 'CONCLUIDO';
    const now = new Date().toISOString();

    setWorkflowItems(prevItems =>
      prevItems.map(item => {
        if (item.id === taskId && item.tipo === 'TAREFA') {
          return {
            ...item,
            status: newStatus,
            // Se estiver concluindo, define a data de conclusão. Se estiver revertendo, anula.
            dataConclusao: isConcluding ? now : null,
          };
        }
        return item;
      })
    );

    // Efetua a chamada à API em segundo plano
    try {
      await api.put(`/tasks/${taskId}/status`, { status: newStatus });
      toast.success("Status da demanda atualizado!");
      // Opcional: re-sincronizar com o servidor após um tempo para garantir consistência
      // setTimeout(fetchWorkflowItems, 2000); 
    } catch (error) {
      toast.error("Erro ao atualizar status. Revertendo alteração.");
      // Em caso de erro, re-sincroniza imediatamente para reverter a UI
      fetchWorkflowItems();
    }
  };

  const handleCardClick = (item) => {
    if (item.tipo === 'TAREFA') {
      setTarefaSelecionadaId(item.id);
      setIsDetalheModalOpen(true);
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

  return (
    <div className="view-container painel-workflow anim-fade-in">
      <div className="page-header">
        <h1 className="page-title">Painel de Controle de Workflow</h1>
        <button className="btn-primario" onClick={() => setIsModalOpen(true)}>
          <Plus size={18} /> Adicionar Demanda
        </button>
      </div>
      <div className="card filtros-card">
        <label htmlFor="cliente-select">Filtrar por Cliente:</label>
        <div className="custom-select-wrapper">
          <select id="cliente-select" value={filtroClienteId} onChange={e => setFiltroClienteId(e.target.value)}>
            <option value="ALL">Todos os clientes</option>
            {clientes.map(c => <option key={c.id} value={c.id}>{c.razaoSocial}</option>)}
          </select>
          <ChevronsUpDown size={20} className="custom-select-icon" />
        </div>
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
            <div className="workflow-coluna coluna-atrasadas">
              <h3>Atrasadas</h3>
              <div className="coluna-content">
                {colunas.atrasados.map(item => 
                  <WorkflowCard key={`${item.tipo}-${item.id}`} item={item} onStatusChange={handleStatusChange} onClick={handleCardClick} />
                )}
                {colunas.atrasados.length === 0 && <p className="coluna-vazia">Nenhuma demanda aqui.</p>}
              </div>
            </div>
            <div className="workflow-coluna coluna-pendentes">
              <h3>Pendentes</h3>
              <div className="coluna-content">
                {colunas.pendentes.map(item => 
                  <WorkflowCard key={`${item.tipo}-${item.id}`} item={item} onStatusChange={handleStatusChange} onClick={handleCardClick} />
                )}
                {colunas.pendentes.length === 0 && <p className="coluna-vazia">Nenhuma demanda aqui.</p>}
              </div>
            </div>
            <div className="workflow-coluna coluna-andamento">
              <h3>Em Andamento</h3>
              <div className="coluna-content">
                {colunas.emAndamento.map(item => 
                  <WorkflowCard key={`${item.tipo}-${item.id}`} item={item} onStatusChange={handleStatusChange} onClick={handleCardClick} />
                )}
                {colunas.emAndamento.length === 0 && <p className="coluna-vazia">Nenhuma demanda aqui.</p>}
              </div>
            </div>
            <div className="workflow-coluna coluna-concluidas">
              <h3>Concluídas</h3>
              <div className="coluna-content">
                {colunas.concluidos.map(item => 
                  <WorkflowCard key={`${item.tipo}-${item.id}`} item={item} onStatusChange={handleStatusChange} onClick={handleCardClick} />
                )}
                {colunas.concluidos.length === 0 && <p className="coluna-vazia">Nenhuma demanda aqui.</p>}
              </div>
            </div>
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
          <div className="form-group"><label>Cliente*</label><select name="clienteId" value={novaTarefa.clienteId} onChange={handleInputChange} required><option value="" disabled>Selecione um cliente...</option>{clientes.map(c => <option key={c.id} value={c.id}>{c.razaoSocial}</option>)}</select></div>
          <div className="form-group"><label>Título da Demanda*</label><input type="text" name="titulo" value={novaTarefa.titulo} onChange={handleInputChange} placeholder="Ex: Enviar DAS Ref. 09/2025" required /></div>
          <div className="form-group"><label>Categoria</label><select name="categoria" value={novaTarefa.categoria} onChange={handleInputChange}><option value="OBRIGACAO_ACESSORIA">Obrigação Acessória</option><option value="IMPOSTO">Imposto</option><option value="DOCUMENTO_CLIENTE">Documento do Cliente</option><option value="INTERNO">Interno</option></select></div>
          <div className="form-row"><div className="form-group"><label>Responsável</label><input type="text" name="responsavel" value={novaTarefa.responsavel} onChange={handleInputChange} placeholder="Nome do membro da equipe" /></div><div className="form-group"><label>Prazo Final</label><input type="date" name="prazo" value={novaTarefa.prazo} onChange={handleInputChange} /></div></div>
          <div className="form-group"><label>Descrição / Detalhes</label><textarea name="descricao" value={novaTarefa.descricao} onChange={handleInputChange} rows="3"></textarea></div>
          <div className="modal-actions"><button type="button" className="btn-secundario" onClick={() => setIsModalOpen(false)}>Cancelar</button><button type="submit" className="btn-primario" disabled={isLoading}>{isLoading ? <Spinner /> : 'Criar Demanda'}</button></div>
        </form>
      </Modal>

      <Modal 
          isOpen={isDetalheModalOpen} 
          onRequestClose={() => setIsDetalheModalOpen(false)} 
          className="ReactModal__Content"
          overlayClassName="ReactModal__Overlay"
          style={{ content: { maxWidth: '700px', margin: 'auto', height: 'fit-content', padding: '0' } }}
      >
          <div className="modal-header">
              <h3>Detalhes da Demanda</h3>
              <button className="btn-close-modal" onClick={() => setIsDetalheModalOpen(false)}><X/></button>
          </div>
          {tarefaSelecionadaId && (
              <DetalheTarefaModal 
                  tarefaId={tarefaSelecionadaId} 
                  onRequestClose={() => setIsDetalheModalOpen(false)}
                  onActionSuccess={fetchWorkflowItems}
              />
          )}
      </Modal>
    </div>
  );
}