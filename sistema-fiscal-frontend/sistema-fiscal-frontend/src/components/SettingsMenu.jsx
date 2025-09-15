import React, { useState, useRef, useEffect } from "react";
import { LogOut, Sun, Moon, User, Building2, Settings } from "lucide-react";
import "../styles/components/SettingsMenu.css";

export default function SettingsMenu({ dark, setDark, onLogout, user }) {
  const [open, setOpen] = useState(false);
  const menuRef = useRef(null);

  // Fecha o menu ao clicar fora
  useEffect(() => {
    function handleClickOutside(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) setOpen(false);
    }
    if (open) document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  return (
    <div className="settings-menu-root">
      <button
        className="settings-gear-btn"
        aria-label="Configurações"
        onClick={() => setOpen((v) => !v)}
      >
        <Settings size={24} />
      </button>
      {open && (
        <div className={`settings-menu-popover${dark ? " dark" : ""}`} ref={menuRef}>
          <div className="settings-menu-section">
            <div className="settings-menu-title">Temas</div>
            <div className="settings-theme-options">
              <button
                className={`settings-theme-btn${!dark ? " selected" : ""}`}
                onClick={() => setDark(false)}
              >
                <Sun size={18} />
                Modo Claro
                {!dark && <span className="settings-theme-dot" />}
              </button>
              <button
                className={`settings-theme-btn${dark ? " selected" : ""}`}
                onClick={() => setDark(true)}
              >
                <Moon size={18} />
                Modo Escuro
                {dark && <span className="settings-theme-dot" />}
              </button>
            </div>
          </div>
          <div className="settings-menu-section">
            <div className="settings-menu-title">Usuário</div>
            <div className="settings-user-info">
              <User size={16} /> <span>{user?.nome || "Usuário"}</span>
            </div>
            <div className="settings-user-info">
              <Building2 size={16} /> <span>{user?.escritorio || localStorage.getItem("tenant")}</span>
            </div>
          </div>
          <div className="settings-menu-section">
            <button className="settings-logout-btn" onClick={onLogout}>
              <LogOut size={18} /> Sair
            </button>
          </div>
        </div>
      )}
    </div>
  );
}