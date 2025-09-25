import React, { useState, useEffect, useRef } from 'react';
import { ChevronDown, X, Search, Globe } from 'lucide-react';
import api from '../services/api';
import Spinner from './Spinner';
import '../styles/components/ClienteSearchableSelect.css';

function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);
  useEffect(() => {
    const handler = setTimeout(() => { setDebouncedValue(value); }, delay);
    return () => { clearTimeout(handler); };
  }, [value, delay]);
  return debouncedValue;
}

export default function ClienteSearchableSelect({ value, onChange, placeholder = "Pesquise por CNPJ ou Razão Social..." }) {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedClientName, setSelectedClientName] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  
  const debouncedSearchTerm = useDebounce(searchTerm, 300);
  const wrapperRef = useRef(null);
  const inputRef = useRef(null);

  // Busca sugestões de clientes com base na digitação
  useEffect(() => {
    if (debouncedSearchTerm.length < 2) {
      setSuggestions([]);
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    api.get(`/clientes/busca?q=${encodeURIComponent(debouncedSearchTerm)}`)
      .then(res => setSuggestions(res.data || []))
      .catch(() => setSuggestions([]))
      .finally(() => setIsLoading(false));
  }, [debouncedSearchTerm]);

  // Define o nome do cliente exibido quando um 'value' (ID) é passado
  useEffect(() => {
    if (value && value !== "ALL") {
      api.get(`/clientes/id/${value}`).then(res => {
        setSelectedClientName(res.data.cliente.razaoSocial);
      }).catch(() => setSelectedClientName('Cliente não encontrado'));
    } else {
      setSelectedClientName('Todos os Clientes');
    }
  }, [value]);

  // Fecha o dropdown ao clicar fora
  useEffect(() => {
    function handleClickOutside(event) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setIsOpen(false);
        setSearchTerm(''); // Limpa a busca ao fechar
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [wrapperRef]);

  const handleSelect = (client) => {
    onChange(client.id);
    setIsOpen(false);
    setSearchTerm('');
  };

  const handleSelectAll = () => {
    onChange("ALL");
    setIsOpen(false);
    setSearchTerm('');
  };

  const clearSelection = (e) => {
    e.stopPropagation();
    handleSelectAll();
    inputRef.current?.focus();
  };

  const handleFocus = () => {
    setIsOpen(true);
  };

  const isSearching = isOpen || searchTerm;

  return (
    <div className="searchable-select-wrapper" ref={wrapperRef}>
      <div 
        className={`searchable-select-input-container ${isSearching ? 'searching' : ''}`} 
        onClick={() => inputRef.current?.focus()}
      >
        <Search size={18} className="search-icon" />
        
        {/* Rótulo de exibição que será animado */}
        <span className="searchable-select-display-label">{selectedClientName}</span>

        <input
          ref={inputRef}
          type="text"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          onFocus={handleFocus}
          placeholder={placeholder}
          className="searchable-select-input"
        />
        <div className="searchable-select-icons">
            {value !== "ALL" && !isSearching && (
                <button onClick={clearSelection} className="clear-button" title="Limpar seleção">
                    <X size={16} />
                </button>
            )}
            <ChevronDown size={20} className={`arrow-icon ${isOpen ? 'open' : ''}`} />
        </div>
      </div>
      {isOpen && (
        <ul className="searchable-select-dropdown">
          <li className="all-clients-option" onMouseDown={handleSelectAll}>
            <Globe size={16} />
            <span>Todos os Clientes</span>
          </li>
          {isLoading ? (
            <li className="dropdown-message"><Spinner /> Carregando...</li>
          ) : debouncedSearchTerm.length > 0 && debouncedSearchTerm.length < 2 ? (
            <li className="dropdown-message">Digite 2 ou mais caracteres para buscar...</li>
          ) : suggestions.length > 0 ? (
            suggestions.map(client => (
              <li key={client.id} onMouseDown={() => handleSelect(client)}>
                <span className="client-name">{client.razaoSocial}</span>
                <span className="client-cnpj">{client.cnpj}</span>
              </li>
            ))
          ) : debouncedSearchTerm.length >= 2 ? (
            <li className="dropdown-message">Nenhum cliente encontrado.</li>
          ) : (
            <li className="dropdown-message">Comece a digitar para ver as sugestões.</li>
          )}
        </ul>
      )}
    </div>
  );
}