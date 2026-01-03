import { BrowserRouter, Route, Routes } from "react-router-dom";

import { AuthProvider } from "./common/context/AuthContext";
import { ProtectedRoutes } from "./routing/ProtectedRoutes";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import ForgotPassword from "./pages/auth/ForgotPassword";
import Home from "./pages/home";
import { MainLayout } from "./common/layouts/MainLayout";
import { AdminLayout } from "./common/layouts/AdminLayout";
import type { JSX } from "react";
import DealDetail from "./pages/dealDetail";
import ScrollToTop from "@/common/utils/ScrollToTop";
import Checkout from "./pages/checkout";
import Profile from "./pages/profile";
import { PATHS } from "./common/constants/path";
import Orders from "./pages/orders";
import OrderSuccess from "./pages/orderSuccess";
import Category from "./pages/category";
import Categories from "./pages/categories";
import SearchPage from "./pages/search";
import NotFound from "./pages/notFound";
import Deals from "./pages/deals";
import AdminDashboard from "./pages/admin/dashboard";
import AdminDeals from "./pages/admin/deals";
import AdminUsers from "./pages/admin/users";
import AdminOrders from "./pages/admin/orders";
import AdminSettings from "./pages/admin/settings";
import AdminLogin from "./pages/admin/login";
import AdminMerchants from "./pages/admin/merchants";
import AdminPayments from "./pages/admin/payments";
import AdminCategories from "./pages/admin/categories";
import AdminReports from "./pages/admin/reports";
import AdminHero from "./pages/admin/hero";

function App(): JSX.Element {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <AuthProvider>
        <Routes>
          {/* Routes publiques Auth */}
          <Route path={PATHS.LOGIN} element={<Login />} />
          <Route path={PATHS.REGISTER} element={<Register />} />
          <Route path={PATHS.FORGOT_PASSWORD} element={<ForgotPassword />} />

          {/* Admin Login */}
          <Route path="/admin/login" element={<AdminLogin />} />

          {/* Routes Admin */}
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<AdminDashboard />} />
            <Route path="deals" element={<AdminDeals />} />
            <Route path="users" element={<AdminUsers />} />
            <Route path="orders" element={<AdminOrders />} />
            <Route path="merchants" element={<AdminMerchants />} />
            <Route path="payments" element={<AdminPayments />} />
            <Route path="categories" element={<AdminCategories />} />
            <Route path="reports" element={<AdminReports />} />
            <Route path="settings" element={<AdminSettings />} />
            <Route path="hero" element={<AdminHero />} />
          </Route>

          {/* Routes avec MainLayout */}
          <Route element={<MainLayout />}>
            <Route path={PATHS.HOME} element={<Home />} />
            <Route path={PATHS.DEAL_DETAIL()} element={<DealDetail />} />
            <Route path={PATHS.SEARCH} element={<SearchPage />} />

            {/* Routes protégées */}
            <Route element={<ProtectedRoutes />}>
              <Route path={PATHS.PROFILE} element={<Profile />} />
              <Route path={PATHS.CHECKOUT()} element={<Checkout />} />
              <Route path={PATHS.ORDERS} element={<Orders />} />
              <Route
                path={PATHS.SUCCESS_SUBSCRIPTION()}
                element={<OrderSuccess />}
              />
              <Route path={PATHS.CATEGORIES()} element={<Category />} />
              <Route path={PATHS.ALL_CATEGORIES} element={<Categories />} />
              <Route path={PATHS.DEALS} element={<Deals />} />
            </Route>
          </Route>

          {/* Page 404 */}
          <Route path={PATHS.NOT_FOUND} element={<NotFound />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
