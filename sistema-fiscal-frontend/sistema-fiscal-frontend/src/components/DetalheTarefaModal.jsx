import React, { useState, useEffect, useCallback, useRef } from 'react';
import api from '../services/api';
import Spinner from './Spinner';
import { toast } from 'react-toastify';
import { Calendar, User, Tag, Send, Briefcase, CheckCircle } from 'lucide-react';
import '../styles/components/DetalheTarefaModal.css';

function formatStatus(status) {
    if (!status) return 'N/D';
    return status.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return 'N/D';
    return new Date(dateTimeString).toLocaleString('pt-BR', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

const getInitials = (name = '') => {
    if (!name) return '?';
    const nameParts = name.split(' ');
    if (nameParts.length > 1) {
        return `${nameParts[0][0]}${nameParts[nameParts.length - 1][0]}`.toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
};

const MetaItem = ({ icon, label, value, className = '' }) => (
    <div className={`meta-item ${className}`}>
        <div className="meta-icon">{icon}</div>
        <div className="meta-content">
            <span className="meta-label">{label}</span>
            <span className="meta-value">{value}</span>
        </div>
    </div>
);

const TaskActions = ({ task, onActionSuccess }) => {
    const [status, setStatus] = useState(task.status);
    const [prazo, setPrazo] = useState(task.prazo ? new Date(task.prazo).toISOString().split('T')[0] : '');
    const [isUpdating, setIsUpdating] = useState(false);

    const handleUpdate = async (field, value) => {
        setIsUpdating(true);
        try {
            if (field === 'status') {
                await api.put(`/tasks/${task.id}/status`, { status: value });
                setStatus(value);
            }
            if (field === 'prazo') {
                await api.put(`/tasks/${task.id}`, { prazo: value });
                setPrazo(value);
            }
            toast.success(`Demanda atualizada com sucesso!`);
            if (onActionSuccess) onActionSuccess();
        } catch (error) {
            toast.error(`Erro ao atualizar a demanda.`);
        } finally {
            setIsUpdating(false);
        }
    };

    const handleStatusChange = (e) => {
        const newStatus = e.target.value;
        setStatus(newStatus);
        handleUpdate('status', newStatus);
    };
    
    const handlePrazoChange = (e) => {
        const newPrazo = e.target.value;
        setPrazo(newPrazo);
        handleUpdate('prazo', newPrazo);
    };

    const handleCompleteTask = () => {
        handleUpdate('status', 'CONCLUIDO');
    };

    return (
        <div className="task-actions-bar">
            <div className="action-item">
                <label htmlFor="status-select">Alterar Status</label>
                <select id="status-select" value={status} onChange={handleStatusChange} disabled={isUpdating}>
                    <option value="PENDENTE">Pendente</option>
                    <option value="EM_ANDAMENTO">Em Andamento</option>
                    <option value="CONCLUIDO">Concluído</option>
                </select>
            </div>
            <div className="action-item">
                <label htmlFor="prazo-input">Alterar Prazo</label>
                <input id="prazo-input" type="date" value={prazo} onChange={handlePrazoChange} disabled={isUpdating} />
            </div>
            <button className="btn-complete-task" onClick={handleCompleteTask} disabled={isUpdating || status === 'CONCLUIDO'}>
                <CheckCircle size={18} />
                Marcar como Concluída
            </button>
        </div>
    );
};


export default function DetalheTarefaModal({ item, onRequestClose, onActionSuccess }) {
    const [comentarios, setComentarios] = useState([]);
    const [novoComentario, setNovoComentario] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const [isActionLoading, setIsActionLoading] = useState(false);
    const [clienteNome, setClienteNome] = useState('Carregando...');
    const commentsEndRef = useRef(null);

    useEffect(() => {
        if (item?.cliente?.razaoSocial) {
            setClienteNome(item.cliente.razaoSocial);
        } else if (item?.clienteId) {
            api.get(`/clientes/id/${item.clienteId}`)
                .then(res => {
                    setClienteNome(res.data.cliente.razaoSocial);
                })
                .catch(() => {
                    setClienteNome('Cliente não encontrado');
                });
        } else {
            setClienteNome('N/D');
        }
    }, [item]);

    const fetchComments = useCallback(async () => {
        if (!item?.id || item.tipo !== 'TAREFA') {
            setIsLoading(false);
            return;
        };
        setIsLoading(true);
        try {
            const comentariosRes = await api.get(`/tasks/${item.id}/comments`);
            setComentarios(comentariosRes.data);
        } catch (error) {
            toast.error("Erro ao carregar comentários.");
        } finally {
            setIsLoading(false);
        }
    }, [item]);

    useEffect(() => {
        fetchComments();
    }, [fetchComments]);

    useEffect(() => {
        if(isActionLoading) {
            commentsEndRef.current?.scrollIntoView({ behavior: "smooth" });
        }
    }, [comentarios, isActionLoading]);

    const handleAddComment = async (e) => {
        e.preventDefault();
        if (!novoComentario.trim()) return;
        
        const textoComentario = novoComentario;
        setNovoComentario('');
        setIsActionLoading(true);

        const optimisticComment = {
            id: Date.now(),
            texto: textoComentario,
            autor: localStorage.getItem("user_nome") || 'Você',
            dataCriacao: new Date().toISOString(),
            isOptimistic: true
        };
        setComentarios(prev => [...prev, optimisticComment]);

        try {
            const response = await api.post(`/tasks/${item.id}/comments`, { texto: textoComentario });
            const savedComment = response.data;
            setComentarios(prev => prev.map(c => c.id === optimisticComment.id ? savedComment : c));
        } catch (error) {
            toast.error('Erro ao adicionar comentário. Desfazendo.');
            setComentarios(prev => prev.filter(c => c.id !== optimisticComment.id));
        } finally {
            setIsActionLoading(false);
        }
    };

    if (!item) return null;

    return (
        <>
            <div className="modal-body detalhe-tarefa-modal">
                <h3 className="tarefa-titulo">{item.titulo}</h3>
                
                <div className="tarefa-meta-grid">
                    <MetaItem className="cliente-destaque" icon={<Briefcase size={20} />} label="Cliente" value={clienteNome} />
                    <MetaItem icon={<Tag size={20} />} label="Status" value={formatStatus(item.status)} />
                    <MetaItem icon={<Calendar size={20} />} label="Prazo" value={item.prazo ? new Date(item.prazo).toLocaleDateString('pt-BR', {timeZone: 'UTC'}) : 'N/D'} />
                    <MetaItem icon={<User size={20} />} label="Responsável" value={item.responsavel || 'N/D'} />
                </div>

                {item.descricao && <p className="tarefa-descricao">{item.descricao}</p>}

                {item.tipo === 'TAREFA' && (
                    <>
                        <TaskActions task={item} onActionSuccess={onActionSuccess} />
                        <div className="comentarios-section">
                            <h4 className="comentarios-titulo">Comentários</h4>
                            <div className="comentarios-timeline">
                                {isLoading ? <Spinner /> : (
                                    comentarios.length > 0 ? (
                                        comentarios.map(c => (
                                            <div key={c.id} className={`comentario-item ${c.isOptimistic ? 'optimistic' : ''}`}>
                                                <div className="comentario-avatar">{getInitials(c.autor)}</div>
                                                <div className="comentario-content">
                                                    <div className="comentario-header">
                                                        <span className="comentario-autor">{c.autor}</span>
                                                        <span className="comentario-data">{formatDateTime(c.dataCriacao)}</span>
                                                    </div>
                                                    <div className="comentario-texto">{c.texto}</div>
                                                </div>
                                            </div>
                                        ))
                                    ) : <p className="sem-comentarios">Nenhum comentário ainda. Seja o primeiro a comentar!</p>
                                )}
                                <div ref={commentsEndRef} />
                            </div>
                            <form onSubmit={handleAddComment} className="comentario-form">
                                <div className="comentario-input-wrapper">
                                    <input
                                        type="text"
                                        value={novoComentario}
                                        onChange={(e) => setNovoComentario(e.target.value)}
                                        placeholder="Adicione um comentário..."
                                        disabled={isActionLoading}
                                    />
                                    <button type="submit" className="btn-send" disabled={isActionLoading || !novoComentario.trim()}>
                                        {isActionLoading ? <Spinner size={18}/> : <Send size={18} />}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </>
                )}
            </div>
            <div className="modal-actions">
                <button type="button" className="btn-secundario" onClick={onRequestClose}>Fechar</button>
            </div>
        </>
    );
}