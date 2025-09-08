import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import PdfUploader from '../components/PdfUploader';
import '../styles/pages/Calculo.css';

function Calculo() {
  const { clienteId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [cliente, setCliente] = useState(location.state?.clienteData || null);
  const [isLoading, setIsLoading] = useState(!location.state?.clienteData);

  const [mesRef, setMesRef] = useState(new Date().getMonth() + 1);
  const [anoRef, setAnoRef] = useState(new Date().getFullYear());
  const [receitas, setReceitas] = useState({
    anexoI: { rpaNormal: 0, rpaSt: 0, rpaRetencao: 0 },
    anexoII: { rpaNormal: 0, rpaSt: 0, rpaRetencao: 0 },
    anexoIII: { rpaNormal: 0, rpaSt: 0, rpaRetencao: 0 },
    anexoIV: { rpaNormal: 0, rpaSt: 0, rpaRetencao: 0 },
    anexoV: { rpaNormal: 0, rpaSt: 0, rpaRetencao: 0 },
  });
  const [anexosSelecionados, setAnexosSelecionados] = useState([]);

  const fetchCliente = useCallback(async () => {
    if (clienteId && !cliente) {
      setIsLoading(true);
      try {
        const response = await axios.get(`http://localhost:8080/api/clientes/id/${clienteId}`);
        setCliente(response.data);
      } catch (error) {
        toast.error("Não foi possível carregar os dados do cliente.");
        navigate('/clientes/todos');
      } finally {
        setIsLoading(false);
      }
    }
  }, [clienteId, cliente, navigate]);

  useEffect(() => {
    if (!cliente) {
        fetchCliente();
    }
  }, [cliente, fetchCliente]);


  const handleToggleAnexo = (anexo) => {
    setAnexosSelecionados(prev =>
      prev.includes(anexo) ? prev.filter(a => a !== anexo) : [...prev, anexo]
    );
  };

  const handleReceitaChange = (anexoKey, tipoReceita, valor) => {
    setReceitas(prevReceitas => ({
      ...prevReceitas,
      [anexoKey]: {
        ...prevReceitas[anexoKey],
        [tipoReceita]: parseFloat(valor) || 0
      }
    }));
  };

  const handleExecutarCalculo = async () => {
    setIsLoading(true);
    const requestData = {
      clienteId: cliente.cliente.id,
      mesRef: parseInt(mesRef, 10),
      anoRef: parseInt(anoRef, 10),
      receitas: receitas
    };
    try {
      const response = await axios.post('http://localhost:8080/api/calculos', requestData);
      toast.success("Cálculo realizado com sucesso!");
      navigate(`/clientes/${clienteId}/resultado/${response.data.id}`);
    } catch (error) {
      toast.error('Ocorreu um erro ao executar o cálculo.');
      setIsLoading(false);
    }
  };

  const handlePdfData = (data) => {
    const valorComRetencao = parseFloat(data.comRetencao || 0);
    const valorSemRetencao = parseFloat(data.semRetencao || 0);

    toast.info(`Preenchendo R$ ${valorComRetencao.toFixed(2)} (com retenção) e R$ ${valorSemRetencao.toFixed(2)} (sem retenção)`);
    
    // Aplicando a regra de negócio: tudo para o Anexo III (para este tipo de PDF)
    setReceitas(prevState => ({
      ...prevState,
      // Garante que outros anexos sejam zerados
      anexoI: { rpaNormal: 0, rpaSt: 0, rpaRetencao: 0 }, 
      anexoII: { rpaNormal: 0, rpaSt: 0, rpaRetencao: 0 },
      anexoIII: {
          ...prevState.anexoIII,
          rpaNormal: valorSemRetencao, // Receita SEM retenção vai para "Receita Normal"
          rpaRetencao: valorComRetencao // Receita COM retenção vai para "Receita c/ Retenção de ISS"
      },
      anexoIV: { rpaNormal: 0, rpaSt: 0, rpaRetencao: 0 },
      anexoV: { rpaNormal: 0, rpaSt: 0, rpaRetencao: 0 },
    }));

    // Mostra apenas o Anexo III
    setAnexosSelecionados(['anexoIII']);
  };

  if (isLoading || !cliente) {
    return (
      <div className="view-container">
        <div className="card" style={{ height: '200px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Spinner />
          <p style={{marginLeft: '1rem'}}>Carregando dados do cliente...</p>
        </div>
      </div>
    );
  }

  return (
    <div className='view-container'>
      <div className="page-header"><h1 className="page-title">Novo Cálculo</h1></div>
      <div className='card'>
        <h3>Lançamento de Receitas para {cliente.cliente.razaoSocial}</h3>
        <div className='form-row'>
          <div className="form-group"><label>Mês de Referência:</label><input type="number" value={mesRef} onChange={e => setMesRef(e.target.value)} min="1" max="12" /></div>
          <div className="form-group"><label>Ano de Referência:</label><input type="number" value={anoRef} onChange={e => setAnoRef(e.target.value)} min="2020" /></div>
        </div>

        <div className="separador-ou">PREENCHIMENTO AUTOMÁTICO (VIA PDF)</div>
        <PdfUploader onUploadSuccess={handlePdfData} />

        <div className="separador-ou">OU PREENCHA MANUALMENTE</div>
        
        <div className="selecao-anexos">
        {['anexoI', 'anexoII', 'anexoIII', 'anexoIV', 'anexoV'].map(anexo => (
            <button key={anexo} type="button" className={`btn-anexo ${anexosSelecionados.includes(anexo) ? 'selecionado' : ''}`} onClick={() => handleToggleAnexo(anexo)}>
            {anexo.replace('anexo', 'Anexo ')}
            </button>
        ))}
        </div>
        {anexosSelecionados.map(anexoKey => (
        <div key={anexoKey} className="anexo-bloco">
            <h4>{anexoKey.replace('anexo', 'Anexo ')}</h4>
            <div className="form-group"><label>Receita Normal (R$):</label><input type="number" value={receitas[anexoKey].rpaNormal} onFocus={e => e.target.select()} onChange={e => handleReceitaChange(anexoKey, 'rpaNormal', e.target.value)} step="0.01"/></div>
            {(anexoKey === 'anexoI' || anexoKey === 'anexoII') && <div className="form-group"><label>Receita c/ ICMS-ST (R$):</label><input type="number" value={receitas[anexoKey].rpaSt} onFocus={e => e.target.select()} onChange={e => handleReceitaChange(anexoKey, 'rpaSt', e.target.value)} step="0.01"/></div>}
            {(anexoKey === 'anexoIII' || anexoKey === 'anexoIV' || anexoKey === 'anexoV') && <div className="form-group"><label>Receita c/ Retenção de ISS (R$):</label><input type="number" value={receitas[anexoKey].rpaRetencao} onFocus={e => e.target.select()} onChange={e => handleReceitaChange(anexoKey, 'rpaRetencao', e.target.value)} step="0.01"/></div>}
        </div>
        ))}

        <div className="botoes-acao">
          <button type="button" className="btn-secundario" onClick={() => navigate(`/clientes/${clienteId}/dashboard`)}>Cancelar</button>
          <button type="button" className="btn-primario" onClick={handleExecutarCalculo} disabled={isLoading}>
            {isLoading ? <Spinner /> : 'Executar Cálculo'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default Calculo;