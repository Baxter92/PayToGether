import {
  Pagination as ShadcnPagination,
  PaginationContent,
  PaginationItem,
  PaginationNext,
  PaginationPrevious,
  PaginationEllipsis,
} from "@components/ui/pagination";
import { Button } from "../ui/button";
import { cn } from "@/common/lib/utils";

export type PaginationProps = {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
  showEdges?: boolean;
  siblingCount?: number;
  className?: string;
  align?: "left" | "center" | "right";
  showSummary?: boolean;
  perPage?: number;
  totalItems?: number;
};

export default function Pagination({
  page,
  totalPages,
  onChange,
  showEdges = true,
  siblingCount = 1,
  className,
  align = "center",
  showSummary = true,
  perPage = 24,
  totalItems = 0,
}: PaginationProps) {
  function getPageNumbers() {
    const pages: (number | "ellipsis")[] = [];
    const start = Math.max(2, page - siblingCount);
    const end = Math.min(totalPages - 1, page + siblingCount);

    if (showEdges) pages.push(1);
    if (start > 2) pages.push("ellipsis");
    for (let p = start; p <= end; p++) pages.push(p);
    if (end < totalPages - 1) pages.push("ellipsis");
    if (showEdges && totalPages > 1) pages.push(totalPages);

    return pages;
  }

  const pages = getPageNumbers();

  const startItem = totalItems === 0 ? 0 : (page - 1) * perPage + 1;
  const endItem = Math.min(page * perPage, totalItems);

  return (
    <div
      className={cn(
        "flex flex-col gap-3 w-full",
        align === "center"
          ? "md:items-center"
          : align === "right"
          ? "md:items-end"
          : "md:items-start"
      )}
    >
      <ShadcnPagination
        className={cn(
          align === "center"
            ? "md:justify-center"
            : align === "right"
            ? "md:justify-end"
            : "md:justify-start",
          className
        )}
      >
        <PaginationContent>
          {/* PREVIOUS */}
          <PaginationItem>
            <PaginationPrevious
              href="#"
              onClick={(e) => {
                e.preventDefault();
                if (page > 1) onChange(page - 1);
              }}
            />
          </PaginationItem>

          {/* PAGES */}
          {pages.map((p, i) =>
            p === "ellipsis" ? (
              <PaginationItem key={"ellipsis-" + i}>
                <PaginationEllipsis />
              </PaginationItem>
            ) : (
              <PaginationItem key={p}>
                <Button
                  variant={page === p ? "default" : "outline"}
                  onClick={(e) => {
                    e.preventDefault();
                    onChange(p);
                  }}
                >
                  {p}
                </Button>
              </PaginationItem>
            )
          )}

          {/* NEXT */}
          <PaginationItem>
            <PaginationNext
              href="#"
              onClick={(e) => {
                e.preventDefault();
                if (page < totalPages) onChange(page + 1);
              }}
            />
          </PaginationItem>
        </PaginationContent>
      </ShadcnPagination>
      {showSummary && (
        <div className="text-sm text-muted-foreground">
          {totalItems === 0
            ? ""
            : `${startItem} – ${endItem} parmi ${totalItems} résultats`}
        </div>
      )}
    </div>
  );
}
