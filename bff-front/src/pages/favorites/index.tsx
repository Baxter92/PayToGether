import { useState } from "react";
import { Heart, Trash2, ShoppingBag } from "lucide-react";
import { mockDeals } from "@/common/constants/data";
import DealCard from "@/common/containers/DealCard";
import { Button } from "@/common/components/ui/button";
import { Link } from "react-router-dom";
import { PATHS } from "@/common/constants/path";

// Simulate saved favorites (first 4 deals for demo)
const initialFavorites = mockDeals.slice(0, 4).map((d) => d.id);

export default function Favorites() {
  const [favoriteIds, setFavoriteIds] = useState<number[]>(initialFavorites);

  const favoriteDeals = mockDeals.filter((deal) => favoriteIds.includes(deal.id));

  const removeFavorite = (id: number) => {
    setFavoriteIds((prev) => prev.filter((fid) => fid !== id));
  };

  const clearAll = () => {
    setFavoriteIds([]);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
            <Heart className="w-8 h-8 text-red-500 fill-red-500" />
            Mes Favoris
          </h1>
          <p className="text-muted-foreground mt-2">
            {favoriteDeals.length} offre{favoriteDeals.length !== 1 ? "s" : ""} sauvegardée
            {favoriteDeals.length !== 1 ? "s" : ""}
          </p>
        </div>

        {favoriteDeals.length > 0 && (
          <Button variant="outline" onClick={clearAll} className="flex items-center gap-2">
            <Trash2 className="w-4 h-4" />
            Tout supprimer
          </Button>
        )}
      </div>

      {/* Favorites Grid */}
      {favoriteDeals.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {favoriteDeals.map((deal) => (
            <div key={deal.id} className="relative group">
              <DealCard deal={deal} />
              <button
                onClick={() => removeFavorite(deal.id)}
                className="absolute top-3 right-3 p-2 bg-white/90 hover:bg-red-50 rounded-full shadow-md transition-all opacity-0 group-hover:opacity-100"
                title="Retirer des favoris"
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
            Aucun favori pour le moment
          </h2>
          <p className="text-muted-foreground mb-6 max-w-md mx-auto">
            Parcourez nos offres et cliquez sur le cœur pour sauvegarder vos deals préférés.
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
            Les offres sont à durée limitée ! N'attendez pas trop longtemps pour profiter de vos favoris.
          </p>
        </div>
      )}
    </div>
  );
}
