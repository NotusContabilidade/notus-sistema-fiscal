import React from 'react';
import { FileText, CheckSquare, AlertTriangle } from 'lucide-react';

// Adicionamos a propriedade 'onClick'
export default function WorkflowCard({ item, onStatusChange, onClick }) { 
  const isAtrasado = item.prazo && new Date(item.prazo) < new Date();

  const handleSelectClick = (e) => {
    e.stopPropagation();
  };
  
  // Adicionamos o evento onClick ao div principal
  return (
    <div className="workflow-card" onClick={() => onClick(item)}> 
      <div className="card-header">
        <span className="card-tipo-icon">
          {item.tipo === 'TAREFA' ? <CheckSquare size={16} /> : <FileText size={16} />}
        </span>
        <span className="card-cliente">{item.clienteNome}</span>
      </div>
      <p className="card-titulo">{item.titulo}</p>
      <div className="card-footer-container">
        <div className={`card-footer ${isAtrasado ? 'atrasado' : ''}`}>
          {isAtrasado && <AlertTriangle size={14} />}
          <span>{item.prazo ? `Vence em: ${new Date(item.prazo).toLocaleDateString()}` : 'Sem prazo'}</span>
        </div>
        {item.tipo === 'TAREFA' && (
          <select 
            value={item.status} 
            onChange={(e) => onStatusChange(item.id, e.target.value)}
            onClick={handleSelectClick} 
            className="status-select"
          >
            <option value="PENDENTE">Pendente</option>
            <option value="EM_ANDAMENTO">Em Andamento</option>
            <option value="CONCLUIDO">Concluído</option>
          </select>
        )}
      </div>
    </div>
  );
}