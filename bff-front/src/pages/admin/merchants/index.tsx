import type { ReactElement } from "react";
import { useState } from "react";
import { Store, Search, MoreHorizontal, Eye, Ban, Check } from "lucide-react";
import { Input } from "@/common/components/ui/input";
import { Button } from "@/common/components/ui/button";
import { Badge } from "@/common/components/ui/badge";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/common/components/ui/dropdown-menu";
import DataTable from "@/common/components/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { useI18n } from "@/common/hooks/useI18n";

interface Merchant {
  id: string;
  name: string;
  email: string;
  phone: string;
  totalDeals: number;
  totalSales: number;
  status: "active" | "pending" | "suspended";
  createdAt: string;
}

const mockMerchants: Merchant[] = [
  {
    id: "1",
    name: "Restaurant Le Gourmet",
    email: "contact@legourmet.cm",
    phone: "+237 6 90 00 00 01",
    totalDeals: 12,
    totalSales: 450000,
    status: "active",
    createdAt: "2024-01-15",
  },
  {
    id: "2",
    name: "Spa Wellness",
    email: "info@spawellness.cm",
    phone: "+237 6 90 00 00 02",
    totalDeals: 8,
    totalSales: 280000,
    status: "active",
    createdAt: "2024-02-20",
  },
  {
    id: "3",
    name: "Boutique Mode",
    email: "contact@boutiquemode.cm",
    phone: "+237 6 90 00 00 03",
    totalDeals: 5,
    totalSales: 150000,
    status: "pending",
    createdAt: "2024-03-10",
  },
  {
    id: "4",
    name: "Auto Service Pro",
    email: "service@autopro.cm",
    phone: "+237 6 90 00 00 04",
    totalDeals: 3,
    totalSales: 95000,
    status: "suspended",
    createdAt: "2024-03-25",
  },
];

const statusConfig = {
  active: { colorScheme: "success" as const },
  pending: { colorScheme: "warning" as const },
  suspended: { colorScheme: "danger" as const },
};

export default function AdminMerchants(): ReactElement {
  const [searchQuery, setSearchQuery] = useState("");
  const { t: tAdmin } = useI18n("admin");
  const { t: tStatus } = useI18n("status");

  const filteredMerchants = mockMerchants.filter(
    (merchant) =>
      merchant.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      merchant.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const columns: ColumnDef<Merchant>[] = [
    {
      accessorKey: "name",
      header: tAdmin("merchants.name"),
      cell: ({ row }) => (
        <div>
          <p className="font-medium">{row.original.name}</p>
          <p className="text-sm text-muted-foreground">{row.original.email}</p>
        </div>
      ),
    },
    {
      accessorKey: "phone",
      header: tAdmin("merchants.phone"),
    },
    {
      accessorKey: "totalDeals",
      header: tAdmin("merchants.dealsCount"),
      cell: ({ row }) => <span>{row.original.totalDeals}</span>,
    },
    {
      accessorKey: "totalSales",
      header: tAdmin("merchants.totalSales"),
      cell: ({ row }) => <span>{formatCurrency(row.original.totalSales)}</span>,
    },
    {
      accessorKey: "status",
      header: tAdmin("merchants.status"),
      cell: ({ row }) => {
        const config = statusConfig[row.original.status];
        return (
          <Badge colorScheme={config.colorScheme}>
            {tStatus(row.original.status)}
          </Badge>
        );
      },
    },
    {
      accessorKey: "createdAt",
      header: tAdmin("merchants.joinedAt"),
      cell: ({ row }) =>
        new Date(row.original.createdAt).toLocaleDateString("fr-FR"),
    },
    {
      id: "actions",
      cell: ({ row }) => (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon">
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem
              onClick={() => console.log("View", row.original.id)}
            >
              <Eye className="mr-2 h-4 w-4" />
              {tAdmin("merchants.viewDetails")}
            </DropdownMenuItem>
            <DropdownMenuItem
              onClick={() => console.log("Approve", row.original.id)}
            >
              <Check className="mr-2 h-4 w-4" />
              {tAdmin("merchants.approve")}
            </DropdownMenuItem>
            <DropdownMenuItem
              className="text-destructive"
              onClick={() => console.log("Suspend", row.original.id)}
            >
              <Ban className="mr-2 h-4 w-4" />
              {tAdmin("merchants.suspend")}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      ),
    },
  ];

  const stats = {
    total: mockMerchants.length,
    active: mockMerchants.filter((m) => m.status === "active").length,
    pending: mockMerchants.filter((m) => m.status === "pending").length,
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-heading font-bold flex items-center gap-2">
          <Store className="h-8 w-8" />
          {tAdmin("merchants.title")}
        </h1>
        <p className="text-muted-foreground mt-1">
          {tAdmin("merchants.description")}
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>{tAdmin("merchants.total")}</CardDescription>
            <CardTitle className="text-2xl">{stats.total}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>{tStatus("active")}</CardDescription>
            <CardTitle className="text-2xl text-green-600">
              {stats.active}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>{tStatus("pending")}</CardDescription>
            <CardTitle className="text-2xl text-yellow-600">
              {stats.pending}
            </CardTitle>
          </CardHeader>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <CardTitle>{tAdmin("merchants.listTitle")}</CardTitle>
            <div className="relative w-full sm:w-64">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder={tAdmin("merchants.search")}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <DataTable columns={columns} data={filteredMerchants} />
        </CardContent>
      </Card>
    </div>
  );
}
