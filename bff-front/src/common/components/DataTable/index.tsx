import * as React from "react";
import {
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
import Checkbox from "../Checkbox";

/**
 * Props du DataTable
 */
interface DataTableProps<TData, TValue> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  searchKey?: string | string[]; // une colonne ou un tableau de colonnes pour la recherche OR
  searchPlaceholder?: string;
  enableSelection?: boolean; // colonne checkbox
  showSelectionCount?: boolean; // afficher le compteur de sélection
  enableRowNumber?: boolean; // colonne numéro
  pageSizeOptions?: number[];
  actionsRow?: IButtonProps[];
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
}: DataTableProps<TData, TValue>) {
  const [sorting, setSorting] = React.useState<SortingState>([]);
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
    []
  );
  const [columnVisibility, setColumnVisibility] =
    React.useState<VisibilityState>({});
  const [rowSelection, setRowSelection] = React.useState({});
  const [globalFilter, setGlobalFilter] = React.useState("");

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
          return <span className="text-sm">{globalIndex}</span>;
        },
        enableHiding: true,
        size: 1,
      } as ColumnDef<TData, any>);
    }

    if (enableSelection) {
      head.push({
        id: "select",
        header: ({ table }) => {
          // checkbox select all on page (nom des méthodes peut varier selon version)
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
                // essaie toggleAllPageRowsSelected sinon toggleAllRowsSelected
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
        size: 1,
      } as ColumnDef<TData, any>);
    }

    return [...head, ...columns];
  }, [columns, enableSelection, enableRowNumber]);

  // Global filter function : si searchKey fourni -> OR sur les colonnes listées
  const globalFilterFn = React.useCallback(
    (row: any, columnIds: string[], filterValue: string) => {
      if (!filterValue) return true;
      const keys = Array.isArray(searchKey)
        ? searchKey
        : searchKey
        ? [searchKey]
        : [];

      if (keys.length === 0) {
        // fallback: tester toutes les colonnes
        return columnIds.some((colId) =>
          String(row.getValue(colId) ?? "")
            .toLowerCase()
            .includes(String(filterValue).toLowerCase())
        );
      }

      return keys.some((k) =>
        String(row.getValue(k) ?? "")
          .toLowerCase()
          .includes(String(filterValue).toLowerCase())
      );
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

  const columnItems = table
    .getAllColumns()
    .filter((c) => c.getCanHide && c.getCanHide())
    .map((c) => ({
      label: c.id === "rowNumber" ? "#" : c.id,
      value: c.id,
      checked: !!c.getIsVisible?.(),
    }));

  const selectedColumns = columnItems
    .filter((i) => i.checked)
    .map((i) => i.value);

  return (
    <div className="w-full">
      {/* Toolbar */}
      <HStack spacing={4} align="center" justify="between" className="py-4">
        {searchKey && (
          <Input
            placeholder={searchPlaceholder}
            value={globalFilter}
            onChange={(e) => {
              const v = e.target.value;
              setGlobalFilter(v);
              table.setGlobalFilter?.(v);
            }}
            className=""
          />
        )}

        <HStack spacing={4} align="center">
          {/* Colonnes : Dropdown multi-checkbox */}
          <Dropdown
            label={"Colonnes"}
            items={columnItems}
            multiple
            selectedValue={selectedColumns}
            onChange={(values) => {
              const arr = Array.isArray(values) ? values : [String(values)];
              table.getAllColumns().forEach((col) => {
                if (!col.getCanHide?.()) return;
                const shouldBeVisible = arr.includes(col.id);
                // utilise toggleVisibility seulement si nécessaire
                if (col.getIsVisible?.() !== shouldBeVisible) {
                  col.toggleVisibility?.(shouldBeVisible);
                }
              });
            }}
            contentClassName="w-56"
          />

          <Dropdown
            label={`
                Par page : ${table.getState().pagination.pageSize}
             `}
            items={pageSizeOptions.map((n) => ({
              label: `${n}`,
              value: `${n}`,
            }))}
            selectedValue={`${table.getState().pagination.pageSize}`}
            onChange={(value) => {
              table.setPageSize(Number(value));
            }}
            contentClassName="w-40"
          />
        </HStack>
      </HStack>

      {/* Table */}
      <div className="rounded-md border">
        <table className="w-full caption-bottom text-sm">
          <thead className="[&_tr]:border-b">
            {table.getHeaderGroups().map((headerGroup) => (
              <tr
                key={headerGroup.id}
                className="border-b transition-colors hover:bg-muted/50"
              >
                {headerGroup.headers.map((header) => (
                  <th
                    key={header.id}
                    className="h-12 px-4 text-left align-middle font-medium text-muted-foreground [&:has([role=checkbox])]:pr-0"
                  >
                    {header.isPlaceholder
                      ? null
                      : flexRender(
                          header.column.columnDef.header,
                          header.getContext()
                        )}
                  </th>
                ))}
                {actionsRow &&
                Array.isArray(actionsRow) &&
                actionsRow.length > 0 ? (
                  <th className="h-12 px-4 text-left align-middle font-medium text-muted-foreground [&:has([role=checkbox])]:pr-0">
                    Actions
                  </th>
                ) : null}
              </tr>
            ))}
          </thead>

          <tbody className="[&_tr:last-child]:border-0">
            {table.getRowModel().rows?.length ? (
              table.getRowModel().rows.map((row) => (
                <tr
                  key={row.id}
                  data-state={
                    row.getIsSelected && row.getIsSelected()
                      ? "selected"
                      : undefined
                  }
                  className="border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted"
                >
                  {row.getVisibleCells().map((cell) => (
                    <td
                      key={cell.id}
                      className="p-4 align-middle [&:has([role=checkbox])]:pr-0"
                    >
                      {flexRender(
                        cell.column.columnDef.cell,
                        cell.getContext()
                      )}
                    </td>
                  ))}
                  {actionsRow &&
                  Array.isArray(actionsRow) &&
                  actionsRow.length > 0 ? (
                    <td className="">
                      <HStack spacing={10} wrap>
                        {actionsRow.map((action, i) => (
                          <Button
                            key={i}
                            size="sm"
                            variant="square-outline"
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
                <td colSpan={cols.length} className="h-24 text-center">
                  Aucun résultat.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Footer : selection count + pagination */}
      <div className="flex items-center justify-end space-x-2 py-4">
        {showSelectionCount ? (
          <div className="flex-1 text-sm text-muted-foreground">
            {table.getFilteredSelectedRowModel().rows.length} sur{" "}
            {table.getFilteredRowModel().rows.length} ligne(s) sélectionnée(s).
          </div>
        ) : (
          <div className="flex-1" />
        )}

        <div>
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
    </div>
  );
}
