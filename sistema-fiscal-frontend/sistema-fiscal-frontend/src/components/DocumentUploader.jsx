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
    formData.append("usuarioUpload", localStorage.getItem("user") || ""); // ajuste conforme seu contexto
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
    <form className="card" onSubmit={handleSubmit}>
      <h4>Enviar Documento</h4>
      <input
        type="file"
        onChange={(e) => setFile(e.target.files[0])}
        accept="application/pdf,image/*"
      />
      <input
        type="text"
        placeholder="Tipo do documento"
        value={tipoDocumento}
        onChange={(e) => setTipoDocumento(e.target.value)}
      />
      <button type="submit" disabled={loading}>
        {loading ? <Spinner /> : "Enviar"}
      </button>
      {msg && <div style={{ marginTop: 8 }}>{msg}</div>}
    </form>
  );
};

export default DocumentUploader;