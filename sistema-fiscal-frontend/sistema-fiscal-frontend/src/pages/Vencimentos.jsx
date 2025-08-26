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
import { useDebounce } from '../hooks/useDebounce'; // ✅ 1. Importar o seu hook de debounce

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

    // ✅ 2. ESTADOS PARA GERENCIAR O FILTRO
    const [filtro, setFiltro] = useState('');
    // Usa o debounce para esperar 500ms após o usuário parar de digitar antes de pesquisar
    const filtroDebounced = useDebounce(filtro, 500);

    const fetchEventos = useCallback(async (start, end, filtroAtual) => {
        setIsLoading(true);
        try {
            const startDate = format(start, 'yyyy-MM-dd');
            const endDate = format(end, 'yyyy-MM-dd');
            
            // ✅ 3. MODIFICAR A URL DA API PARA INCLUIR O FILTRO
            let apiUrl = `http://localhost:8080/api/vencimentos?start=${startDate}&end=${endDate}`;
            if (filtroAtual) {
                apiUrl += `&filtro=${encodeURIComponent(filtroAtual)}`;
            }

            const response = await axios.get(apiUrl);
            
            const eventosFormatados = response.data.map(evento => ({ ...evento, start: new Date(evento.start), end: new Date(evento.end) }));
            setEventos(eventosFormatados);
        } catch (error) {
            toast.error("Não foi possível carregar os vencimentos.");
        } finally {
            setIsLoading(false);
        }
    }, []);

    // ✅ 4. O useEffect AGORA REAGE TAMBÉM AO FILTRO "DEBOUNCED"
    useEffect(() => {
        const [start, end] = (() => {
            const primeiroDia = new Date(date.getFullYear(), date.getMonth(), 1);
            const ultimoDia = new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59);
            return [primeiroDia, ultimoDia];
        })();
        
        fetchEventos(start, end, filtroDebounced);

    }, [date, view, filtroDebounced, fetchEventos]);

    const eventPropGetter = useCallback((event) => ({ className: `status-${event.status}` }), []);

    return (
        <div className="view-container">
            <div className="page-header">
                <h1 className="page-title">Controle de Vencimentos</h1>
            </div>

            {/* ✅ 5. PASSAR O ESTADO E A FUNÇÃO PARA O COMPONENTE FILHO */}
            <ControleVencimentos filtro={filtro} onFiltroChange={setFiltro} />
            
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
        </div>
    );
}

export default Vencimentos;