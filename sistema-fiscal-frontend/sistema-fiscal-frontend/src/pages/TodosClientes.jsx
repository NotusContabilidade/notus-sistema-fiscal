import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';

function TodosClientes() {
  const [clientes, setClientes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchClientes = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/clientes/todos');
        setClientes(response.data);
      } catch (error) {
        toast.error('Não foi possível carregar a lista de clientes.');
      } finally {
        setIsLoading(false);
      }
    };
    fetchClientes();
  }, []);

  if (isLoading) {
    return <div className="view-container"><Spinner /></div>;
  }

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Todos os Clientes</h1></div>
      <div className="card">
        {clientes.length > 0 ? (
          <table className="lista-detalhes-tabela">
            <thead><tr><th>Razão Social</th><th>CNPJ</th></tr></thead>
            <tbody>
              {clientes.map(cliente => (
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