import { useEffect, useState } from "react";
import { Heart, Trash2, ShoppingBag } from "lucide-react";
import { useDeals } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import DealCard from "@/common/containers/DealCard";
import { Button } from "@/common/components/ui/button";
import { Link } from "react-router-dom";
import { PATHS } from "@/common/constants/path";
import Grid from "@/common/components/Grid";
import { Heading } from "@/common/containers/Heading";

export default function Favorites() {
  const { data: dealsData, isLoading } = useDeals();
  const deals = (dealsData ?? []).map(mapDealToView);
  const [favoriteIds, setFavoriteIds] = useState<string[]>([]);

  useEffect(() => {
    if (favoriteIds.length === 0 && deals.length > 0) {
      setFavoriteIds(deals.slice(0, 4).map((d: any) => String(d.id)));
    }
  }, [deals, favoriteIds.length]);

  const favoriteDeals = deals.filter((deal: any) =>
    favoriteIds.includes(String(deal.id))
  );

  const removeFavorite = (id: string) => {
    setFavoriteIds((prev) => prev.filter((fid) => fid !== String(id)));
  };

  const clearAll = () => {
    setFavoriteIds([]);
  };

  return (
    <div className="">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <Heading
            title="Favoris"
            description={`${favoriteDeals.length} offre${
              favoriteDeals.length !== 1 ? "s" : ""
            } sauvegardée${favoriteDeals.length !== 1 ? "s" : ""}`}
            underline
          />
        </div>

        {favoriteDeals.length > 0 && (
          <Button
            variant="outline"
            onClick={clearAll}
            className="flex items-center gap-2"
          >
            <Trash2 className="w-4 h-4" />
            Tout supprimer
          </Button>
        )}
      </div>

      {/* Favorites Grid */}
      {isLoading ? (
        <div className="text-center py-8 text-muted-foreground">Chargement...</div>
      ) : favoriteDeals.length > 0 ? (
        <Grid cols={{ md: 3 }} gap={10}>
          {favoriteDeals.map((deal) => (
            <div key={deal.id} className="relative group">
              <DealCard deal={deal} />
              <button
                onClick={() => removeFavorite(String(deal.id))}
                className="absolute top-3 right-3 p-2 bg-white/90 hover:bg-red-50 rounded-full shadow-md transition-all opacity-0 group-hover:opacity-100"
                title="Retirer des favoris"
              >
                <Trash2 className="w-4 h-4 text-red-500" />
              </button>
            </div>
          ))}
        </Grid>
      ) : (
        <div className="text-center py-16 bg-card border border-border rounded-lg">
          <Heart className="w-20 h-20 text-muted-foreground mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-foreground mb-2">
            Aucun favori pour le moment
          </h2>
          <p className="text-muted-foreground mb-6 max-w-md mx-auto">
            Parcourez nos offres et cliquez sur le cœur pour sauvegarder vos
            deals préférés.
          </p>
          <Link to={PATHS.HOME}>
            <Button className="flex items-center gap-2">
              <ShoppingBag className="w-5 h-5" />
              Découvrir les offres
            </Button>
          </Link>
        </div>
      )}

      {/* Tips Section */}
      {favoriteDeals.length > 0 && (
        <div className="mt-12 bg-primary-50 dark:bg-primary/10 rounded-lg p-6">
          <h3 className="font-semibold text-foreground mb-2">💡 Astuce</h3>
          <p className="text-muted-foreground text-sm">
            Les offres sont à durée limitée ! N'attendez pas trop longtemps pour
            profiter de vos favoris.
          </p>
        </div>
      )}
    </div>
  );
}
