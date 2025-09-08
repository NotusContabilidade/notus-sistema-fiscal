import { Navigate } from "react-router-dom";

export default function ProtectedRoute({ children }) {
  const token = localStorage.getItem("token");
  const tenant = localStorage.getItem("tenant");
  if (!token || !tenant) {
    return <Navigate to="/login" replace />;
  }
  return children;
}