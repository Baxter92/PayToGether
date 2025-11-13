import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

interface DealsFilterProps {
  filters: {
    category: string;
    priceRange: [number, number];
    city: string;
    status: string;
  };
  setFilters: (filters: any) => void;
  mockDeals: any[];
}

export default function DealsFilter({
  filters,
  setFilters,
  mockDeals,
}: DealsFilterProps) {
  const categories = [
    { value: "all", label: "Tous les deals" },
    { value: "wellness", label: "Bien-être" },
    { value: "food", label: "Restaurant" },
    { value: "tech", label: "Tech" },
    { value: "fashion", label: "Mode" },
    { value: "education", label: "Éducation" },
  ];

  const cities = [
    { value: "all", label: "Toutes les villes" },
    ...Array.from(new Set(mockDeals.map((d) => d.city))).map((city) => ({
      value: city.toLowerCase(),
      label: city,
    })),
  ];

  const statuses = [
    { value: "all", label: "Tous les statuts" },
    { value: "active", label: "En cours" },
    { value: "sold-out", label: "Épuisé" },
  ];

  return (
    <Card className="p-6 sticky top-4 h-fit">
      <h3 className="text-lg font-semibold text-foreground mb-6">Filtres</h3>

      {/* Categories */}
      <div className="mb-6">
        <h4 className="text-sm font-semibold text-foreground mb-3">
          Catégorie
        </h4>
        <div className="space-y-2">
          {categories.map((cat) => (
            <div key={cat.value} className="flex items-center">
              <input
                type="radio"
                id={`cat-${cat.value}`}
                name="category"
                value={cat.value}
                checked={filters.category === cat.value}
                onChange={(e) =>
                  setFilters({ ...filters, category: e.target.value })
                }
                className="w-4 h-4 cursor-pointer"
              />
              <label
                htmlFor={`cat-${cat.value}`}
                className="ml-2 text-sm cursor-pointer text-foreground"
              >
                {cat.label}
              </label>
            </div>
          ))}
        </div>
      </div>

      {/* Price Range */}
      <div className="mb-6">
        <h4 className="text-sm font-semibold text-foreground mb-3">Prix</h4>
        <div className="space-y-2">
          <div>
            <label className="text-sm text-muted-foreground">
              Jusqu'à {filters.priceRange[1]}€
            </label>
            <input
              type="range"
              min="0"
              max="300"
              value={filters.priceRange[1]}
              onChange={(e) =>
                setFilters({
                  ...filters,
                  priceRange: [
                    filters.priceRange[0],
                    Number.parseInt(e.target.value),
                  ],
                })
              }
              className="w-full"
            />
          </div>
        </div>
      </div>

      {/* Cities */}
      <div className="mb-6">
        <h4 className="text-sm font-semibold text-foreground mb-3">Ville</h4>
        <div className="space-y-2">
          {cities.map((city) => (
            <div key={city.value} className="flex items-center">
              <input
                type="radio"
                id={`city-${city.value}`}
                name="city"
                value={city.value}
                checked={filters.city === city.value}
                onChange={(e) =>
                  setFilters({ ...filters, city: e.target.value })
                }
                className="w-4 h-4 cursor-pointer"
              />
              <label
                htmlFor={`city-${city.value}`}
                className="ml-2 text-sm cursor-pointer text-foreground"
              >
                {city.label}
              </label>
            </div>
          ))}
        </div>
      </div>

      {/* Status */}
      <div className="mb-6">
        <h4 className="text-sm font-semibold text-foreground mb-3">Statut</h4>
        <div className="space-y-2">
          {statuses.map((status) => (
            <div key={status.value} className="flex items-center">
              <input
                type="radio"
                id={`status-${status.value}`}
                name="status"
                value={status.value}
                checked={filters.status === status.value}
                onChange={(e) =>
                  setFilters({ ...filters, status: e.target.value })
                }
                className="w-4 h-4 cursor-pointer"
              />
              <label
                htmlFor={`status-${status.value}`}
                className="ml-2 text-sm cursor-pointer text-foreground"
              >
                {status.label}
              </label>
            </div>
          ))}
        </div>
      </div>

      {/* Reset Button */}
      <Button
        variant="outline"
        className="w-full bg-transparent"
        onClick={() =>
          setFilters({
            category: "all",
            priceRange: [0, 300],
            city: "all",
            status: "all",
          })
        }
      >
        Réinitialiser
      </Button>
    </Card>
  );
}
