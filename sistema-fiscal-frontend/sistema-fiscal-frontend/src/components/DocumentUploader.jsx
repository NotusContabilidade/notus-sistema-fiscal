// src/components/DocumentUploader.jsx

import React, { useState } from "react";
import api from "../services/api";
import Spinner from "./Spinner";

const DocumentUploader = ({ clienteId, onUpload }) => {
  const [file, setFile] = useState(null);
  const [tipoDocumento, setTipoDocumento] = useState("");
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file || !tipoDocumento) {
      setMsg("Selecione um arquivo e o tipo do documento.");
      return;
    }
    setLoading(true);
    setMsg("");
    const formData = new FormData();
    formData.append("file", file);
    formData.append("tipoDocumento", tipoDocumento);
    formData.append("usuarioUpload", localStorage.getItem("user_nome") || "usuario");
    formData.append("clienteId", clienteId);

    try {
      await api.post("/documentos", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setMsg("Documento enviado com sucesso!");
      setFile(null);
      setTipoDocumento("");
      if (onUpload) onUpload();
    } catch (err) {
      setMsg("Erro ao enviar documento.");
    } finally {
      setLoading(false);
    }
  };

  return (
    // Usando as classes de formulário padrão
    <form className="form-portal" onSubmit={handleSubmit}>
      <h4>Enviar Documento</h4>
      <div className="form-group">
        <label>Arquivo</label>
        <input
          type="file"
          onChange={(e) => setFile(e.target.files[0])}
          accept="application/pdf,image/*"
        />
      </div>
      <div className="form-group">
        <label>Tipo do documento</label>
        <input
          type="text"
          placeholder="Ex: Guia DAS, Contrato Social..."
          value={tipoDocumento}
          onChange={(e) => setTipoDocumento(e.target.value)}
        />
      </div>
      <button type="submit" className="btn-primario" disabled={loading} style={{width: '100%'}}>
        {loading ? <Spinner /> : "Enviar"}
      </button>
      {msg && <div className="form-message">{msg}</div>}
    </form>
  );
};

export default DocumentUploader;