import React, { useEffect, useMemo, useState, useRef } from "react";
import Grid, { type IColsProp } from "@components/Grid";
import DealCard from "../DealCard";
import Pagination from "@components/Pagination";
import { Search, Filter, X, Sliders } from "lucide-react";
import { cn } from "@lib/utils";

/** Déclare le type de filtre */
interface DealFilters {
  category?: string;
  priceRange?: [number, number];
  city?: string;
  status?: string;
  searchQuery?: string;
}

/** Props du composant list */
interface DealsListProps {
  deals: any[];
  showPagination?: boolean;
  itemsPerPage?: number;
  totalItems?: number;
  totalPages?: number;
  cols?: IColsProp;
  gapX?: number;
  gapY?: number;
  showFilters?: boolean;
  filterPosition?: "top" | "sidebar";
  availableFilters?: ("category" | "price" | "city" | "status" | "search")[];
  onFilterChange?: (filters: DealFilters) => void;
}

/** Debounce hook simple */
function useDebounced<T>(value: T, delay = 350) {
  const [v, setV] = useState(value);
  useEffect(() => {
    const id = setTimeout(() => setV(value), delay);
    return () => clearTimeout(id);
  }, [value, delay]);
  return v;
}

/** Mobile bottom sheet component */
function MobileFilterSheet({
  open,
  onClose,
  children,
  title = "Filtres",
}: {
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
  title?: string;
}) {
  return (
    <>
      {/* backdrop */}
      <div
        className={cn(
          "fixed inset-0 bg-black/40 transition-opacity z-40",
          open
            ? "opacity-100 pointer-events-auto"
            : "opacity-0 pointer-events-none"
        )}
        onClick={onClose}
        aria-hidden={!open}
      />
      <div
        className={cn(
          "fixed left-0 right-0 bottom-0 z-50 transform transition-transform bg-white dark:bg-surface-800 border-t shadow-xl",
          open ? "translate-y-0" : "translate-y-full"
        )}
        style={{ minHeight: "40vh", maxHeight: "90vh", overflow: "auto" }}
      >
        <div className="flex items-center justify-between p-4 border-b">
          <h3 className="text-lg font-semibold">{title}</h3>
          <button onClick={onClose} aria-label="Fermer" className="p-2">
            <X className="w-5 h-5" />
          </button>
        </div>
        <div className="p-4">{children}</div>
      </div>
    </>
  );
}

