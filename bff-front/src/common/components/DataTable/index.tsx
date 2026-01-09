import * as React from "react";
import {
  type CellContext,
  type ColumnDef,
  type ColumnFiltersState,
  type SortingState,
  type VisibilityState,
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  useReactTable,
} from "@tanstack/react-table";
import { Button, type IButtonProps } from "@components/ui/button";
import { Input } from "@components/ui/input";
import Pagination from "../Pagination";
import { Dropdown } from "../Dropdown";
import HStack from "../HStack";
import VStack from "../VStack";
import Checkbox from "../Checkbox";
import { cn } from "@lib/utils";
import {
  Search,
  Filter,
  X,
  Download,
  ArrowUpDown,
  ArrowUp,
  ArrowDown,
  RefreshCw,
  SlidersHorizontal,
  ChevronDown,
} from "lucide-react";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/common/components/ui/popover";

/** Configuration d'un filtre de colonne */
export interface IColumnFilter {
  id: string;
  label: string;
  type: "text" | "select" | "number" | "date";
  options?: { label: string; value: string }[];
}

/**
 * Props du DataTable
 */
export interface IDataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  searchKey?: string | string[];
  searchPlaceholder?: string;
  enableSelection?: boolean;
  showSelectionCount?: boolean;
  enableRowNumber?: boolean;
  pageSizeOptions?: number[];
  actionsRow?: (props: CellContext<TData, TValue>) => IButtonProps[];
  /** Filtres de colonnes disponibles */
  columnFiltersConfig?: IColumnFilter[];
  /** Activer l'export CSV */
  enableExport?: boolean;
  /** Activer le tri */
  enableSorting?: boolean;
  /** Callback lors de l'export */
  onExport?: (data: TData[]) => void;
  /** Callback lors du refresh */
  onRefresh?: () => void;

  onFilter?: (params: {
    globalFilter: string;
    columnFilters: ColumnFiltersState;
    activeFilters: Record<string, any>;
  }) => void;
}

