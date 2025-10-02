import React, { useState } from "react";
import api from "../services/api";
import { toast } from 'react-toastify';
import TaskHistoricoCliente from "./TaskHistoricoCliente";
import { Trash2, Send, Clock, CheckCircle, ChevronDown, ChevronUp } from 'lucide-react';
import Spinner from "./Spinner";

// Função auxiliar para formatar a data de forma segura
const formatarData = (dataInput) => {
    // Se a data for nula, indefinida ou vazia, retorna um traço
    if (!dataInput) {
        return '—';
    }
    // Tenta criar um objeto Date
    const data = new Date(dataInput);
    // Verifica se a data criada é válida
    if (isNaN(data.getTime())) {
        return 'Data inválida';
    }
    // Formata a data para o padrão brasileiro
    return data.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
};


export default function TaskCardCliente({ task, onDeleted }) {
    const [showHistorico, setShowHistorico] = useState(false);
    const [isSending, setIsSending] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

    const handleEnviar = async (canal) => {
        setIsSending(true);
        try {
            await api.post(`/tasks/${task.id}/enviar?canal=${canal}`);
            toast.success(`Enviado via ${canal === "email" ? "E-mail" : "WhatsApp"}!`);
        } catch (e) {
            toast.error("Erro ao enviar a tarefa.");
        } finally {
            setIsSending(false);
        }
    };

    const handleExcluir = async () => {
        if (!window.confirm(`Tem certeza que deseja excluir a tarefa "${task.titulo}"? Esta ação não pode ser desfeita.`)) {
            return;
        }
        setIsDeleting(true);
        try {
            await api.delete(`/tasks/${task.id}`);
            toast.success("Tarefa excluída com sucesso!");
            // Chama a função onDeleted passada como prop para atualizar a lista na tela pai
            if (onDeleted) {
                onDeleted(task.id);
            }
        } catch (error) {
            toast.error("Erro ao excluir a tarefa.");
        } finally {
            setIsDeleting(false);
        }
    };

    const dataConclusaoFormatada = formatarData(task.dataConclusao);

    const getStatusInfo = () => {
        switch (task.status) {
            case 'CONCLUIDO':
                return { text: 'Concluído', icon: <CheckCircle size={14} />, color: 'var(--cor-sucesso)' };
            case 'PENDENTE':
                return { text: 'Pendente', icon: <Clock size={14} />, color: '#f59e0b' };
            default:
                return { text: task.status, icon: <Clock size={14} />, color: 'var(--cor-texto-secundario)' };
        }
    };

    const statusInfo = getStatusInfo();

    return (
        <div className="task-card-cliente">
            <div className="task-card-header">
                <h4 className="task-title">{task.titulo}</h4>
                <div className="task-status-badge" style={{ color: statusInfo.color, backgroundColor: `${statusInfo.color}20` }}>
                    {statusInfo.icon}
                    <span>{statusInfo.text}</span>
                </div>
            </div>

            {task.status === "CONCLUIDO" && (
                <div className="task-detail-row">
                    <CheckCircle size={14} className="task-detail-icon" />
                    <span>Concluído em: <strong>{dataConclusaoFormatada}</strong></span>
                </div>
            )}

            <div className="task-detail-row">
                <span>Anexos:</span>
                <div className="task-attachments">
                    {task.anexos && task.anexos.length > 0
                        ? task.anexos.map((anexo, index) => (
                            <a href={anexo} target="_blank" rel="noopener noreferrer" key={index} className="attachment-link">
                                {anexo.split('/').pop()}
                            </a>
                        ))
                        : <span className="no-attachments">Nenhum anexo</span>}
                </div>
            </div>

            <div className="task-card-actions">
                <button className="btn-icon btn-send-email" onClick={() => handleEnviar("email")} disabled={isSending}>
                    <Send size={16} /> Enviar por E-mail
                </button>
                <button className="btn-icon btn-send-whatsapp" onClick={() => handleEnviar("whatsapp")} disabled={isSending}>
                    <Send size={16} /> Enviar por WhatsApp
                </button>
                <button className="btn-icon btn-delete" onClick={handleExcluir} disabled={isDeleting}>
                    {isDeleting ? <Spinner size={16} /> : <Trash2 size={16} />}
                    Excluir
                </button>
            </div>

            <div className="task-history-toggle">
                <button onClick={() => setShowHistorico(h => !h)}>
                    {showHistorico ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                    {showHistorico ? "Ocultar Histórico" : "Ver Histórico"}
                </button>
            </div>

            {showHistorico && <TaskHistoricoCliente historico={task.historico} />}
        </div>
    );
}