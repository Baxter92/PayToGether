import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./common/context/AuthContext";
import { ProtectedRoutes } from "./routing/ProtectedRoutes";
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import ForgotPassword from "./pages/auth/ForgotPassword";
import Home from "./pages/home";
import { MainLayout } from "./common/layouts/MainLayout";
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
