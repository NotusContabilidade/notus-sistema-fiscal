import React from "react";

export default function TaskHistoricoCliente({ historico }) {
  if (!historico || historico.length === 0) return <div>Sem histórico.</div>;
  return (
    <ul style={{ marginTop: 8 }}>
      {historico.map((h, i) => (
        <li key={i}>{h}</li>
      ))}
    </ul>
  );
}