import React from 'react';
import { useLocation, useNavigate, useParams, Link } from 'react-router-dom';

function Resultado() {
  const { clienteId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const { resultado, cliente } = location.state || {};

  if (!resultado || !cliente) {
    return (
      <div className='view-container'>
        <div className="card">
            <h3>Dados do cálculo não encontrados.</h3>
            <p>Por favor, volte ao dashboard e tente novamente.</p>
            <div className="botoes-acao">
                <Link to="/" className="btn-primario">Voltar à Busca</Link>
            </div>
        </div>
      </div>
    );
  }

  return (
    <div className="view-container">
      <div className="page-header"><h1 className="page-title">Relatório de Apuração</h1></div>
      <div className="card">
        <h3>{cliente.cliente.razaoSocial}</h3>
        <div className="info-relatorio-geral">
          <span><strong>Período de Apuração:</strong> {String(resultado.mesReferencia).padStart(2, '0')}/{resultado.anoReferencia}</span>
          <span><strong>Data do Cálculo:</strong> {resultado.dataCalculo}</span>
        </div>
        <div className="total-das">Valor Total do DAS: R$ {resultado.dasTotal.toFixed(2)}</div>
        
        <h4>Detalhamento por Atividade</h4>
        {resultado.detalhes.length > 0 ? (
          resultado.detalhes.map((detalhe, index) => (
            <div key={index} className="detalhe-anexo-card">
              <h4>{detalhe.anexoAplicado.replace('Anexo', 'Anexo ')}</h4>
              <table className="relatorio-tabela">
                <tbody>
                  <tr><th>Descrição</th><th>Receita (R$)</th><th>Valor do DAS (R$)</th></tr>
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
          <button type="button" className="btn-primario" onClick={() => navigate('/')}>Nova Consulta</button>
        </div>
      </div>
    </div>
  );
}

export default Resultado;