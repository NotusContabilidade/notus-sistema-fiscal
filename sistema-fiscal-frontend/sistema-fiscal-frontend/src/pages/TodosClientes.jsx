import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api'
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import '../styles/pages/TodosClientes.css';

function useDebounce(value, delay) {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const handler = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);
  return debounced;
}

function TodosClientes() {
  const [clientes, setClientes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // Novos estados para busca/autocomplete
  const [busca, setBusca] = useState('');
  const [sugestoes, setSugestoes] = useState([]);
  const [showSugestoes, setShowSugestoes] = useState(false);
  const debouncedBusca = useDebounce(busca, 350);
  const navigate = useNavigate();
  const inputRef = useRef();

  useEffect(() => {
    const fetchClientes = async () => {
      try {
        const response = await api.get('/clientes/todos');
        setClientes(response.data);
      } catch (error) {
        toast.error('Não foi possível carregar a lista de clientes.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchClientes();
  }, []);

  // Busca por razão social (autocomplete)
  useEffect(() => {
    if (debouncedBusca.length < 2) {
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
    setTimeout(() => setShowSugestoes(false), 150); // para permitir clique
  };

  if (isLoading) {
    return <div className="view-container"><Spinner /></div>;
  }

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Todos os Clientes</h1></div>
      <div className="card">
        <div style={{ marginBottom: 24, position: 'relative', maxWidth: 400 }}>
          <input
            ref={inputRef}
            type="text"
            placeholder="Buscar por razão social ou CNPJ"
            value={busca}
            onChange={handleInputChange}
            onFocus={() => setShowSugestoes(true)}
            onBlur={handleBlur}
            style={{
              width: '100%',
              padding: '0.7rem 1rem',
              borderRadius: 6,
              border: '1.5px solid #a13751',
              fontSize: '1.08rem'
            }}
          />
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
        {clientes.length > 0 ? (
          <table className="lista-detalhes-tabela">
            <thead><tr><th>Razão Social</th><th>CNPJ</th></tr></thead>
            <tbody>
              {clientes
                .filter(cliente =>
                  !busca ||
                  cliente.razaoSocial.toLowerCase().includes(busca.toLowerCase()) ||
                  cliente.cnpj.replace(/\D/g, '').includes(busca.replace(/\D/g, ''))
                )
                .map(cliente => (
                  <tr key={cliente.id}>
                    <td><Link to={`/clientes/${cliente.id}/dashboard`}>{cliente.razaoSocial}</Link></td>
                    <td>{cliente.cnpj}</td>
                  </tr>
                ))}
            </tbody>
          </table>
        ) : <p>Nenhum cliente cadastrado.</p>}
      </div>
    </div>
  );
}

export default TodosClientes;