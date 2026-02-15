import { useEffect, useState } from "react";
import { Heart, Trash2, ShoppingBag } from "lucide-react";
import { useDealsByStatut } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import { StatutDeal } from "@/common/api/types/deal";
import DealCard from "@/common/containers/DealCard";
import { Button } from "@/common/components/ui/button";
import { Link } from "react-router-dom";
import { PATHS } from "@/common/constants/path";
import { useI18n } from "@hooks/useI18n";

export default function Favorites() {
  const { t } = useI18n();
  const { data: dealsData, isLoading } = useDealsByStatut(StatutDeal.PUBLIE);
  const deals = (dealsData ?? []).map(mapDealToView);
  const [favoriteIds, setFavoriteIds] = useState<string[]>([]);

  useEffect(() => {
    if (favoriteIds.length === 0 && deals.length > 0) {
      setFavoriteIds(deals.slice(0, 4).map((d: any) => String(d.id)));
    }
  }, [deals, favoriteIds.length]);

  const favoriteDeals = deals.filter((deal: any) =>
    favoriteIds.includes(String(deal.id)),
  );

  const removeFavorite = (id: string) => {
    setFavoriteIds((prev) => prev.filter((fid) => fid !== String(id)));
  };

  const clearAll = () => {
    setFavoriteIds([]);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
            <Heart className="w-8 h-8 text-red-500 fill-red-500" />
            {t("favorites.title")}
          </h1>
          <p className="text-muted-foreground mt-2">
            {favoriteDeals.length === 1
              ? t("favorites.count", { count: favoriteDeals.length })
              : t("favorites.countPlural", { count: favoriteDeals.length })}
          </p>
        </div>

        {favoriteDeals.length > 0 && (
          <Button
            variant="outline"
            onClick={clearAll}
            className="flex items-center gap-2"
          >
            <Trash2 className="w-4 h-4" />
            {t("favorites.removeAll")}
          </Button>
        )}
      </div>

      {isLoading ? (
        <div className="text-center py-16 text-muted-foreground">
          Chargement...
        </div>
      ) : favoriteDeals.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {favoriteDeals.map((deal: any) => (
            <div key={deal.id} className="relative group">
              <DealCard deal={deal as any} />
              <button
                onClick={() => removeFavorite(String(deal.id))}
                className="absolute top-3 right-3 p-2 bg-white/90 hover:bg-red-50 rounded-full shadow-md transition-all opacity-0 group-hover:opacity-100"
                title={t("favorites.removeFromFavorites")}
              >
                <Trash2 className="w-4 h-4 text-red-500" />
              </button>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-16 bg-card border border-border rounded-lg">
          <Heart className="w-20 h-20 text-muted-foreground mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-foreground mb-2">
            {t("favorites.noFavorites")}
          </h2>
          <p className="text-muted-foreground mb-6 max-w-md mx-auto">
            {t("favorites.noFavoritesHint")}
          </p>
          <Link to={PATHS.HOME}>
            <Button className="flex items-center gap-2">
              <ShoppingBag className="w-5 h-5" />
              {t("favorites.discoverOffers")}
            </Button>
          </Link>
        </div>
      )}

      {favoriteDeals.length > 0 && (
        <div className="mt-12 bg-primary-50 dark:bg-primary/10 rounded-lg p-6">
          <h3 className="font-semibold text-foreground mb-2">
            💡 {t("favorites.tip")}
          </h3>
          <p className="text-muted-foreground text-sm">
            {t("favorites.tipMessage")}
          </p>
        </div>
      )}
    </div>
  );
}
