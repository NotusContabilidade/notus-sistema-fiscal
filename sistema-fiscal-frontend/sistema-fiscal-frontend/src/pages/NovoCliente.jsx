import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../services/api'
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import { IMaskInput } from 'react-imask';
import '../styles/pages/NovoCliente.css';

function NovoCliente() {
  const navigate = useNavigate();
  const location = useLocation();
  
  const [cnpj, setCnpj] = useState('');
  const [form, setForm] = useState({
    razaoSocial: '',
    rbt12: '',
    folha12m: '',
    email: '',
    regimeTributario: 'SIMPLES_NACIONAL' // <-- CORREÇÃO: Adicionado com valor padrão
  });
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
        folha12m: parseFloat(form.folha12m),
        email: form.email,
        regimeTributario: form.regimeTributario // <-- CORREÇÃO: Enviando o regime tributário
      };
      const response = await api.post('http://localhost:8080/api/clientes', payload);
      toast.success('Cliente cadastrado com sucesso!');
      // A resposta agora é o ClienteDashboardDTO, então pegamos o ID de dentro do objeto cliente
      navigate(`/clientes/${response.data.cliente.id}/dashboard`);
    } catch (error) {
      const errorMsg = error.response?.data?.erro || 'Não foi possível salvar o cliente. Verifique os dados.';
      toast.error(errorMsg);
      setIsLoading(false);
    }
  };

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Cadastro de Cliente</h1></div>
      <form className="card" onSubmit={handleSubmit}>
        <div className="form-group">
          <label>CNPJ</label>
          <IMaskInput
            mask="00.000.000/0000-00"
            value={cnpj}
            onAccept={setCnpj}
            required
          />
        </div>
        <div className="form-group">
          <label>Razão Social</label>
          <input type="text" name="razaoSocial" value={form.razaoSocial} onChange={handleChange} required />
        </div>
        
        {/* <-- CORREÇÃO: Adicionado campo de seleção para Regime Tributário --> */}
        <div className="form-group">
          <label>Regime Tributário</label>
          <select name="regimeTributario" value={form.regimeTributario} onChange={handleChange} required>
            <option value="SIMPLES_NACIONAL">Simples Nacional</option>
            <option value="LUCRO_PRESUMIDO">Lucro Presumido</option>
            <option value="LUCRO_REAL">Lucro Real</option>
          </select>
        </div>

        <div className="form-group">
          <label>RBT12 (faturamento dos últimos 12 meses)</label>
          <input type="number" step="0.01" name="rbt12" value={form.rbt12} onFocus={e => e.target.select()} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label>Folha de Pagamento (últimos 12 meses)</label>
          <input type="number" step="0.01" name="folha12m" value={form.folha12m} onFocus={e => e.target.select()} onChange={handleChange} required />
        </div>
        <div className="form-group">
          <label>E-mail Principal</label>
          <input
            type="email"
            name="email"
            value={form.email}
            onChange={handleChange}
            required
          />
        </div>
        <div className="botoes-acao">
          <button type="button" className="btn-secundario" onClick={() => navigate(-1)}>Cancelar</button>
          <button type="submit" className="btn-primario" disabled={isLoading}>
            {isLoading ? <Spinner /> : 'Salvar Cliente'}
          </button>
        </div>
      </form>
    </div>
  );
}

export default NovoCliente;