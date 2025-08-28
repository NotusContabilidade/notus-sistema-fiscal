import React, { useEffect, useState } from 'react';
import Modal from 'react-modal';
import { useForm, useFieldArray } from 'react-hook-form';
import { X, PlusCircle, Trash2 } from 'lucide-react';

// Estilos para o Modal (pode ser movido para um arquivo CSS depois)
const customStyles = {
    content: { top: '50%', left: '50%', right: 'auto', bottom: 'auto', marginRight: '-50%', transform: 'translate(-50%, -50%)', width: '600px', borderRadius: '8px', padding: '0', boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)', border: 'none', overflow: 'hidden' },
    overlay: { backgroundColor: 'rgba(0, 0, 0, 0.75)', zIndex: 1100 }
};

// Garante que o modal funcione bem com leitores de tela
Modal.setAppElement('#root');

function FormularioTarefaModelo({ isOpen, onClose, onSave, modeloParaEditar }) {
    const [isLoading, setIsLoading] = useState(false);
    const isEditing = !!modeloParaEditar;

    const { register, handleSubmit, formState: { errors }, reset, control } = useForm({
        // Valores padrão para o formulário
        defaultValues: {
            titulo: '', 
            descricao: '', 
            departamento: '', 
            diasParaCriacaoAntecipada: 15, 
            diaVencimentoMes: 20, 
            checklist: []
        }
    });

    // Hook para gerenciar o array de checklist dinâmico
    const { fields, append, remove } = useFieldArray({
        control,
        name: "checklist"
    });

    // Efeito para popular o formulário quando ele abre
    useEffect(() => {
        if (isOpen) {
            if (isEditing) {
                // Transforma a lista de strings do backend (ex: ["item 1", "item 2"])
                // em um formato que o useFieldArray entende (ex: [{value: "item 1"}, {value: "item 2"}])
                const checklistFormatado = modeloParaEditar.checklist.map(item => ({ value: item }));
                reset({ ...modeloParaEditar, checklist: checklistFormatado });
            } else {
                // Valores padrão para um novo modelo
                reset({ 
                    titulo: '', 
                    descricao: '', 
                    departamento: '', 
                    diasParaCriacaoAntecipada: 15, 
                    diaVencimentoMes: 20, 
                    checklist: [{ value: 'Verificar pendências com o cliente' }] 
                });
            }
        }
    }, [isOpen, isEditing, modeloParaEditar, reset]);

    // Função chamada ao submeter o formulário
    const onFormSubmit = (data) => {
        setIsLoading(true);
        // Transforma o checklist de volta para o formato que o backend espera (um array de strings)
        const dataFormatada = {
            ...data,
            checklist: data.checklist.map(item => item.value).filter(item => item.trim() !== '') // Remove itens vazios
        };

        onSave(dataFormatada, modeloParaEditar?.id)
            .catch(() => {}) // O erro já é tratado no componente pai, aqui só garantimos que o loading pare
            .finally(() => setIsLoading(false));
    };

    return (
        <Modal isOpen={isOpen} onRequestClose={onClose} style={customStyles}>
            <div className="modal-header">
                <h3>{isEditing ? 'Editar Modelo de Tarefa' : 'Novo Modelo de Tarefa'}</h3>
                <button onClick={onClose} className="btn-close-modal"><X size={20}/></button>
            </div>
            <form onSubmit={handleSubmit(onFormSubmit)}>
                <div className="modal-body" style={{ maxHeight: '70vh', overflowY: 'auto' }}>
                    
                    <div className="form-group">
                        <label>Título da Tarefa</label>
                        <input type="text" {...register("titulo", { required: "Título é obrigatório" })} />
                        {errors.titulo && <p className="erro-mensagem-form">{errors.titulo.message}</p>}
                    </div>
                    <div className="form-group">
                        <label>Departamento</label>
                        <input type="text" {...register("departamento")} placeholder="Ex: Fiscal, Contábil, Pessoal..."/>
                    </div>
                    <div className="form-group">
                        <label>Descrição / Instruções Padrão</label>
                        <textarea {...register("descricao")} rows="3"></textarea>
                    </div>
                    <div className="form-row">
                        <div className="form-group">
                            <label>Vencimento (Dia do Mês)</label>
                            <input type="number" {...register("diaVencimentoMes", { required: "Campo obrigatório", valueAsNumber: true, min: 1, max: 31 })} />
                            {errors.diaVencimentoMes && <p className="erro-mensagem-form">Deve ser entre 1 e 31</p>}
                        </div>
                        <div className="form-group">
                            <label>Criar Tarefa (Dias Antes)</label>
                            <input type="number" {...register("diasParaCriacaoAntecipada", { required: "Campo obrigatório", valueAsNumber: true, min: 1 })} />
                            {errors.diasParaCriacaoAntecipada && <p className="erro-mensagem-form">Mínimo 1 dia</p>}
                        </div>
                    </div>
                    <hr style={{margin: '1.5rem 0'}}/>
                    
                    {/* Checklist Dinâmico */}
                    <div className="form-group">
                        <label>Checklist Padrão</label>
                        {fields.map((field, index) => (
                            <div key={field.id} className="input-group" style={{ marginBottom: '0.5rem' }}>
                                <input {...register(`checklist.${index}.value`)} placeholder={`Item ${index + 1}`} />
                                <button type="button" className="btn-perigo" style={{padding: '0.5rem'}} onClick={() => remove(index)}><Trash2 size={16}/></button>
                            </div>
                        ))}
                        <button type="button" className="btn-secundario" onClick={() => append({ value: '' })} style={{marginTop: '0.5rem', fontSize: '0.9rem', padding: '0.5rem 1rem'}}>
                            <PlusCircle size={16}/> Adicionar item ao checklist
                        </button>
                    </div>
                </div>
                <div className="modal-actions">
                    <button type="button" className="btn-secundario" onClick={onClose} disabled={isLoading}>Cancelar</button>
                    <button type="submit" className="btn-primario" disabled={isLoading}>{isLoading ? 'Salvando...' : 'Salvar Modelo'}</button>
                </div>
            </form>
        </Modal>
    );
}

export default FormularioTarefaModelo;