import React from 'react';
import { Clock, AlertTriangle, FileText, CheckSquare, CalendarCheck } from 'lucide-react';
import '../styles/components/WorkflowCard.css';

// Função para calcular a diferença de tempo de forma legível
const formatTimeDiff = (start, end) => {
    if (!start || !end) return null;
    const diff = new Date(end) - new Date(start);
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
    if (days > 0) return `${days}d ${hours}h`;
    if (hours > 0) return `${hours}h`;
    const minutes = Math.floor((diff / 1000 / 60) % 60);
    return `${minutes}m`;
};

// Função auxiliar para determinar o status e a classe CSS correspondente
const getStatusInfo = (item) => {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const isAtrasado = item.prazo && new Date(item.prazo) < hoje && item.status?.toUpperCase() !== 'CONCLUIDO';

    if (isAtrasado) {
        return { text: 'Atrasado', className: 'status-atrasado' };
    }

    switch (item.status?.toUpperCase()) {
        case 'PENDENTE':
            return { text: 'Pendente', className: 'status-pendente' };
        case 'EM_ANDAMENTO':
            return { text: 'Em Andamento', className: 'status-em-andamento' };
        case 'CONCLUIDO':
            return { text: 'Concluído', className: 'status-concluido' };
        default:
            return { text: item.status || 'N/D', className: 'status-default' };
    }
};

// **NOVO**: Função segura para formatar datas, evitando "Invalid Date"
const formatarData = (dataString) => {
    if (!dataString) return 'N/D';
    const data = new Date(dataString);
    // Verifica se a data é válida
    if (isNaN(data.getTime())) {
        return 'Data Inválida';
    }
    return data.toLocaleDateString('pt-BR', { timeZone: 'UTC' });
};


export default function WorkflowCard({ item, onStatusChange, onClick }) {
    const statusInfo = getStatusInfo(item);
    const isAtrasado = statusInfo.className === 'status-atrasado';
    const isConcluido = statusInfo.className === 'status-concluido';
    const tempoResolucao = formatTimeDiff(item.dataCriacao, item.dataConclusao);

    // Impede que o clique no select propague para o card e abra o modal
    const handleSelectClick = (e) => {
        e.stopPropagation();
    };

    // Função para o clique no card, que abre o modal de detalhes
    const handleCardClick = () => {
        if (onClick) {
            onClick(item);
        }
    };

    return (
        <div className={`workflow-card ${isAtrasado ? 'card-atrasado' : ''} ${isConcluido ? 'card-concluido' : ''}`} onClick={handleCardClick}>
            <div className="card-header">
                <span className={`status-tag ${statusInfo.className}`}>{statusInfo.text}</span>
                <div className="card-tipo-icon">
                    {/* Usa ícones diferentes para Tarefa ou Documento */}
                    {item.tipo === 'TAREFA' ? <CheckSquare size={16} /> : <FileText size={16} />}
                    <span>{item.cliente?.razaoSocial || item.clienteNome || 'N/A'}</span>
                </div>
            </div>

            <h4 className="card-titulo">{item.titulo}</h4>

            <div className="card-footer-container">
                <div className="card-footer">
                    {isConcluido ? (
                        <span className="prazo-info concluido">
                            <CalendarCheck size={14} />
                            {/* **CORRIGIDO**: Usa a função segura de formatação */}
                            Concluído em: {formatarData(item.dataConclusao)}
                            {tempoResolucao && ` (${tempoResolucao})`}
                        </span>
                    ) : isAtrasado ? (
                        <span className="prazo-info atrasado">
                            <AlertTriangle size={14} />
                            {/* **CORRIGIDO**: Usa a função segura de formatação */}
                            Venceu em: {formatarData(item.prazo)}
                        </span>
                    ) : (
                        item.prazo && (
                            <span className="prazo-info">
                                <Clock size={14} />
                                {/* **CORRIGIDO**: Usa a função segura de formatação */}
                                Vence em: {formatarData(item.prazo)}
                            </span>
                        )
                    )}
                </div>
                {/* Mostra o seletor de status apenas para tarefas não concluídas */}
                {item.tipo === 'TAREFA' && (
                     <select
                        className="status-select"
                        value={item.status}
                        onChange={(e) => onStatusChange(item.id, e.target.value)}
                        onClick={handleSelectClick}
                    >
                        <option value="PENDENTE">Pendente</option>
                        <option value="EM_ANDAMENTO">Em Andamento</option>
                        <option value="CONCLUIDO">Concluir</option>
                    </select>
                )}
            </div>
        </div>
    );
}