import React, { useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import Spinner from './Spinner';
import { toast } from 'react-toastify';
import { Check, X } from 'lucide-react';

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return 'N/D';
    return new Date(dateTimeString).toLocaleString('pt-BR', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
}

export default function DetalheTarefaModal({ tarefaId, onRequestClose, onActionSuccess }) {
    const [tarefa, setTarefa] = useState(null);
    const [comentarios, setComentarios] = useState([]);
    const [novoComentario, setNovoComentario] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const [isActionLoading, setIsActionLoading] = useState(false);

    const fetchData = useCallback(async () => {
        if (!tarefaId) return;
        setIsLoading(true);
        try {
            const [tarefaRes, comentariosRes] = await Promise.all([
                api.get(`/tasks/${tarefaId}`),
                api.get(`/tasks/${tarefaId}/comments`)
            ]);
            setTarefa(tarefaRes.data);
            setComentarios(comentariosRes.data);
        } catch (error) {
            toast.error("Erro ao carregar dados da demanda.");
            onRequestClose();
        } finally {
            setIsLoading(false);
        }
    }, [tarefaId, onRequestClose]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const handleAddComment = async (e) => {
        e.preventDefault();
        if (!novoComentario.trim()) return;
        const tempComentario = novoComentario;
        setNovoComentario('');
        try {
            const res = await api.post(`/tasks/${tarefaId}/comments`, { texto: tempComentario });
            setComentarios(prev => [...prev, res.data]);
            toast.success('Comentário adicionado!');
        } catch (error) {
            toast.error('Erro ao adicionar comentário.');
            setNovoComentario(tempComentario);
        }
    };

    const handleDocumentAction = async (action) => {
        setIsActionLoading(true);
        try {
            const nomeArquivo = tarefa.titulo.replace("Revisar Documento: ", "");
            const resDocs = await api.get(`/documentos/cliente/${tarefa.clienteId}`);
            const documento = resDocs.data.find(doc => doc.nomeArquivo === nomeArquivo && doc.status === 'PENDENTE');

            if (!documento) throw new Error("Documento correspondente não encontrado ou já processado.");

            const endpoint = `/documentos/${documento.id}/${action}`;
            await api.post(endpoint, { usuarioAprovador: localStorage.getItem("user_nome") || 'sistema', comentario: `Ação via tarefa #${tarefa.id}` });

            toast.success(`Documento ${action === 'aprovar' ? 'aprovado' : 'rejeitado'}!`);
            if (onActionSuccess) onActionSuccess();
            onRequestClose();

        } catch (error) {
            toast.error(error.message || `Erro ao ${action} documento.`);
        } finally {
            setIsActionLoading(false);
        }
    };

    if (isLoading) {
        return <div className="modal-body" style={{minHeight: '300px'}}><Spinner /></div>;
    }
    if (!tarefa) {
        return <div className="modal-body"><p>Não foi possível carregar a demanda.</p></div>;
    }

    const isDocumentReviewTask = tarefa.titulo.startsWith("Revisar Documento:");

    return (
        <>
            <div className="modal-body detalhe-tarefa-modal">
                <h3>{tarefa.titulo}</h3>
                <div className="tarefa-meta">
                    <span><strong>Status:</strong> {tarefa.status}</span>
                    <span><strong>Prazo:</strong> {tarefa.prazo ? new Date(tarefa.prazo).toLocaleDateString('pt-BR', {timeZone: 'UTC'}) : 'N/D'}</span>
                    <span><strong>Responsável:</strong> {tarefa.responsavel || 'N/D'}</span>
                </div>
                <p className="tarefa-descricao">{tarefa.descricao || 'Sem descrição.'}</p>

                <h4>Comentários</h4>
                <div className="comentarios-timeline">
                    {comentarios.length > 0 ? (
                        comentarios.map(c => (
                            <div key={c.id} className="comentario-item">
                                <div className="comentario-header">
                                    <span className="comentario-autor">{c.autor}</span>
                                    <span className="comentario-data">{formatDateTime(c.dataCriacao)}</span>
                                </div>
                                <div className="comentario-texto">{c.texto}</div>
                            </div>
                        ))
                    ) : <p>Nenhum comentário ainda.</p>}
                </div>
                <form onSubmit={handleAddComment} className="comentario-form">
                    <input
                        type="text"
                        value={novoComentario}
                        onChange={(e) => setNovoComentario(e.target.value)}
                        placeholder="Adicione um comentário..."
                    />
                    <button type="submit" className="btn-primario">Comentar</button>
                </form>
            </div>
            <div className="modal-actions">
                {isDocumentReviewTask && (
                    <div className="document-actions">
                        <button className="btn-rejeitar" onClick={() => handleDocumentAction('rejeitar')} disabled={isActionLoading}>
                            {isActionLoading ? <Spinner /> : <><X size={16}/> Rejeitar</>}
                        </button>
                        <button className="btn-aprovar" onClick={() => handleDocumentAction('aprovar')} disabled={isActionLoading}>
                            {isActionLoading ? <Spinner /> : <><Check size={16}/> Aprovar e Concluir</>}
                        </button>
                    </div>
                )}
                <button type="button" className="btn-secundario" onClick={onRequestClose}>Fechar</button>
            </div>
        </>
    );
}