export default function DataTable<TData, TValue>({
  columns,
  data,
  searchKey,
  searchPlaceholder = "Rechercher...",
  enableSelection = true,
  showSelectionCount = true,
  enableRowNumber = true,
  pageSizeOptions = [10, 24, 50, 100],
  actionsRow,
  columnFiltersConfig = [],
  enableExport = true,
  enableSorting = true,
  onExport,
  onRefresh,
  onFilter,
}: IDataTableProps<TData, TValue>) {
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
    []
  );
  const [columnVisibility, setColumnVisibility] =
    React.useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = React.useState({});
  const [globalFilter, setGlobalFilter] = React.useState("");
  const [showFilters, setShowFilters] = React.useState(false);
  const [activeFilters, setActiveFilters] = React.useState<Record<string, any>>(
    {}
  );

  React.useEffect(() => {
    onFilter?.({
      globalFilter,
      columnFilters,
      activeFilters,
    });
  }, [globalFilter, columnFilters, activeFilters]);

  // Construire colonnes (rowNumber + select + user columns)
  const cols = React.useMemo(() => {
    const head: ColumnDef<TData, any>[] = [];

    if (enableRowNumber) {
      head.push({
        id: "rowNumber",
        header: "#",
        cell: ({ row, table }) => {
          const pageIndex = table.getState().pagination.pageIndex ?? 0;
          const pageSize = table.getState().pagination.pageSize ?? 0;
          const globalIndex = pageIndex * pageSize + row.index + 1;
          return (
            <span className="text-sm font-medium text-muted-foreground">
              {globalIndex}
            </span>
          );
        },
        enableHiding: true,
        enableSorting: false,
        size: 50,
      } as ColumnDef<TData, any>);
    }

    if (enableSelection) {
      head.push({
        id: "select",
        header: ({ table }) => {
          const all =
            typeof table.getIsAllPageRowsSelected === "function"
              ? table.getIsAllPageRowsSelected()
              : false;
          const some =
            typeof table.getIsSomeRowsSelected === "function"
              ? table.getIsSomeRowsSelected()
              : false;

          return (
            <Checkbox
              aria-label="Select all"
              checked={!!some && !all ? "indeterminate" : !!all}
              onChange={() => {
                if (typeof table.toggleAllPageRowsSelected === "function") {
                  table.toggleAllPageRowsSelected();
                } else if (typeof table.toggleAllRowsSelected === "function") {
                  table.toggleAllRowsSelected();
                }
              }}
            />
          );
        },
        cell: ({ row }) => {
          return (
            <Checkbox
              aria-label={`Select row ${row.index}`}
              checked={row.getIsSelected?.() ?? false}
              onChange={() => row.toggleSelected?.()}
            />
          );
        },
        enableHiding: false,
        enableSorting: false,
        size: 40,
      } as ColumnDef<TData, any>);
    }

    // Ajouter le tri aux colonnes utilisateur si enableSorting
    const userColumns = enableSorting
      ? columns.map((col) => ({
          ...col,
          enableSorting: col.enableSorting !== false,
        }))
      : columns;

    return [...head, ...userColumns];
  }, [columns, enableSelection, enableRowNumber, enableSorting]);

  // Global filter function - utilise row.original pour accéder aux données directement
  const globalFilterFn = React.useCallback(
    (row: any, _columnId: string, filterValue: string) => {
      if (!filterValue) return true;
      const keys = Array.isArray(searchKey)
        ? searchKey
        : searchKey
        ? [searchKey]
        : [];

      const searchLower = String(filterValue).toLowerCase();

      if (keys.length === 0) {
        // Recherche dans toutes les valeurs de la ligne
        return Object.values(row.original).some((val) =>
          String(val ?? "")
            .toLowerCase()
            .includes(searchLower)
        );
      }

      // Recherche dans les clés spécifiées en utilisant row.original
      return keys.some((k) => {
        const value = row.original[k];
        return String(value ?? "")
          .toLowerCase()
          .includes(searchLower);
      });
    },
    [searchKey]
  );

  const table = useReactTable({
    data,
    columns: cols,
    state: {
      sorting,
      columnFilters,
      columnVisibility,
      rowSelection,
      globalFilter,
    },
    onSortingChange: setSorting,
    onColumnFiltersChange: setColumnFilters,
    onColumnVisibilityChange: setColumnVisibility,
    onRowSelectionChange: setRowSelection,
    onGlobalFilterChange: setGlobalFilter,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    globalFilterFn,
  });

  // Gérer les filtres de colonnes
  const handleColumnFilter = (filterId: string, value: any) => {
    setActiveFilters((prev) => ({
      ...prev,
      [filterId]: value,
    }));

    const column = table.getColumn(filterId);
    if (column && column.getCanFilter() && !onFilter) {
      column.setFilterValue(
        value === "all" || value === "" ? undefined : value
      );
    }
  };

  // Réinitialiser tous les filtres
  const clearAllFilters = () => {
    setGlobalFilter("");
    setActiveFilters({});
    table.resetColumnFilters();
    table.setGlobalFilter("");
  };

  // Compter les filtres actifs
  const activeFilterCount =
    Object.values(activeFilters).filter((v) => v && v !== "all").length +
    (globalFilter ? 1 : 0);

  // Export CSV
  const handleExport = () => {
    if (onExport) {
      const filteredData = table
        .getFilteredRowModel()
        .rows.map((row) => row.original);
      onExport(filteredData);
    } else {
      // Export par défaut en CSV
      const filteredData = table
        .getFilteredRowModel()
        .rows.map((row) => row.original);
      const headers = columns
        .map((col) => (col as any).accessorKey || col.id)
        .filter(Boolean);
      const csvContent = [
        headers.join(","),
        ...filteredData.map((row) =>
          headers.map((h) => JSON.stringify((row as any)[h] ?? "")).join(",")
        ),
      ].join("\n");

      const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = "export.csv";
      link.click();
    }
  };

  // Render header avec tri
  const renderSortableHeader = (column: any, label: string) => {
    if (!enableSorting || column.getCanSort?.() === false) {
      return <span>{label}</span>;
    }

    const sorted = column.getIsSorted();
    return (
      <button
        className="flex items-center gap-1 hover:text-foreground transition-colors group"
        onClick={() => column.toggleSorting()}
      >
        <span>{label}</span>
        <span className="opacity-0 group-hover:opacity-100 transition-opacity">
          {sorted === "asc" ? (
            <ArrowUp className="h-3.5 w-3.5 text-primary" />
          ) : sorted === "desc" ? (
            <ArrowDown className="h-3.5 w-3.5 text-primary" />
          ) : (
            <ArrowUpDown className="h-3.5 w-3.5" />
          )}
        </span>
        {sorted && (
          <span className="opacity-100">
            {sorted === "asc" ? (
              <ArrowUp className="h-3.5 w-3.5 text-primary" />
            ) : (
              <ArrowDown className="h-3.5 w-3.5 text-primary" />
            )}
          </span>
        )}
      </button>
    );
  };

  return (
    <div className="w-full space-y-4">
      {/* Toolbar principal */}
      <div className="flex flex-col gap-4">
        {/* Barre de recherche et actions */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 p-4 bg-gradient-to-r from-muted/30 to-muted/10 rounded-xl border border-border/50">
          <div className="flex items-center gap-3 flex-1">
            {searchKey && (
              <div className="relative flex-1 max-w-md">
                <Input
                  placeholder={searchPlaceholder}
                  value={globalFilter}
                  leftIcon={<Search className="h-4 w-4" />}
                  onChange={(e) => {
                    const v = e.target.value;
                    setGlobalFilter(v);
                    table.setGlobalFilter?.(v);
                  }}
                  rightIcon={
                    globalFilter && (
                      <button
                        onClick={() => {
                          setGlobalFilter("");
                          table.setGlobalFilter("");
                        }}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                      >
                        <X className="h-4 w-4" />
                      </button>
                    )
                  }
                  className=" pr-4 h-11 bg-background/80 backdrop-blur-sm border-border/50 rounded-lg focus:ring-2 focus:ring-primary/20 transition-all"
                />
              </div>
            )}

            {/* Bouton filtres avancés */}
            {columnFiltersConfig.length > 0 && (
              <Button
                variant={showFilters ? "default" : "outline"}
                size="sm"
                onClick={() => setShowFilters(!showFilters)}
                className="gap-2"
              >
                <SlidersHorizontal className="h-4 w-4" />
                Filtres
                {activeFilterCount > 0 && (
                  <span className="ml-1 h-5 w-5 rounded-full bg-primary text-primary-foreground text-xs flex items-center justify-center">
                    {activeFilterCount}
                  </span>
                )}
              </Button>
            )}
          </div>

          <HStack spacing={2} align="center" className="flex-shrink-0">
            {/* Refresh */}
            {onRefresh && (
              <Button
                variant="outline"
                size="icon-sm"
                onClick={onRefresh}
                title="Actualiser"
                className="h-9 w-9"
              >
                <RefreshCw className="h-4 w-4" />
              </Button>
            )}

            {/* Export */}
            {enableExport && (
              <Button
                variant="outline"
                size="sm"
                onClick={handleExport}
                className="gap-2"
              >
                <Download className="h-4 w-4" />
                <span className="hidden sm:inline">Exporter</span>
              </Button>
            )}

            {/* Colonnes */}
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="outline" size="sm" className="gap-2">
                  <SlidersHorizontal className="h-4 w-4" />
                  <span className="hidden sm:inline">Colonnes</span>
                  <ChevronDown className="h-3 w-3" />
                </Button>
              </PopoverTrigger>
              <PopoverContent
                className="w-56 p-2 bg-popover border border-border shadow-lg z-50"
                align="end"
              >
                <VStack spacing={1}>
                  {table
                    .getAllColumns()
                    .filter((col) => col.getCanHide?.())
                    .map((column) => (
                      <label
                        key={column.id}
                        className="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-muted cursor-pointer text-sm"
                      >
                        <Checkbox
                          checked={column.getIsVisible()}
                          onChange={() => column.toggleVisibility()}
                        />
                        <span className="capitalize">{column.id}</span>
                      </label>
                    ))}
                </VStack>
              </PopoverContent>
            </Popover>

            {/* Page size */}
            <Dropdown
              label={`${table.getState().pagination.pageSize}`}
              items={pageSizeOptions.map((n) => ({
                label: `${n} lignes`,
                value: `${n}`,
              }))}
              selectedValue={`${table.getState().pagination.pageSize}`}
              onChange={(value) => {
                table.setPageSize(Number(value));
              }}
              contentClassName="w-32"
            />
          </HStack>
        </div>

        {/* Panneau de filtres avancés */}
        {showFilters && columnFiltersConfig.length > 0 && (
          <div className="p-4 bg-card rounded-xl border border-border/50 animate-in slide-in-from-top-2 duration-200">
            <div className="flex items-center justify-between mb-4">
              <h4 className="font-semibold text-sm text-foreground flex items-center gap-2">
                <Filter className="h-4 w-4" />
                Filtres avancés
              </h4>
              {activeFilterCount > 0 && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={clearAllFilters}
                  className="text-muted-foreground hover:text-foreground gap-1"
                >
                  <X className="h-3 w-3" />
                  Réinitialiser
                </Button>
              )}
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              {columnFiltersConfig.map((filter) => (
                <div key={filter.id} className="space-y-1.5">
                  <label className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                    {filter.label}
                  </label>
                  {filter.type === "select" && filter.options ? (
                    <Dropdown
                      label={
                        activeFilters[filter.id]
                          ? filter.options.find(
                              (o) => o.value === activeFilters[filter.id]
                            )?.label || "Tous"
                          : "Tous"
                      }
                      items={[
                        { label: "Tous", value: "all" },
                        ...filter.options,
                      ]}
                      selectedValue={activeFilters[filter.id] || "all"}
                      onChange={(value) => handleColumnFilter(filter.id, value)}
                      contentClassName="w-full min-w-[180px]"
                    />
                  ) : filter.type === "number" ? (
                    <Input
                      type="number"
                      placeholder="Filtrer..."
                      value={activeFilters[filter.id] || ""}
                      onChange={(e) =>
                        handleColumnFilter(filter.id, e.target.value)
                      }
                      className="h-9"
                    />
                  ) : (
                    <Input
                      placeholder="Filtrer..."
                      value={activeFilters[filter.id] || ""}
                      onChange={(e) =>
                        handleColumnFilter(filter.id, e.target.value)
                      }
                      className="h-9"
                    />
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Tags des filtres actifs */}
        {activeFilterCount > 0 && !showFilters && (
          <div className="flex items-center gap-2 flex-wrap">
            <span className="text-xs text-muted-foreground">
              Filtres actifs:
            </span>
            {globalFilter && (
              <span className="inline-flex items-center gap-1 px-2 py-1 bg-primary/10 text-primary rounded-full text-xs">
                Recherche: "{globalFilter}"
                <button
                  onClick={() => {
                    setGlobalFilter("");
                    table.setGlobalFilter("");
                  }}
                >
                  <X className="h-3 w-3" />
                </button>
              </span>
            )}
            {Object.entries(activeFilters).map(([key, value]) => {
              if (!value || value === "all") return null;
              const filterConfig = columnFiltersConfig.find(
                (f) => f.id === key
              );
              const label =
                filterConfig?.options?.find((o) => o.value === value)?.label ||
                value;
              return (
                <span
                  key={key}
                  className="inline-flex items-center gap-1 px-2 py-1 bg-primary/10 text-primary rounded-full text-xs"
                >
                  {filterConfig?.label}: {label}
                  <button onClick={() => handleColumnFilter(key, "all")}>
                    <X className="h-3 w-3" />
                  </button>
                </span>
              );
            })}
            <button
              onClick={clearAllFilters}
              className="text-xs text-muted-foreground hover:text-foreground underline"
            >
              Tout effacer
            </button>
          </div>
        )}
      </div>

      {/* Table */}
      <div className="rounded-xl border border-border/50 overflow-hidden bg-card shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full caption-bottom text-sm">
            <thead className="bg-muted/50">
              {table.getHeaderGroups().map((headerGroup) => (
                <tr key={headerGroup.id}>
                  {headerGroup.headers.map((header) => (
                    <th
                      key={header.id}
                      className="h-14 px-4 text-left align-middle font-semibold text-foreground/80 text-xs uppercase tracking-wider [&:has([role=checkbox])]:pr-0 first:pl-6 last:pr-6"
                      style={{ width: header.column.getSize() }}
                    >
                      {header.isPlaceholder
                        ? null
                        : header.column.getCanSort()
                        ? renderSortableHeader(
                            header.column,
                            typeof header.column.columnDef.header === "string"
                              ? header.column.columnDef.header
                              : header.column.id
                          )
                        : flexRender(
                            header.column.columnDef.header,
                            header.getContext()
                          )}
                    </th>
                  ))}
                  {actionsRow ? (
                    <th className="h-14 px-4 text-left align-middle font-semibold text-foreground/80 text-xs uppercase tracking-wider pr-6">
                      Actions
                    </th>
                  ) : null}
                </tr>
              ))}
            </thead>

            <tbody className="divide-y divide-border/30">
              {table.getRowModel().rows?.length ? (
                table.getRowModel().rows.map((row, index) => (
                  <tr
                    key={row.id}
                    data-state={
                      row.getIsSelected && row.getIsSelected()
                        ? "selected"
                        : undefined
                    }
                    className={cn(
                      "transition-colors hover:bg-muted/30 data-[state=selected]:bg-primary/5",
                      index % 2 === 0 ? "bg-background" : "bg-muted/10"
                    )}
                  >
                    {row.getVisibleCells().map((cell) => (
                      <td
                        key={cell.id}
                        className="p-4 align-middle [&:has([role=checkbox])]:pr-0 first:pl-6 last:pr-6"
                        style={{ width: cell.column.getSize() }}
                      >
                        {flexRender(
                          cell.column.columnDef.cell,
                          cell.getContext()
                        )}
                      </td>
                    ))}
                    {actionsRow ? (
                      <td className="p-4 pr-6">
                        <HStack spacing={2}>
                          {actionsRow({
                            ...row.getVisibleCells()[0].getContext(),
                            row,
                          } as CellContext<TData, TValue>).map((action, i) => (
                            <Button
                              key={i}
                              size="sm"
                              variant="ghost"
                              className="h-8 px-3 hover:bg-primary/10 hover:text-primary transition-colors"
                              {...action}
                            />
                          ))}
                        </HStack>
                      </td>
                    ) : null}
                  </tr>
                ))
              ) : (
                <tr>
                  <td
                    colSpan={cols.length + (actionsRow ? 1 : 0)}
                    className="h-32 text-center"
                  >
                    <div className="flex flex-col items-center justify-center gap-2 text-muted-foreground">
                      <span className="text-4xl">📭</span>
                      <span>Aucun résultat trouvé</span>
                      {activeFilterCount > 0 && (
                        <Button
                          variant="link"
                          size="sm"
                          onClick={clearAllFilters}
                        >
                          Réinitialiser les filtres
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Footer */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-2">
        <div className="flex items-center gap-4">
          {showSelectionCount && (
            <div className="text-sm text-muted-foreground bg-muted/30 px-4 py-2 rounded-lg">
              <span className="font-medium text-foreground">
                {table.getFilteredSelectedRowModel().rows.length}
              </span>{" "}
              sur{" "}
              <span className="font-medium text-foreground">
                {table.getFilteredRowModel().rows.length}
              </span>{" "}
              ligne(s) sélectionnée(s)
            </div>
          )}
          {!showSelectionCount && (
            <div className="text-sm text-muted-foreground">
              {table.getFilteredRowModel().rows.length} résultat(s)
            </div>
          )}
        </div>

        <Pagination
          page={(table.getState().pagination.pageIndex ?? 0) + 1}
          totalPages={table.getPageCount()}
          perPage={table.getState().pagination.pageSize ?? 0}
          totalItems={table.getFilteredRowModel().rows.length}
          onChange={(p) => {
            table.setPageIndex?.(p - 1);
          }}
          align="right"
          showSummary={false}
        />
      </div>
    </div>
  );
}
