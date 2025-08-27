import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { UploadCloud, XCircle, CheckCircle } from 'lucide-react';
import axios from 'axios';
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
      const response = await axios.post('http://localhost:8080/api/pdf/upload/livro-fiscal-bauru', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      toast.success('PDF processado com sucesso!');
      onUploadSuccess(response.data); // Envia os dados extraídos para o componente pai
      setFile(null); // Limpa o arquivo após o sucesso
    } catch (error) {
      toast.error('Erro ao processar o PDF.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="pdf-uploader-container">
      <div {...getRootProps()} className={`dropzone ${isDragActive ? 'active' : ''}`}>
        <input {...getInputProps()} />
        <UploadCloud size={48} color="#a13751" />
        {isDragActive ? (
          <p>Solte o arquivo aqui...</p>
        ) : (
          <p>Arraste e solte o PDF do Livro Fiscal aqui, ou clique para selecionar</p>
        )}
      </div>

      {file && (
        <div className="file-preview">
          <span>{file.name}</span>
          <button onClick={() => setFile(null)} className="btn-remove-file"><XCircle size={18} /></button>
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