import { type JSX } from "react";
import { type ColumnDef } from "@tanstack/react-table";
import { DataTable } from "@/common/components";
import { Badge } from "@/common/components/ui/badge";
import { formatCurrency } from "@/common/utils/formatCurrency";
import {
  CheckIcon,
  DownloadIcon,
  EyeIcon,
  Phone,
  Wallet2Icon,
} from "lucide-react";
import { timeAgo } from "@/common/utils/date";
import { mockPayments } from "@/common/constants/data";
import { Heading } from "@/common/containers/Heading";

// Types
type PaymentStatus = "pending" | "succeeded" | "failed" | "refunded";

interface Buyer {
  id: string;
  name: string;
  email?: string;
}

interface Payment {
  id: string; // uuid / payment id
  orderId: string;
  buyer: Buyer;
  amount: number; // en centimes ou unité (ici on utilisera unité)
  currency: string;
  method: string; // Mobile Money, Card, Cash
  createdAt: string; // ISO date
  status: PaymentStatus;
  refundedAmount?: number;
  note?: string;
}

const StatusBadge: React.FC<{ status: PaymentStatus }> = ({ status }) => {
  switch (status) {
    case "succeeded":
      return (
        <Badge variant="outline" colorScheme="success" size="sm">
          Payé
        </Badge>
      );
    case "pending":
      return (
        <Badge variant="outline" colorScheme="warning" size="sm">
          En attente
        </Badge>
      );
    case "failed":
      return (
        <Badge variant="outline" colorScheme="danger" size="sm">
          Échoué
        </Badge>
      );
    case "refunded":
      return (
        <Badge variant="outline" colorScheme="info" size="sm">
          Remboursé
        </Badge>
      );
    default:
      return (
        <Badge variant="outline" colorScheme="default" size="sm">
          {status}
        </Badge>
      );
  }
};

// Cols pour le DataTable
const columns: ColumnDef<Payment, any>[] = [
  {
    header: "Commande",
    accessorKey: "orderId",
    cell: ({ getValue }) => (
      <div className="font-medium">{getValue<string>()}</div>
    ),
  },
  {
    header: "Acheteur",
    accessorFn: (row) => row.buyer.name,
    id: "buyer",
    cell: ({ row }) => (
      <div>
        <div className="font-medium">{row.original.buyer.name}</div>
        <div className="text-xs text-muted-foreground">
          {row.original.buyer.email}
        </div>
      </div>
    ),
  },
  {
    header: "Montant",
    accessorKey: "amount",
    cell: ({ row }) => (
      <div className="font-medium">
        {formatCurrency(row.original.amount, row.original.currency)}
      </div>
    ),
  },
  {
    header: "Méthode",
    accessorKey: "method",
    cell: ({ getValue }) => <div className="text-sm">{getValue<string>()}</div>,
  },
  {
    header: "Date",
    accessorKey: "createdAt",
    cell: ({ getValue }) => {
      const v = getValue<string>();
      return (
        <span className="text-xs text-muted-foreground">
          il y a {timeAgo(v)}
        </span>
      );
    },
  },
  {
    header: "Statut",
    accessorKey: "status",
    cell: ({ getValue }) => <StatusBadge status={getValue<PaymentStatus>()} />,
  },
  {
    header: "Détails",
    id: "note",
    cell: ({ row }) => (
      <div className="text-sm text-muted-foreground">
        {row.original.note ?? "-"}
      </div>
    ),
  },
];

export default function PaymentsList(): JSX.Element {
  return (
    <section>
      <Heading
        level={2}
        title="Paiements reçus"
        description="Historique des paiements effectués sur votre compte."
        underline
      />

      <DataTable
        columns={columns}
        data={mockPayments as Payment[]}
        searchKey={["orderId", "buyer.name", "buyer.email"]}
        searchPlaceholder="Rechercher par commande, acheteur..."
        pageSizeOptions={[10, 25, 50]}
        enableSelection={false}
        showSelectionCount={true}
        enableRowNumber={true}
        actionsRow={({ row }) => {
          const p = row.original as Payment;

          const canRefund =
            p.status === "succeeded" && (p.refundedAmount ?? 0) < p.amount;
          const canMarkPaid = p.status === "pending";
          return [
            {
              tooltip: "Voir",
              leftIcon: <EyeIcon className="w-4 h-4" />,
              onClick: () => alert(`Voir ${row.original.id}`),
            },
            ...(canMarkPaid
              ? [
                  {
                    tooltip: "Marquer payé",
                    leftIcon: <CheckIcon className="w-4 h-4" />,
                    colorScheme: "success" as const,
                    onClick: () =>
                      alert(
                        `Marquer ${row.original.id} comme payé (simulation)`
                      ),
                  },
                ]
              : []),
            ...(canRefund
              ? [
                  {
                    tooltip: "Rembourser",
                    leftIcon: <Wallet2Icon className="w-4 h-4" />,
                    colorScheme: "warning" as const,
                    onClick: () =>
                      alert(`Rembourser ${row.original.id} (simulation)`),
                  },
                ]
              : []),

            {
              tooltip: "Contacter l'acheteur",
              leftIcon: <Phone className="w-4 h-4" />,
              colorScheme: "danger" as const,
              onClick: () => alert(`Contacter l'acheteur ${row.original.id}`),
            },
            {
              tooltip: "Télécharger reçu",
              leftIcon: <DownloadIcon className="w-4 h-4" />,
              colorScheme: "secondary" as const,
              onClick: () => alert(`Télécharger reçu ${row.original.id}`),
            },
          ];
        }}
      />
    </section>
  );
}
