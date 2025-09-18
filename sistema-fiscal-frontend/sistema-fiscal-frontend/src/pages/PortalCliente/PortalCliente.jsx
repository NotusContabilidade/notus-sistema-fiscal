import React, { useEffect, useState } from "react";
import api from "../../services/api";
import TaskList from "../../components/TaskList";
import "../../styles/pages/PortalCliente.css";

export default function PortalCliente() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Supondo que o cliente já está autenticado e o backend retorna só as tasks dele
    api.get("/tasks/cliente/me").then(res => {
      setTasks(res.data);
      setLoading(false);
    });
  }, []);

  if (loading) return <div>Carregando...</div>;

  return (
    <div className="portal-cliente-container">
      <h2>Minhas Tarefas Fiscais</h2>
      {tasks.length === 0 && <div>Nenhuma tarefa encontrada.</div>}
      <TaskList tasks={tasks} />
    </div>
  );
}