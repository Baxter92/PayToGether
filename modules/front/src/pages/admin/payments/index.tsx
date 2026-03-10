import type { ReactElement } from "react";
import { useState } from "react";
import {
  CreditCard,
  Download,
  ArrowUpRight,
  ArrowDownRight,
  Loader2,
} from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { Badge } from "@/common/components/ui/badge";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import DataTable from "@/common/components/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { useI18n } from "@/common/hooks/useI18n";
import { VStack } from "@/common/components";
import ViewOrderDetailsModal from "../orders/components/ViewOrderDetailsModal";
import { useAdminPayments } from "@/common/api/hooks/usePayments";
import {
  StatutPaiement,
  type StatutPaiementType,
  type PaiementListDTO,
} from "@/common/api/types/payment";

const statusConfig: Record<
  StatutPaiementType,
  { colorScheme: "success" | "warning" | "danger" | "info" }
> = {
  [StatutPaiement.CONFIRME]: { colorScheme: "success" },
  [StatutPaiement.EN_ATTENTE]: { colorScheme: "warning" },
  [StatutPaiement.ECHOUE]: { colorScheme: "danger" },
  [StatutPaiement.PROCESSING]: { colorScheme: "info" },
  [StatutPaiement.REFUNDED]: { colorScheme: "info" },
  [StatutPaiement.CANCELLED]: { colorScheme: "info" },
};

const statusTranslationKey: Record<StatutPaiementType, string> = {
  [StatutPaiement.CONFIRME]: "completed",
  [StatutPaiement.EN_ATTENTE]: "pending",
  [StatutPaiement.ECHOUE]: "failed",
  [StatutPaiement.PROCESSING]: "processing",
  [StatutPaiement.REFUNDED]: "refunded",
  [StatutPaiement.CANCELLED]: "cancelled",
};

const methodLabels: Record<string, string> = {
  MOBILE_MONEY: "methodMobileMoney",
  CARTE_BANCAIRE: "methodCard",
  VIREMENT_BANCAIRE: "methodBankTransfer",
  SQUARE_CARD: "methodCard",
};

export default function AdminPayments(): ReactElement {
  const [selectedPayment, setSelectedPayment] = useState<any>(null);
  const [openOrderDetails, setOpenOrderDetails] = useState(false);
  const { t: tAdmin } = useI18n("admin");
  const { t: tStatus } = useI18n("status");

  const { data, isLoading, error } = useAdminPayments();

  const payments = data?.paiements ?? [];
  const stats = data?.statistiques;

  const columns: ColumnDef<PaiementListDTO>[] = [
    {
      accessorKey: "uuid",
      header: tAdmin("payments.paymentId"),
      cell: ({ row }) => (
        <span className="font-mono text-xs truncate max-w-[100px] block">
          {row.original.uuid}
        </span>
      ),
    },
    {
      accessorKey: "clientNom",
      header: tAdmin("payments.customer"),
      cell: ({ row }) =>
        `${row.original.clientPrenom} ${row.original.clientNom}`,
    },
    {
      accessorKey: "numeroCommande",
      header: tAdmin("payments.order"),
      cell: ({ row }) => (
        <Button
          variant="link"
          size="sm"
          className="p-0 h-auto"
          onClick={() => {
            setSelectedPayment({
              id: row.original.numeroCommande,
              customer: `${row.original.clientPrenom} ${row.original.clientNom}`,
              deal: row.original.dealTitre,
              date: row.original.datePaiement
                ? new Date(row.original.datePaiement).toLocaleDateString()
                : "-",
              amount: row.original.montant,
              status:
                statusTranslationKey[row.original.statutPaiement] || "pending",
            });
            setOpenOrderDetails(true);
          }}
        >
          {row.original.numeroCommande}
        </Button>
      ),
    },
    {
      accessorKey: "marchandNom",
      header: tAdmin("payments.merchant"),
      cell: ({ row }) =>
        `${row.original.marchandPrenom} ${row.original.marchandNom}`,
    },
    {
      accessorKey: "montant",
      header: tAdmin("payments.amount"),
      cell: ({ row }) => (
        <span className="font-medium">
          {formatCurrency(row.original.montant)}
        </span>
      ),
    },
    {
      accessorKey: "methodePaiement",
      header: tAdmin("payments.method"),
      cell: ({ row }) =>
        tAdmin(
          methodLabels[row.original.methodePaiement] ||
            row.original.methodePaiement,
        ),
    },
    {
      accessorKey: "statutPaiement",
      header: tAdmin("payments.status"),
      cell: ({ row }) => {
        const config = statusConfig[row.original.statutPaiement];
        const translationKey =
          statusTranslationKey[row.original.statutPaiement];
        return (
          <Badge colorScheme={config?.colorScheme || "info"}>
            {tStatus(
              translationKey || row.original.statutPaiement.toLowerCase(),
            )}
          </Badge>
        );
      },
    },
    {
      accessorKey: "datePaiement",
      header: tAdmin("payments.date"),
      cell: ({ row }) =>
        row.original.datePaiement
          ? new Date(row.original.datePaiement).toLocaleDateString()
          : "-",
    },
  ];

  if (isLoading) {
    return (
      <div className="flex h-[400px] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-[400px] items-center justify-center text-destructive">
        Erreur lors du chargement des paiements
      </div>
    );
  }

  return (
    <VStack spacing={6} className="p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            {tAdmin("payments.title")}
          </h1>
          <p className="text-muted-foreground">
            {tAdmin("payments.description")}
          </p>
        </div>
        <Button variant="outline">
          <Download className="mr-2 h-4 w-4" />
          {tAdmin("payments.export")}
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {tAdmin("payments.totalTransactions")}
            </CardTitle>
            <CreditCard className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats?.totalTransactions || 0}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {tAdmin("payments.totalRevenue")}
            </CardTitle>
            <div className="h-4 w-4 text-emerald-500 font-bold">$</div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(stats?.montantTotal || 0)}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {tAdmin("payments.completedTransactions")}
            </CardTitle>
            <ArrowUpRight className="h-4 w-4 text-emerald-500" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats?.transactionsReussies || 0}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {tAdmin("payments.failed")}
            </CardTitle>
            <ArrowDownRight className="h-4 w-4 text-destructive" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats?.transactionsEchouees || 0}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardContent>
          <DataTable
            columns={columns}
            data={payments}
            enableSelection={false}
            enableExport={false}
          />
        </CardContent>
      </Card>

      {selectedPayment && (
        <ViewOrderDetailsModal
          open={openOrderDetails}
          onClose={() => {
            setOpenOrderDetails(false);
            setSelectedPayment(null);
          }}
          order={selectedPayment}
        />
      )}
    </VStack>
  );
}
