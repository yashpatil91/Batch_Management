import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import AdminDashboardPage from "./pages/AdminDashboardPage";
import TrainerManagementPage from "./pages/TrainerManagementPage";
import TrainerDashboardPage from "./pages/TrainerDashboardPage";
import ProtectedRoute from "./components/ProtectedRoute";
import { useAuth } from "./hooks/useAuth";

const HomeRedirect = () => {
  const { isAuthenticated, role } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={role === "ADMIN" ? "/admin/dashboard" : "/trainer/dashboard"} replace />;
};

const App = () => {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/admin/dashboard"
        element={
          <ProtectedRoute allowedRoles={["ADMIN"]}>
            <AdminDashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/trainers"
        element={
          <ProtectedRoute allowedRoles={["ADMIN"]}>
            <TrainerManagementPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/trainer/dashboard"
        element={
          <ProtectedRoute allowedRoles={["TRAINER"]}>
            <TrainerDashboardPage />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;
