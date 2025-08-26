import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { Calendar, dateFnsLocalizer, Views } from 'react-big-calendar';
import format from 'date-fns/format';
import parse from 'date-fns/parse';
import startOfWeek from 'date-fns/startOfWeek';
import getDay from 'date-fns/getDay';
import ptBR from 'date-fns/locale/pt-BR';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import { toast } from 'react-toastify';
import Spinner from '../components/Spinner';
import ControleVencimentos from '../components/ControleVencimentos';
import { useDebounce } from '../hooks/useDebounce';
import FormularioVencimento from '../components/FormularioVencimento'; // ✅ 1. Importar o formulário

// Configs (sem mudanças)
const locales = { 'pt-BR': ptBR };
const localizer = dateFnsLocalizer({ format, parse, startOfWeek, getDay, locales });
const messages = { allDay: 'Dia Inteiro', previous: 'Anterior', next: 'Próximo', today: 'Hoje', month: 'Mês', week: 'Semana', day: 'Dia', agenda: 'Agenda', date: 'Data', time: 'Hora', event: 'Evento', noEventsInRange: 'Não há vencimentos neste período.', showMore: total => `+ Ver mais (${total})`};
const formats = { monthHeaderFormat: (date, culture, localizer) => localizer.format(date, 'MMMM yyyy', culture).replace(/^\w/, c => c.toUpperCase()), weekdayFormat: (date, culture, localizer) => localizer.format(date, 'eeeeee', culture).replace(/^\w/, c => c.toUpperCase()) };

function Vencimentos() {
    const [eventos, setEventos] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [date, setDate] = useState(new Date());
    const [view, setView] = useState(Views.MONTH);
    const [filtro, setFiltro] = useState('');
    const filtroDebounced = useDebounce(filtro, 500);

    // ✅ 2. State para controlar a visibilidade do modal
    const [isModalOpen, setIsModalOpen] = useState(false);
    
    // Função para buscar eventos (agora com useCallback para incluir na dependência do useEffect)
    const fetchEventos = useCallback(async () => {
        setIsLoading(true);
        const primeiroDia = new Date(date.getFullYear(), date.getMonth(), 1);
        const ultimoDia = new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59);
        const startDate = format(primeiroDia, 'yyyy-MM-dd');
        const endDate = format(ultimoDia, 'yyyy-MM-dd');
        
        let apiUrl = `http://localhost:8080/api/vencimentos?start=${startDate}&end=${endDate}`;
        if (filtroDebounced) {
            apiUrl += `&filtro=${encodeURIComponent(filtroDebounced)}`;
        }
        
        try {
            const response = await axios.get(apiUrl);
            const eventosFormatados = response.data.map(evento => ({ ...evento, start: new Date(evento.start), end: new Date(evento.end) }));
            setEventos(eventosFormatados);
        } catch (error) {
            toast.error("Não foi possível carregar os vencimentos.");
        } finally {
            setIsLoading(false);
        }
    }, [date, filtroDebounced]); // Depende da data e do filtro

    useEffect(() => {
        fetchEventos();
    }, [fetchEventos]);

    const eventPropGetter = useCallback((event) => ({ className: `status-${event.status}` }), []);

    // ✅ 3. Função para salvar o novo vencimento
    const handleSaveVencimento = async (formData) => {
        try {
            await axios.post('http://localhost:8080/api/vencimentos', formData);
            toast.success('Vencimento salvo com sucesso!');
            setIsModalOpen(false); // Fecha o modal
            fetchEventos(); // Atualiza o calendário com o novo evento
        } catch (error) {
            toast.error(error.response?.data?.message || 'Erro ao salvar o vencimento.');
            // Não fecha o modal em caso de erro, para o usuário poder corrigir
        }
    };

    return (
        <div className="view-container">
            <div className="page-header">
                <h1 className="page-title">Controle de Vencimentos</h1>
            </div>

            <ControleVencimentos 
                filtro={filtro} 
                onFiltroChange={setFiltro}
                onAdicionarClick={() => setIsModalOpen(true)} // ✅ 4. Ação para abrir o modal
            />
            
            <div className="card" style={{ height: '75vh', padding: '1rem', position: 'relative', borderTopLeftRadius: 0, borderTopRightRadius: 0 }}>
                {isLoading && <div style={{position: 'absolute', top: '50%', left: '50%'}}><Spinner/></div>}
                <Calendar
                    localizer={localizer}
                    events={eventos}
                    startAccessor="start"
                    endAccessor="end"
                    style={{ height: '100%' }}
                    messages={messages}
                    formats={formats}
                    eventPropGetter={eventPropGetter}
                    date={date}
                    view={view}
                    onNavigate={newDate => setDate(newDate)}
                    onView={newView => setView(newView)}
                />
            </div>

            {/* ✅ 5. Renderizar o formulário modal */}
            <FormularioVencimento 
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSave={handleSaveVencimento}
            />
        </div>
    );
}

export default Vencimentos;