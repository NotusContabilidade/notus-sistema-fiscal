import React, { useEffect, useState, useCallback } from "react";
import api from "../services/api";
import Spinner from "./Spinner";
import { Download } from 'lucide-react'; // Usando ícones para os botões

const DocumentList = ({ clienteId, onUpload }) => {
  const [docs, setDocs] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchDocs = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get(`/documentos/cliente/${clienteId}`);
      setDocs(res.data);
    } catch {
      console.error("Erro ao buscar documentos.");
    } finally {
      setLoading(false);
    }
  }, [clienteId]);

  useEffect(() => {
    fetchDocs();
  }, [fetchDocs, onUpload]); // Re-executa o fetch quando `onUpload` muda (após um novo upload)

  const handleDownload = async (id, nomeArquivo) => {
    try {
      const res = await api.get(`/documentos/${id}/download`, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", nomeArquivo);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch {
      console.error("Erro ao baixar documento.");
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="document-list-container">
      <h4>Documentos Enviados</h4>
      {docs.length > 0 ? (
        <table className="lista-detalhes-tabela">
          <thead>
            <tr>
              <th>Nome</th>
              <th>Tipo</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {docs.map((doc) => (
              <tr key={doc.id}>
                <td>{doc.nomeArquivo}</td>
                <td>{doc.tipoDocumento}</td>
                <td>
                  <span className={`status-badge ${doc.status?.toLowerCase()}`}>
                    {doc.status}
                  </span>
                </td>
                <td className="acoes-tabela">
                  <button onClick={() => handleDownload(doc.id, doc.nomeArquivo)} className="btn-acao download" title="Baixar documento">
                    <Download size={16} />
                  </button>
                  {/* Futuramente, botões de aprovar/rejeitar podem entrar aqui com a mesma estilização */}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : <p>Nenhum documento encontrado para este cliente.</p>}
    </div>
  );
};

export default DocumentList;