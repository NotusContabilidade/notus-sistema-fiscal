import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api'
import { toast } from 'react-toastify';
import { IMaskInput } from 'react-imask';
import Spinner from '../components/Spinner';
import { UserPlus, Search } from 'lucide-react';
import '../styles/pages/BuscaCliente.css';

function useDebounce(value, delay) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const handler = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);
  return debounced;
}

function BuscaCliente() {
  const [busca, setBusca] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [sugestoes, setSugestoes] = useState([]);
  const [showSugestoes, setShowSugestoes] = useState(false);
  const debouncedBusca = useDebounce(busca, 350);
  const navigate = useNavigate();
  const inputRef = useRef();

  // Autocomplete razão social
  useEffect(() => {
    if (debouncedBusca.length < 2 || /^\d/.test(debouncedBusca)) {
      setSugestoes([]);
      return;
    }
    let cancelado = false;
    api.get(`/clientes/busca?razaoSocial=${encodeURIComponent(debouncedBusca)}`)
      .then(res => {
        if (!cancelado) setSugestoes(res.data || []);
      })
      .catch(() => setSugestoes([]));
    return () => { cancelado = true; };
  }, [debouncedBusca]);

  const handleBuscarCliente = async (e) => {
    e.preventDefault();
    const cnpjLimpo = busca.replace(/\D/g, '');

    if (!busca.trim()) {
      toast.warn('Digite o CNPJ ou parte da razão social.');
      return;
    }

    // Se for CNPJ, busca por CNPJ
    if (cnpjLimpo.length === 14) {
      setIsLoading(true);
      try {
        const response = await api.get(`/clientes?cnpj=${cnpjLimpo}`);
        if (response.data && response.data.parametros) {
          toast.success(`Cliente ${response.data.cliente.razaoSocial} encontrado!`);
          navigate(`/clientes/${response.data.cliente.id}/dashboard`);
        } else if (response.data && response.data.cliente) {
          toast.info('Cliente encontrado, mas os parâmetros fiscais precisam ser cadastrados.');
          navigate(`/clientes/novo`, { 
              state: { 
                  cnpj: busca,
                  razaoSocial: response.data.cliente.razaoSocial 
              } 
          });
        } else {
          throw new Error("Resposta inválida da API");
        }
      } catch (error) {
        toast.info('Cliente não encontrado. Redirecionando para cadastro...');
        navigate(`/clientes/novo`, { state: { cnpj: busca } });
      } finally {
        setIsLoading(false);
      }
    } else {
      // Se não for CNPJ, tenta autocomplete
      setShowSugestoes(true);
      if (sugestoes.length === 1) {
        navigate(`/clientes/${sugestoes[0].id}/dashboard`);
      } else if (sugestoes.length === 0) {
        toast.info('Nenhum cliente encontrado com esse nome.');
      }
    }
  };

  const handleNovoCliente = () => {
    navigate('/clientes/novo');
  };

  const handleInputChange = (e) => {
    setBusca(e.target.value);
    setShowSugestoes(true);
  };

  const handleSugestaoClick = (cliente) => {
    setShowSugestoes(false);
    setBusca('');
    navigate(`/clientes/${cliente.id}/dashboard`);
  };

  const handleBlur = () => {
    setTimeout(() => setShowSugestoes(false), 150); // permite clique
  };

  return (
    <div className="busca-cliente-saas">
      <h1 className="busca-titulo-saas">Buscar Cliente</h1>
      <div className="busca-card-saas">
        <form onSubmit={handleBuscarCliente} className="busca-form-saas" autoComplete="off">
          <label htmlFor="busca-cliente" className="label-cnpj-saas">CNPJ ou Razão Social</label>
          <div className="input-group-saas" style={{ position: 'relative' }}>
            <input
              id="busca-cliente"
              ref={inputRef}
              type="text"
              value={busca}
              onChange={handleInputChange}
              onFocus={() => setShowSugestoes(true)}
              onBlur={handleBlur}
              placeholder="Digite o CNPJ ou parte do nome"
              disabled={isLoading}
              autoFocus
              style={{ background: 'inherit' }}
            />
            <button type="submit" className="btn-primario-saas" disabled={isLoading}>
              {isLoading ? <Spinner /> : (<><Search size={18} /> Buscar</>)}
            </button>
            {showSugestoes && sugestoes.length > 0 && (
              <ul
                style={{
                  position: 'absolute',
                  left: 0,
                  right: 0,
                  top: '110%',
                  background: 'var(--card-bg, #fff)',
                  border: '1.5px solid #a13751',
                  borderRadius: 6,
                  zIndex: 10,
                  maxHeight: 220,
                  overflowY: 'auto',
                  boxShadow: '0 2px 8px 0 #a1375133',
                  margin: 0,
                  padding: 0,
                  listStyle: 'none'
                }}
              >
                {sugestoes.map(cliente => (
                  <li
                    key={cliente.id}
                    style={{
                      padding: '0.7rem 1rem',
                      cursor: 'pointer',
                      background: '#fff'
                    }}
                    onMouseDown={() => handleSugestaoClick(cliente)}
                  >
                    <strong>{cliente.razaoSocial}</strong>
                    <div style={{ fontSize: '0.95em', color: '#a13751' }}>{cliente.cnpj}</div>
                  </li>
                ))}
              </ul>
            )}
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