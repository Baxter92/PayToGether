import { useAuth } from "@/common/context/AuthContext";
import { Eye, EyeOff, Lock, LogIn, Mail } from "lucide-react";
import { useState, type FormEvent } from "react";
import { useNavigate, useLocation, Link } from "react-router-dom";
import { PATHS } from "@/common/constants/path";
import { useI18n } from "@hooks/useI18n";

export default function Login() {
  const { t, i18n } = useI18n("auth");
  const navigate = useNavigate();
  const location = useLocation();
  const { login, user, loading: userLoading } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const from = (location.state as any)?.from?.pathname || "/";

  if (user) {
    navigate(from, { replace: true });
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await login(email, password);

      if (from.includes("/checkout")) {
        navigate(from.replace(/\/checkout$/, ""), { replace: true });
      } else if (user?.role === "ADMIN") {
        navigate("/admin", { replace: true });
      } else {
        navigate(from, { replace: true });
      }
    } catch (err) {
      if (err instanceof Error) {
        setError(i18n.exists(err.message) ? t(err.message) : err.message);
      } else {
        setError(t("loginError"));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-linear-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-8">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-100 rounded-full mb-4">
            <Lock className="w-8 h-8 text-primary" />
          </div>
          <h1 className="text-3xl font-bold text-gray-900">
            {t("loginTitle")}
          </h1>
          <p className="text-gray-600 mt-2">{t("loginSubtitle")}</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              {t("email")}
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full pl-11 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
                placeholder={t("emailPlaceholder")}
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              {t("password")}
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full pl-11 pr-11 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
                placeholder={t("passwordPlaceholder")}
                required
                minLength={6}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPassword ? (
                  <EyeOff className="w-5 h-5" />
                ) : (
                  <Eye className="w-5 h-5" />
                )}
              </button>
            </div>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading || userLoading}
            className="w-full bg-primary hover:bg-blue-700 text-white font-semibold py-3 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading || userLoading ? (
              <div className="animate-spin rounded-full h-5 w-5 border-2 border-white border-t-transparent"></div>
            ) : (
              <>
                <LogIn className="w-5 h-5" />
                {t("loginButton")}
              </>
            )}
          </button>
        </form>

        <div className="mt-4 text-center">
          <Link
            to={PATHS.FORGOT_PASSWORD}
            className="text-sm text-gray-600 hover:text-primary"
          >
            {t("forgotPassword")}
          </Link>
        </div>

        <div className="mt-4 text-center">
          <Link
            to={PATHS.REGISTER}
            className="text-primary hover:text-primary/80 text-sm font-medium"
          >
            {t("noAccount")} {t("signUp")}
          </Link>
        </div>

        <div className="mt-6 pt-6 border-t border-gray-200">
          <p className="text-xs text-gray-500 text-center">{t("demoHint")}</p>
        </div>
      </div>
    </div>
  );
}
