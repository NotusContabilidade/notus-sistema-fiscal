import React from 'react';
import { NavLink, Outlet, Link } from 'react-router-dom';
// ✅ 1. IMPORTE O NOVO ÍCONE
import { LayoutDashboard, Users, CalendarClock, ListTodo } from 'lucide-react';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const Header = () => (
  <header className="top-navbar">
    <Link to="/" className="navbar-brand">
      <img src="/logo-notus.jpg" alt="Logotipo Nótus Contábil" className="navbar-logo" />
      <h1>Nótus Sistema Fiscal</h1>
    </Link>
    <nav className="navbar-links">
      <NavLink to="/" end>
        <LayoutDashboard size={18} />
        <span>Dashboard Gerencial</span>
      </NavLink>
      <NavLink to="/clientes/busca">
        <Users size={18} />
        <span>Clientes</span>
      </NavLink>
      <NavLink to="/vencimentos">
        <CalendarClock size={18} />
        <span>Vencimentos</span>
      </NavLink>

      {/* ✅ 2. ADICIONE O NOVO LINK NO MENU */}
      <NavLink to="/tarefas-modelo">
        <ListTodo size={18} />
        <span>Configurar Tarefas</span>
      </NavLink>

    </nav>
  </header>
);

const Footer = () => (
  <footer className="app-footer">
    © {new Date().getFullYear()} Nótus Contábil. Todos os direitos reservados.
  </footer>
);

function Layout() {
  return (
    <div className="app-layout-vertical">
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="colored"
      />
      <Header />
      <main className="main-content">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}

export default Layout;