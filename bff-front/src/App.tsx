import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./common/context/AuthContext";
import { ProtectedRoutes } from "./routing/ProtectedRoutes";
import Login from "./pages/auth/Login";
import Home from "./pages/home";
import { MainLayout } from "./common/layouts/MainLayout";
import type { JSX } from "react";
import DealDetail from "./pages/dealDetail";
import ScrollToTop from "@/common/utils/ScrollToTop";
import Checkout from "./pages/checkout";

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
          <Route element={<MainLayout />}>
            <Route path="/" element={<Home />} />
            <Route path="/deals/:id" element={<DealDetail />} />
            <Route element={<ProtectedRoutes />}>
              <Route path="/deals/:id/checkout" element={<Checkout />} />
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
