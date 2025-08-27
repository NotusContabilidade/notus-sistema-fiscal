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
import FormularioVencimento from '../components/FormularioVencimento';

// Configurações de tradução (corretas e completas)
const locales = { 'pt-BR': ptBR };
const localizer = dateFnsLocalizer({ format, parse, startOfWeek, getDay, locales });
const messages = { allDay: 'Dia Inteiro', previous: 'Anterior', next: 'Próximo', today: 'Hoje', month: 'Mês', week: 'Semana', day: 'Dia', agenda: 'Agenda', date: 'Data', time: 'Hora', event: 'Evento', noEventsInRange: 'Não há vencimentos neste período.', showMore: total => `+ Ver mais (${total})`};
const formats = { monthHeaderFormat: (date, culture, localizer) => localizer.format(date, 'MMMM yyyy', culture).replace(/^\w/, c => c.toUpperCase()), weekdayFormat: (date, culture, localizer) => localizer.format(date, 'E', culture).replace(/^\w/, c => c.toUpperCase()), agendaHeaderFormat: ({ start, end }, culture, localizer) => localizer.format(start, 'dd/MM/yyyy', culture) + ' – ' + localizer.format(end, 'dd/MM/yyyy', culture) };

function Vencimentos() {
    const [eventos, setEventos] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [date, setDate] = useState(new Date());
    const [view, setView] = useState(Views.MONTH);
    const [filtro, setFiltro] = useState('');
    const filtroDebounced = useDebounce(filtro, 500);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [clienteFiltrado, setClienteFiltrado] = useState(null);
    const [vencimentoSelecionado, setVencimentoSelecionado] = useState(null);

    // ✅ FUNÇÃO fetchEventos COMPLETA E CORRIGIDA
    const fetchEventos = useCallback(async () => {
        setIsLoading(true);
        setClienteFiltrado(null);

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

            if (filtroDebounced && response.data.length > 0) {
                setClienteFiltrado(response.data[0].nomeCliente);
            }
        } catch (error) {
            toast.error("Não foi possível carregar os vencimentos.");
        } finally {
            setIsLoading(false);
        }
    }, [date, filtroDebounced]);

    useEffect(() => {
        fetchEventos();
    }, [fetchEventos]);

    const eventPropGetter = useCallback((event) => ({ className: `status-${event.status}` }), []);

    const handleSaveVencimento = async (formData, id) => {
        const isEditing = !!id;
        const url = isEditing ? `http://localhost:8080/api/vencimentos/${id}` : 'http://localhost:8080/api/vencimentos';
        const method = isEditing ? 'put' : 'post';

        try {
            await axios[method](url, formData);
            toast.success(`Vencimento ${isEditing ? 'atualizado' : 'salvo'} com sucesso!`);
            handleCloseModal();
            fetchEventos();
        } catch (error) {
            toast.error(error.response?.data?.message || 'Erro ao salvar o vencimento.');
            throw error;
        }
    };

    const handleDeleteVencimento = async (id) => {
        try {
            await axios.delete(`http://localhost:8080/api/vencimentos/${id}`);
            toast.success('Vencimento excluído com sucesso!');
            handleCloseModal();
            fetchEventos();
        } catch (error) {
            toast.error('Erro ao excluir o vencimento.');
            throw error;
        }
    }

    const handleSelectEvent = (event) => {
        setVencimentoSelecionado(event);
        setIsModalOpen(true);
    };

    const handleOpenCreateModal = () => {
        setVencimentoSelecionado(null);
        setIsModalOpen(true);
    };
    
    const handleCloseModal = () => {
        setIsModalOpen(false);
        setVencimentoSelecionado(null);
    }

    return (
        <div className="view-container">
            <div className="page-header"><h1 className="page-title">{clienteFiltrado ? `Vencimentos de: ${clienteFiltrado}` : 'Controle de Vencimentos'}</h1></div>

            <ControleVencimentos 
                filtro={filtro} 
                onFiltroChange={setFiltro}
                onAdicionarClick={handleOpenCreateModal}
            />
            
            <div className="card" style={{ height: '75vh', padding: '1rem', position: 'relative', borderTopLeftRadius: 0, borderTopRightRadius: 0 }}>
                {isLoading && <div style={{position: 'absolute', top: '50%', left: '50%'}}><Spinner/></div>}
                <Calendar
                    localizer={localizer}
                    events={eventos}
                    style={{ height: '100%' }}
                    messages={messages}
                    formats={formats}
                    eventPropGetter={eventPropGetter}
                    date={date}
                    view={view}
                    onNavigate={newDate => setDate(newDate)}
                    onView={newView => setView(newView)}
                    onSelectEvent={handleSelectEvent}
                    culture='pt-BR'
                />
            </div>

            <FormularioVencimento 
                isOpen={isModalOpen}
                onClose={handleCloseModal}
                onSave={handleSaveVencimento}
                onDelete={handleDeleteVencimento}
                vencimentoParaEditar={vencimentoSelecionado}
            />
        </div>
    );
}

export default Vencimentos;