/** Small filter row control components */
function SelectField({
  label,
  value,
  onChange,
  options,
}: {
  label?: string;
  value?: string;
  onChange: (v: string) => void;
  options: { value: string; label: string }[];
}) {
  return (
    <div className="mb-3">
      {label && (
        <label className="block text-xs text-muted-foreground mb-1">
          {label}
        </label>
      )}
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded border px-3 py-2 bg-transparent"
      >
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}

function RangeField({
  label,
  min,
  max,
  value,
  onChange,
}: {
  label?: string;
  min: number;
  max: number;
  value: [number, number];
  onChange: (v: [number, number]) => void;
}) {
  const [a, b] = value;
  return (
    <div className="mb-3">
      {label && (
        <label className="block text-xs text-muted-foreground mb-1">
          {label}
        </label>
      )}
      <div className="flex gap-2 items-center">
        <input
          type="number"
          min={min}
          max={max}
          value={a}
          onChange={(e) =>
            onChange([Math.min(Number(e.target.value) || min, b), b])
          }
          className="w-1/2 rounded border px-2 py-1"
        />
        <span className="text-sm text-muted-foreground">—</span>
        <input
          type="number"
          min={min}
          max={max}
          value={b}
          onChange={(e) =>
            onChange([a, Math.max(Number(e.target.value) || max, a)])
          }
          className="w-1/2 rounded border px-2 py-1"
        />
      </div>
      <div className="text-xs text-muted-foreground mt-1">
        Min: {min} — Max: {max}
      </div>
    </div>
  );
}

/** The Filters Panel (used both in sidebar & sheet) */
function FiltersPanel({
  filters,
  setFilters,
  availableFilters,
  onReset,
}: {
  filters: DealFilters;
  setFilters: (f: DealFilters) => void;
  availableFilters: ("category" | "price" | "city" | "status" | "search")[];
  onReset: () => void;
}) {
  // Replace these by real options from your API if available
  const categories = [
    { value: "all", label: "Toutes les catégories" },
    { value: "clim", label: "Climatiseurs" },
    { value: "ventilo", label: "Ventilateurs" },
  ];
  const cities = [
    { value: "all", label: "Toutes les villes" },
    { value: "yaounde", label: "Yaoundé" },
    { value: "douala", label: "Douala" },
  ];
  const statuses = [
    { value: "all", label: "Tous" },
    { value: "active", label: "Actif" },
    { value: "soldout", label: "Épuisé" },
  ];

  return (
    <div>
      {/* Search */}
      {availableFilters.includes("search") && (
        <div className="mb-4">
          <label className="text-xs text-muted-foreground mb-2 block">
            Rechercher
          </label>
          <div className="flex items-center gap-2">
            <Search className="w-4 h-4 text-muted-foreground" />
            <input
              type="search"
              value={filters.searchQuery || ""}
              onChange={(e) =>
                setFilters({ ...filters, searchQuery: e.target.value })
              }
              placeholder="Chercher un produit..."
              className="flex-1 rounded border px-3 py-2 bg-transparent"
            />
            <button
              onClick={() => setFilters({ ...filters, searchQuery: "" })}
              className="px-3 py-2 text-sm text-muted-foreground"
              aria-label="Clear search"
            >
              Effacer
            </button>
          </div>
        </div>
      )}

      {/* Category */}
      {availableFilters.includes("category") && (
        <SelectField
          label="Catégorie"
          value={filters.category ?? "all"}
          onChange={(v) => setFilters({ ...filters, category: v })}
          options={categories}
        />
      )}

      {/* City */}
      {availableFilters.includes("city") && (
        <SelectField
          label="Ville"
          value={filters.city ?? "all"}
          onChange={(v) => setFilters({ ...filters, city: v })}
          options={cities}
        />
      )}

      {/* Status */}
      {availableFilters.includes("status") && (
        <SelectField
          label="Etat"
          value={filters.status ?? "all"}
          onChange={(v) => setFilters({ ...filters, status: v })}
          options={statuses}
        />
      )}

      {/* Price */}
      {availableFilters.includes("price") && (
        <RangeField
          label="Prix (€)"
          min={0}
          max={5000}
          value={filters.priceRange ?? [0, 300]}
          onChange={(v) => setFilters({ ...filters, priceRange: v })}
        />
      )}

      <div className="flex items-center gap-2 mt-4">
        <button
          onClick={() => onReset()}
          className="px-3 py-2 rounded border bg-transparent text-sm"
        >
          Réinitialiser
        </button>
        <div className="flex-1" />
      </div>
    </div>
  );
}

/** Component principal */
export default function DealsList({
  cols = { base: 1, md: 2, lg: 4 },
  deals,
  showPagination = true,
  totalItems = 0,
  totalPages = 0,
  itemsPerPage = 24,
  showFilters = true,
  filterPosition = "sidebar",
  availableFilters = ["search", "category", "price", "city", "status"],
  onFilterChange,
}: DealsListProps) {
  const [currentPage, setCurrentPage] = useState(1);
  const [filters, setFilters] = useState<DealFilters>({
    category: "all",
    priceRange: [0, 300],
    city: "all",
    status: "all",
    searchQuery: "",
  });

  // Mobile sheet state
  const [mobileOpen, setMobileOpen] = useState(false);

  // Debounce search & notify parent when filters change
  const debouncedFilters = useDebounced(filters, 300);
  const didMountRef = useRef(false);
  useEffect(() => {
    if (didMountRef.current) {
      onFilterChange?.(debouncedFilters);
    } else {
      didMountRef.current = true;
    }
  }, [debouncedFilters, onFilterChange]);

  // Reset handler
  const resetFilters = () =>
    setFilters({
      category: "all",
      priceRange: [0, 300],
      city: "all",
      status: "all",
      searchQuery: "",
    });

  // Pagination derived values
  const _totalPages = useMemo(() => {
    return totalPages > 0 ? totalPages : Math.ceil(totalItems / itemsPerPage);
  }, [totalItems, itemsPerPage, totalPages]);

  const _deals = useMemo(() => {
    // NOTE: server side filtering/pagination recommended; this is local slice/example
    // Apply local filtering for demo (basic)
    let dataset = [...deals];

    // search
    if (filters.searchQuery && filters.searchQuery.trim() !== "") {
      const q = filters.searchQuery.toLowerCase();
      dataset = dataset.filter((d) =>
        (d.title || "").toLowerCase().includes(q)
      );
    }
    // category
    if (filters.category && filters.category !== "all") {
      dataset = dataset.filter((d) => d.category === filters.category);
    }
    // city
    if (filters.city && filters.city !== "all") {
      dataset = dataset.filter((d) => d.city === filters.city);
    }
    // status
    if (filters.status && filters.status !== "all") {
      dataset = dataset.filter((d) => d.status === filters.status);
    }
    // price range
    if (filters.priceRange) {
      dataset = dataset.filter(
        (d) =>
          d.groupPrice >= filters.priceRange![0] &&
          d.groupPrice <= filters.priceRange![1]
      );
    }

    // pagination slice
    const start = (currentPage - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    return dataset.slice(start, end);
  }, [deals, filters, currentPage, itemsPerPage]);

  // If filterPosition === "top" we render filters above grid
  const renderFiltersInline = filterPosition === "top";

  return (
    <section className="py-8">
      <div className=" mx-auto">
        {/* Header: mobile bottom bar + top filters */}
        <div className="mb-4 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <h2 className="text-2xl font-semibold">Offres</h2>
            <span className="text-sm text-muted-foreground">
              {totalItems} résultats
            </span>
          </div>

          {/* Desktop: show small filters button */}
          <div className="hidden md:flex items-center gap-3">
            {showFilters && (
              <button
                onClick={() => {
                  // If sidebar available, scroll to it or toggle? we keep as no-op
                }}
                className="inline-flex items-center gap-2 px-3 py-2 rounded border"
                aria-label="Filtres"
              >
                <Filter className="w-4 h-4" />
                <span className="text-sm">Filtres</span>
              </button>
            )}
          </div>

          {/* Mobile: bottom tab trigger (we render bottom sheet activation button) */}
          <div className="md:hidden flex items-center gap-2">
            <button
              onClick={() => setMobileOpen(true)}
              className="inline-flex items-center gap-2 px-3 py-2 rounded border"
              aria-label="Ouvrir les filtres"
            >
              <Sliders className="w-4 h-4" />
              <span className="text-sm">Filtres</span>
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-12 gap-6">
          {/* Sidebar filters (desktop) */}
          {showFilters && !renderFiltersInline && (
            <aside className="hidden md:block md:col-span-3 lg:col-span-3">
              <div className="sticky top-20 bg-transparent p-4 border rounded">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="font-medium">Filtres</h3>
                  <button
                    onClick={resetFilters}
                    className="text-sm text-muted-foreground"
                  >
                    Réinitialiser
                  </button>
                </div>
                <FiltersPanel
                  filters={filters}
                  setFilters={setFilters}
                  availableFilters={availableFilters}
                  onReset={resetFilters}
                />
              </div>
            </aside>
          )}

          {/* Main content */}
          <div className={cn("col-span-1 md:col-span-9 lg:col-span-9")}>
            {/* Top inline filters (if filterPosition === 'top') */}
            {showFilters && renderFiltersInline && (
              <div className="mb-6 p-4 border rounded bg-surface-50">
                <FiltersPanel
                  filters={filters}
                  setFilters={setFilters}
                  availableFilters={availableFilters}
                  onReset={resetFilters}
                />
              </div>
            )}

            {/* Grid */}
            <Grid cols={cols} gap="gap-8">
              {_deals.map((deal, idx) => (
                <DealCard key={deal.id ?? idx} deal={deal} />
              ))}
            </Grid>

            {/* Pagination */}
            {showPagination && (
              <div className="mt-6">
                <Pagination
                  page={currentPage}
                  totalPages={_totalPages}
                  onChange={(p) => setCurrentPage(p)}
                  perPage={itemsPerPage}
                  totalItems={totalItems}
                />
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Mobile bottom sheet for filters */}
      <MobileFilterSheet open={mobileOpen} onClose={() => setMobileOpen(false)}>
        <FiltersPanel
          filters={filters}
          setFilters={(f) => setFilters(f)}
          availableFilters={availableFilters}
          onReset={() => {
            resetFilters();
            setMobileOpen(false);
          }}
        />
        <div className="flex gap-2 mt-4">
          <button
            onClick={() => {
              setMobileOpen(false);
            }}
            className="flex-1 px-4 py-2 rounded border"
          >
            Annuler
          </button>
          <button
            onClick={() => {
              // apply & close: onFilterChange will be triggered by debounced effect
              setMobileOpen(false);
            }}
            className="flex-1 px-4 py-2 rounded bg-primary-500 text-white"
          >
            Appliquer
          </button>
        </div>
      </MobileFilterSheet>
    </section>
  );
}
