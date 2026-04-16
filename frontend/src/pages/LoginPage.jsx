import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import Card from "../components/Card";
import Input from "../components/Input";
import Button from "../components/Button";
import { authService } from "../services/authService";
import { useAuth } from "../hooks/useAuth";
import { validateLoginForm } from "../utils/validation";

const LoginPage = () => {
  const navigate = useNavigate();
  const { login, isAuthenticated, role } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  if (isAuthenticated) {
    return <Navigate to={role === "ADMIN" ? "/admin/dashboard" : "/trainer/dashboard"} replace />;
  }

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const payload = {
      email: form.email.trim(),
      password: form.password
    };
    const validationErrors = validateLoginForm(payload);
    setErrors(validationErrors);
    setApiError("");
    if (Object.keys(validationErrors).length > 0) return;

    try {
      setIsLoading(true);
      const data = await authService.login(payload);
      login(data);
      navigate(data.role === "ADMIN" ? "/admin/dashboard" : "/trainer/dashboard", { replace: true });
    } catch (error) {
      if (error?.response) {
        setApiError(error?.response?.data?.message || `Login failed (${error.response.status}).`);
      } else {
        setApiError("Cannot reach backend. Check if backend is running on port 8080.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 p-4">
      <Card className="w-full max-w-md p-8">
        <h1 className="mb-2 text-2xl font-bold text-slate-100">Batch Management</h1>
        <p className="mb-6 text-sm text-slate-400">Sign in to continue</p>
        <form className="space-y-4" onSubmit={handleSubmit}>
          <Input
            label="Email"
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            error={errors.email}
            placeholder="admin@batch.com"
          />
          <Input
            label="Password"
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            error={errors.password}
            placeholder="Enter your password"
          />
          {apiError && <p className="text-sm text-red-300">{apiError}</p>}
          <Button type="submit" loading={isLoading} className="w-full">
            Login
          </Button>
        </form>
      </Card>
    </div>
  );
};

export default LoginPage;
