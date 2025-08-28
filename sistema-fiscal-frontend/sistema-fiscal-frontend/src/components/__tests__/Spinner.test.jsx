// Em: src/components/__tests__/Spinner.test.jsx

import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import Spinner from '../Spinner';

describe('Componente Spinner', () => {
  it('deve renderizar corretamente com a classe "spinner"', () => {
    render(<Spinner />);

    // ✅ Troque a busca genérica por esta, que é específica e confiável
    const spinnerElement = screen.getByTestId('spinner');
    
    expect(spinnerElement).toBeInTheDocument();
    expect(spinnerElement).toHaveClass('spinner');
  });
});