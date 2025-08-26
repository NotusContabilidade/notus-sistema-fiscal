import { useEffect } from 'react';

// Este hook atualiza o título da aba do navegador
export function usePageTitle(title) {
  useEffect(() => {
    if (title) {
      document.title = `${title} | Nótus Sistema Fiscal`;
    } else {
      document.title = 'Nótus Sistema Fiscal';
    }
  }, [title]);
}