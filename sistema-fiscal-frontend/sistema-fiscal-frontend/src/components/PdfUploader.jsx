import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { UploadCloud, XCircle } from 'lucide-react';
import api from '../services/api'
import { toast } from 'react-toastify';
import Spinner from './Spinner';

function PdfUploader({ onUploadSuccess }) {
  const [file, setFile] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const onDrop = useCallback(acceptedFiles => {
    if (acceptedFiles.length > 0) {
      setFile(acceptedFiles[0]);
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { 'application/pdf': ['.pdf'] },
    multiple: false,
  });

  const handleProcessFile = async () => {
    if (!file) return;

    setIsLoading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.post('/pdf/upload/livro-fiscal-bauru', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      toast.success('PDF processado com sucesso!');
      onUploadSuccess(response.data);
      setFile(null);
    } catch (error) {
      toast.error('Erro ao processar o PDF.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="pdf-uploader-container">
      <div {...getRootProps()} className={`dropzone${isDragActive ? ' active' : ''}`} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '2rem 1rem' }}>
        <input {...getInputProps()} />
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '0.5rem' }}>
          <UploadCloud size={48} color="#a13751" style={{ marginBottom: '0.5rem' }} />
          {isDragActive ? (
            <p>Solte o arquivo aqui...</p>
          ) : (
            <p>Arraste e solte o PDF do Livro Fiscal aqui, ou clique para selecionar</p>
          )}
        </div>
      </div>

      {file && (
        <div className="file-preview" style={{ display: 'flex', alignItems: 'center', marginTop: '1rem', background: '#faf7fa', borderRadius: 8, padding: '0.5rem 1rem', border: '1.5px solid #a13751' }}>
          <span style={{ flex: 1 }}>{file.name}</span>
          <button onClick={() => setFile(null)} className="btn-remove-file" style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a13751' }}><XCircle size={18} /></button>
        </div>
      )}

      <button
        type="button"
        className="btn-primario"
        onClick={handleProcessFile}
        disabled={!file || isLoading}
        style={{ width: '100%', marginTop: '1rem' }}
      >
        {isLoading ? <Spinner /> : 'Processar PDF e Preencher Receitas'}
      </button>
    </div>
  );
}

export default PdfUploader;