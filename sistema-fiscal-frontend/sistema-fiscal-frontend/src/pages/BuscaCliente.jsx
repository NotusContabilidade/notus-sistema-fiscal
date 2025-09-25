import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { toast } from 'react-toastify';
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
  const debouncedBusca = useDebounce(busca, 300);
  const navigate = useNavigate();
  const wrapperRef = useRef(null);

  // CORREÇÃO: Lógica de busca unificada, igual ao ClienteSearchableSelect
  useEffect(() => {
    if (debouncedBusca.length < 2) {
      setSugestoes([]);
      setIsLoading(false);
      return;
    }
    
    let cancelado = false;
    setIsLoading(true);
    // USA O ENDPOINT GENÉRICO '/busca?q='
    api.get(`/clientes/busca?q=${encodeURIComponent(debouncedBusca)}`)
      .then(res => {
        if (!cancelado) setSugestoes(res.data || []);
      })
      .catch(() => {
        if (!cancelado) setSugestoes([]);
      })
      .finally(() => {
        if (!cancelado) setIsLoading(false);
      });

    return () => { cancelado = true; };
  }, [debouncedBusca]);

  // Fecha o dropdown ao clicar fora
  useEffect(() => {
    function handleClickOutside(event) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setShowSugestoes(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [wrapperRef]);

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
    toast.success(`Acessando cliente ${cliente.razaoSocial}...`);
    navigate(`/clientes/${cliente.id}/dashboard`);
  };

  return (
    <div className="busca-cliente-saas">
      <h1 className="busca-titulo-saas">Buscar Cliente</h1>
      <div className="busca-card-saas" ref={wrapperRef}>
        <div className="busca-form-saas">
          <label htmlFor="busca-cliente" className="label-cnpj-saas">CNPJ ou Razão Social</label>
          <div className="input-group-saas">
            <input
              id="busca-cliente"
              type="text"
              value={busca}
              onChange={handleInputChange}
              onFocus={() => setShowSugestoes(true)}
              placeholder="Digite o CNPJ ou parte do nome"
              disabled={isLoading}
              autoFocus
              autoComplete="off"
            />
            {/* O botão agora é apenas um indicador visual */}
            <div className="btn-primario-saas-indicator">
              {isLoading ? <Spinner /> : <Search size={18} />}
            </div>
            
            {/* Dropdown de sugestões */}
            {showSugestoes && (
              <ul className="sugestoes-dropdown-saas">
                {isLoading ? (
                  <li className="sugestao-mensagem"><Spinner size="sm" /> Carregando...</li>
                ) : debouncedBusca.length > 0 && debouncedBusca.length < 2 ? (
                  <li className="sugestao-mensagem">Digite 2 ou mais caracteres...</li>
                ) : sugestoes.length > 0 ? (
                  sugestoes.map(cliente => (
                    <li
                      key={cliente.id}
                      className="sugestao-item-saas"
                      onMouseDown={() => handleSugestaoClick(cliente)}
                    >
                      <span className="sugestao-nome">{cliente.razaoSocial}</span>
                      <span className="sugestao-cnpj">{cliente.cnpj}</span>
                    </li>
                  ))
                ) : debouncedBusca.length >= 2 ? (
                  <li className="sugestao-mensagem">Nenhum cliente encontrado.</li>
                ) : (
                  <li className="sugestao-mensagem">Comece a digitar para buscar.</li>
                )}
              </ul>
            )}
          </div>
        </div>
        <div className="separador-ou-saas"><span>ou</span></div>
        <button className="btn-secundario-saas" type="button" onClick={handleNovoCliente}>
          <UserPlus size={18} /> Cadastrar novo cliente
        </button>
      </div>
    </div>
  );
}

export default BuscaCliente;