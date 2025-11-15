import { useState } from "react";
import { useI18n } from "./context/I18nContext";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ProtectedRoutes } from "./routing/ProtectedRoutes";
import Login from "./pages/auth/Login";
import Home from "./pages/home";
import { MainLayout } from "./layouts/MainLayout";

function App() {
  const [count, setCount] = useState(0);
  const { t } = useI18n();

  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Routes publiques avec AuthLayout */}
          <Route>
            <Route path="/login" element={<Login />} />
          </Route>

          {/* Routes protégées avec MainLayout */}
          <Route element={<ProtectedRoutes />}>
            <Route element={<MainLayout />}>
              <Route path="/" element={<Home />} />
              <Route path="/dashboard" element={<div>Dashboard</div>} />
              <Route path="/profile" element={<div>Profile</div>} />
            </Route>
          </Route>

          {/* Route par défaut */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
