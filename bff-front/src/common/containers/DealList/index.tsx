import React, { useEffect, useMemo, useState, useRef } from "react";
import Grid, { type IColsProp } from "@components/Grid";
import DealCard from "../DealCard";
import Pagination from "@components/Pagination";
import { X, Sliders } from "lucide-react";
import { cn } from "@lib/utils";
import Form, { type IFieldConfig } from "@containers/Form";
import { z } from "zod";
import VStack from "@components/VStack";
import HStack from "@components/HStack";
import { Button } from "@/common/components/ui/button";

/** Déclare le type de filtre */
interface DealFilters {
  category?: string;
  priceMin?: number;
  priceMax?: number;
  city?: string;
  status?: string;
  searchQuery?: string;
}

/** Props du composant list */
interface IDealsListProps {
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
  className?: string;
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
        <HStack className="items-center justify-between p-4 border-b">
          <h3 className="text-lg font-semibold">{title}</h3>
          <button onClick={onClose} aria-label="Fermer" className="p-2">
            <X className="w-5 h-5" />
          </button>
        </HStack>
        <div className="p-4">{children}</div>
      </div>
    </>
  );
}

/** Component principal */
export default function DealsList({
  cols = { md: 2, lg: 3, xl: 4 },
  deals,
  showPagination = true,
  totalItems = 0,
  totalPages = 0,
  itemsPerPage = 24,
  showFilters = true,
  filterPosition = "sidebar",
  availableFilters = ["search", "category", "price", "city", "status"],
  onFilterChange,
  className,
}: IDealsListProps) {
  const [currentPage, setCurrentPage] = useState(1);
  const [filters, setFilters] = useState<DealFilters>({
    category: "all",
    priceMin: 0,
    priceMax: 300,
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
      priceMin: 0,
      priceMax: 300,
      city: "all",
      status: "all",
      searchQuery: "",
    });

  // Configuration des champs du formulaire de filtres
  const filterFields: IFieldConfig[] = useMemo(() => {
    const fields: IFieldConfig[] = [];

    if (availableFilters.includes("search")) {
      fields.push({
        name: "searchQuery",
        label: "Rechercher",
        type: "text",
        placeholder: "Chercher un produit...",
      });
    }

    if (availableFilters.includes("category")) {
      fields.push({
        name: "category",
        label: "Catégorie",
        type: "select",
        items: [
          { value: "all", label: "Toutes les catégories" },
          { value: "clim", label: "Climatiseurs" },
          { value: "ventilo", label: "Ventilateurs" },
        ],
      });
    }

    if (availableFilters.includes("city")) {
      fields.push({
        name: "city",
        label: "Ville",
        type: "select",
        items: [
          { value: "all", label: "Toutes les villes" },
          { value: "yaounde", label: "Yaoundé" },
          { value: "douala", label: "Douala" },
        ],
      });
    }

    if (availableFilters.includes("status")) {
      fields.push({
        name: "status",
        label: "État",
        type: "select",
        items: [
          { value: "all", label: "Tous" },
          { value: "active", label: "Actif" },
          { value: "soldout", label: "Épuisé" },
        ],
      });
    }

    if (availableFilters.includes("price")) {
      fields.push(
        {
          name: "priceMin",
          label: "Prix minimum (€)",
          type: "number",
          placeholder: "0",
        },
        {
          name: "priceMax",
          label: "Prix maximum (€)",
          type: "number",
          placeholder: "300",
        }
      );
    }

    return fields;
  }, [availableFilters]);

  // Schéma de validation pour les filtres
  const filterSchema = z.object({
    searchQuery: z.string().optional(),
    category: z.string().optional(),
    city: z.string().optional(),
    status: z.string().optional(),
    priceMin: z.number().optional(),
    priceMax: z.number().optional(),
  });

  // Handle filter form submission
  const handleFilterSubmit = (data: any) => {
    setFilters(data);
    setMobileOpen(false);
  };

  // Pagination derived values
  const _totalPages = useMemo(() => {
    return totalPages > 0 ? totalPages : Math.ceil(totalItems / itemsPerPage);
  }, [totalItems, itemsPerPage, totalPages]);

  const _deals = useMemo(() => {
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
    if (filters.priceMin !== undefined && filters.priceMax !== undefined) {
      dataset = dataset.filter(
        (d) =>
          d.groupPrice >= filters.priceMin! && d.groupPrice <= filters.priceMax!
      );
    }

    // pagination slice
    const start = (currentPage - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    return dataset.slice(start, end);
  }, [deals, filters, currentPage, itemsPerPage]);

  // If filterPosition === "top" we render filters above grid
  const renderFiltersInline = filterPosition === "top";

  // Render filter form
  const renderFiltersForm = (inSheet = false) => (
    <VStack spacing={4}>
      <Form
        fields={filterFields}
        columns={inSheet ? 1 : 1}
        schema={filterSchema}
        onSubmit={handleFilterSubmit}
        submitLabel={inSheet ? "Appliquer" : "Filtrer"}
        resetLabel="Réinitialiser"
      />
    </VStack>
  );

  return (
    <section className={cn("py-8", className)}>
      <VStack spacing={8}>
        {/* Header */}
        <HStack justify="between">
          <HStack spacing={8} align="center">
            <h2 className="text-2xl font-semibold">Offres</h2>
            <span className="text-sm text-muted-foreground">
              {totalItems} résultats
            </span>
          </HStack>

          {/* Mobile: bottom sheet trigger */}
          <div className="md:hidden">
            <Button
              variant="outline"
              leftIcon={<Sliders className="w-4 h-4" />}
              title="Filtres"
              onClick={() => setMobileOpen(true)}
            />
          </div>
        </HStack>

        <div
          className={cn(
            "grid gap-6",
            showFilters && !renderFiltersInline
              ? "grid-cols-1 md:grid-cols-12"
              : "grid-cols-1"
          )}
        >
          {/* Sidebar filters (desktop) */}
          {showFilters && !renderFiltersInline && (
            <aside className="hidden md:block md:col-span-3 lg:col-span-3">
              <div className="sticky top-20 bg-transparent p-4 border rounded">
                <HStack className="items-center justify-between mb-4">
                  <h3 className="font-medium">Filtres</h3>
                </HStack>
                {renderFiltersForm()}
              </div>
            </aside>
          )}

          {/* Main content */}
          <div className={cn("col-span-1 md:col-span-9 lg:col-span-9")}>
            {/* Top inline filters (if filterPosition === 'top') */}
            {showFilters && renderFiltersInline && (
              <div className="mb-6 p-4 border rounded bg-surface-50">
                {renderFiltersForm()}
              </div>
            )}

            {/* Grid */}
            <VStack spacing={6}>
              <Grid cols={cols} gap="gap-8">
                {_deals.map((deal, idx) => (
                  <DealCard key={deal.id ?? idx} deal={deal} />
                ))}
              </Grid>

              {/* Pagination */}
              {showPagination && (
                <Pagination
                  page={currentPage}
                  totalPages={_totalPages}
                  onChange={(p) => setCurrentPage(p)}
                  perPage={itemsPerPage}
                  totalItems={totalItems}
                />
              )}
            </VStack>
          </div>
        </div>
      </VStack>

      {/* Mobile bottom sheet for filters */}
      <MobileFilterSheet open={mobileOpen} onClose={() => setMobileOpen(false)}>
        {renderFiltersForm(true)}
        <HStack spacing={2} className="mt-4">
          <button
            onClick={() => setMobileOpen(false)}
            className="flex-1 px-4 py-2 rounded border"
          >
            Annuler
          </button>
          <button
            onClick={() => setMobileOpen(false)}
            className="flex-1 px-4 py-2 rounded bg-primary-500 text-white"
          >
            Fermer
          </button>
        </HStack>
      </MobileFilterSheet>
    </section>
  );
}
