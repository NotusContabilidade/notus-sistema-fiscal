import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api'
import { toast } from 'react-toastify';
import { IMaskInput } from 'react-imask';
import Spinner from '../components/Spinner';
import { UserPlus, Search } from 'lucide-react';
import '../styles/pages/BuscaCliente.css';

function BuscaCliente() {
  const [cnpj, setCnpj] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleBuscarCliente = async (e) => {
    e.preventDefault();
    const cnpjLimpo = cnpj.replace(/\D/g, '');

    if (cnpjLimpo.length !== 14) {
      toast.warn('Por favor, digite um CNPJ completo.');
      return;
    }

    setIsLoading(true);
    try {
      const response = await api.get(`http://localhost:8080/api/clientes?cnpj=${cnpjLimpo}`);
      if (response.data && response.data.parametros) {
        toast.success(`Cliente ${response.data.cliente.razaoSocial} encontrado!`);
        navigate(`/clientes/${response.data.cliente.id}/dashboard`);
      } else if (response.data && response.data.cliente) {
        toast.info('Cliente encontrado, mas os parâmetros fiscais precisam ser cadastrados.');
        navigate(`/clientes/novo`, { 
            state: { 
                cnpj: cnpj,
                razaoSocial: response.data.cliente.razaoSocial 
            } 
        });
      } else {
        throw new Error("Resposta inválida da API");
      }
    } catch (error) {
      toast.info('Cliente não encontrado. Redirecionando para cadastro...');
      navigate(`/clientes/novo`, { state: { cnpj: cnpj } });
    } finally {
      setIsLoading(false);
    }
  };

  const handleNovoCliente = () => {
    navigate('/clientes/novo');
  };

  return (
    <div className="busca-cliente-saas">
      <h1 className="busca-titulo-saas">Buscar Cliente</h1>
      <div className="busca-card-saas">
        <form onSubmit={handleBuscarCliente} className="busca-form-saas">
          <label htmlFor="cnpj-busca" className="label-cnpj-saas">CNPJ do cliente</label>
          <div className="input-group-saas">
            <IMaskInput
              id="cnpj-busca"
              mask="00.000.000/0000-00"
              value={cnpj}
              onAccept={(value) => setCnpj(value)}
              disabled={isLoading}
              placeholder="Digite o CNPJ"
              autoFocus
            />
            <button type="submit" className="btn-primario-saas" disabled={isLoading}>
              {isLoading ? <Spinner /> : (<><Search size={18} /> Buscar</>)}
            </button>
          </div>
        </form>
        <div className="separador-ou-saas"><span>ou</span></div>
        <button className="btn-secundario-saas" type="button" onClick={handleNovoCliente}>
          <UserPlus size={18} /> Cadastrar novo cliente
        </button>
      </div>
    </div>
  );
}

export default BuscaCliente;