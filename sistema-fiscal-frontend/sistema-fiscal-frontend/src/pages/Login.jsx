import React, { useState } from "react";
import api from "../services/api";
import { useNavigate } from "react-router-dom";
import '../styles/pages/Login.css';

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [tenant, setTenant] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await api.post(
        "/auth/login",
        { tenantId: tenant.toLowerCase(), email, password },
        {
          headers: {
            "X-Tenant-ID": tenant.toLowerCase(),
          },
        }
      );
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("tenant", tenant.toLowerCase());
      navigate("/");
    } catch (err) {
      setError("Login inválido! Verifique os dados.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-bg-vinho">
      <div className="login-center">
        <div className="login-header">
          <img src="/logo-notus.jpg" alt="Notus Logo" className="login-logo-big shadow-pop" />
          <h1 className="login-title">Nótus Sistema Fiscal</h1>
          <p className="login-institutional">
            Soluções inteligentes para a contabilidade do seu negócio.
          </p>
        </div>
        <div className="login-card-glass">
          <h2>Bem-vindo!</h2>
          <p className="login-subtitle">Acesse sua conta para continuar</p>
          <form onSubmit={handleSubmit} autoComplete="off">
            <input
              type="text"
              placeholder="Tenant (escritório)"
              value={tenant}
              onChange={(e) => setTenant(e.target.value)}
              required
              autoFocus
            />
            <input
              type="email"
              placeholder="E-mail"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <input
              type="password"
              placeholder="Senha"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <button type="submit" className="btn-glow" disabled={loading}>
              {loading ? <span className="loader"></span> : "Entrar"}
            </button>
            {error && <div className="login-error">{error}</div>}
          </form>
          <div className="login-footer">
            <span>
              Esqueceu a senha? <a href="mailto:suporte@notuscontabil.com.br">Contate o suporte</a>
            </span>
          </div>
        </div>
        <div className="login-powered">
          <span>
            © {new Date().getFullYear()} Nótus Contábil &nbsp;|&nbsp; 
            <a href="https://www.notuscontabil.com.br" target="_blank" rel="noopener noreferrer">
              www.notuscontabil.com.br
            </a>
          </span>
        </div>
      </div>
    </div>
  );
}