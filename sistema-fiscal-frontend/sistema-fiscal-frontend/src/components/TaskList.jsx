import React, { useEffect, useState } from "react";
import api from "../services/api";

export default function TaskList({ onEdit }) {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      const res = await api.get("/tasks");
      setTasks(res.data);
    } catch {
      setTasks([]);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchTasks();
  }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Deseja excluir esta task?")) return;
    await api.delete(`/tasks/${id}`);
    fetchTasks();
  };

  if (loading) return <div>Carregando...</div>;

  return (
    <div>
      <h2>Minhas Tasks</h2>
      <ul>
        {tasks.map((task) => (
          <li key={task.id}>
            <b>{task.titulo}</b> - {task.descricao}
            <button onClick={() => onEdit(task)}>Editar</button>
            <button onClick={() => handleDelete(task.id)}>Excluir</button>
          </li>
        ))}
      </ul>
    </div>
  );
}