import React, { useState, useEffect } from "react";
import api from "../services/api";

export default function TaskForm({ task, onSaved, onCancel }) {
  const [titulo, setTitulo] = useState("");
  const [descricao, setDescricao] = useState("");

  useEffect(() => {
    if (task) {
      setTitulo(task.titulo);
      setDescricao(task.descricao);
    } else {
      setTitulo("");
      setDescricao("");
    }
  }, [task]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (task) {
      await api.put(`/tasks/${task.id}`, { titulo, descricao });
    } else {
      await api.post("/tasks", { titulo, descricao });
    }
    onSaved();
  };

  return (
    <form onSubmit={handleSubmit}>
      <h3>{task ? "Editar Task" : "Nova Task"}</h3>
      <input
        type="text"
        placeholder="Título"
        value={titulo}
        onChange={(e) => setTitulo(e.target.value)}
        required
        style={{ width: "100%", marginBottom: 8 }}
      />
      <textarea
        placeholder="Descrição"
        value={descricao}
        onChange={(e) => setDescricao(e.target.value)}
        style={{ width: "100%", marginBottom: 8 }}
      />
      <button type="submit">{task ? "Salvar" : "Criar"}</button>
      {onCancel && (
        <button type="button" onClick={onCancel} style={{ marginLeft: 8 }}>
          Cancelar
        </button>
      )}
    </form>
  );
}