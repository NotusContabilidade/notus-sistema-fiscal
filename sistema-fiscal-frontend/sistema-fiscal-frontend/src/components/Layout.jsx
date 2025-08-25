import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { Users } from 'lucide-react'; 
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const Sidebar = () => {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <img src="/logo-notus.jpg" alt="Logotipo Nótus Contábil" className="sidebar-logo" />
        <div className="sidebar-title">Nótus Fiscal</div>
      </div>
      <ul className="sidebar-nav">
        <li className="sidebar-nav-item">
          <NavLink to="/" className={({ isActive }) => isActive ? "active" : ""}>
            <Users size={20} />
            <span>Clientes</span>
          </NavLink>
        </li>
      </ul>
    </aside>
  );
};

const Footer = () => (
  <footer className="app-footer">
    © {new Date().getFullYear()} Nótus Contábil. Todos os direitos reservados.
  </footer>
);

function Layout() {
  return (
    <div className="app-layout">
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
      <Sidebar />
      <div className="content-wrapper">
        <main className="main-content">
          <Outlet />
        </main>
        <Footer />
      </div>
    </div>
  );
}

export default Layout;