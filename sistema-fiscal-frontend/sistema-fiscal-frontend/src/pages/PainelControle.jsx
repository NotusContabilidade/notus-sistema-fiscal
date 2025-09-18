import React, { useState, useEffect, useMemo, useCallback } from "react";
import api from "../services/api";
import Spinner from "../components/Spinner";
import { toast } from 'react-toastify';
import Modal from 'react-modal';
import { FileText, CheckSquare, Clock, AlertTriangle, Plus } from 'lucide-react';
import WorkflowCard from '../components/WorkflowCard'; // Importa o novo componente
import '../styles/pages/PainelControle.css';

const StatCard = ({ icon, title, value, color }) => (
  <div className="stat-card-painel" style={{ borderLeftColor: color }}>
    <div className="stat-icon">{icon}</div>
    <div className="stat-info"><span className="stat-value">{value}</span><span className="stat-title">{title}</span></div>
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

  // --- NOVA FUNÇÃO PARA ALTERAR STATUS ---
  const handleStatusChange = async (taskId, newStatus) => {
    const originalItems = [...workflowItems];
    const updatedItems = workflowItems.map(item => 
      (item.id === taskId && item.tipo === 'TAREFA') ? { ...item, status: newStatus } : item
    );
    setWorkflowItems(updatedItems);

    try {
      await api.put(`/tasks/${taskId}/status`, { status: newStatus });
      toast.success("Status da demanda atualizado!");
    } catch (error) {
      toast.error("Erro ao atualizar status. Revertendo.");
      setWorkflowItems(originalItems); // Reverte em caso de erro
    }
  };

  const colunas = useMemo(() => {
    const pendentes = workflowItems.filter(item => ["PENDENTE", "EM_ANDAMENTO"].includes(item.status?.toUpperCase()));
    const concluidos = workflowItems.filter(item => item.status?.toUpperCase() === "CONCLUIDO");
    const atrasados = pendentes.filter(item => item.prazo && new Date(item.prazo) < new Date());
    return { pendentes, concluidos, atrasados };
  }, [workflowItems]);

  return (
    <div className="view-container painel-workflow">
      <div className="page-header">
        <h1 className="page-title">Painel de Controle de Workflow</h1>
        <button className="btn-primario" onClick={() => setIsModalOpen(true)}>
          <Plus size={18} /> Adicionar Demanda
        </button>
      </div>
      <div className="card filtros-card">
        <label htmlFor="cliente-select">Filtrar por Cliente:</label>
        <select id="cliente-select" value={filtroClienteId} onChange={e => setFiltroClienteId(e.target.value)}>
          <option value="ALL">Todos os clientes</option>
          {clientes.map(c => <option key={c.id} value={c.id}>{c.razaoSocial}</option>)}
        </select>
      </div>
      <div className="stats-grid-painel">
        <StatCard icon={<Clock size={28} />} title="Demandas Pendentes" value={colunas.pendentes.length} color="#f59e0b" />
        <StatCard icon={<AlertTriangle size={28} />} title="Atrasadas" value={colunas.atrasados.length} color="#ef4444" />
        <StatCard icon={<CheckSquare size={28} />} title="Concluídas no Período" value={colunas.concluidos.length} color="#10b981" />
      </div>
      <div className="workflow-board">
        {isLoading && workflowItems.length === 0 ? <Spinner /> : (
          <>
            <div className="workflow-coluna">
              <h3>Pendentes / Em Andamento</h3>
              <div className="coluna-content">
                {colunas.pendentes.map(item => <WorkflowCard key={`${item.tipo}-${item.id}`} item={item} onStatusChange={handleStatusChange} />)}
                {colunas.pendentes.length === 0 && <p className="coluna-vazia">Nenhuma demanda aqui.</p>}
              </div>
            </div>
            <div className="workflow-coluna">
              <h3>Concluídas</h3>
              <div className="coluna-content">
                {colunas.concluidos.map(item => <WorkflowCard key={`${item.tipo}-${item.id}`} item={item} onStatusChange={handleStatusChange} />)}
                {colunas.concluidos.length === 0 && <p className="coluna-vazia">Nenhuma demanda aqui.</p>}
              </div>
            </div>
          </>
        )}
      </div>
      <Modal isOpen={isModalOpen} onRequestClose={() => setIsModalOpen(false)} style={{ content: { maxWidth: '600px', margin: 'auto', height: 'fit-content' } }}>
        <div className="modal-header"><h3>Adicionar Nova Demanda</h3><button className="btn-close-modal" onClick={() => setIsModalOpen(false)}>X</button></div>
        <form onSubmit={handleCriarTarefa} className="modal-body form-nova-demanda">
          <div className="form-group"><label>Cliente*</label><select name="clienteId" value={novaTarefa.clienteId} onChange={handleInputChange} required><option value="" disabled>Selecione um cliente...</option>{clientes.map(c => <option key={c.id} value={c.id}>{c.razaoSocial}</option>)}</select></div>
          <div className="form-group"><label>Título da Demanda*</label><input type="text" name="titulo" value={novaTarefa.titulo} onChange={handleInputChange} placeholder="Ex: Enviar DAS Ref. 08/2025" required /></div>
          <div className="form-group"><label>Categoria</label><select name="categoria" value={novaTarefa.categoria} onChange={handleInputChange}><option value="OBRIGACAO_ACESSORIA">Obrigação Acessória</option><option value="IMPOSTO">Imposto</option><option value="DOCUMENTO_CLIENTE">Documento do Cliente</option><option value="INTERNO">Interno</option></select></div>
          <div className="form-row"><div className="form-group"><label>Responsável</label><input type="text" name="responsavel" value={novaTarefa.responsavel} onChange={handleInputChange} placeholder="Nome do membro da equipe" /></div><div className="form-group"><label>Prazo Final</label><input type="date" name="prazo" value={novaTarefa.prazo} onChange={handleInputChange} /></div></div>
          <div className="form-group"><label>Descrição / Detalhes</label><textarea name="descricao" value={novaTarefa.descricao} onChange={handleInputChange} rows="3"></textarea></div>
          <div className="modal-actions"><button type="button" className="btn-secundario" onClick={() => setIsModalOpen(false)}>Cancelar</button><button type="submit" className="btn-primario" disabled={isLoading}>{isLoading ? <Spinner /> : 'Criar Demanda'}</button></div>
        </form>
      </Modal>
    </div>
  );
}