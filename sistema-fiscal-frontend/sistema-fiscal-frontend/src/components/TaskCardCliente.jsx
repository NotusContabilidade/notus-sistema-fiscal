import React, { useState } from "react";
import api from "../services/api";
import TaskHistoricoCliente from "./TaskHistoricoCliente";

export default function TaskCardCliente({ task }) {
  const [showHistorico, setShowHistorico] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const handleEnviar = async (canal) => {
    setEnviando(true);
    try {
      await api.post(`/tasks/${task.id}/enviar?canal=${canal}`);
      alert(`Enviado via ${canal === "email" ? "E-mail" : "WhatsApp"}!`);
      // Aqui você pode atualizar o histórico/status se quiser
    } catch (e) {
      alert("Erro ao enviar.");
    }
    setEnviando(false);
  };

  return (
    <div className="task-card-cliente">
      <h4>{task.titulo}</h4>
      <div>Status: <b>{task.status}</b></div>
      <div>
        Anexos:{" "}
        {task.anexos && task.anexos.length > 0
          ? task.anexos.map((a, i) => (
              <a
                href={a}
                target="_blank"
                rel="noopener noreferrer"
                key={i}
                style={{ marginRight: 8 }}
              >
                {a.split("/").pop()}
              </a>
            ))
          : <span>Nenhum</span>}
      </div>
      <button onClick={() => setShowHistorico(h => !h)}>
        {showHistorico ? "Ocultar Histórico" : "Ver Histórico"}
      </button>
      {showHistorico && <TaskHistoricoCliente historico={task.historico} />}
      <div>
        <button
          disabled={enviando}
          onClick={() => handleEnviar("email")}
          style={{ marginRight: 8 }}
        >
          {enviando ? "Enviando..." : "Enviar por E-mail"}
        </button>
        <button
          disabled={enviando}
          onClick={() => handleEnviar("whatsapp")}
        >
          {enviando ? "Enviando..." : "Enviar por WhatsApp"}
        </button>
      </div>
    </div>
  );
}