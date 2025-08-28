import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import { PlusCircle, Loader, AlertTriangle, Edit, Trash2 } from 'lucide-react';
import FormularioTarefaModelo from '../components/FormularioTarefaModelo'; // O formulário que vamos criar a seguir

// Estilos simples para os botões de ação na tabela
const actionButtonStyles = {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '4px',
    padding: '4px 8px',
    fontSize: '0.8rem',
    marginRight: '8px'
};

function TarefasModelo() {
    const [modelos, setModelos] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modeloParaEditar, setModeloParaEditar] = useState(null);

    // Função para buscar os dados da API
    const fetchModelos = async () => {
        setIsLoading(true);
        try {
            // =========================================================================================
            // IMPORTANTE: Para esta chamada funcionar, você precisa passar o header do tenant.
            // Por enquanto, vamos "mockar" um header com um valor fixo.
            // No futuro, quando tivermos login, este valor virá do usuário autenticado.
            // =========================================================================================
            const response = await axios.get('http://localhost:8080/api/tarefas-modelo', {
                headers: { 'X-Tenant-ID': 'escritorio_escritorio_a_5845' } // <<-- COLOQUE AQUI O SEU SCHEMA DE TESTE
            });
            setModelos(response.data);
        } catch (error) {
            toast.error("Falha ao buscar os modelos de tarefa. Verifique se o backend está rodando.");
            console.error(error);
        } finally {
            setIsLoading(false);
        }
    };

    // Busca os dados quando o componente é montado
    useEffect(() => {
        fetchModelos();
    }, []);

    const handleOpenModal = (modelo = null) => {
        setModeloParaEditar(modelo);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setModeloParaEditar(null);
        setIsModalOpen(false);
    };

    const handleSave = async (data, id) => {
        const isEditing = !!id;
        const method = isEditing ? 'put' : 'post';
        const url = `http://localhost:8080/api/tarefas-modelo${isEditing ? `/${id}` : ''}`;

        try {
            await axios({
                method,
                url,
                data,
                headers: { 'X-Tenant-ID': 'escritorio_escritorio_a_5845' } // <<-- COLOQUE AQUI O SEU SCHEMA DE TESTE
            });
            toast.success(`Modelo ${isEditing ? 'atualizado' : 'criado'} com sucesso!`);
            fetchModelos(); // Re-busca os dados para atualizar a lista
            handleCloseModal();
        } catch (error) {
            const errorMsg = error.response?.data?.message || `Falha ao ${isEditing ? 'atualizar' : 'criar'} o modelo.`;
            toast.error(errorMsg);
            console.error(error);
            throw error; // Propaga o erro para o formulário saber que falhou
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Tem certeza que deseja excluir este modelo? Isso não afetará tarefas já criadas.")) {
            try {
                await axios.delete(`http://localhost:8080/api/tarefas-modelo/${id}`, {
                    headers: { 'X-Tenant-ID': 'escritorio_escritorio_a_5845' } // <<-- COLOQUE AQUI O SEU SCHEMA DE TESTE
                });
                toast.success("Modelo excluído com sucesso!");
                fetchModelos();
            } catch (error) {
                toast.error("Falha ao excluir o modelo.");
                console.error(error);
            }
        }
    };

    return (
        <div className="view-container">
            <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1 className="page-title">Configuração de Tarefas Modelo</h1>
                <button className="btn-primario" onClick={() => handleOpenModal()}>
                    <PlusCircle size={16} />
                    <span>Novo Modelo de Tarefa</span>
                </button>
            </div>

            <div className="card">
                {isLoading ? (
                    <div style={{ display: 'flex', justifyContent: 'center', padding: '2rem' }}>
                        <Loader className="spinner-center" />
                    </div>
                ) : modelos.length === 0 ? (
                    <div className="empty-state" style={{ padding: '2rem', textAlign: 'center' }}>
                        <AlertTriangle size={48} color="#f59e0b" style={{ marginBottom: '1rem' }} />
                        <h3>Nenhum Modelo de Tarefa Encontrado</h3>
                        <p>Comece criando um novo modelo para automatizar suas rotinas.</p>
                    </div>
                ) : (
                    <table className="lista-detalhes-tabela">
                        <thead>
                            <tr>
                                <th>Título</th>
                                <th>Departamento</th>
                                <th>Dia do Vencimento</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            {modelos.map(modelo => (
                                <tr key={modelo.id}>
                                    <td>{modelo.titulo}</td>
                                    <td>{modelo.departamento || 'N/D'}</td>
                                    <td>Todo dia {modelo.diaVencimentoMes}</td>
                                    <td>
                                        <button className="btn-secundario" style={actionButtonStyles} onClick={() => handleOpenModal(modelo)}>
                                            <Edit size={14} /> Editar
                                        </button>
                                        <button className="btn-perigo" style={actionButtonStyles} onClick={() => handleDelete(modelo.id)}>
                                            <Trash2 size={14} /> Excluir
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            {/* O formulário será renderizado aqui, mas ficará invisível até ser ativado */}
            <FormularioTarefaModelo
                isOpen={isModalOpen}
                onClose={handleCloseModal}
                onSave={handleSave}
                modeloParaEditar={modeloParaEditar}
            />
        </div>
    );
}

export default TarefasModelo;