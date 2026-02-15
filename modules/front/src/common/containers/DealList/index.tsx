import React, { useEffect, useMemo, useState, useRef } from "react";
import { useI18n } from "@/common/hooks/useI18n";
import Grid, { type IColsProp } from "@components/Grid";
import DealCard from "../DealCard";
import Pagination from "@components/Pagination";
import {
  X,
  Sliders,
  List,
  LayoutGrid,
  Edit2,
  Trash2,
  FileEdit,
  Globe,
} from "lucide-react";
import { cn } from "@lib/utils";
import Form, { type IFieldConfig } from "@containers/Form";
import { z } from "zod";
import VStack from "@components/VStack";
import HStack from "@components/HStack";
import { Button } from "@/common/components/ui/button";
import DataTable, { type IDataTableProps } from "@components/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import { Avatar, AvatarImage } from "@/common/components/ui/avatar";
import { Heading } from "../Heading";
import { Progress } from "@/common/components/ui/progress";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { Badge } from "@/common/components/ui/badge";
import { CreateDealModal } from "@/pages/profile/components/CreateDealModal";
import { useDealVilles, useGetDealImageUrl } from "@/common/api";

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
  viewMode?: "grid" | "list";
  viewModeToggleable?: boolean;
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
  showTitle?: boolean;
  titleClassName?: string;
  description?: string;
  title?: string;
  tableProps?: Partial<IDataTableProps<any, any>>;
  isAdmin?: boolean;
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
      <div
        className={cn(
          "fixed inset-0 bg-black/40 transition-opacity z-40",
          open
            ? "opacity-100 pointer-events-auto"
            : "opacity-0 pointer-events-none",
        )}
        onClick={onClose}
        aria-hidden={!open}
      />
      <div
        className={cn(
          "fixed left-0 right-0 bottom-0 z-50 transform transition-transform bg-white dark:bg-surface-800 border-t shadow-xl",
          open ? "translate-y-0" : "translate-y-full",
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

function DealTableProductCell({ deal }: { deal: any }) {
  const { data: imageUrl } = useGetDealImageUrl(deal?.id, deal?.image?.imageUuid);

  return (
    <div className="flex items-center gap-3">
      <Avatar className="h-12 w-12 rounded-lg border border-border/50 shadow-sm">
        <AvatarImage
          src={imageUrl?.url || "/placeholder.svg"}
          alt={deal?.title}
          className="object-cover"
        />
      </Avatar>
      <div className="flex flex-col">
        <span className="font-semibold text-foreground">{deal?.title}</span>
        {deal?.subtitle && (
          <span className="text-xs text-muted-foreground">{deal.subtitle}</span>
        )}
      </div>
    </div>
  );
}

/** Component principal */
export default function DealsList({
  cols = { md: 2, lg: 3, xl: 4 },
  viewMode = "grid",
  viewModeToggleable = false,
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
  showTitle = false,
  titleClassName,
  title = "Offres",
  description,
  tableProps,
  isAdmin = false,
}: IDealsListProps) {
  const { t: tFilters } = useI18n("filters");
  const { t: tTable } = useI18n("table");
  const [currentPage, setCurrentPage] = useState(1);
  const [filters, setFilters] = useState<DealFilters>({
    category: "all",
    priceMin: undefined,
    priceMax: undefined,
    city: "all",
    status: "all",
    searchQuery: "",
  });

  const [view, setView] = useState<"grid" | "list">(viewMode);
  const [createModalOpen, setCreateModalOpen] = useState(false);

  const { data: cities } = useDealVilles();

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

  // Extraire les catégories et villes uniques dynamiquement
  const uniqueCategories = useMemo(() => {
    const cats = new Set(deals.map((d) => d.category).filter(Boolean));
    return Array.from(cats).map((cat) => ({
      value: cat,
      label: cat.charAt(0).toUpperCase() + cat.slice(1),
    }));
  }, [deals]);

  const uniqueCities = useMemo(() => {
    return (cities ?? []).map((city) => ({
      value: city,
      label: city,
    }));
  }, [cities]);

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
          ...uniqueCategories,
        ],
      });
    }

    if (availableFilters.includes("city")) {
      fields.push({
        name: "city",
        label: "Ville",
        type: "select",
        items: [{ value: "all", label: "Toutes les villes" }, ...uniqueCities],
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
          label: "Prix minimum",
          type: "number",
          placeholder: "0",
        },
        {
          name: "priceMax",
          label: "Prix maximum",
          type: "number",
          placeholder: "200000",
        },
      );
    }

    return fields;
  }, [availableFilters, uniqueCategories, uniqueCities]);

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
    setCurrentPage(1);
  };

  // Filtering (WITHOUT pagination) -> used for LIST view (DataTable)
  const filteredDeals = useMemo(() => {
    if (!showFilters) return deals;

    let dataset = [...deals];

    // search
    if (filters.searchQuery && filters.searchQuery.trim() !== "") {
      const q = filters.searchQuery.toLowerCase();
      dataset = dataset.filter((d) =>
        (d.title || "").toLowerCase().includes(q),
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
    // price range (only if price filter is enabled)
    if (availableFilters.includes("price")) {
      const minPrice =
        typeof filters.priceMin === "number" ? filters.priceMin : undefined;
      const maxPrice =
        typeof filters.priceMax === "number" ? filters.priceMax : undefined;

      if (minPrice !== undefined || maxPrice !== undefined) {
        dataset = dataset.filter((d) => {
          const price = Number(d.groupPrice) || 0;
          if (minPrice !== undefined && price < minPrice) return false;
          if (maxPrice !== undefined && price > maxPrice) return false;
          return true;
        });
      }
    }

    return dataset;
  }, [deals, filters, availableFilters]);

  // Grid dataset: apply pagination slice
  const _totalPages = useMemo(() => {
    return totalPages > 0
      ? totalPages
      : Math.ceil((totalItems || filteredDeals.length) / itemsPerPage);
  }, [totalItems, itemsPerPage, totalPages, filteredDeals.length]);

  const gridDeals = useMemo(() => {
    const start = (currentPage - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    return filteredDeals.slice(start, end);
  }, [filteredDeals, currentPage, itemsPerPage]);

  // Filters inline flag
  const renderFiltersInline = filterPosition === "top";

  // Render filter form
  const renderFiltersForm = (inSheet = false) => (
    <VStack spacing={4}>
      <Form
        fields={filterFields}
        columns={inSheet ? 1 : 1}
        schema={filterSchema}
        onSubmit={handleFilterSubmit}
        submitLabel={inSheet ? tFilters("apply") : tFilters("filter")}
        resetLabel={tFilters("reset")}
      />
    </VStack>
  );

  // Columns for DataTable (LIST view)
  const tableColumns = useMemo<ColumnDef<any, any>[]>(() => {
    return [
      {
        accessorKey: "image",
        header: tTable("product"),
        cell: ({ row }) => {
          return <DealTableProductCell deal={row.original} />;
        },
      },
      {
        accessorKey: "category",
        header: tFilters("category"),
        cell: ({ getValue }) => (
          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-secondary/50 text-secondary-foreground capitalize">
            {String(getValue() ?? "")}
          </span>
        ),
      },
      {
        accessorKey: "groupPrice",
        header: tTable("price"),
        cell: ({ row }) => {
          const d = row.original;
          return (
            <div className="flex flex-col">
              <span className="font-bold text-primary">
                {formatCurrency(d.groupPrice)}
              </span>
              {d.originalPrice && d.originalPrice !== d.groupPrice && (
                <span className="text-xs text-muted-foreground line-through">
                  {formatCurrency(d.originalPrice)}
                </span>
              )}
            </div>
          );
        },
      },
      {
        accessorKey: "sold",
        header: tTable("partsSold"),
        cell: ({ row }) => {
          const d = row.original;
          const sold = d.sold ?? 0;
          const total = d.total ?? 100;
          const percentage = Math.round((sold / total) * 100);

          return (
            <div className="flex flex-col gap-1.5 min-w-[120px]">
              <div className="flex items-center justify-between text-xs">
                <span className="text-muted-foreground">
                  {sold}/{total}
                </span>
                <span
                  className={cn(
                    "font-semibold",
                    percentage >= 80
                      ? "text-destructive"
                      : percentage >= 50
                        ? "text-amber-500"
                        : "text-primary",
                  )}
                >
                  {percentage}%
                </span>
              </div>
              <Progress
                value={percentage}
                className={cn(
                  "h-2",
                  percentage >= 80
                    ? "[&>div]:bg-destructive"
                    : percentage >= 50
                      ? "[&>div]:bg-amber-500"
                      : "[&>div]:bg-primary",
                )}
              />
            </div>
          );
        },
      },
      {
        accessorKey: "deadline",
        header: tTable("deadline"),
        cell: ({ getValue }) => (
          <span className="text-sm text-muted-foreground">
            {String(getValue() ?? "")}
          </span>
        ),
      },
      {
        accessorKey: "city",
        header: tFilters("city"),
        cell: ({ getValue }) => (
          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs bg-muted text-muted-foreground">
            {String(getValue() ?? "")}
          </span>
        ),
      },
      {
        accessorKey: "status",
        header: tTable("status"),
        cell: ({ getValue }) => (
          <Badge
            size="sm"
            colorScheme={getValue() === "published" ? "success" : "secondary"}
          >
            {String(getValue() ?? "")}
          </Badge>
        ),
      },
    ];
  }, []);

  return (
    <section className={cn("", className)}>
      <VStack spacing={20}>
        {/* Header : titre + toggles */}
        <HStack justify="between" align="end" spacing={10}>
          {showTitle && (
            <HStack
              spacing={4}
              align="end"
              className={cn("w-full", titleClassName)}
            >
              <Heading
                level={2}
                title={title}
                underline
                className="flex-1"
                descriptionSize="lg"
                description={description}
              />
              <span className="text-sm text-muted-foreground">
                {filteredDeals.length} résultats
              </span>
            </HStack>
          )}

          <HStack spacing={2} className="items-center">
            {/* Filters mobile trigger */}
            <div className="md:hidden">
              <Button
                variant="outline"
                leftIcon={<Sliders className="w-4 h-4" />}
                title={tFilters("title")}
                onClick={() => setMobileOpen(true)}
              />
            </div>

            {/* View toggles */}
            {viewModeToggleable && (
              <div className="flex items-center gap-2">
                <Button
                  variant={view === "grid" ? "default" : "outline"}
                  size="icon-sm"
                  onClick={() => setView("grid")}
                  title={tFilters("gridView")}
                >
                  <LayoutGrid className="w-4 h-4" />
                </Button>

                <Button
                  variant={view === "list" ? "default" : "outline"}
                  size="icon-sm"
                  onClick={() => setView("list")}
                  title={tFilters("listView")}
                >
                  <List className="w-4 h-4" />
                </Button>
              </div>
            )}
          </HStack>
        </HStack>

        <div
          className={cn(
            "grid gap-6",
            showFilters && !renderFiltersInline
              ? "grid-cols-1 md:grid-cols-12"
              : "grid-cols-1",
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

            <VStack spacing={6}>
              {/* GRID VIEW */}
              {view === "grid" && (
                <>
                  <Grid cols={cols} gap="gap-8">
                    {gridDeals.map((deal, idx) => (
                      <DealCard key={deal.id ?? idx} deal={deal} />
                    ))}
                  </Grid>

                  {/* Pagination (externe) */}
                  {showPagination && (
                    <Pagination
                      page={currentPage}
                      totalPages={_totalPages}
                      onChange={(p) => setCurrentPage(p)}
                      perPage={itemsPerPage}
                      totalItems={filteredDeals.length}
                    />
                  )}
                </>
              )}

              {/* LIST VIEW (DataTable) */}
              {view === "list" && (
                <div className="w-full">
                  <DataTable
                    columns={tableColumns}
                    data={filteredDeals}
                    searchKey={["title", "category", "city", "status"]}
                    searchPlaceholder="Rechercher dans la liste..."
                    showSelectionCount={true}
                    enableRowNumber={true}
                    pageSizeOptions={[itemsPerPage, 24, 50, 100]}
                    {...tableProps}
                    {...(isAdmin
                      ? {
                          actionsRow: (props: any) => [
                            ...(tableProps?.actionsRow?.(props) || []),
                            {
                              leftIcon:
                                props.row.original.status === "published" ? (
                                  <FileEdit />
                                ) : (
                                  <Globe />
                                ),
                              tooltip:
                                props.row.original.status === "published"
                                  ? "Mettre en brouillon"
                                  : "Publier",
                            },
                            {
                              leftIcon: <Edit2 className="w-4 h-4" />,
                              onClick: () => {
                                setCreateModalOpen(true);
                              },
                            },
                            {
                              leftIcon: <Trash2 className="w-4 h-4" />,
                              colorScheme: "danger",
                              tooltip: "Supprimer",
                              onClick: () => {
                                console.log(props.row);
                              },
                            },
                          ],
                        }
                      : {})}
                  />
                </div>
              )}
            </VStack>
          </div>
        </div>
        <CreateDealModal
          open={createModalOpen}
          onClose={() => setCreateModalOpen(false)}
        />
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
