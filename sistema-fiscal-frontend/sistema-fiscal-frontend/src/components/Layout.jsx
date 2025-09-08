import React, { useEffect, useState } from 'react';
import { NavLink, Outlet, Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, Users, CalendarClock, KeyRound } from 'lucide-react';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const Header = ({ isAuthenticated, showMenu }) => (
  <header className="top-navbar">
    <Link to="/" className="navbar-brand">
      <img src="/logo-notus.jpg" alt="Logotipo Nótus Contábil" className="navbar-logo" />
      <h1>Nótus Sistema Fiscal</h1>
    </Link>
    <nav className={`navbar-links${showMenu ? " navbar-links-fadein" : ""}`}>
      {isAuthenticated && (
        <>
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
        </>
      )}
      {/* Portal do Cliente - sempre visível no menu */}
      <NavLink to="/portal-cliente">
        <KeyRound size={18} />
        <span>Portal do Cliente</span>
      </NavLink>
    </nav>
  </header>
);

function Layout({ dark, setDark, children }) {
  const location = useLocation();
  const [showMenu, setShowMenu] = useState(false);

  // Considere autenticado se houver token no localStorage
  const isAuthenticated = Boolean(localStorage.getItem("token"));

  // Fade nos links do menu após login
  useEffect(() => {
    if (isAuthenticated && location.pathname !== "/login") {
      setShowMenu(false);
      const timer = setTimeout(() => setShowMenu(true), 400); // delay para fade
      return () => clearTimeout(timer);
    } else {
      setShowMenu(false);
    }
  }, [isAuthenticated, location.pathname]);

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
      <Header isAuthenticated={isAuthenticated && location.pathname !== "/login"} showMenu={showMenu} />
      <main className="main-content">
        {children || <Outlet />}
      </main>
      <footer className="app-footer">
        © {new Date().getFullYear()} Nótus Contábil. Todos os direitos reservados.
        <button
          className="btn-darkmode"
          onClick={() => setDark((d) => !d)}
          type="button"
        >
          {dark ? "Modo Claro" : "Modo Escuro"}
        </button>
      </footer>
    </div>
  );
}

export default Layout;