import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
// 1. Nova importação da biblioteca 'react-imask'
import { IMaskInput } from 'react-imask';
import Spinner from '../components/Spinner';

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
      const response = await axios.get(`http://localhost:8080/api/clientes?cnpj=${cnpjLimpo}`);
      toast.success(`Cliente ${response.data.cliente.razaoSocial} encontrado!`);
      navigate(`/clientes/${response.data.cliente.id}/dashboard`);
    } catch (error) {
      toast.info('Cliente não encontrado. Redirecionando para cadastro...');
      navigate(`/clientes/novo`, { state: { cnpj: cnpj } });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="view-container">
      <div className="card welcome-card">
        <h3>Bem-vindo ao Nótus Sistema Fiscal</h3>
        <p>Sua plataforma para gestão contábil. Use a busca para encontrar um cliente ou cadastrar um novo.</p>
      </div>
      <div className="card">
        <form onSubmit={handleBuscarCliente}>
          <div className="input-group">
            {/* 2. Uso do novo componente IMaskInput */}
            <IMaskInput
              mask="00.000.000/0000-00" // A máscara agora usa '0' para dígitos
              value={cnpj}
              // O evento correto para atualizar o estado é 'onAccept'
              // Ele entrega o valor já sem a máscara, mas vamos usar o onComplete para pegar o valor completo
              onAccept={(value) => setCnpj(value)}
              disabled={isLoading}
              placeholder="Digite o CNPJ para buscar"
              // As classes e outras props do input normal funcionam aqui
              className="form-group-input" // Você pode criar essa classe no CSS se quiser
            />
            <button type="submit" className="btn-primario" disabled={isLoading}>
              {isLoading ? <Spinner /> : 'Buscar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default BuscaCliente;