import React, { useEffect, useState, useCallback } from "react";
import { useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from "../services/api";
import DocumentUploader from "../components/DocumentUploader";
import DocumentList from "../components/DocumentList";
import Spinner from "../components/Spinner";
import { Home, Folder, Bell, HelpCircle, Building, AlertTriangle, Download, FileText, UploadCloud } from 'lucide-react';
import "../styles/pages/PortalCliente.css";

// --- Função de Download Reutilizável ---
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

// --- Componentes das Abas ---

const PortalDashboard = ({ cliente, tasks, comunicados, documentos, setActiveTab }) => {
  const pendencias = tasks.filter(task => task.status?.toUpperCase() !== 'CONCLUIDO');

  return (
    <div className="portal-tab-content anim-fade-in">
      <div className="portal-grid">
        <div className="portal-widget">
          <div className="widget-header">
            <AlertTriangle size={20} />
            <h4>Pendências Urgentes</h4>
          </div>
          <div className="widget-content">
            {pendencias.length > 0 ? pendencias.slice(0, 3).map((task, index) => (
              <div 
                key={task.id} 
                className="widget-list-item clickable anim-list-item" 
                style={{ animationDelay: `${150 + index * 100}ms` }}
                onClick={() => setActiveTab('solicitacoes')}>
                <span>{task.titulo}</span>
                <span className={`status-badge status-${task.status.toLowerCase()}`}>{task.status}</span>
              </div>
            )) : <p className="empty-message anim-list-item">Nenhuma pendência no momento.</p>}
          </div>
        </div>
        <div className="portal-widget">
          <div className="widget-header">
            <Folder size={20} />
            <h4>Últimos Documentos</h4>
          </div>
          <div className="widget-content">
            {documentos.length > 0 ? documentos.slice(0, 3).map((doc, index) => (
              <div 
                key={doc.id} 
                className="widget-list-item anim-list-item"
                style={{ animationDelay: `${150 + index * 100}ms` }}>
                <div className="doc-info">
                  <FileText size={18} className="doc-icon" />
                  <span className="doc-name">{doc.nomeArquivo}</span>
                </div>
                <button onClick={() => handleDownload(doc)} className="btn-icon-download" title="Baixar documento">
                  <Download size={16} />
                </button>
              </div>
            )) : <p className="empty-message anim-list-item">Nenhum documento recente.</p>}
          </div>
        </div>
        <div className="portal-widget full-width">
          <div className="widget-header">
            <Bell size={20} />
            <h4>Comunicados Importantes</h4>
          </div>
          <div className="widget-content">
            {comunicados.length > 0 ? comunicados.map((com, index) => (
              <div 
                key={com.id} 
                className="comunicado-item anim-list-item"
                style={{ animationDelay: `${200 + index * 120}ms` }}>
                <div className="comunicado-icon-wrapper"><Bell size={20} /></div>
                <div className="comunicado-content">
                  <p><strong>{com.titulo}</strong></p>
                  <p>{com.mensagem}</p>
                  <small>Postado em: {new Date(com.dataCriacao).toLocaleDateString()}</small>
                </div>
              </div>
            )) : <p className="empty-message anim-list-item">Nenhum comunicado da contabilidade.</p>}
          </div>
        </div>
      </div>
    </div>
  );
};

const PortalDocumentos = ({ clienteId }) => {
    const [refreshKey, setRefreshKey] = useState(0);
    const handleUploadSuccess = () => {
        toast.success("Documento enviado com sucesso!");
        setRefreshKey(prev => prev + 1);
    };

    return (
        <div className="portal-tab-content anim-fade-in">
            <div className="documentos-portal-layout">
                <div className="documentos-uploader-container">
                    <div className="widget-header">
                        <UploadCloud size={20} />
                        <h4>Enviar Documentos</h4>
                    </div>
                    <p className="widget-subtitle">Arraste ou selecione os arquivos que a contabilidade solicitou.</p>
                    <DocumentUploader clienteId={clienteId} onUpload={handleUploadSuccess} />
                </div>
                <div className="documentos-list-container">
                    <div className="widget-header">
                        <Folder size={20} />
                        <h4>Documentos Disponíveis</h4>
                    </div>
                    <p className="widget-subtitle">Baixe guias, relatórios e outros arquivos importantes.</p>
                    <DocumentList key={refreshKey} clienteId={clienteId} onUpload={refreshKey} />
                </div>
            </div>
        </div>
    );
};

const PortalSolicitacoes = ({ tasks }) => (
    <div className="portal-tab-content anim-fade-in">
        <h4>Acompanhe as Solicitações da Contabilidade</h4>
        <div className="solicitacoes-list">
            {tasks.length > 0 ? tasks.map(task => (
                <div key={task.id} className="solicitacao-card">
                    <div className="solicitacao-info">
                        <h5>{task.titulo}</h5>
                        <p>{task.descricao || "Sem descrição detalhada."}</p>
                    </div>
                    <div className="solicitacao-meta">
                        <span className={`status-badge status-${task.status.toLowerCase()}`}>{task.status}</span>
                        <span>Prazo: {task.prazo ? new Date(task.prazo).toLocaleDateString() : 'N/D'}</span>
                    </div>
                </div>
            )) : <p className="empty-message">Nenhuma solicitação em aberto.</p>}
        </div>
    </div>
);

const PortalDados = ({ cliente }) => (
    <div className="portal-tab-content anim-fade-in">
        <h4>Dados Cadastrais</h4>
        <div className="dados-cadastrais-grid">
            <p><strong>Razão Social:</strong> {cliente?.cliente.razaoSocial}</p>
            <p><strong>CNPJ:</strong> {cliente?.cliente.cnpj}</p>
            <p><strong>E-mail:</strong> {cliente?.cliente.email}</p>
            <p><strong>Regime Tributário:</strong> {cliente?.cliente.regimeTributario.replace('_', ' ')}</p>
            <p><strong>Inscrição Estadual:</strong> {cliente?.cliente.inscricaoEstadual || 'Não informado'}</p>
            <p><strong>Inscrição Municipal:</strong> {cliente?.cliente.inscricaoMunicipal || 'Não informado'}</p>
        </div>
        <div className="portal-aviso">
            <AlertTriangle size={16} />
            <span>Para alterar qualquer informação, por favor, entre em contato com a contabilidade.</span>
        </div>
    </div>
);

// --- Componente Principal do Portal ---

export default function PortalCliente() {
  const { clienteId } = useParams();
  const [activeTab, setActiveTab] = useState('inicio');
  const [cliente, setCliente] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [comunicados, setComunicados] = useState([]);
  const [documentos, setDocumentos] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      // Busca todos os dados em paralelo para máxima performance
      const [clienteRes, tasksRes, comunicadosRes, documentosRes] = await Promise.all([
        api.get(`/clientes/id/${clienteId}`),
        api.get(`/tasks/por-cliente/${clienteId}`),
        api.get(`/comunicados/por-cliente/${clienteId}`),
        api.get(`/documentos/cliente/${clienteId}`)
      ]);
      
      setCliente(clienteRes.data);
      setTasks(tasksRes.data);
      setComunicados(comunicadosRes.data);
      setDocumentos(documentosRes.data);

    } catch (err) {
      console.error("Erro ao buscar dados do portal", err);
      toast.error("Não foi possível carregar os dados do portal. Verifique sua conexão e tente novamente.");
    } finally {
      setLoading(false);
    }
  }, [clienteId]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const renderTabContent = () => {
    switch (activeTab) {
      case 'inicio':
        return <PortalDashboard cliente={cliente} tasks={tasks} comunicados={comunicados} documentos={documentos} setActiveTab={setActiveTab} />;
      case 'documentos':
        return <PortalDocumentos clienteId={clienteId} />;
      case 'solicitacoes':
        return <PortalSolicitacoes tasks={tasks} />;
      case 'dados':
        return <PortalDados cliente={cliente} />;
      default:
        return null;
    }
  };

  if (loading) return <div className="view-container card"><Spinner /></div>;

  return (
    <div className="view-container portal-cliente-page">
      <div className="portal-header">
        <div>
          <h1 className="page-title">Portal do Cliente</h1>
          <p className="portal-subtitle">{cliente?.cliente.razaoSocial}</p>
        </div>
      </div>
      
      <div className="portal-main-card">
        <nav className="portal-tabs">
          <button onClick={() => setActiveTab('inicio')} className={activeTab === 'inicio' ? 'active' : ''}><Home size={16} /> Início</button>
          <button onClick={() => setActiveTab('documentos')} className={activeTab === 'documentos' ? 'active' : ''}><Folder size={16} /> Documentos</button>
          <button onClick={() => setActiveTab('solicitacoes')} className={activeTab === 'solicitacoes' ? 'active' : ''}><HelpCircle size={16} /> Solicitações</button>
          <button onClick={() => setActiveTab('dados')} className={activeTab === 'dados' ? 'active' : ''}><Building size={16} /> Dados da Empresa</button>
        </nav>
        <div className="portal-content-container">
          {renderTabContent()}
        </div>
      </div>
    </div>
  );
}