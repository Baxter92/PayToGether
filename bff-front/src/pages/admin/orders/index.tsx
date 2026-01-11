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
import { mockDeals } from "@/common/constants/data";

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
  },
];

export default function AdminOrders(): ReactElement {
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [openViewDetails, setOpenViewDetails] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<any>(null);
  const [openDealDetails, setOpenDealDetails] = useState(false);

  const columns = [
    {
      id: "id",
      header: "ID",
      accessorKey: "id",
    },
    {
      id: "customer",
      header: "Client",
      accessorKey: "customer",
    },
    {
      id: "deal",
      header: "Deal",
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
      header: "Date",
      accessorKey: "date",
    },
    {
      id: "amount",
      header: "Montant",
      accessorKey: "amount",
      cell: ({ row }: { row: any }) => formatCurrency(row.original.amount),
    },
    {
      id: "status",
      header: "Statut",
      accessorKey: "status",
      cell: ({ row }: { row: any }) => getStatusBadge(row.original.status),
    },
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
            Complété
          </Badge>
        );
      case "pending":
        return (
          <Badge className="bg-yellow-100 text-yellow-800 hover:bg-yellow-100">
            En attente
          </Badge>
        );
      case "refunded":
        return (
          <Badge className="bg-blue-100 text-blue-800 hover:bg-blue-100">
            Remboursé
          </Badge>
        );
      case "cancelled":
        return (
          <Badge className="bg-destructive/10 text-destructive hover:bg-destructive/10">
            Annulé
          </Badge>
        );
      default:
        return <Badge>{status}</Badge>;
    }
  };

  const stats = [
    { label: "Total commandes", value: mockOrders.length },
    {
      label: "Complétées",
      value: mockOrders.filter((o) => o.status === "completed").length,
    },
    {
      label: "En attente",
      value: mockOrders.filter((o) => o.status === "pending").length,
    },
    {
      label: "Remboursées",
      value: mockOrders.filter((o) => o.status === "refunded").length,
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold font-heading">
            Gestion des Commandes
          </h1>
          <p className="text-muted-foreground">
            Suivez et gérez toutes les commandes
          </p>
        </div>
        <Button variant="outline">
          <Download className="h-4 w-4 mr-2" />
          Exporter
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
                placeholder="Rechercher une commande..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
            <Select
              value={statusFilter}
              onValueChange={setStatusFilter}
              placeholder="Filtrer par statut"
              triggerClassName="w-[180px]"
              items={[
                { value: "all", label: "Tous les statuts" },
                { value: "completed", label: "Complété" },
                { value: "pending", label: "En attente" },
                { value: "refunded", label: "Remboursé" },
                { value: "cancelled", label: "Annulé" },
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
              },
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
            deal={mockDeals.find((d) => d.id === selectedOrder?.dealId)}
          />
        </CardContent>
      </Card>
    </div>
  );
}
