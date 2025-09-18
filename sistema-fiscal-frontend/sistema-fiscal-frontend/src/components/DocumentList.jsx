import React, { useEffect, useState } from "react";
import api from "../services/api";
import Spinner from "./Spinner";

const DocumentList = ({ clienteId }) => {
  const [docs, setDocs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [comentario, setComentario] = useState({});
  const [msg, setMsg] = useState("");

  const fetchDocs = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/documentos/cliente/${clienteId}`);
      setDocs(res.data);
    } catch {
      setMsg("Erro ao buscar documentos.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocs();
    // eslint-disable-next-line
  }, [clienteId]);

  const handleAprovar = async (id) => {
    setLoading(true);
    try {
      await api.post(`/documentos/${id}/aprovar`, {
        usuarioAprovador: localStorage.getItem("user") || "",
        comentario: comentario[id] || "",
      });
      setMsg("Documento aprovado!");
      fetchDocs();
    } catch {
      setMsg("Erro ao aprovar.");
    } finally {
      setLoading(false);
    }
  };

  const handleRejeitar = async (id) => {
    setLoading(true);
    try {
      await api.post(`/documentos/${id}/rejeitar`, {
        usuarioAprovador: localStorage.getItem("user") || "",
        comentario: comentario[id] || "",
      });
      setMsg("Documento rejeitado!");
      fetchDocs();
    } catch {
      setMsg("Erro ao rejeitar.");
    } finally {
      setLoading(false);
    }
  };

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
      setMsg("Erro ao baixar documento.");
    }
  };

  return (
    <div className="card">
      <h4>Documentos</h4>
      {loading && <Spinner />}
      {msg && <div style={{ marginBottom: 8 }}>{msg}</div>}
      <table>
        <thead>
          <tr>
            <th>Nome</th>
            <th>Tipo</th>
            <th>Status</th>
            <th>Comentário</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {docs.map((doc) => (
            <tr key={doc.id}>
              <td>{doc.nomeArquivo}</td>
              <td>{doc.tipoDocumento}</td>
              <td>{doc.status}</td>
              <td>
                <input
                  type="text"
                  placeholder="Comentário"
                  value={comentario[doc.id] || ""}
                  onChange={(e) =>
                    setComentario({ ...comentario, [doc.id]: e.target.value })
                  }
                  disabled={doc.status !== "PENDENTE"}
                />
              </td>
              <td>
                <button onClick={() => handleDownload(doc.id, doc.nomeArquivo)}>
                  Baixar
                </button>
                {doc.status === "PENDENTE" && (
                  <>
                    <button onClick={() => handleAprovar(doc.id)}>
                      Aprovar
                    </button>
                    <button onClick={() => handleRejeitar(doc.id)}>
                      Rejeitar
                    </button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default DocumentList;