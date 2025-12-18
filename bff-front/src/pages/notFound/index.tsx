import { Link } from "react-router-dom";
import { Home, Search, ArrowLeft } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { PATHS } from "@/common/constants/path";

export default function NotFound() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-teal-50 flex items-center justify-center p-4">
      <div className="text-center max-w-lg">
        {/* 404 Illustration */}
        <div className="relative mb-8">
          <h1 className="text-[150px] sm:text-[200px] font-black text-primary/10 leading-none select-none">
            404
          </h1>
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="bg-white rounded-full p-6 shadow-xl">
              <Search className="w-12 h-12 text-primary" />
            </div>
          </div>
        </div>

        {/* Message */}
        <h2 className="text-2xl sm:text-3xl font-bold text-foreground mb-4">
          Page introuvable
        </h2>
        <p className="text-muted-foreground mb-8 text-lg">
          Oups ! La page que vous recherchez n'existe pas ou a été déplacée.
        </p>

        {/* Actions */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link to={PATHS.HOME}>
            <Button size="lg" className="w-full sm:w-auto flex items-center gap-2">
              <Home className="w-5 h-5" />
              Retour à l'accueil
            </Button>
          </Link>
          <Button
            size="lg"
            variant="outline"
            onClick={() => window.history.back()}
            className="w-full sm:w-auto flex items-center gap-2"
          >
            <ArrowLeft className="w-5 h-5" />
            Page précédente
          </Button>
        </div>

        {/* Suggestions */}
        <div className="mt-12 text-left bg-white/80 backdrop-blur rounded-lg p-6 shadow-sm">
          <h3 className="font-semibold text-foreground mb-3">Suggestions :</h3>
          <ul className="space-y-2 text-muted-foreground">
            <li className="flex items-center gap-2">
              <span className="w-1.5 h-1.5 bg-primary rounded-full"></span>
              Vérifiez l'URL dans la barre d'adresse
            </li>
            <li className="flex items-center gap-2">
              <span className="w-1.5 h-1.5 bg-primary rounded-full"></span>
              <Link to={PATHS.ALL_CATEGORIES} className="text-primary hover:underline">
                Parcourir nos catégories
              </Link>
            </li>
            <li className="flex items-center gap-2">
              <span className="w-1.5 h-1.5 bg-primary rounded-full"></span>
              <Link to={PATHS.HOME} className="text-primary hover:underline">
                Découvrir les meilleures offres
              </Link>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}
