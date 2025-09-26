import React, { useEffect, useState, useCallback } from "react";
import { useParams } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import api from "../services/api"; // CORRIGIDO: O caminho era "../../services/api"
import DocumentUploader from "../components/DocumentUploader";
import DocumentList from "../components/DocumentList";
import Spinner from "../components/Spinner";
import "../styles/pages/PortalCliente.css"; // CORRIGIDO: O caminho era "../../styles/pages/PortalCliente.css"

// Função para pegar a Role do usuário a partir do Token
const getUserRole = () => {
  const token = localStorage.getItem("token");
  if (!token) return null;
  try {
    const decodedToken = jwtDecode(token);
    // O nome da claim de role no seu JWT é "role"
    return decodedToken.role;
  } catch (error) {
    console.error("Erro ao decodificar token:", error);
    return null;
  }
};

export default function PortalCliente() {
  const { clienteId } = useParams(); // Pega o ID do cliente da URL
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [userRole] = useState(getUserRole());
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const fetchTasks = useCallback(() => {
    // LÓGICA INTELIGENTE:
    // Se a role for de contador/admin E tiver um clienteId na URL, use o novo endpoint.
    // Senão, usa o endpoint padrão do cliente (para o caso de um cliente logar no futuro).
    const endpoint = (userRole === 'ROLE_ADMIN' || userRole === 'ROLE_CONTADOR') && clienteId
      ? `/tasks/por-cliente/${clienteId}`
      : "/tasks/cliente/me";

    setLoading(true);
    api.get(endpoint).then(res => {
      setTasks(res.data);
    }).catch(err => {
      console.error("Erro ao buscar tarefas", err);
      setTasks([]);
    }).finally(() => {
      setLoading(false);
    });
  }, [clienteId, userRole]);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks, refreshTrigger]);
  
  const handleUploadSuccess = () => {
    // Força a atualização da lista de documentos (e tarefas, se necessário)
    setRefreshTrigger(prev => prev + 1);
  };

  if (loading) return <div className="portal-cliente-container card"><Spinner /></div>;

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Visão do Portal do Cliente</h1></div>
      
      <div className="card portal-cliente-container">
        <div className="portal-section">
          <h3>Tarefas do Cliente</h3>
          {tasks.length === 0 ? (
            <p>Nenhuma tarefa encontrada para este cliente.</p>
          ) : (
            tasks.map(task => (
              <div key={task.id} className="task-card-cliente">
                <h4>{task.titulo}</h4>
                <p>Status: <strong>{task.status}</strong></p>
                <p>Prazo: {task.prazo ? new Date(task.prazo).toLocaleDateString() : 'N/D'}</p>
              </div>
            ))
          )}
        </div>

        {/* Renderiza a seção de documentos, pois estamos na visão do contador */}
        <div className="portal-section">
          <h3>Documentos</h3>
          <DocumentUploader clienteId={clienteId} onUpload={handleUploadSuccess} />
          <DocumentList key={refreshTrigger} clienteId={clienteId} />
        </div>
      </div>
    </div>
  );
}