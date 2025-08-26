import React from 'react';
import { PlusCircle, Search } from 'lucide-react';

// Agora o componente recebe a função onAdicionarClick
function ControleVencimentos({ filtro, onFiltroChange, onAdicionarClick }) {
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
            value={filtro}
            onChange={e => onFiltroChange(e.target.value)}
          />
        </div>
      </div>
      
      {/* O clique neste botão agora chama a função passada pelo pai */}
      <button className="btn-primario" onClick={onAdicionarClick}>
        <PlusCircle size={16} />
        <span>Adicionar Novo Vencimento</span>
      </button>
    </div>
  );
}

export default ControleVencimentos;