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
type PaymentMethod = "stripe" | "paypal";

interface Buyer {
  id: string;
  name: string;
  email?: string;
}

interface Payment {
  id: string;
  orderId: string;
  dealTitle?: string;
  buyer: Buyer;
  amount: number;
  currency: string;
  method: PaymentMethod;
  createdAt: string;
  status: PaymentStatus;
  refundedAmount?: number;
  note?: string;
}

// Icônes SVG pour les méthodes de paiement
const StripeIcon = () => (
  <svg viewBox="0 0 24 24" className="w-5 h-5" fill="currentColor">
    <path d="M13.976 9.15c-2.172-.806-3.356-1.426-3.356-2.409 0-.831.683-1.305 1.901-1.305 2.227 0 4.515.858 6.09 1.631l.89-5.494C18.252.975 15.697 0 12.165 0 9.667 0 7.589.654 6.104 1.872 4.56 3.147 3.757 4.992 3.757 7.218c0 4.039 2.467 5.76 6.476 7.219 2.585.92 3.445 1.574 3.445 2.583 0 .98-.84 1.545-2.354 1.545-1.875 0-4.965-.921-6.99-2.109l-.9 5.555C5.175 22.99 8.385 24 11.714 24c2.641 0 4.843-.624 6.328-1.813 1.664-1.305 2.525-3.236 2.525-5.732 0-4.128-2.524-5.851-6.591-7.305z" />
  </svg>
);

const PayPalIcon = () => (
  <svg viewBox="0 0 24 24" className="w-5 h-5" fill="currentColor">
    <path d="M7.076 21.337H2.47a.641.641 0 0 1-.633-.74L4.944 3.72a.77.77 0 0 1 .757-.63h6.727c2.254 0 3.939.544 5.003 1.617.942.95 1.331 2.275 1.156 3.94-.012.11-.023.219-.038.328a8.31 8.31 0 0 1-.065.42c-.7 3.948-2.9 5.927-6.739 6.049h-1.623a.77.77 0 0 0-.758.63l-.955 5.263zm12.477-13.3c-.03.206-.065.41-.105.615-1.102 5.678-4.64 7.048-9.053 7.048H9.03a.93.93 0 0 0-.917.764l-.877 5.556-.248 1.574a.49.49 0 0 0 .484.568h3.397a.77.77 0 0 0 .758-.63l.031-.163.603-3.824.039-.212a.77.77 0 0 1 .758-.631h.476c3.088 0 5.504-1.254 6.21-4.88.294-1.514.142-2.779-.637-3.667a3.047 3.047 0 0 0-.554-.518z" />
  </svg>
);

const MethodBadge: React.FC<{ method: PaymentMethod }> = ({ method }) => {
  const config = {
    stripe: {
      label: "Stripe",
      icon: <StripeIcon />,
      className: "bg-[#635bff]/10 text-[#635bff] border-[#635bff]/20",
    },
    paypal: {
      label: "PayPal",
      icon: <PayPalIcon />,
      className: "bg-[#003087]/10 text-[#003087] border-[#003087]/20",
    },
  };

  const { label, icon, className } = config[method] || config.stripe;

  return (
    <div className={`inline-flex items-center gap-1.5 px-2 py-1 rounded-md border text-xs font-medium ${className}`}>
      {icon}
      <span>{label}</span>
    </div>
  );
};

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
    cell: ({ row }) => (
      <div>
        <div className="font-medium">{row.original.orderId}</div>
        {row.original.dealTitle && (
          <div className="text-xs text-muted-foreground truncate max-w-[180px]">
            {row.original.dealTitle}
          </div>
        )}
      </div>
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
    cell: ({ row }) => <MethodBadge method={row.original.method} />,
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
  }
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
                }
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
                }
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
            }
          ];
        }}
      />
    </section>
  );
}
