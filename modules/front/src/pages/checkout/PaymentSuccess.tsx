import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { Card, CardContent } from "@components/ui/card";
import { Button } from "@components/ui/button";
import { Clock, Home, Mail, AlertCircle } from "lucide-react";
import VStack from "@components/VStack";
import HStack from "@components/HStack";
import { PATHS } from "@/common/constants/path";
import { useI18n } from "@/common/hooks/useI18n";

/**
 * Payment processing page - Paiement en cours de traitement
 */
export default function PaymentSuccess() {
  const navigate = useNavigate();
  const { t } = useI18n("checkout");

  useEffect(() => {
    // Prevent back navigation
    window.history.pushState(null, "", window.location.href);
    window.addEventListener("popstate", preventBack);

    return () => {
      window.removeEventListener("popstate", preventBack);
    };
  }, []);

  const preventBack = () => {
    window.history.pushState(null, "", window.location.href);
  };

  const handleGoHome = () => {
    navigate(PATHS.HOME, { replace: true });
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-primary/5 via-background to-secondary/5">
      <Card className="w-full max-w-md shadow-2xl border-2">
        <CardContent className="p-8">
          <VStack spacing={6} align="center">
            {/* Processing icon */}
            <div className="relative">
              <div className="absolute inset-0 bg-blue-500/20 rounded-full blur-2xl animate-pulse" />
              <div className="relative bg-gradient-to-br from-blue-400 to-blue-600 p-6 rounded-full shadow-lg">
                <Clock className="w-16 h-16 text-white animate-pulse" strokeWidth={2.5} />
              </div>
            </div>

            {/* Title */}
            <div className="text-center">
              <h1 className="text-3xl font-bold bg-gradient-to-r from-blue-600 to-blue-500 bg-clip-text text-transparent mb-2">
                {t("paymentSuccess.title")}
              </h1>
              <p className="text-muted-foreground text-sm">
                {t("paymentSuccess.subtitle")}
              </p>
            </div>

            {/* Info message - Paiement en cours */}
            <Card className="w-full bg-blue-50 dark:bg-blue-950/20 border-blue-200 dark:border-blue-800">
              <CardContent className="p-4">
                <VStack spacing={3}>
                  <HStack spacing={2} align="start">
                    <AlertCircle className="w-5 h-5 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-0.5" />
                    <div>
                      <p className="text-sm text-blue-800 dark:text-blue-200 font-medium">
                        {t("paymentSuccess.processing")}
                      </p>
                      <p className="text-xs text-blue-700 dark:text-blue-300 mt-1">
                        {t("paymentSuccess.processingDescription")}
                      </p>
                    </div>
                  </HStack>
                </VStack>
              </CardContent>
            </Card>

            {/* Email notification info */}
            <Card className="w-full bg-amber-50 dark:bg-amber-950/20 border-amber-200 dark:border-amber-800">
              <CardContent className="p-4">
                <VStack spacing={3}>
                  <HStack spacing={2} align="start">
                    <Mail className="w-5 h-5 text-amber-600 dark:text-amber-400 flex-shrink-0 mt-0.5" />
                    <div>
                      <p className="text-sm text-amber-800 dark:text-amber-200 font-medium">
                        {t("paymentSuccess.emailTitle")}
                      </p>
                      <p className="text-xs text-amber-700 dark:text-amber-300 mt-1">
                        {t("paymentSuccess.emailNotice")}
                      </p>
                      <ul className="text-xs text-amber-700 dark:text-amber-300 mt-2 space-y-1 list-none pl-0">
                        <li className="flex items-start gap-1">
                          <span className="flex-shrink-0">✅</span>
                          <span>{t("paymentSuccess.emailConfirm")}</span>
                        </li>
                        <li className="flex items-start gap-1">
                          <span className="flex-shrink-0">❌</span>
                          <span>{t("paymentSuccess.emailFailed")}</span>
                        </li>
                      </ul>
                    </div>
                  </HStack>
                </VStack>
              </CardContent>
            </Card>

            {/* Order tracking */}
            <div className="w-full bg-muted/50 rounded-lg p-3">
              <p className="text-xs text-muted-foreground text-center">
                {t("paymentSuccess.orderTracking")}
              </p>
            </div>

            {/* Back home button */}
            <Button
              onClick={handleGoHome}
              size="lg"
              className="w-full bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 shadow-lg hover:shadow-xl transition-all duration-300"
            >
              <HStack spacing={2} align="center">
                <Home className="w-5 h-5" />
                <span>{t("paymentSuccess.backHome")}</span>
              </HStack>
            </Button>

            {/* Footer */}
            <div className="text-center pt-4 border-t w-full">
              <p className="text-xs text-muted-foreground">
                {t("paymentSuccess.thankYou")}
              </p>
            </div>
          </VStack>
        </CardContent>
      </Card>
    </div>
  );
}
