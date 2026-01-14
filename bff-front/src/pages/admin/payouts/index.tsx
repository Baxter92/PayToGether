import { useState, type ReactElement } from "react";
import DataTable from "@/common/components/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import { Heading } from "@/common/containers/Heading";
import { Badge } from "@/common/components/ui/badge";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { CheckCircle, Clock, Send } from "lucide-react";
import { mockDeals } from "@/common/constants/data";
import { Button } from "@/common/components/ui/button";
import { ViewDetailDealModal } from "../deals/containers/ViewDetailDealModal";
import ViewDetailMerchantModal from "../merchants/containers/ViewDetailMerchantModal";

interface Payout {
  id: string;
  merchantId: string;
  merchantName: string;
  merchantEmail: string;
  dealId: number;
  amount: number;
  commission: number;
  netAmount: number;
  status: "pending" | "processing" | "paid" | "rejected";
  requestedAt: string;
  processedAt?: string;
  paymentMethod: string;
  bankAccount?: string;
}

const mockPayouts: Payout[] = [
  {
    id: "PAY001",
    merchantId: "M001",
    merchantName: "Jean Dupont",
    merchantEmail: "jean.dupont@email.com",
    dealId: 1,
    amount: 500000,
    commission: 50000,
    netAmount: 450000,
    status: "pending",
    requestedAt: "2024-01-15",
    paymentMethod: "Virement bancaire",
    bankAccount: "****4532",
  },
  {
    id: "PAY002",
    merchantId: "M002",
    merchantName: "Marie Martin",
    merchantEmail: "marie.martin@email.com",
    dealId: 2,
    amount: 320000,
    commission: 32000,
    netAmount: 288000,
    status: "processing",
    requestedAt: "2024-01-14",
    paymentMethod: "Mobile Money",
  },
  {
    id: "PAY003",
    merchantId: "M003",
    merchantName: "Pierre Kamga",
    merchantEmail: "p.kamga@email.com",
    dealId: 3,
    amount: 180000,
    commission: 18000,
    netAmount: 162000,
    status: "paid",
    requestedAt: "2024-01-10",
    processedAt: "2024-01-12",
    paymentMethod: "Virement bancaire",
    bankAccount: "****7821",
  },
  {
    id: "PAY004",
    merchantId: "M004",
    merchantName: "Sophie Nkomo",
    merchantEmail: "sophie.n@email.com",
    dealId: 4,
    amount: 420000,
    commission: 42000,
    netAmount: 378000,
    status: "rejected",
    requestedAt: "2024-01-08",
    processedAt: "2024-01-09",
    paymentMethod: "Mobile Money",
  },
  {
    id: "PAY005",
    merchantId: "M001",
    merchantName: "Jean Dupont",
    merchantEmail: "jean.dupont@email.com",
    dealId: 5,
    amount: 280000,
    commission: 28000,
    netAmount: 252000,
    status: "pending",
    requestedAt: "2024-01-16",
    paymentMethod: "Virement bancaire",
    bankAccount: "****4532",
  },
];

const statusConfig: Record<
  Payout["status"],
  {
    label: string;
    colorScheme: "default" | "secondary" | "warning" | "success" | "danger";
  }
> = {
  pending: { label: "En attente", colorScheme: "warning" },
  processing: { label: "En cours", colorScheme: "secondary" },
  paid: { label: "Payé", colorScheme: "success" },
  rejected: { label: "Rejeté", colorScheme: "danger" },
};

