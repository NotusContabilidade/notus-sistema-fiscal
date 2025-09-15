import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api'
import { toast } from 'react-toastify';
import { Users, FileWarning, DollarSign, Search } from 'lucide-react';
import Spinner from '../components/Spinner';
import '../styles/pages/DashboardGeral.css';

function StatCard({ icon, title, value, color, onClick, as = 'div', to }) {
  const Tag = to ? Link : as;
  const props = to
    ? { to, className: 'stat-card-link', style: { borderLeftColor: color, textDecoration: 'none' } }
    : { className: 'stat-card', style: { borderLeftColor: color, cursor: onClick ? 'pointer' : 'default' }, onClick };

  return (
    <Tag {...props}>
      <div className="stat-card-icon">{icon}</div>
      <div className="stat-card-info">
        <span className="stat-card-title">{title}</span>
        <span className="stat-card-value">{value}</span>
      </div>
    </Tag>
  );
}

function DashboardGeral() {
  const [stats, setStats] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [listaVisivel, setListaVisivel] = useState(null);
  const [listaDados, setListaDados] = useState([]);
  const [isLoadingLista, setIsLoadingLista] = useState(false);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const response = await api.get('http://localhost:8080/api/dashboard/stats');
        setStats(response.data);
      } catch (error) {
        toast.error('Não foi possível carregar as estatísticas.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchStats();
  }, []);

  const handleCardClick = async (tipo) => {
    if (listaVisivel === tipo) {
      setListaVisivel(null);
      return;
    }
    setIsLoadingLista(true);
    setListaVisivel(tipo);
    setListaDados([]);
    try {
      const endpoint = tipo === 'pendentes' ? '/api/dashboard/clientes-pendentes' : '/api/dashboard/financeiro-mes';
      const response = await api.get(`http://localhost:8080${endpoint}`);
      setListaDados(response.data);
    } catch (error) {
      toast.error(`Não foi possível carregar a lista de clientes ${tipo}.`);
      setListaVisivel(null);
    } finally {
      setIsLoadingLista(false);
    }
  };

  const formatCurrency = (value) => {
    if (value == null) return "R$ 0,00";
    return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  };

  return (
    <div className="view-container">
      <div className="page-header">
        <h1 className="page-title">Dashboard Gerencial</h1>
        <Link to="/clientes/busca" className="btn-primario">
          <Search size={18} />
          <span>Buscar / Novo</span>
        </Link>
      </div>

      <div className="stats-grid">
        <StatCard
          to="/clientes"
          icon={<Users size={38} color="#3b82f6" />}
          title="Total de Clientes"
          value={isLoading ? '...' : stats?.totalClientes}
          color="#3b82f6"
        />
        <StatCard
          icon={<DollarSign size={38} color="#16a34a" />}
          title="DAS Calculado no Mês"
          value={isLoading ? '...' : formatCurrency(stats?.totalDasNoMes)}
          color="#16a34a"
          onClick={() => handleCardClick('financeiro')}
        />
        <StatCard
          icon={<FileWarning size={38} color="#ef4444" />}
          title="Clientes com Cálculo Pendente"
          value={isLoading ? '...' : stats?.clientesPendentes}
          color="#ef4444"
          onClick={() => handleCardClick('pendentes')}
        />
      </div>

      {listaVisivel && (
        <div className="card lista-detalhes">
          {isLoadingLista ? (
            <Spinner />
          ) : (
            <>
              <h3>{listaVisivel === 'pendentes' ? 'Clientes Pendentes de Cálculo' : 'Financeiro do Mês'}</h3>
              <table className="lista-detalhes-tabela">
                <thead>
                  <tr>
                    <th>Cliente</th>
                    {listaVisivel === 'financeiro' && <th>Valor do DAS</th>}
                  </tr>
                </thead>
                <tbody>
                  {listaDados.map(item => (
                    <tr key={item.id}>
                      <td>
                        <Link to={`/clientes/${item.id}/dashboard`}>{item.razaoSocial}</Link>
                      </td>
                      {listaVisivel === 'financeiro' && <td>{formatCurrency(item.dasTotal)}</td>}
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </div>
      )}
    </div>
  );
}

export default DashboardGeral;