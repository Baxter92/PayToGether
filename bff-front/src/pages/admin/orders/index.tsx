import type { ReactElement } from "react";
import { useState } from "react";
import { Search, Eye, Download } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { Input } from "@/common/components/ui/input";
import { Card, CardContent, CardHeader } from "@/common/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/common/components/ui/table";
import { Badge } from "@/common/components/ui/badge";
import Select from "@/common/components/Select";
import { formatCurrency } from "@/common/utils/formatCurrency";

const mockOrders = [
  {
    id: "ORD-001",
    customer: "Jean Dupont",
    deal: "Spa relaxant 1h",
    date: "2024-01-15",
    amount: 89,
    status: "completed",
  },
  {
    id: "ORD-002",
    customer: "Marie Martin",
    deal: "Restaurant gastronomique",
    date: "2024-01-14",
    amount: 120,
    status: "pending",
  },
  {
    id: "ORD-003",
    customer: "Pierre Bernard",
    deal: "Séance photo pro",
    date: "2024-01-14",
    amount: 75,
    status: "completed",
  },
  {
    id: "ORD-004",
    customer: "Sophie Laurent",
    deal: "Cours de cuisine",
    date: "2024-01-13",
    amount: 55,
    status: "refunded",
  },
  {
    id: "ORD-005",
    customer: "Lucas Petit",
    deal: "Escape game",
    date: "2024-01-13",
    amount: 35,
    status: "completed",
  },
  {
    id: "ORD-006",
    customer: "Emma Dubois",
    deal: "Massage duo",
    date: "2024-01-12",
    amount: 149,
    status: "pending",
  },
  {
    id: "ORD-007",
    customer: "Thomas Moreau",
    deal: "Karting session",
    date: "2024-01-12",
    amount: 45,
    status: "cancelled",
  },
];

export default function AdminOrders(): ReactElement {
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");

  const filteredOrders = mockOrders.filter((order) => {
    const matchesSearch =
      order.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      order.customer.toLowerCase().includes(searchQuery.toLowerCase()) ||
      order.deal.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus =
      statusFilter === "all" || order.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

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
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>Client</TableHead>
                <TableHead>Deal</TableHead>
                <TableHead>Date</TableHead>
                <TableHead className="text-right">Montant</TableHead>
                <TableHead>Statut</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredOrders.map((order) => (
                <TableRow key={order.id}>
                  <TableCell className="font-mono text-sm">
                    {order.id}
                  </TableCell>
                  <TableCell className="font-medium">
                    {order.customer}
                  </TableCell>
                  <TableCell>{order.deal}</TableCell>
                  <TableCell>{order.date}</TableCell>
                  <TableCell className="text-right">
                    {formatCurrency(order.amount)}
                  </TableCell>
                  <TableCell>{getStatusBadge(order.status)}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="icon">
                      <Eye className="h-4 w-4" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
