import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import { Download, ChevronsUp } from 'lucide-react';
import '../styles/pages/Resultado.css';

const SkeletonReport = () => (
    <div className="card">
      <div className="skeleton-card" style={{ height: '2em', width: '60%', margin: '0 auto 1rem auto', backgroundColor: '#e0e0e0', borderRadius: '4px' }}></div>
      <div className="skeleton-card" style={{ height: '1.5em', width: '80%', margin: '0 auto 2rem auto', backgroundColor: '#e0e0e0', borderRadius: '4px' }}></div>
      <div className="skeleton-card" style={{ height: '5em', width: '100%', margin: '0 auto 1rem auto', backgroundColor: '#e0e0e0', borderRadius: '4px' }}></div>
      <div className="skeleton-card" style={{ height: '10em', width: '100%', backgroundColor: '#e0e0e0', borderRadius: '4px' }}></div>
    </div>
);

function Resultado() {
  const { clienteId, calculoId } = useParams();
  const navigate = useNavigate();

  const [resultado, setResultado] = useState(null);
  const [cliente, setCliente] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isExportMenuOpen, setIsExportMenuOpen] = useState(false);
  const exportMenuRef = useRef(null);

  const fetchData = useCallback(async () => {
    try {
        const [calculoResponse, clienteResponse] = await Promise.all([
            axios.get(`http://localhost:8080/api/calculos/${calculoId}`),
            axios.get(`http://localhost:8080/api/clientes/id/${clienteId}`)
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

  const urlExcel = `http://localhost:8080/api/relatorios/calculo/${calculoId}/exportar/excel`;
  const urlPdf = `http://localhost:8080/api/relatorios/calculo/${calculoId}/exportar/pdf`;

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Relatório de Apuração</h1></div>
      <div className="card">
        {/* ✅ ESTA PARTE FOI RESTAURADA */}
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
        {/* FIM DA PARTE RESTAURADA */}
        
        <div className="botoes-acao">
          <button type="button" className="btn-secundario" onClick={() => navigate(`/clientes/${clienteId}/dashboard`)}>Voltar ao Dashboard</button>
          
          <div className="export-container" ref={exportMenuRef}>
            {isExportMenuOpen && (
              <div className="export-dropdown">
                <a href={urlPdf} download>Exportar como PDF</a>
                <a href={urlExcel} download>Exportar como Planilha</a>
              </div>
            )}
            <button 
              type="button" 
              className="btn-primario" 
              onClick={() => setIsExportMenuOpen(!isExportMenuOpen)}
            >
              <ChevronsUp size={16}/> Exportar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Resultado;