import React from 'react';
import { PlusCircle, Search } from 'lucide-react';

// ✅ CORREÇÃO: O componente agora recebe props do componente pai.
function ControleVencimentos({ filtro, onFiltroChange }) {
  return (
    <div className="card controle-vencimentos-card">
      <div className="form-group-inline">
        <label htmlFor="filtroCliente">Filtrar por Cliente:</label>
        <div className="input-com-icone">
          <Search size={18} className="input-icone" />
          <input
            type="text"
            id="filtroCliente"
            placeholder="Digite o nome ou CNPJ..."
            // ✅ CORREÇÃO: O valor e a ação de mudança vêm do pai.
            value={filtro}
            onChange={e => onFiltroChange(e.target.value)}
          />
        </div>
      </div>
      
      <button className="btn-primario">
        <PlusCircle size={16} />
        <span>Adicionar Novo Vencimento</span>
      </button>
    </div>
  );
}

export default ControleVencimentos;