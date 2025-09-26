// src/components/DocumentUploader.jsx

import React, { useState, useRef } from "react";
import api from "../services/api";
import Spinner from "./Spinner";
import { UploadCloud, File, XCircle } from 'lucide-react';
import { toast } from 'react-toastify';

const DocumentUploader = ({ clienteId, onUpload }) => {
  const [file, setFile] = useState(null);
  const [tipoDocumento, setTipoDocumento] = useState("");
  const [loading, setLoading] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileChange = (files) => {
    if (files && files[0]) {
      setFile(files[0]);
    }
  };

  const handleDragEvents = (e, dragging) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(dragging);
  };

  const handleDrop = (e) => {
    handleDragEvents(e, false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFileChange(e.dataTransfer.files);
      e.dataTransfer.clearData();
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file || !tipoDocumento) {
      toast.warn("Selecione um arquivo e defina o tipo do documento.");
      return;
    }
    setLoading(true);
    const formData = new FormData();
    formData.append("file", file);
    formData.append("tipoDocumento", tipoDocumento);
    formData.append("usuarioUpload", localStorage.getItem("user_nome") || "usuario");
    formData.append("clienteId", clienteId);

    try {
      await api.post("/documentos", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      toast.success("Documento enviado com sucesso!");
      setFile(null);
      setTipoDocumento("");
      if (onUpload) onUpload();
    } catch (err) {
      toast.error("Erro ao enviar documento.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="document-uploader-form" onSubmit={handleSubmit}>
      <div
        className={`dropzone-area ${isDragging ? 'is-dragging' : ''}`}
        onClick={() => fileInputRef.current.click()}
        onDragEnter={(e) => handleDragEvents(e, true)}
        onDragLeave={(e) => handleDragEvents(e, false)}
        onDragOver={(e) => handleDragEvents(e, true)}
        onDrop={handleDrop}
      >
        <input
          type="file"
          ref={fileInputRef}
          onChange={(e) => handleFileChange(e.target.files)}
          accept="application/pdf,image/*,.doc,.docx,.xml,.xls,.xlsx"
          style={{ display: 'none' }}
        />
        {file ? (
          <div className="file-preview">
            <File size={40} />
            <span className="file-name">{file.name}</span>
            <button type="button" className="btn-remove-file" onClick={(e) => { e.stopPropagation(); setFile(null); }}>
              <XCircle size={20} />
            </button>
          </div>
        ) : (
          <div className="dropzone-prompt">
            <UploadCloud size={48} />
            <p><strong>Arraste e solte</strong> um arquivo aqui, ou <strong>clique para selecionar</strong>.</p>
          </div>
        )}
      </div>

      <div className="form-group">
        <label>Tipo do documento</label>
        <input
          type="text"
          placeholder="Ex: Guia DAS, Contrato Social..."
          value={tipoDocumento}
          onChange={(e) => setTipoDocumento(e.target.value)}
          required
        />
      </div>
      <button type="submit" className="btn-primario" disabled={loading || !file || !tipoDocumento}>
        {loading ? <Spinner /> : "Enviar Documento"}
      </button>
    </form>
  );
};

export default DocumentUploader;