import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api'
import { toast } from 'react-toastify';
import { ChevronsUp } from 'lucide-react';
import '../styles/pages/Resultado.css';

const SkeletonReport = () => (
    <div className="card">
      <div className="skeleton-card" style={{ height: '2em', width: '60%', margin: '0 auto 1rem auto', backgroundColor: '#e0e0e0', borderRadius: '4px' }}></div>
      <div className="skeleton-card" style={{ height: '1.5em', width: '80%', margin: '0 auto 2rem auto', backgroundColor: '#e0e0e0', borderRadius: '4px' }}></div>
      <div className="skeleton-card" style={{ height: '5em', width: '100%', margin: '0 auto 1rem auto', backgroundColor: '#e0e0e0', borderRadius: '4px' }}></div>
      <div className="skeleton-card" style={{ height: '10em', width: '100%', backgroundColor: '#e0e0e0', borderRadius: '4px' }}></div>
    </div>
);

function formatFileName(tipo, razaoSocial, dataCalculo, ext) {
  // Exemplo: Guia Das, CR TEMPO EDITORACAO GRAFICA LTDA, 10-09-2025.pdf
  const data = dataCalculo?.split(' ')[0]?.split('-').reverse().join('-') || '';
  const razao = razaoSocial?.replace(/[\\/:*?"<>|]/g, '').trim() || '';
  return `Guia Das, ${razao}, ${data}.${ext}`;
}

function Resultado() {
  const { clienteId, calculoId } = useParams();
  const navigate = useNavigate();

  const [resultado, setResultado] = useState(null);
  const [cliente, setCliente] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isExportMenuOpen, setIsExportMenuOpen] = useState(false);
  const [exportando, setExportando] = useState({ pdf: false, excel: false });
  const exportMenuRef = useRef(null);

  const fetchData = useCallback(async () => {
    try {
        const [calculoResponse, clienteResponse] = await Promise.all([
            api.get(`/calculos/${calculoId}`),
            api.get(`/clientes/id/${clienteId}`)
        ]);
        setResultado(calculoResponse.data);
        setCliente(clienteResponse.data);
    } catch (error) {
        toast.error("Não foi possível carregar os dados do relatório.");
        navigate(`/clientes/${clienteId}/dashboard`);
    } finally {
        setIsLoading(false);
    }
  }, [calculoId, clienteId, navigate]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (exportMenuRef.current && !exportMenuRef.current.contains(event.target)) {
        setIsExportMenuOpen(false);
      }
    };
    if (isExportMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isExportMenuOpen]);

  // Exporta PDF via Axios, apenas o botão clicado mostra "Exportando..."
  const handleExportPdf = async () => {
    setExportando(e => ({ ...e, pdf: true }));
    try {
      const response = await api.get(
        `/relatorios/calculo/${calculoId}/exportar/pdf`,
        { responseType: 'blob' }
      );
      const fileName = formatFileName(
        'pdf',
        cliente?.cliente?.razaoSocial,
        resultado?.dataCalculoFormatada,
        'pdf'
      );
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      toast.error('Erro ao exportar PDF');
    } finally {
      setExportando(e => ({ ...e, pdf: false }));
      setIsExportMenuOpen(false);
    }
  };

  // Exporta Excel via Axios, apenas o botão clicado mostra "Exportando..."
  const handleExportExcel = async () => {
    setExportando(e => ({ ...e, excel: true }));
    try {
      const response = await api.get(
        `/relatorios/calculo/${calculoId}/exportar/excel`,
        { responseType: 'blob' }
      );
      const fileName = formatFileName(
        'excel',
        cliente?.cliente?.razaoSocial,
        resultado?.dataCalculoFormatada,
        'xlsx'
      );
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      toast.error('Erro ao exportar Excel');
    } finally {
      setExportando(e => ({ ...e, excel: false }));
      setIsExportMenuOpen(false);
    }
  };

  if (isLoading) {
    return (
      <div className="view-container">
        <div className="page-header"><h1 className="page-title">Relatório de Apuração</h1></div>
        <SkeletonReport />
      </div>
    );
  }

  if (!resultado || !cliente) {
    return null; 
  }

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Relatório de Apuração</h1></div>
      <div className="card">
        <h3>{cliente.cliente.razaoSocial}</h3>
        <div className="info-relatorio-geral">
          <span><strong>Período de Apuração:</strong> {String(resultado.mesReferencia).padStart(2, '0')}/{resultado.anoReferencia}</span>
          <span><strong>Data do Cálculo:</strong> {resultado.dataCalculoFormatada}</span>
        </div>
        <div className="total-das">Valor Total do DAS: R$ {resultado.dasTotal.toFixed(2)}</div>
        
        <h4>Detalhamento por Atividade</h4>
        {resultado.detalhes && resultado.detalhes.length > 0 ? (
          resultado.detalhes.map((detalhe, index) => (
            <div key={index} className="detalhe-anexo-card">
              <h4>Anexo {detalhe.anexoAplicado}</h4>
              <table className="relatorio-tabela">
                <thead>
                    <tr><th>Descrição</th><th>Receita (R$)</th><th>Valor do DAS (R$)</th></tr>
                </thead>
                <tbody>
                  <tr><td>Receita Normal</td><td>{detalhe.rpaNormal.toFixed(2)}</td><td>{detalhe.dasNormal.toFixed(2)}</td></tr>
                  {detalhe.rpaComRetencao > 0 && <tr><td>Receita c/ Retenção ISS</td><td>{detalhe.rpaComRetencao.toFixed(2)}</td><td>{detalhe.dasComRetencaoLiquido.toFixed(2)}</td></tr>}
                  {detalhe.rpaComRetencao > 0 && <tr><td><em>(ISS Retido na Fonte)</em></td><td>-</td><td><em>({detalhe.issRetido.toFixed(2)})</em></td></tr>}
                  {detalhe.rpaStICMS > 0 && <tr><td>Receita c/ ICMS-ST</td><td>{detalhe.rpaStICMS.toFixed(2)}</td><td>{detalhe.dasStICMS.toFixed(2)}</td></tr>}
                  <tr className="subtotal-row"><td>Subtotal do Anexo</td><td>{detalhe.rpaTotal.toFixed(2)}</td><td>{detalhe.dasTotal.toFixed(2)}</td></tr>
                </tbody>
              </table>
              <div className="info-extra-relatorio">
                <span>Alíquota Efetiva: {(detalhe.aliquotaEfetivaTotal * 100).toFixed(4)}%</span>
                {detalhe.fatorR != null && <span> | Fator R: {detalhe.fatorR.toFixed(2)}%</span>}
              </div>
            </div>
          ))
        ) : <p>Não foram encontrados detalhes para este cálculo.</p>}
        
        <div className="botoes-acao">
          <button type="button" className="btn-secundario" onClick={() => navigate(`/clientes/${clienteId}/dashboard`)}>Voltar ao Dashboard</button>
          
          <div className="export-container" ref={exportMenuRef} style={{ position: 'relative' }}>
            <button 
              type="button" 
              className="btn-primario"
              style={{ minWidth: 160 }}
              onClick={() => setIsExportMenuOpen(!isExportMenuOpen)}
            >
              <ChevronsUp size={16}/> Exportar
            </button>
            {isExportMenuOpen && (
              <div
                className="export-dropdown"
                style={{
                  position: 'absolute',
                  left: '100%',
                  top: 0,
                  marginLeft: 12,
                  minWidth: 180,
                  display: 'flex',
                  flexDirection: 'column',
                  zIndex: 20,
                  boxShadow: '0 2px 8px 0 #a1375133'
                }}
              >
                <button
                  type="button"
                  className="btn-primario"
                  style={{
                    width: '100%',
                    marginBottom: 8,
                    minWidth: 180,
                    fontWeight: 700,
                    fontSize: '1rem',
                    whiteSpace: 'normal',
                  }}
                  onClick={handleExportPdf}
                  disabled={exportando.pdf}
                >
                  {exportando.pdf ? 'Exportando...' : 'Exportar como PDF'}
                </button>
                <button
                  type="button"
                  className="btn-primario"
                  style={{
                    width: '100%',
                    minWidth: 180,
                    fontWeight: 700,
                    fontSize: '1rem',
                    whiteSpace: 'normal',
                  }}
                  onClick={handleExportExcel}
                  disabled={exportando.excel}
                >
                  {exportando.excel ? 'Exportando...' : 'Exportar como Planilha'}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Resultado;