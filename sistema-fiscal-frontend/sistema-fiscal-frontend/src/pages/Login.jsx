import React, { useState } from "react";
import api from "../services/api";
import { useNavigate } from "react-router-dom";
import "../styles/pages/Login.css";

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
            "X-Tenant-Id": tenant.toLowerCase(), // <-- CORRIGIDO!
          },
        }
      );
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("tenant", tenant.toLowerCase());
      // Salva nome e escritório se vierem na resposta
      if (response.data.nome) {
        localStorage.setItem("user_nome", response.data.nome);
      } else {
        // Usa a primeira parte do e-mail como nome
        const nomeEmail = email.split("@")[0];
        localStorage.setItem("user_nome", nomeEmail);
      }
      if (response.data.escritorio) localStorage.setItem("user_escritorio", response.data.escritorio);
      navigate("/");
    } catch (err) {
      setError("Login inválido! Verifique os dados.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-bg-animated">
      <div className="login-center login-fadein">
        <div className="login-header">
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
            Esqueceu a senha?{" "}
            <span className="login-suporte">
              Contate o suporte: suporte@notuscontabil.com.br
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}