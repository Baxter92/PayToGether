import { ArrowLeft, CheckCircle, Loader2, XCircle } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { PATHS } from "@/common/constants/path";
import { useAuth } from "@/common/context/AuthContext";
import { useI18n } from "@hooks/useI18n";

export default function ActivateAccount() {
  const { t } = useI18n("auth");
  const { activateAccount } = useAuth();
  const location = useLocation();
  const token = useMemo(
    () => new URLSearchParams(location.search).get("token") ?? "",
    [location.search],
  );

  const [loading, setLoading] = useState(true);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const runActivation = async () => {
      if (!token) {
        setError(t("activationTokenMissing"));
        setLoading(false);
        return;
      }

      try {
        await activateAccount(token);
        setSuccess(true);
      } catch {
        setError(t("activateAccountError"));
      } finally {
        setLoading(false);
      }
    };

    void runActivation();
  }, [activateAccount, t, token]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-teal-100 dark:from-gray-900 dark:to-gray-800 flex items-center justify-center p-4">
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl w-full max-w-md p-8 text-center">
        {loading && (
          <>
            <div className="inline-flex items-center justify-center w-16 h-16 bg-primary-100 dark:bg-primary-900/50 rounded-full mb-4">
              <Loader2 className="w-8 h-8 text-primary animate-spin" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
              {t("activationInProgressTitle")}
            </h1>
            <p className="text-gray-600 dark:text-gray-400">
              {t("activationInProgressMessage")}
            </p>
          </>
        )}

        {!loading && success && (
          <>
            <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 dark:bg-green-900/50 rounded-full mb-4">
              <CheckCircle className="w-8 h-8 text-green-600 dark:text-green-400" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
              {t("activationSuccessTitle")}
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              {t("activationSuccessMessage")}
            </p>
            <Link
              to={PATHS.LOGIN}
              className="inline-flex items-center gap-2 text-primary hover:text-primary/80 font-medium"
            >
              <ArrowLeft className="w-4 h-4" />
              {t("backToLogin")}
            </Link>
          </>
        )}

        {!loading && !success && (
          <>
            <div className="inline-flex items-center justify-center w-16 h-16 bg-red-100 dark:bg-red-900/50 rounded-full mb-4">
              <XCircle className="w-8 h-8 text-red-600 dark:text-red-400" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
              {t("activationErrorTitle")}
            </h1>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              {error || t("activateAccountError")}
            </p>
            <Link
              to={PATHS.LOGIN}
              className="inline-flex items-center gap-2 text-primary hover:text-primary/80 font-medium"
            >
              <ArrowLeft className="w-4 h-4" />
              {t("backToLogin")}
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
