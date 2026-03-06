import { useNavigate } from "react-router-dom";
import { Card, CardContent } from "@components/ui/card";
import { Button } from "@components/ui/button";
import { CheckCircle2, Home } from "lucide-react";
import VStack from "@components/VStack";
import HStack from "@components/HStack";
import { PATHS } from "@/common/constants/path";
import { useEffect } from "react";

/**
 * Page de confirmation de paiement initialisé avec succès
 */
export default function PaymentSuccess() {
  const navigate = useNavigate();

  useEffect(() => {
    // Empêcher le retour arrière
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
            {/* Icône de succès animée */}
            <div className="relative">
              <div className="absolute inset-0 bg-green-500/20 rounded-full blur-2xl animate-pulse" />
              <div className="relative bg-gradient-to-br from-green-400 to-green-600 p-6 rounded-full shadow-lg">
                <CheckCircle2 className="w-16 h-16 text-white" strokeWidth={2.5} />
              </div>
            </div>

            {/* Titre */}
            <div className="text-center">
              <h1 className="text-3xl font-bold bg-gradient-to-r from-green-600 to-green-500 bg-clip-text text-transparent mb-2">
                Paiement initialisé !
              </h1>
              <p className="text-muted-foreground text-sm">
                Votre transaction a été enregistrée avec succès
              </p>
            </div>

            {/* Message informatif */}
            <Card className="w-full bg-green-50 dark:bg-green-950/20 border-green-200 dark:border-green-800">
              <CardContent className="p-4">
                <VStack spacing={2}>
                  <p className="text-sm text-green-800 dark:text-green-200 font-medium">
                    ✓ Votre paiement est en cours de traitement
                  </p>
                  <p className="text-xs text-green-700 dark:text-green-300">
                    Vous recevrez une confirmation par email dans quelques instants.
                  </p>
                  <p className="text-xs text-green-700 dark:text-green-300">
                    Vous pouvez consulter l'état de votre commande dans votre profil.
                  </p>
                </VStack>
              </CardContent>
            </Card>

            {/* Bouton retour à l'accueil */}
            <Button
              onClick={handleGoHome}
              size="lg"
              className="w-full bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 shadow-lg hover:shadow-xl transition-all duration-300"
            >
              <HStack spacing={2} align="center">
                <Home className="w-5 h-5" />
                <span>Retour à l'accueil</span>
              </HStack>
            </Button>

            {/* Footer */}
            <div className="text-center pt-4 border-t w-full">
              <p className="text-xs text-muted-foreground">
                Merci pour votre confiance ! 🎉
              </p>
            </div>
          </VStack>
        </CardContent>
      </Card>
    </div>
  );
}

