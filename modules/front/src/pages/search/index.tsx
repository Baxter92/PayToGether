import { useState, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import { Search, SlidersHorizontal, X, MapPin } from "lucide-react";
import { mockDeals, categories } from "@/common/constants/data";
import DealCard from "@/common/containers/DealCard";
import { Button } from "@/common/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/common/components/ui/select";
import { useI18n } from "@hooks/useI18n";

const locations = ["Douala", "Yaoundé", "Bafoussam", "Garoua"];

export default function SearchPage() {
  const { t } = useI18n("search");
  const [searchParams, setSearchParams] = useSearchParams();
  const query = searchParams.get("q") || "";
  const categoryParam = searchParams.get("category") || "";
  const locationParam = searchParams.get("location") || "";

  const [searchInput, setSearchInput] = useState(query);
  const [selectedCategory, setSelectedCategory] = useState(categoryParam);
  const [selectedLocation, setSelectedLocation] = useState(locationParam);
  const [sortBy, setSortBy] = useState("popular");
  const [showFilters, setShowFilters] = useState(false);

  const sortOptions = [
    { value: "popular", label: t("popularity") },
    { value: "price-asc", label: t("priceAsc") },
    { value: "price-desc", label: t("priceDesc") },
    { value: "discount", label: t("bestDiscount") }
  ];

  const filteredDeals = useMemo(() => {
    let results = [...mockDeals];

    // Filter by search query
    if (query) {
      const lowerQuery = query.toLowerCase();
      results = results.filter(
        (deal) =>
          deal.title.toLowerCase().includes(lowerQuery) ||
          deal.category.toLowerCase().includes(lowerQuery)
      );
    }

    // Filter by category
    if (selectedCategory) {
      results = results.filter((deal) => deal.category === selectedCategory);
    }

    // Filter by location
    if (selectedLocation) {
      results = results.filter((deal) => deal.city === selectedLocation);
    }

    // Sort
    switch (sortBy) {
      case "price-asc":
        results.sort((a, b) => a.groupPrice - b.groupPrice);
        break;
      case "price-desc":
        results.sort((a, b) => b.groupPrice - a.groupPrice);
        break;
      case "discount":
        results.sort((a, b) => b.discount - a.discount);
        break;
      default:
        results.sort((a, b) => b.sold - a.sold);
    }

    return results;
  }, [query, selectedCategory, selectedLocation, sortBy]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const params = new URLSearchParams();
    if (searchInput) params.set("q", searchInput);
    if (selectedCategory) params.set("category", selectedCategory);
    if (selectedLocation) params.set("location", selectedLocation);
    setSearchParams(params);
  };

  const clearFilters = () => {
    setSelectedCategory("");
    setSelectedLocation("");
    setSortBy("popular");
    setSearchParams(query ? { q: query } : {});
  };

  const hasActiveFilters = selectedCategory || selectedLocation;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Search Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-foreground mb-4">
          {query ? `${t("resultsFor")} "${query}"` : t("title")}
        </h1>

        <form onSubmit={handleSearch} className="flex gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder={t("placeholder")}
              className="w-full pl-11 pr-4 py-3 border border-border rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition bg-background"
            />
          </div>
          <Button type="submit" className="px-6">
            {t("searchButton")}
          </Button>
          <Button
            type="button"
            variant="outline"
            onClick={() => setShowFilters(!showFilters)}
            className="flex items-center gap-2"
          >
            <SlidersHorizontal className="w-4 h-4" />
            {t("filters")}
            {hasActiveFilters && (
              <span className="bg-primary text-primary-foreground text-xs px-2 py-0.5 rounded-full">
                {[selectedCategory, selectedLocation].filter(Boolean).length}
              </span>
            )}
          </Button>
        </form>
      </div>

      {/* Filters */}
      {showFilters && (
        <div className="bg-card border border-border rounded-lg p-4 mb-6">
          <div className="flex flex-wrap gap-4 items-end">
            <div className="flex-1 min-w-[200px]">
              <label className="block text-sm font-medium text-foreground mb-2">
                {t("category")}
              </label>
              <Select
                value={selectedCategory}
                onValueChange={setSelectedCategory}
              >
                <SelectTrigger>
                  <SelectValue placeholder={t("allCategories")} />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">{t("allCategories")}</SelectItem>
                  {categories.map((cat) => (
                    <SelectItem key={cat.id} value={cat.name}>
                      {cat.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex-1 min-w-[200px]">
              <label className="block text-sm font-medium text-foreground mb-2">
                {t("location")}
              </label>
              <Select
                value={selectedLocation}
                onValueChange={setSelectedLocation}
              >
                <SelectTrigger>
                  <SelectValue placeholder={t("allCities")} />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">{t("allCities")}</SelectItem>
                  {locations.map((loc) => (
                    <SelectItem key={loc} value={loc}>
                      <span className="flex items-center gap-2">
                        <MapPin className="w-4 h-4" />
                        {loc}
                      </span>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex-1 min-w-[200px]">
              <label className="block text-sm font-medium text-foreground mb-2">
                {t("sortBy")}
              </label>
              <Select value={sortBy} onValueChange={setSortBy}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {sortOptions.map((opt) => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {hasActiveFilters && (
              <Button
                variant="ghost"
                onClick={clearFilters}
                className="flex items-center gap-2"
              >
                <X className="w-4 h-4" />
                {t("clearFilters")}
              </Button>
            )}
          </div>
        </div>
      )}

      {/* Results Count */}
      <p className="text-muted-foreground mb-6">
        {filteredDeals.length}{" "}
        {filteredDeals.length !== 1 ? t("resultsPlural") : t("results")}{" "}
        {filteredDeals.length !== 1 ? t("foundPlural") : t("found")}
      </p>

      {/* Results Grid */}
      {filteredDeals.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredDeals.map((deal) => (
            <DealCard key={deal.id} deal={deal} />
          ))}
        </div>
      ) : (
        <div className="text-center py-16">
          <Search className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
          <h2 className="text-xl font-semibold text-foreground mb-2">
            {t("noResults")}
          </h2>
          <p className="text-muted-foreground mb-6">{t("noResultsHint")}</p>
          <Button onClick={clearFilters} variant="outline">
            {t("resetSearch")}
          </Button>
        </div>
      )}
    </div>
  );
}
