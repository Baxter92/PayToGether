import React, { useMemo, useState } from "react";
import Grid, { type IColsProp } from "@components/Grid";
import Pagination from "@components/Pagination";
import VStack from "@components/VStack";
import HStack from "@components/HStack";
import { Heading } from "../Heading";
import { cn } from "@lib/utils";
import { Button } from "@/common/components/ui/button";
import { LayoutGrid, List } from "lucide-react";
import type { ICategory } from "../CategoryCard/type";
import CategoryCard from "../CategoryCard";

/** Props de CategoriesList */
interface ICategoriesListProps {
  categories: ICategory[];
  cols?: IColsProp;
  itemsPerPage?: number;
  showPagination?: boolean;
  className?: string;
  showTitle?: boolean;
  title?: string;
  description?: string;
  viewModeToggleable?: boolean;
  initialView?: "grid" | "list";
}

export default function CategoriesList({
  categories,
  cols = { sm: 2, md: 3, lg: 4, xl: 5 },
  itemsPerPage = 12,
  showPagination = true,
  className,
  showTitle = true,
  title = "Catégories",
  description,
  viewModeToggleable = false,
  initialView = "grid",
}: ICategoriesListProps) {
  const [currentPage, setCurrentPage] = useState(1);
  const [view, setView] = useState<"grid" | "list">(initialView);

  // pagination calculée
  const totalItems = categories.length;
  const totalPages = useMemo(
    () => Math.max(1, Math.ceil(totalItems / itemsPerPage)),
    [totalItems, itemsPerPage]
  );

  const paginated = useMemo(() => {
    const start = (currentPage - 1) * itemsPerPage;
    return categories.slice(start, start + itemsPerPage);
  }, [categories, currentPage, itemsPerPage]);

  // reset page when categories change
  React.useEffect(() => {
    setCurrentPage(1);
  }, [categories, itemsPerPage]);

  return (
    <section className={cn("w-full", className)}>
      <VStack spacing={6}>
        {/* Header */}
        <HStack justify="between" align="end" spacing={6} className="w-full">
          {showTitle && (
            <HStack spacing={4} align="end" className="w-full">
              <div className="flex-1">
                <Heading
                  level={2}
                  title={title}
                  underline
                  description={description}
                  descriptionSize="lg"
                />
              </div>
              <span className="text-sm text-muted-foreground">
                {totalItems} catégories
              </span>
            </HStack>
          )}

          <HStack spacing={2} className="items-center">
            {viewModeToggleable && (
              <div className="flex items-center gap-2">
                <Button
                  variant={view === "grid" ? "default" : "outline"}
                  size="icon-sm"
                  onClick={() => setView("grid")}
                  title="Grille"
                >
                  <LayoutGrid className="w-4 h-4" />
                </Button>

                <Button
                  variant={view === "list" ? "default" : "outline"}
                  size="icon-sm"
                  onClick={() => setView("list")}
                  title="Liste"
                >
                  <List className="w-4 h-4" />
                </Button>
              </div>
            )}
          </HStack>
        </HStack>

        {/* Content */}
        <div className="w-full">
          {view === "grid" ? (
            <>
              <Grid cols={cols} gap="gap-6">
                {paginated.map((c) => (
                  <CategoryCard key={c.id} category={c} />
                ))}
              </Grid>

              {showPagination && totalPages > 1 && (
                <div className="mt-6">
                  <Pagination
                    page={currentPage}
                    totalPages={totalPages}
                    onChange={(p) => setCurrentPage(p)}
                    perPage={itemsPerPage}
                    totalItems={totalItems}
                  />
                </div>
              )}
            </>
          ) : (
            // Simple list view — remplace par un DataTable si tu veux plus de fonctionnalités
            <div className="space-y-3">
              {paginated.map((c) => (
                <div
                  key={c.id}
                  className="flex items-center gap-4 p-4 border rounded bg-card"
                >
                  <div className="w-16 h-12 overflow-hidden rounded">
                    <img
                      src={
                        c.image ??
                        "https://images.unsplash.com/photo-1521335629791-ce4aec67dd47?auto=format&fit=crop&w=600&q=60"
                      }
                      alt={c.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <h4 className="font-medium truncate">{c.name}</h4>
                      {typeof c.count === "number" && (
                        <span className="text-sm text-muted-foreground">
                          {c.count} produits
                        </span>
                      )}
                    </div>
                    {c.description && (
                      <p className="text-xs text-muted-foreground truncate">
                        {c.description}
                      </p>
                    )}
                  </div>
                </div>
              ))}

              {showPagination && totalPages > 1 && (
                <div className="mt-2">
                  <Pagination
                    page={currentPage}
                    totalPages={totalPages}
                    onChange={(p) => setCurrentPage(p)}
                    perPage={itemsPerPage}
                    totalItems={totalItems}
                  />
                </div>
              )}
            </div>
          )}
        </div>
      </VStack>
    </section>
  );
}
