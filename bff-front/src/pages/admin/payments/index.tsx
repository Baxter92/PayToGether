import type { ReactElement } from "react";
import { useState } from "react";
import {
  CreditCard,
  Search,
  Download,
  ArrowUpRight,
  ArrowDownRight,
} from "lucide-react";
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
import Select from "@/common/components/Select";
import DataTable from "@/common/components/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { VStack } from "@/common/components";
import ViewOrderDetailsModal from "../orders/components/ViewOrderDetailsModal";
import { mockOrders } from "../orders";

interface Payment {
  id: string;
  orderId: string;
  customer: string;
  merchant: string;
  amount: number;
  commission: number;
  method: "mobile_money" | "card" | "bank_transfer";
  status: "completed" | "pending" | "failed" | "refunded";
  createdAt: string;
}

const mockPayments: Payment[] = [
  {
    id: "PAY-001",
    orderId: "ORD-001",
    customer: "Jean Dupont",
    merchant: "Restaurant Le Gourmet",
    amount: 25000,
    commission: 2500,
    method: "mobile_money",
    status: "completed",
    createdAt: "2024-03-28T10:30:00",
  },
  {
    id: "PAY-002",
    orderId: "ORD-002",
    customer: "Marie Claire",
    merchant: "Spa Wellness",
    amount: 45000,
    commission: 4500,
    method: "card",
    status: "completed",
    createdAt: "2024-03-28T09:15:00",
  },
  {
    id: "PAY-003",
    orderId: "ORD-003",
    customer: "Paul Martin",
    merchant: "Boutique Mode",
    amount: 18000,
    commission: 1800,
    method: "mobile_money",
    status: "pending",
    createdAt: "2024-03-27T16:45:00",
  },
  {
    id: "PAY-004",
    orderId: "ORD-004",
    customer: "Alice Nkomo",
    merchant: "Auto Service Pro",
    amount: 75000,
    commission: 7500,
    method: "bank_transfer",
    status: "failed",
    createdAt: "2024-03-27T14:20:00",
  },
  {
    id: "PAY-005",
    orderId: "ORD-005",
    customer: "Bruno Ekwalla",
    merchant: "Restaurant Le Gourmet",
    amount: 32000,
    commission: 3200,
    method: "card",
    status: "refunded",
    createdAt: "2024-03-26T11:00:00",
  },
];

const statusConfig = {
  completed: { label: "Complété", colorScheme: "success" as const },
  pending: { label: "En attente", colorScheme: "warning" as const },
  failed: { label: "Échoué", colorScheme: "danger" as const },
  refunded: { label: "Remboursé", colorScheme: "info" as const },
};

const methodLabels = {
  mobile_money: "Mobile Money",
  card: "Carte bancaire",
  bank_transfer: "Virement",
};

export default function AdminPayments(): ReactElement {
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [selectedPayment, setSelectedPayment] = useState<any>(null);
  const [openOrderDetails, setOpenOrderDetails] = useState(false);

  const filteredPayments = mockPayments.filter((payment) => {
    const matchesSearch =
      payment.customer.toLowerCase().includes(searchQuery.toLowerCase()) ||
      payment.merchant.toLowerCase().includes(searchQuery.toLowerCase()) ||
      payment.id.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesStatus =
      statusFilter === "all" || payment.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const columns: ColumnDef<Payment>[] = [
    {
      accessorKey: "id",
      header: "ID Paiement",
      cell: ({ row }) => (
        <span className="font-mono text-sm">{row.original.id}</span>
      ),
    },
    {
      accessorKey: "customer",
      header: "Client",
    },
    {
      accessorKey: "orderId",
      header: "Commande",
      cell: ({ row }) => (
        <Button
          variant="link"
          size="sm"
          onClick={() => {
            setSelectedPayment(row.original);
            setOpenOrderDetails(true);
          }}
        >
          {row.original.orderId}
        </Button>
      ),
    },
    {
      accessorKey: "merchant",
      header: "Marchand",
    },
    {
      accessorKey: "amount",
      header: "Montant",
      cell: ({ row }) => (
        <span className="font-medium">
          {formatCurrency(row.original.amount)}
        </span>
      ),
    },
    {
      accessorKey: "commission",
      header: "Commission",
      cell: ({ row }) => (
        <span className="text-muted-foreground">
          {formatCurrency(row.original.commission)}
        </span>
      ),
    },
    {
      accessorKey: "method",
      header: "Méthode",
      cell: ({ row }) => methodLabels[row.original.method],
    },
    {
      accessorKey: "status",
      header: "Statut",
      cell: ({ row }) => {
        const config = statusConfig[row.original.status];
        return <Badge colorScheme={config.colorScheme}>{config.label}</Badge>;
      },
    },
    {
      accessorKey: "createdAt",
      header: "Date",
      cell: ({ row }) =>
        new Date(row.original.createdAt).toLocaleDateString("fr-FR", {
          day: "2-digit",
          month: "short",
          year: "numeric",
          hour: "2-digit",
          minute: "2-digit",
        }),
    },
  ];

  const totalRevenue = mockPayments
    .filter((p) => p.status === "completed")
    .reduce((acc, p) => acc + p.amount, 0);

  const totalCommission = mockPayments
    .filter((p) => p.status === "completed")
    .reduce((acc, p) => acc + p.commission, 0);

  return (
    <VStack>
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-heading font-bold flex items-center gap-2">
            <CreditCard className="h-8 w-8" />
            Paiements
          </h1>
          <p className="text-muted-foreground mt-1">
            Suivi des transactions et paiements
          </p>
        </div>
        <Button variant="outline" leftIcon={<Download className="h-4 w-4" />}>
          Exporter
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Total transactions</CardDescription>
            <CardTitle className="text-2xl">{mockPayments.length}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Revenus totaux</CardDescription>
            <CardTitle className="text-2xl flex items-center gap-1">
              <ArrowUpRight className="h-5 w-5 text-green-600" />
              {formatCurrency(totalRevenue)}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Commissions</CardDescription>
            <CardTitle className="text-2xl">
              {formatCurrency(totalCommission)}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardDescription>Échoués</CardDescription>
            <CardTitle className="text-2xl flex items-center gap-1 text-destructive">
              <ArrowDownRight className="h-5 w-5" />
              {mockPayments.filter((p) => p.status === "failed").length}
            </CardTitle>
          </CardHeader>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <CardTitle>Historique des paiements</CardTitle>
            <div className="flex flex-col sm:flex-row gap-2">
              <div className="relative w-full sm:w-64">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Rechercher..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Select
                value={statusFilter}
                onValueChange={setStatusFilter}
                placeholder="Statut"
                triggerClassName="w-full sm:w-40"
                items={[
                  { value: "all", label: "Tous" },
                  { value: "completed", label: "Complétés" },
                  { value: "pending", label: "En attente" },
                  { value: "failed", label: "Échoués" },
                  { value: "refunded", label: "Remboursés" },
                ]}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <DataTable
            columns={columns}
            data={filteredPayments}
            columnFiltersConfig={[
              {
                id: "orderId",
                label: "Commande",
                type: "select",
                options: mockOrders.map((o) => ({ label: o.id, value: o.id })),
              },
            ]}
          />
        </CardContent>
      </Card>
      <ViewOrderDetailsModal
        open={openOrderDetails}
        onClose={() => setOpenOrderDetails(false)}
        order={mockOrders.find((o) => o.id === selectedPayment?.orderId) as any}
      />
    </VStack>
  );
}
