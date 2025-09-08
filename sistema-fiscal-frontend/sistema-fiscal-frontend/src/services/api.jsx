import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
});

// Adiciona token e tenant em todas as requisições
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  const tenant = localStorage.getItem("tenant");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (tenant) {
    config.headers["X-Tenant-ID"] = tenant;
  }
  return config;
});

export default api;