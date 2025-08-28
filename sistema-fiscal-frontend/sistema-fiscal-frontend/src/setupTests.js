import { expect, afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';

// Executa a limpeza da DOM do JSDOM após cada teste
// (ex: desmonta componentes, etc.) para evitar vazamento de estado entre testes.
afterEach(() => {
  cleanup();
});