import { DataTable, HStack, StarRating } from "@/common/components";
import { Badge } from "@/common/components/ui/badge";
import { Button } from "@/common/components/ui/button";
import { mockReviews } from "@/common/constants/data";
import { Heading } from "@/common/containers/Heading";
import { timeAgo } from "@/common/utils/date";
import type { ColumnDef } from "@tanstack/react-table";
import { EyeIcon, EyeOffIcon, Phone, ReplyIcon } from "lucide-react";

export type ReviewStatus = "published" | "pending" | "hidden";

export interface Review {
  id: string;
  orderNumber: string;
  dealTitle: string;
  buyer: {
    id: string;
    name: string;
    email?: string;
  };
  rating: number; // ex: 4.5
  comment: string;
  status: ReviewStatus;
  createdAt: string;
}

const ReviewStatusBadge: React.FC<{ status: ReviewStatus }> = ({ status }) => {
  switch (status) {
    case "published":
      return (
        <Badge variant="outline" colorScheme="success" size="sm">
          Publié
        </Badge>
      );
    case "pending":
      return (
        <Badge variant="outline" colorScheme="warning" size="sm">
          En attente
        </Badge>
      );
    case "hidden":
      return (
        <Badge variant="outline" colorScheme="secondary" size="sm">
          Masqué
        </Badge>
      );
    default:
      return null;
  }
};

export interface IReviewsListProps {
  data: Review[];
  isMyReviews?: boolean;
}

export default function ReviewsList({
  data,
  isMyReviews = false,
}: IReviewsListProps) {
  const columns: ColumnDef<Review, any>[] = [
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
      header: "Note",
      accessorKey: "rating",
      cell: ({ getValue }) => (
        <StarRating value={getValue<number>()} size="sm" />
      ),
    },
    {
      header: "Commentaire",
      accessorKey: "comment",
      cell: ({ getValue }) => (
        <p className="max-w-md truncate text-sm">{getValue<string>()}</p>
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
        <ReviewStatusBadge status={getValue<ReviewStatus>()} />
      ),
    },
  ];
  return (
    <section>
      <Heading
        level={2}
        title="Avis des clients"
        description="Historique des avis"
        underline
      />

      <DataTable
        columns={columns}
        data={mockReviews as Review[]}
        searchKey={["orderNumber", "buyer.name", "dealTitle", "comment"]}
        searchPlaceholder="Commande, client, offre, commentaire..."
        pageSizeOptions={[10, 25, 50]}
        enableSelection
        showSelectionCount
        enableRowNumber
        actionsRow={({ row }) => [
          {
            tooltip: "Voir",
            leftIcon: <EyeIcon className="w-4 h-4" />,
          },
          {
            tooltip: "Publier",
            leftIcon: <EyeIcon className="w-4 h-4" />,
            colorScheme: "success",
            onClick: () => alert(`Publier ${row.original.id}`),
          },
          {
            tooltip: "Masquer",
            leftIcon: <EyeOffIcon className="w-4 h-4" />,
            colorScheme: "warning",
            onClick: () => alert(`Masquer ${row.original.id}`),
          },
          {
            tooltip: "Répondre",
            colorScheme: "secondary",
            leftIcon: <ReplyIcon className="w-4 h-4" />,
          },
          {
            tooltip: "Contacter le client",
            colorScheme: "danger",
            leftIcon: <Phone className="w-4 h-4" />,
          },
        ]}
      />
    </section>
  );
}
