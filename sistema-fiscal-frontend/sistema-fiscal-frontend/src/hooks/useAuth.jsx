import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function useAuth() {
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem("token");
    const tenant = localStorage.getItem("tenant");
    if (!token || !tenant) {
      navigate("/login");
    }
  }, [navigate]);
}