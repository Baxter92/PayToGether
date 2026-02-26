import { Mail, ArrowLeft, Send, CheckCircle } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { PATHS } from "@/common/constants/path";
import { useI18n } from "@hooks/useI18n";

export default function ForgotPassword() {
  const { t } = useI18n("auth");
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await new Promise((resolve) => setTimeout(resolve, 1500));
      setSent(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : t("errorOccurred"));
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-primary-50 to-teal-100 flex items-center justify-center p-4">
        <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-8 text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 rounded-full mb-4">
            <CheckCircle className="w-8 h-8 text-green-600" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">
            {t("emailSent")}
          </h1>
          <p
            className="text-gray-600 mb-6"
            dangerouslySetInnerHTML={{
              __html: t("emailSentMessage", { email }),
            }}
          />
          <Link
            to={PATHS.LOGIN}
            className="inline-flex items-center gap-2 text-primary hover:text-primary/80 font-medium"
          >
            <ArrowLeft className="w-4 h-4" />
            {t("backToLogin")}
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-teal-100 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-8">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-primary-100 rounded-full mb-4">
            <Mail className="w-8 h-8 text-primary" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">
            {t("forgotPasswordTitle")}
          </h1>
          <p className="text-gray-600 mt-2">{t("forgotPasswordSubtitle")}</p>
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
                className="w-full pl-11 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition"
                placeholder={t("emailPlaceholder")}
                required
              />
            </div>
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-primary hover:bg-primary/90 text-white font-semibold py-3 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <div className="animate-spin rounded-full h-5 w-5 border-2 border-white border-t-transparent"></div>
            ) : (
              <>
                <Send className="w-5 h-5" />
                {t("sendLink")}
              </>
            )}
          </button>
        </form>

        <div className="mt-6 text-center">
          <Link
            to={PATHS.LOGIN}
            className="inline-flex items-center gap-2 text-primary hover:text-primary/80 text-sm font-medium"
          >
            <ArrowLeft className="w-4 h-4" />
            {t("backToLogin")}
          </Link>
        </div>
      </div>
    </div>
  );
}
