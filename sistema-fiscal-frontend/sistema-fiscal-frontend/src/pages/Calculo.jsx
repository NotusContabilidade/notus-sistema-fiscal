import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';

function Calculo() {
  const { clienteId } = useParams();
  const navigate = useNavigate();

  const [cliente, setCliente] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
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
    try {
      const response = await axios.get(`http://localhost:8080/api/clientes/id/${clienteId}`);
      setCliente(response.data);
    } catch (error) {
      toast.error("Cliente não encontrado.");
      navigate('/');
    } finally {
      setIsLoading(false);
    }
  }, [clienteId, navigate]);

  useEffect(() => {
    fetchCliente();
  }, [fetchCliente]);

  const handleToggleAnexo = (anexo) => {
    setAnexosSelecionados(prev => prev.includes(anexo) ? prev.filter(a => a !== anexo) : [...prev, anexo]);
  };

  const handleReceitaChange = (anexo, tipo, valor) => {
    setReceitas(prevState => ({
      ...prevState,
      [anexo]: { ...prevState[anexo], [tipo]: parseFloat(valor) || 0 }
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
      navigate(`/clientes/${clienteId}/resultado`, { state: { resultado: response.data, cliente: cliente } });
    } catch (error) {
      toast.error('Ocorreu um erro ao executar o cálculo.');
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return <div className="view-container"><h1>Carregando...</h1></div>;
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
        <div className="separador-ou">PREENCHIMENTO MANUAL</div>
        <div className="selecao-anexos">
            {['anexoI', 'anexoII', 'anexoIII', 'anexoIV', 'anexoV'].map(anexo => (
                <button key={anexo} className={`btn-anexo ${anexosSelecionados.includes(anexo) ? 'selecionado' : ''}`} onClick={() => handleToggleAnexo(anexo)}>
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