export default function AdminPayouts(): ReactElement {
  const [payouts] = useState<Payout[]>(mockPayouts);
  const [selectedPayout, setSelectedPayout] = useState<any>();
  const [openDealDetails, setOpenDealDetails] = useState(false);
  const [openMerchantDetails, setOpenMerchantDetails] = useState(false);

  const columns: ColumnDef<Payout>[] = [
    {
      accessorKey: "id",
      header: "ID",
      cell: ({ row }) => (
        <span className="font-mono text-sm">{row.original.id}</span>
      ),
    },
    {
      accessorKey: "merchantName",
      header: "Marchand",
      cell: ({ row }) => (
        <div>
          <Button
            variant="link"
            size="sm"
            onClick={() => setOpenMerchantDetails(true)}
          >
            {row.original.merchantName}
          </Button>
          <p className="text-xs text-muted-foreground">
            {row.original.merchantEmail}
          </p>
        </div>
      ),
    },
    {
      accessorKey: "dealId",
      header: "Commande",
      cell: ({ row }) => (
        <Button
          variant="link"
          size="sm"
          onClick={() => {
            setSelectedPayout(row.original);
            setOpenDealDetails(true);
          }}
        >
          {mockDeals.find((d) => d.id === row.original.dealId)?.title}
        </Button>
      ),
    },
    {
      accessorKey: "amount",
      header: "Montant brut",
      cell: ({ row }) => formatCurrency(row.original.amount),
    },
    {
      accessorKey: "commission",
      header: "Commission",
      cell: ({ row }) => (
        <span className="text-destructive">
          -{formatCurrency(row.original.commission)}
        </span>
      ),
    },
    {
      accessorKey: "netAmount",
      header: "Net à payer",
      cell: ({ row }) => (
        <span className="font-semibold text-primary">
          {formatCurrency(row.original.netAmount)}
        </span>
      ),
    },
    {
      accessorKey: "paymentMethod",
      header: "Méthode",
      cell: ({ row }) => (
        <div>
          <p className="text-sm">{row.original.paymentMethod}</p>
          {row.original.bankAccount && (
            <p className="text-xs text-muted-foreground">
              {row.original.bankAccount}
            </p>
          )}
        </div>
      ),
    },
    {
      accessorKey: "status",
      header: "Statut",
      cell: ({ row }) => (
        <Badge colorScheme={statusConfig[row.original.status].colorScheme}>
          {statusConfig[row.original.status].label}
        </Badge>
      ),
    },
    {
      accessorKey: "requestedAt",
      header: "Demandé le",
      cell: ({ row }) =>
        new Date(row.original.requestedAt).toLocaleDateString("fr-FR"),
    },
  ];

  const totalPending = payouts
    .filter((p) => p.status === "pending")
    .reduce((sum, p) => sum + p.netAmount, 0);
  const totalProcessing = payouts
    .filter((p) => p.status === "processing")
    .reduce((sum, p) => sum + p.netAmount, 0);
  const totalPaid = payouts
    .filter((p) => p.status === "paid")
    .reduce((sum, p) => sum + p.netAmount, 0);

  return (
    <main className="space-y-6">
      <Heading
        title="Paiements aux Marchands"
        description="Gérez les paiements à effectuer aux créateurs de deals"
        level={2}
        underline
      />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-card border rounded-lg p-4">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-orange-100 dark:bg-orange-900/30 rounded-full">
              <Clock className="h-5 w-5 text-orange-600" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">En attente</p>
              <p className="text-xl font-bold">
                {formatCurrency(totalPending)}
              </p>
            </div>
          </div>
        </div>
        <div className="bg-card border rounded-lg p-4">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-full">
              <Send className="h-5 w-5 text-blue-600" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">En cours</p>
              <p className="text-xl font-bold">
                {formatCurrency(totalProcessing)}
              </p>
            </div>
          </div>
        </div>
        <div className="bg-card border rounded-lg p-4">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-green-100 dark:bg-green-900/30 rounded-full">
              <CheckCircle className="h-5 w-5 text-green-600" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Payés ce mois</p>
              <p className="text-xl font-bold">{formatCurrency(totalPaid)}</p>
            </div>
          </div>
        </div>
      </div>

      <DataTable<Payout, unknown>
        data={payouts}
        columns={columns}
        searchKey="merchantName"
      />
      <ViewDetailDealModal
        open={openDealDetails}
        onClose={() => setOpenDealDetails(false)}
        deal={mockDeals.find((d) => d.id === selectedPayout?.dealId)}
      />
      <ViewDetailMerchantModal
        open={openMerchantDetails}
        onClose={() => setOpenMerchantDetails(false)}
      />
    </main>
  );
}
