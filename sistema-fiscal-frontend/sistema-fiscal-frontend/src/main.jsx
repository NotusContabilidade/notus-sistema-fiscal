import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import Modal from 'react-modal';

import './styles/global/variables.css';
import './styles/global/global.css';
import './styles/global/darkmode.css';
import './styles/components/Navbar.css';
import './styles/components/Footer.css';
import './styles/components/Card.css';
import './styles/components/Form.css';
import './styles/components/Modal.css';
import './styles/components/Spinner.css';
import './App.css';
import App from './App.jsx';

Modal.setAppElement('#root');

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
);