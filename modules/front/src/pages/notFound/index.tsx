import { Link } from "react-router-dom";
import { Home, Search, ArrowLeft } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { PATHS } from "@/common/constants/path";
import { useI18n } from "@hooks/useI18n";

export default function NotFound() {
  const { t } = useI18n();

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-teal-50 flex items-center justify-center p-4">
      <div className="text-center max-w-lg">
        <div className="relative mb-8">
          <h1 className="text-[150px] sm:text-[200px] font-black text-primary/10 leading-none select-none">
            {t("notFound.title")}
          </h1>
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="bg-white rounded-full p-6 shadow-xl">
              <Search className="w-12 h-12 text-primary" />
            </div>
          </div>
        </div>

        <h2 className="text-2xl sm:text-3xl font-bold text-foreground mb-4">
          {t("notFound.heading")}
        </h2>
        <p className="text-muted-foreground mb-8 text-lg">
          {t("notFound.message")}
        </p>

        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link to={PATHS.HOME}>
            <Button size="lg" className="w-full sm:w-auto flex items-center gap-2">
              <Home className="w-5 h-5" />
              {t("notFound.backToHome")}
            </Button>
          </Link>
          <Button
            size="lg"
            variant="outline"
            onClick={() => window.history.back()}
            className="w-full sm:w-auto flex items-center gap-2"
          >
            <ArrowLeft className="w-5 h-5" />
            {t("notFound.previousPage")}
          </Button>
        </div>

        <div className="mt-12 text-left bg-white/80 backdrop-blur rounded-lg p-6 shadow-sm">
          <h3 className="font-semibold text-foreground mb-3">{t("notFound.suggestions")}</h3>
          <ul className="space-y-2 text-muted-foreground">
            <li className="flex items-center gap-2">
              <span className="w-1.5 h-1.5 bg-primary rounded-full"></span>
              {t("notFound.checkUrl")}
            </li>
            <li className="flex items-center gap-2">
              <span className="w-1.5 h-1.5 bg-primary rounded-full"></span>
              <Link to={PATHS.ALL_CATEGORIES} className="text-primary hover:underline">
                {t("notFound.browseCategories")}
              </Link>
            </li>
            <li className="flex items-center gap-2">
              <span className="w-1.5 h-1.5 bg-primary rounded-full"></span>
              <Link to={PATHS.HOME} className="text-primary hover:underline">
                {t("notFound.discoverBestOffers")}
              </Link>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}
