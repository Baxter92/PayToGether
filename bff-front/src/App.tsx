import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./common/context/AuthContext";
import { ProtectedRoutes } from "./routing/ProtectedRoutes";
import Login from "./pages/auth/Login";
import Home from "./pages/home";
import { MainLayout } from "./common/layouts/MainLayout";
import type { JSX } from "react";
import DealDetail from "./pages/dealDetail";
import ScrollToTop from "@utils/ScrollToTop";

function App(): JSX.Element {
  return (
    <BrowserRouter>
      <ScrollToTop />
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
              <Route path="/deals/:id" element={<DealDetail />} />
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
