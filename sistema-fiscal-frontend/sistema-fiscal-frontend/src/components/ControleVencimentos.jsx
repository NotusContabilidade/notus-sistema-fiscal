import React from 'react';
import { PlusCircle, Search } from 'lucide-react';

function ControleVencimentos({ filtro, onFiltroChange, onAdicionarClick }) {
  return (
    <div className="card controle-vencimentos-card">
      <div className="controle-vencimentos-row">
        <div className="form-group-inline">
          <label htmlFor="filtroCliente">Filtrar por Cliente:</label>
          <div className="input-com-icone">
            <Search size={18} className="input-icone" />
            <input
              type="text"
              id="filtroCliente"
              placeholder="Digite o nome ou CNPJ..."
              value={filtro}
              onChange={e => onFiltroChange(e.target.value)}
            />
          </div>
        </div>
        <button className="btn-primario btn-adicionar-vencimento" onClick={onAdicionarClick}>
          <PlusCircle size={16} />
          <span>Adicionar Novo Vencimento</span>
        </button>
      </div>
    </div>
  );
}

export default ControleVencimentos;