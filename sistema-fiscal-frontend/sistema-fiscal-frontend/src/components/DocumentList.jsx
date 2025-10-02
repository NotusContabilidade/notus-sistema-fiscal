import React, { useEffect, useState, useCallback } from "react";
import api from "../services/api";
import Spinner from "./Spinner";
import { Download, FileText, FileCode, FileImage, FileSpreadsheet, FileType } from 'lucide-react';
import '../styles/components/DocumentList.css'; // Importe o novo CSS
import { toast } from "react-toastify";

// Helper para obter o ícone e a classe com base na extensão do arquivo
const getFileIcon = (fileName) => {
    const extension = fileName?.split('.').pop().toLowerCase() || '';
    if (extension === 'pdf') return { Icon: FileText, className: 'type-pdf' };
    if (extension === 'xml') return { Icon: FileCode, className: 'type-xml' };
    if (['jpg', 'jpeg', 'png', 'gif'].includes(extension)) return { Icon: FileImage, className: 'type-img' };
    if (['doc', 'docx'].includes(extension)) return { Icon: FileType, className: 'type-doc' };
    if (['xls', 'xlsx', 'csv'].includes(extension)) return { Icon: FileSpreadsheet, className: 'type-xls' };
    return { Icon: FileText, className: 'type-other' };
};

const DocumentList = ({ clienteId, onUpload }) => {
  const [docs, setDocs] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchDocs = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get(`/documentos/cliente/${clienteId}`);
      setDocs(res.data);
    } catch {
      toast.error("Erro ao buscar documentos.");
    } finally {
      setLoading(false);
    }
  }, [clienteId]);

  useEffect(() => {
    fetchDocs();
  }, [fetchDocs, onUpload]);

  const handleDownload = async (doc) => {
    toast.info(`Iniciando download de ${doc.nomeArquivo}...`);
    try {
      const res = await api.get(`/documentos/${doc.id}/download`, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", doc.nomeArquivo);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error("Erro ao baixar o documento.");
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="document-list-revamped">
      {docs.length > 0 ? (
        docs.map((doc, index) => {
          const { Icon, className } = getFileIcon(doc.nomeArquivo);
          return (
            <div key={doc.id} className="document-card" style={{ animationDelay: `${index * 100}ms` }}>
              <div className={`doc-icon-wrapper ${className}`}>
                <Icon size={28} />
              </div>
              <div className="doc-main-info">
                <h5>{doc.tipoDocumento}</h5>
                <p>{doc.nomeArquivo}</p>
              </div>
              <div className="doc-metadata">
                <span className={`status-badge status-${doc.status?.toLowerCase()}`}>{doc.status}</span>
                <div className="doc-actions">
                  <button onClick={() => handleDownload(doc)} className="btn-doc-action" title="Baixar documento">
                    <Download size={18} />
                  </button>
                </div>
              </div>
            </div>
          );
        })
      ) : (
        <div className="empty-docs-message">
          <p>Nenhum documento disponível para este cliente no momento.</p>
        </div>
      )}
    </div>
  );
};

export default DocumentList;