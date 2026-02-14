import type { ReactElement } from "react";
import { useState } from "react";
import { Search, Eye, Download } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { Input } from "@/common/components/ui/input";
import { Card, CardContent, CardHeader } from "@/common/components/ui/card";
import { Badge } from "@/common/components/ui/badge";
import Select from "@/common/components/Select";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { DataTable } from "@/common/components";
import ViewOrderDetailsModal from "./components/ViewOrderDetailsModal";
import { ViewDetailDealModal } from "../deals/containers/ViewDetailDealModal";
import { useDeals } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import { useI18n } from "@/common/hooks/useI18n";

export const mockOrders = [
  {
    id: "ORD-001",
    customer: "Jean Dupont",
    deal: "Bœuf Charolais Premium - 5kg",
    dealId: 1,
    date: "2024-01-15",
    amount: 89,
    status: "completed",
  },
  {
    id: "ORD-002",
    customer: "Marie Martin",
    deal: "Tilapia Frais du Wouri - 3kg",
    dealId: 2,
    date: "2024-01-14",
    amount: 120,
    status: "pending",
  },
  {
    id: "ORD-003",
    customer: "Pierre Bernard",
    deal: "Viande de Bœuf Hachée - 2kg",
    dealId: 3,
    date: "2024-01-14",
    amount: 75,
    status: "completed",
  },
  {
    id: "ORD-004",
    customer: "Sophie Laurent",
    deal: "Saumon Frais - 2kg",
    dealId: 4,
    date: "2024-01-13",
    amount: 55,
    status: "refunded",
  },
  {
    id: "ORD-005",
    customer: "Lucas Petit",
    deal: "Côtes de Bœuf Grillades - 4kg",
    dealId: 5,
    date: "2024-01-13",
    amount: 35,
    status: "completed",
  },
  {
    id: "ORD-006",
    customer: "Emma Dubois",
    deal: "Crevettes Géantes - 1.5kg",
    dealId: 6,
    date: "2024-01-12",
    amount: 149,
    status: "pending",
  },
  {
    id: "ORD-007",
    customer: "Thomas Moreau",
    dealId: 7,
    deal: "Entrecôte de Bœuf Premium - 3kg",
    date: "2024-01-12",
    amount: 45,
    status: "cancelled",
  }
];

export default function AdminOrders(): ReactElement {
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [openViewDetails, setOpenViewDetails] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<any>(null);
  const [openDealDetails, setOpenDealDetails] = useState(false);
  const { data: dealsData } = useDeals();
  const deals = (dealsData ?? []).map(mapDealToView);
  const resolveDeal = (dealId: number) =>
    deals.find((d: any) => String(d.id) === String(dealId)) ||
    deals[Math.max(0, dealId - 1)];
  const { t: tAdmin } = useI18n("admin");
  const { t: tStatus } = useI18n("status");

  const columns = [
    {
      id: "id",
      header: tAdmin("orders.id"),
      accessorKey: "id",
    },
    {
      id: "customer",
      header: tAdmin("orders.customer"),
      accessorKey: "customer",
    },
    {
      id: "deal",
      header: tAdmin("orders.deal"),
      accessorKey: "deal",
      cell: ({ row }: { row: any }) => (
        <Button
          variant="link"
          size="sm"
          onClick={() => {
            setSelectedOrder(row.original);
            setOpenDealDetails(true);
          }}
        >
          {row.original.deal}
        </Button>
      ),
    },
    {
      id: "date",
      header: tAdmin("orders.date"),
      accessorKey: "date",
    },
    {
      id: "amount",
      header: tAdmin("orders.amount"),
      accessorKey: "amount",
      cell: ({ row }: { row: any }) => formatCurrency(row.original.amount),
    },
    {
      id: "status",
      header: tAdmin("orders.status"),
      accessorKey: "status",
      cell: ({ row }: { row: any }) => getStatusBadge(row.original.status),
    }
  ];

  // const filteredOrders = mockOrders.filter((order) => {
  //   const matchesSearch =
  //     order.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
  //     order.customer.toLowerCase().includes(searchQuery.toLowerCase()) ||
  //     order.deal.toLowerCase().includes(searchQuery.toLowerCase());
  //   const matchesStatus =
  //     statusFilter === "all" || order.status === statusFilter;
  //   return matchesSearch && matchesStatus;
  // });

  const getStatusBadge = (status: string): ReactElement => {
    switch (status) {
      case "completed":
        return (
          <Badge className="bg-green-100 text-green-800 hover:bg-green-100">
            {tStatus("completed")}
          </Badge>
        );
      case "pending":
        return (
          <Badge className="bg-yellow-100 text-yellow-800 hover:bg-yellow-100">
            {tStatus("pending")}
          </Badge>
        );
      case "refunded":
        return (
          <Badge className="bg-blue-100 text-blue-800 hover:bg-blue-100">
            {tStatus("refunded")}
          </Badge>
        );
      case "cancelled":
        return (
          <Badge className="bg-destructive/10 text-destructive hover:bg-destructive/10">
            {tStatus("cancelled")}
          </Badge>
        );
      default:
        return <Badge>{tStatus(status)}</Badge>;
    }
  };

  const stats = [
    { label: tAdmin("orders.stats.totalOrders"), value: mockOrders.length },
    {
      label: tAdmin("orders.stats.completed"),
      value: mockOrders.filter((o) => o.status === "completed").length,
    },
    {
      label: tAdmin("orders.stats.pending"),
      value: mockOrders.filter((o) => o.status === "pending").length,
    },
    {
      label: tAdmin("orders.stats.refunded"),
      value: mockOrders.filter((o) => o.status === "refunded").length,
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold font-heading">
            {tAdmin("orders.title")}
          </h1>
          <p className="text-muted-foreground">
            {tAdmin("orders.description")}
          </p>
        </div>
        <Button variant="outline">
          <Download className="h-4 w-4 mr-2" />
          {tAdmin("orders.export")}
        </Button>
      </div>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-4">
        {stats.map((stat) => (
          <Card key={stat.label}>
            <CardContent className="pt-6">
              <div className="text-2xl font-bold">{stat.value}</div>
              <p className="text-sm text-muted-foreground">{stat.label}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder={tAdmin("orders.search")}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
            <Select
              value={statusFilter}
              onValueChange={setStatusFilter}
              placeholder={tAdmin("orders.filterByStatus")}
              triggerClassName="w-[180px]"
              items={[
                { value: "all", label: tAdmin("orders.filter.all") },
                { value: "completed", label: tStatus("completed") },
                { value: "pending", label: tStatus("pending") },
                { value: "refunded", label: tStatus("refunded") },
                { value: "cancelled", label: tStatus("cancelled") }
              ]}
            />
          </div>
        </CardHeader>
        <CardContent>
          <DataTable
            columns={columns}
            data={mockOrders}
            actionsRow={({ row }) => [
              {
                leftIcon: <Eye />,
                onClick: () => {
                  setSelectedOrder(row.original);
                  setOpenViewDetails(true);
                },
              }
            ]}
          />
          <ViewOrderDetailsModal
            open={openViewDetails}
            onClose={() => setOpenViewDetails(false)}
            order={selectedOrder}
          />
          <ViewDetailDealModal
            open={openDealDetails}
            onClose={() => setOpenDealDetails(false)}
            deal={resolveDeal(Number(selectedOrder?.dealId ?? 0))}
          />
        </CardContent>
      </Card>
    </div>
  );
}
