import { DataTable } from "@/common/components";
import { Badge } from "@/common/components/ui/badge";
import { Heading } from "@/common/containers/Heading";
import { timeAgo } from "@/common/utils/date";
import type { ColumnDef } from "@tanstack/react-table";
import {
  Check,
  CircleCheckBig,
  DownloadIcon,
  EyeIcon,
  Phone,
  X,
} from "lucide-react";

type OrderStatus =
  | "pending" // payée mais pas encore validée
  | "confirmed" // validée par le marchand
  | "used" // consommée
  | "cancelled" // annulée
  | "refunded"; // remboursée

interface Order {
  id: string;
  orderNumber: string;
  dealTitle: string;
  buyer: {
    id: string;
    name: string;
    email?: string;
  };
  quantity: number;
  totalAmount: number;
  currency: string;
  status: OrderStatus;
  createdAt: string;
  usedAt?: string;
}

const OrderStatusBadge: React.FC<{ status: OrderStatus }> = ({ status }) => {
  switch (status) {
    case "pending":
      return (
        <Badge variant="outline" colorScheme="warning" size="sm">
          À valider
        </Badge>
      );
    case "confirmed":
      return (
        <Badge variant="outline" colorScheme="info" size="sm">
          Confirmée
        </Badge>
      );
    case "used":
      return (
        <Badge variant="outline" colorScheme="success" size="sm">
          Completée
        </Badge>
      );
    case "cancelled":
      return (
        <Badge variant="outline" colorScheme="danger" size="sm">
          Annulée
        </Badge>
      );
    case "refunded":
      return (
        <Badge variant="outline" colorScheme="secondary" size="sm">
          Remboursée
        </Badge>
      );
    default:
      return null;
  }
};

export default function OrdersReceivedList({ data }: { data: Order[] }) {
  const columns: ColumnDef<Order, any>[] = [
    {
      header: "Commande",
      accessorKey: "orderNumber",
      cell: ({ getValue }) => (
        <span className="font-medium">{getValue<string>()}</span>
      ),
    },
    {
      header: "Offre",
      accessorKey: "dealTitle",
      cell: ({ getValue }) => (
        <span className="text-sm">{getValue<string>()}</span>
      ),
    },
    {
      header: "Client",
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
      header: "Qté",
      accessorKey: "quantity",
    },
    {
      header: "Montant",
      accessorKey: "totalAmount",
      cell: ({ row }) => (
        <span className="font-medium">
          {row.original.totalAmount.toLocaleString()} {row.original.currency}
        </span>
      ),
    },
    {
      header: "Date",
      accessorKey: "createdAt",
      cell: ({ getValue }) => {
        const v = getValue<string>();
        return (
          <div className="flex flex-col">
            <span className="text-sm">{new Date(v).toLocaleDateString()}</span>
            <span className="text-xs text-muted-foreground">
              il y a {timeAgo(v)}
            </span>
          </div>
        );
      },
    },
    {
      header: "Statut",
      accessorKey: "status",
      cell: ({ getValue }) => (
        <OrderStatusBadge status={getValue<OrderStatus>()} />
      ),
    },
  ];

  return (
    <section>
      <Heading
        level={2}
        title="Mes commandes"
        description="Parcourez les commandes que vous avez faites"
        underline
      />

      <DataTable
        columns={columns}
        data={data}
        searchKey={["orderNumber", "buyer.name", "dealTitle"]}
        searchPlaceholder="Commande, client, offre..."
        pageSizeOptions={[10, 25, 50]}
        enableSelection={false}
        showSelectionCount
        enableRowNumber
        actionsRow={({ row }) => {
          const order = row.original;

          const canConfirm = order.status === "pending";
          const canUse = order.status === "confirmed";
          const canCancel = ["pending", "confirmed"].includes(order.status);

          return [
            {
              tooltip: "Voir",
              leftIcon: <EyeIcon className="w-4 h-4" />,
              onClick: () => alert(`Voir ${row.original.id}`),
            },
            ...(canConfirm
              ? [
                  {
                    tooltip: "Confirmer",
                    leftIcon: <Check className="w-4 h-4" />,
                    colorScheme: "success" as const,
                    onClick: () =>
                      alert(`Confirmer ${row.original.id} (simulation)`),
                  },
                ]
              : []),
            ...(canUse
              ? [
                  {
                    tooltip: "Marquer utilisée",
                    leftIcon: <CircleCheckBig className="w-4 h-4" />,
                    colorScheme: "warning" as const,
                    onClick: () =>
                      alert(
                        `Marquer ${row.original.id} comme utilisée (simulation)`
                      ),
                  },
                ]
              : []),
            ...(canCancel
              ? [
                  {
                    tooltip: "Annuler",
                    leftIcon: <X className="w-4 h-4" />,
                    colorScheme: "danger" as const,
                    onClick: () =>
                      alert(`Annuler ${row.original.id} (simulation)`),
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
              tooltip: "Télécharger ticket",
              leftIcon: <DownloadIcon className="w-4 h-4" />,
              colorScheme: "secondary" as const,
              onClick: () => alert(`Télécharger ticket ${row.original.id}`),
            },
          ];
        }}
      />
    </section>
  );
}
