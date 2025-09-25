import React, { useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import { toast } from 'react-toastify';
import Modal from 'react-modal';
import { Plus, Edit, Trash2, X } from 'lucide-react';
import Spinner from '../components/Spinner';
import ClienteSearchableSelect from '../components/ClienteSearchableSelect'; // IMPORTAÇÃO

const initialStateRecorrencia = {
  clienteId: '',
  titulo: '',
  descricao: '',
  categoria: 'OBRIGACAO_ACESSORIA',
  responsavel: '',
  frequencia: 'MENSAL',
  diaVencimento: 20,
  ativa: true,
};

export default function TarefasRecorrentes() {
  const [clientes, setClientes] = useState([]);
  const [filtroClienteId, setFiltroClienteId] = useState('');
  const [recorrencias, setRecorrencias] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formState, setFormState] = useState(initialStateRecorrencia);
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    api.get("/clientes/todos")
      .then(res => setClientes(res.data))
      .catch(() => toast.error("Erro ao carregar clientes."));
  }, []);

  const fetchRecorrencias = useCallback(() => {
    if (!filtroClienteId) {
        setRecorrencias([]);
        return;
    };
    setIsLoading(true);
    api.get(`/recorrencias/cliente/${filtroClienteId}`)
      .then(res => setRecorrencias(res.data))
      .catch(() => toast.error("Erro ao buscar tarefas recorrentes."))
      .finally(() => setIsLoading(false));
  }, [filtroClienteId]);

  useEffect(() => {
    fetchRecorrencias();
  }, [fetchRecorrencias]);
  
  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormState(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const openModalParaCriar = () => {
    if (!filtroClienteId) {
      toast.warn("Selecione um cliente primeiro.");
      return;
    }
    setEditingId(null);
    setFormState({ ...initialStateRecorrencia, clienteId: filtroClienteId });
    setIsModalOpen(true);
  };
  
  const openModalParaEditar = (recorrencia) => {
    setEditingId(recorrencia.id);
    setFormState({
      clienteId: recorrencia.clienteId,
      titulo: recorrencia.titulo,
      descricao: recorrencia.descricao || '',
      categoria: recorrencia.categoria,
      responsavel: recorrencia.responsavel || '',
      frequencia: recorrencia.frequencia,
      diaVencimento: recorrencia.diaVencimento,
      ativa: recorrencia.ativa
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    const payload = {
        ...formState,
        diaVencimento: parseInt(formState.diaVencimento, 10)
    };

    try {
      if (editingId) {
        await api.put(`/recorrencias/${editingId}`, payload);
        toast.success("Molde de tarefa atualizado!");
      } else {
        await api.post("/recorrencias", payload);
        toast.success("Molde de tarefa recorrente criado!");
      }
      setIsModalOpen(false);
      fetchRecorrencias();
    } catch (err) {
      toast.error("Erro ao salvar molde de tarefa.");
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleDelete = async (id) => {
    if (window.confirm("Tem certeza que deseja excluir este molde de tarefa recorrente? As tarefas já criadas não serão afetadas.")) {
        setIsLoading(true);
        try {
            await api.delete(`/recorrencias/${id}`);
            toast.success("Molde de tarefa excluído.");
            fetchRecorrencias();
        } catch(err) {
            toast.error("Erro ao excluir molde.");
        } finally {
            setIsLoading(false);
        }
    }
  };

  return (
    <div className="view-container anim-fade-in">
      <div className="page-header">
        <h1 className="page-title">Gestão de Tarefas Recorrentes</h1>
        <button className="btn-primario" onClick={openModalParaCriar} disabled={!filtroClienteId}>
          <Plus size={18} /> Criar Novo Molde
        </button>
      </div>
      
      <div className="card filtros-card">
        <label htmlFor="cliente-select">Selecione um Cliente para Gerenciar:</label>
        {/* SUBSTITUIÇÃO DO SELECT */}
        <ClienteSearchableSelect
            clients={clientes}
            value={filtroClienteId}
            onChange={setFiltroClienteId}
        />
      </div>

      <div className="card">
        {isLoading ? <Spinner /> : (
            filtroClienteId ? (
                recorrencias.length > 0 ? (
                    <table className="lista-detalhes-tabela">
                        <thead><tr><th>Título</th><th>Frequência</th><th>Dia Venc.</th><th>Status</th><th>Ações</th></tr></thead>
                        <tbody>
                        {recorrencias.map(r => (
                            <tr key={r.id}>
                            <td>{r.titulo}</td>
                            <td>{r.frequencia === 'MENSAL' ? 'Mensal' : 'Anual'}</td>
                            <td>{r.diaVencimento}</td>
                            <td>
                                <span className={`status-badge ${r.ativa ? 'ativo' : 'inativo'}`}>
                                    {r.ativa ? "Ativa" : "Inativa"}
                                </span>
                            </td>
                            <td className="acoes-tabela">
                                <button className="btn-acao editar" onClick={() => openModalParaEditar(r)}><Edit size={16}/></button>
                                <button className="btn-acao excluir" onClick={() => handleDelete(r.id)}><Trash2 size={16}/></button>
                            </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                ) : <p>Nenhum molde de tarefa recorrente encontrado para este cliente. Clique em "Criar Novo Molde" para começar.</p>
            ) : <p>Por favor, selecione um cliente para visualizar ou criar moldes de tarefas recorrentes.</p>
        )}
      </div>

      <Modal isOpen={isModalOpen} onRequestClose={() => setIsModalOpen(false)} style={{ content: { maxWidth: '600px', width: '90%' } }}>
          <div className="modal-header"><h3>{editingId ? "Editar" : "Criar"} Molde de Tarefa</h3><button className="btn-close-modal" onClick={() => setIsModalOpen(false)}><X/></button></div>
          <form onSubmit={handleSubmit}>
              <div className="modal-body form-nova-demanda">
                  <div className="form-group">
                    <label>Título*</label>
                    <input type="text" name="titulo" value={formState.titulo} onChange={handleInputChange} placeholder="Ex: Gerar Guia DAS" required />
                  </div>
                  <div className="form-group">
                    <label>Categoria</label>
                    <select name="categoria" value={formState.categoria} onChange={handleInputChange}>
                      <option value="OBRIGACAO_ACESSORIA">Obrigação Acessória</option>
                      <option value="IMPOSTO">Imposto</option>
                      <option value="DOCUMENTO_CLIENTE">Documento do Cliente</option>
                      <option value="INTERNO">Interno</option>
                    </select>
                  </div>
                  <div className="form-row">
                    <div className="form-group">
                      <label>Frequência</label>
                      <select name="frequencia" value={formState.frequencia} onChange={handleInputChange}>
                        <option value="MENSAL">Mensal</option>
                        <option value="ANUAL">Anual</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label>Dia do Vencimento*</label>
                      <input type="number" name="diaVencimento" value={formState.diaVencimento} onChange={handleInputChange} min="1" max="31" required />
                    </div>
                  </div>
                  <div className="form-group">
                    <label>Responsável</label>
                    <input type="text" name="responsavel" value={formState.responsavel} onChange={handleInputChange} placeholder="Nome do membro da equipe" />
                  </div>
                  <div className="form-group">
                    <label>Descrição Padrão</label>
                    <textarea name="descricao" value={formState.descricao} onChange={handleInputChange} rows="3"></textarea>
                  </div>
                  <div className="form-group-inline" style={{ justifyContent: 'flex-start' }}>
                    <input type="checkbox" name="ativa" id="ativa" checked={formState.ativa} onChange={handleInputChange} />
                    <label htmlFor="ativa" style={{ marginBottom: 0 }}>Este molde está ativo?</label>
                  </div>
              </div>
              <div className="modal-actions">
                  <button type="button" className="btn-secundario" onClick={() => setIsModalOpen(false)}>Cancelar</button>
                  <button type="submit" className="btn-primario" disabled={isLoading}>{isLoading ? <Spinner /> : "Salvar Molde"}</button>
              </div>
          </form>
      </Modal>
    </div>
  );
}