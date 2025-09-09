import React, { useEffect, useState } from 'react'; // Adicionado useState para a lista de clientes
import Modal from 'react-modal';
import api from '../services/api'
import { toast } from 'react-toastify';
import { X, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';

// Estilos customizados (sem alteração)
const customStyles = {
  content: { top: '50%', left: '50%', right: 'auto', bottom: 'auto', marginRight: '-50%', transform: 'translate(-50%, -50%)', width: '500px', borderRadius: '8px', padding: '0', boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)', border: 'none', overflow: 'hidden' },
  overlay: { backgroundColor: 'rgba(0, 0, 0, 0.75)', zIndex: 1100 }
};

function FormularioVencimento({ isOpen, onClose, onSave, onDelete, vencimentoParaEditar }) {
  const [clientes, setClientes] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  const { register, handleSubmit, formState: { errors }, reset } = useForm({
    mode: 'onBlur',
  });

  const isEditing = !!vencimentoParaEditar;
  
  // ✅ CORREÇÃO: A função 'format' foi adicionada de volta aqui.
  // Ela é necessária para garantir que a data seja passada para o input no formato AAAA-MM-DD.
  const format = (date) => {
    const d = new Date(date);
    // Adiciona 1 ao dia para corrigir problemas de fuso horário que fazem a data voltar um dia
    d.setDate(d.getDate() + 1);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  useEffect(() => {
    if (isOpen) {
      if (isEditing) {
        const descricaoLimpa = vencimentoParaEditar.title.split(' - ')[0];
        reset({
          clienteId: vencimentoParaEditar.clienteId || '',
          descricao: descricaoLimpa || '',
          dataVencimento: format(vencimentoParaEditar.start), // Agora a função 'format' existe
          status: vencimentoParaEditar.status || 'PENDENTE',
        });
      } else {
        reset({
          clienteId: '',
          descricao: '',
          dataVencimento: '',
          status: 'PENDENTE',
        });
      }

      const fetchClientes = async () => {
        try {
          const response = await api.get('http://localhost:8080/api/clientes/todos');
          setClientes(response.data);
        } catch (error) {
          toast.error('Não foi possível carregar a lista de clientes.');
        }
      };
      fetchClientes();
    }
  }, [isOpen, vencimentoParaEditar, isEditing, reset]);

  const onFormSubmit = (data) => {
    setIsLoading(true);
    onSave(data, vencimentoParaEditar?.id)
      .catch(() => {})
      .finally(() => setIsLoading(false));
  };

  const handleDelete = () => {
      if (window.confirm("Tem certeza que deseja excluir este vencimento?")) {
          setIsLoading(true);
          onDelete(vencimentoParaEditar.id)
              .finally(() => setIsLoading(false));
      }
  };

  return (
    <Modal isOpen={isOpen} onRequestClose={onClose} style={customStyles}>
      <div className="modal-header">
        <h3>{isEditing ? 'Editar Vencimento' : 'Adicionar Novo Vencimento'}</h3>
        <button onClick={onClose} className="btn-close-modal"><X size={20}/></button>
      </div>
      <form onSubmit={handleSubmit(onFormSubmit)}>
        <div className="modal-body">
          <div className="form-group">
            <label htmlFor="clienteId">Cliente</label>
            <select
              id="clienteId"
              {...register("clienteId", { required: "Por favor, selecione um cliente." })}
            >
              <option value="" disabled>Selecione um cliente...</option>
              {clientes.map(cliente => (
                <option key={cliente.id} value={cliente.id}>{cliente.razaoSocial}</option>
              ))}
            </select>
            {errors.clienteId && <p className="erro-mensagem-form">{errors.clienteId.message}</p>}
          </div>
          <div className="form-group">
            <label htmlFor="descricao">Descrição</label>
            <input
              type="text"
              id="descricao"
              {...register("descricao", { required: "A descrição é obrigatória." })}
            />
            {errors.descricao && <p className="erro-mensagem-form">{errors.descricao.message}</p>}
          </div>
          <div className="form-group">
            <label htmlFor="dataVencimento">Data de Vencimento</label>
            <input
              type="date"
              id="dataVencimento"
              {...register("dataVencimento", { required: "A data é obrigatória." })}
            />
            {errors.dataVencimento && <p className="erro-mensagem-form">{errors.dataVencimento.message}</p>}
          </div>
          <div className="form-group">
            <label htmlFor="status">Status</label>
            <select
              id="status"
              {...register("status", { required: "O status é obrigatório." })}
            >
              <option value="PENDENTE">Pendente</option>
              <option value="PAGO">Pago</option>
              <option value="ATRASADO">Atrasado</option>
            </select>
            {errors.status && <p className="erro-mensagem-form">{errors.status.message}</p>}
          </div>
        </div>
        <div className="modal-actions">
          {isEditing && (
            <button type="button" className="btn-secundario" onClick={handleDelete} disabled={isLoading} style={{ marginRight: 'auto', backgroundColor: '#ef4444' }}>
              <Trash2 size={16}/> Excluir
            </button>
          )}
          <button type="button" className="btn-secundario" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn-primario" disabled={isLoading}>{isLoading ? 'Salvando...' : 'Salvar'}</button>
        </div>
      </form>
    </Modal>
  );
}

export default FormularioVencimento;