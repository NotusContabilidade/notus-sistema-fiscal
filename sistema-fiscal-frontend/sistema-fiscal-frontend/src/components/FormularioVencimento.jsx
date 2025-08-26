import React, { useState, useEffect } from 'react';
import Modal from 'react-modal';
import axios from 'axios';
import { toast } from 'react-toastify';
import { X } from 'lucide-react';

// Estilos customizados para o Modal
const customStyles = {
  content: {
    top: '50%',
    left: '50%',
    right: 'auto',
    bottom: 'auto',
    marginRight: '-50%',
    transform: 'translate(-50%, -50%)',
    width: '500px',
    borderRadius: '8px',
    padding: '2rem',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
    border: 'none',
  },
  overlay: {
    backgroundColor: 'rgba(0, 0, 0, 0.75)'
  }
};

function FormularioVencimento({ isOpen, onClose, onSave }) {
  const [clientes, setClientes] = useState([]);
  const [formData, setFormData] = useState({
    clienteId: '',
    descricao: '',
    dataVencimento: '',
    status: 'PENDENTE',
  });
  const [isLoading, setIsLoading] = useState(false);

  // Busca a lista de todos os clientes quando o modal é aberto
  useEffect(() => {
    if (isOpen) {
      const fetchClientes = async () => {
        try {
          const response = await axios.get('http://localhost:8080/api/clientes/todos');
          setClientes(response.data);
        } catch (error) {
          toast.error('Não foi possível carregar a lista de clientes.');
        }
      };
      fetchClientes();
    }
  }, [isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setIsLoading(true);
    // Chama a função onSave que foi passada pelo componente pai (Vencimentos.jsx)
    onSave(formData)
      .finally(() => setIsLoading(false));
  };

  return (
    <Modal isOpen={isOpen} onRequestClose={onClose} style={customStyles} contentLabel="Formulário de Novo Vencimento">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{marginTop: 0, color: 'var(--cor-primaria)'}}>Adicionar Novo Vencimento</h2>
        <button onClick={onClose} className="btn-edit" style={{position: 'static'}}><X/></button>
      </div>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="clienteId">Cliente</label>
          <select id="clienteId" name="clienteId" value={formData.clienteId} onChange={handleChange} required>
            <option value="" disabled>Selecione um cliente...</option>
            {clientes.map(cliente => (
              <option key={cliente.id} value={cliente.id}>
                {cliente.razaoSocial}
              </option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="descricao">Descrição</label>
          <input type="text" id="descricao" name="descricao" value={formData.descricao} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label htmlFor="dataVencimento">Data de Vencimento</label>
          <input type="date" id="dataVencimento" name="dataVencimento" value={formData.dataVencimento} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label htmlFor="status">Status</label>
          <select id="status" name="status" value={formData.status} onChange={handleChange} required>
            <option value="PENDENTE">Pendente</option>
            <option value="PAGO">Pago</option>
            <option value="ATRASADO">Atrasado</option>
          </select>
        </div>
        <div className="botoes-acao" style={{paddingTop: '1rem', borderTop: 'none', justifyContent: 'flex-end'}}>
          <button type="button" className="btn-secundario" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn-primario" disabled={isLoading}>{isLoading ? 'Salvando...' : 'Salvar'}</button>
        </div>
      </form>
    </Modal>
  );
}

export default FormularioVencimento;