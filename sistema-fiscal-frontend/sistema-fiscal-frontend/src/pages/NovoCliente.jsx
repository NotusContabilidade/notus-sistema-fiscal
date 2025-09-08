import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import { IMaskInput } from 'react-imask';
import '../styles/pages/NovoCliente.css';

function NovoCliente() {
  const navigate = useNavigate();
  const location = useLocation();
  
  const [cnpj, setCnpj] = useState('');
  const [form, setForm] = useState({ razaoSocial: '', rbt12: '', folha12m: '' });
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (location.state?.cnpj) {
      setCnpj(location.state.cnpj);
    }
    if (location.state?.razaoSocial) {
      setForm(prev => ({ ...prev, razaoSocial: location.state.razaoSocial }));
    }
  }, [location.state]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const payload = {
        cnpj: cnpj.replace(/\D/g, ''),
        razaoSocial: form.razaoSocial,
        rbt12: parseFloat(form.rbt12),
        folha12m: parseFloat(form.folha12m)
      };
      const response = await axios.post('http://localhost:8080/api/clientes', payload);
      toast.success('Cliente cadastrado/atualizado com sucesso!');
      
      // ✅ MUDANÇA: Passa o objeto completo (cliente + parâmetros) recebido do backend para a página de dashboard.
      navigate(`/clientes/${response.data.cliente.id}/dashboard`, { 
          state: { clienteData: response.data } 
      });

    } catch (error) {
      if (error.response && error.response.status === 409) {
        toast.error('Este CNPJ já está cadastrado no sistema.');
      } else {
        toast.error('Não foi possível salvar o cliente. Verifique os dados.');
      }
      setIsLoading(false);
    }
  };

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Cadastro de Novo Cliente</h1></div>
      <form className="card" onSubmit={handleSubmit}>
        <div className="form-group">
          <label>CNPJ</label>
          <IMaskInput
            mask="00.000.000/0000-00"
            value={cnpj}
            disabled
          />
        </div>
        <div className="form-group">
          <label>Razão Social</label>
          <input type="text" name="razaoSocial" value={form.razaoSocial} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label>RBT12 (últimos 12 meses)</label>
          <input type="number" step="0.01" name="rbt12" value={form.rbt12} onFocus={e => e.target.select()} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label>Folha de Pagamento (12m)</label>
          <input type="number" step="0.01" name="folha12m" value={form.folha12m} onFocus={e => e.target.select()} onChange={handleChange} required />
        </div>
        <div className="botoes-acao">
          <button type="button" className="btn-secundario" onClick={() => navigate('/')}>Cancelar</button>
          <button type="submit" className="btn-primario" disabled={isLoading}>
            {isLoading ? <Spinner /> : 'Salvar Cliente'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default NovoCliente;