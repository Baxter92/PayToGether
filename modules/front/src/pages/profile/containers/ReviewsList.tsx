import { DataTable, StarRating } from "@/common/components";
import { Badge } from "@/common/components/ui/badge";
import { Heading } from "@/common/containers/Heading";
import { useI18n } from "@/common/hooks/useI18n";
import { timeAgo } from "@/common/utils/date";
import type { ColumnDef } from "@tanstack/react-table";
import { EyeIcon, Phone, ReplyIcon, Trash2Icon } from "lucide-react";

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
  const { t } = useI18n("profile");

  switch (status) {
    case "published":
      return (
        <Badge variant="outline" colorScheme="success" size="sm">
          {t("reviewsSection.status.published")}
        </Badge>
      );
    case "pending":
      return (
        <Badge variant="outline" colorScheme="warning" size="sm">
          {t("reviewsSection.status.pending")}
        </Badge>
      );
    case "hidden":
      return (
        <Badge variant="outline" colorScheme="secondary" size="sm">
          {t("reviewsSection.status.hidden")}
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
  const { t } = useI18n("profile");

  const columns: ColumnDef<Review, any>[] = [
    {
      header: t("reviewsSection.table.order"),
      accessorKey: "orderNumber",
      cell: ({ getValue }) => (
        <span className="font-medium">{getValue<string>()}</span>
      ),
    },
    {
      header: t("reviewsSection.table.deal"),
      accessorKey: "dealTitle",
      cell: ({ getValue }) => (
        <span className="text-sm">{getValue<string>()}</span>
      ),
    },
    {
      header: t("reviewsSection.table.customer"),
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
      header: t("reviewsSection.table.rating"),
      accessorKey: "rating",
      cell: ({ getValue }) => (
        <StarRating value={getValue<number>()} size="sm" />
      ),
    },
    {
      header: t("reviewsSection.table.comment"),
      accessorKey: "comment",
      cell: ({ getValue }) => (
        <p className="max-w-md truncate text-sm">{getValue<string>()}</p>
      ),
    },
    {
      header: t("reviewsSection.table.date"),
      accessorKey: "createdAt",
      cell: ({ getValue }) => {
        const v = getValue<string>();
        return (
          <div className="flex flex-col">
            <span className="text-sm">{new Date(v).toLocaleDateString()}</span>
            <span className="text-xs text-muted-foreground">
              {t("reviewsSection.timeAgo", { value: timeAgo(v) })}
            </span>
          </div>
        );
      },
    },
    ...(isMyReviews
      ? [
        {
          header: t("reviewsSection.table.status"),
          accessorKey: "status",
          cell: ({ getValue }: { getValue: () => ReviewStatus }) => (
            <ReviewStatusBadge status={getValue()} />
          ),
        },
      ]
      : []),
  ];
  return (
    <section>
      <Heading
        level={2}
        title={t("reviewsSection.title")}
        description={t("reviewsSection.description")}
        underline
      />

      <DataTable
        columns={columns}
        data={data}
        searchKey={["orderNumber", "buyer.name", "dealTitle", "comment"]}
        searchPlaceholder={t("reviewsSection.searchPlaceholder")}
        pageSizeOptions={[10, 25, 50]}
        enableSelection={false}
        showSelectionCount
        enableRowNumber
        actionsRow={() => [
          {
            tooltip: t("reviewsSection.actions.view"),
            leftIcon: <EyeIcon className="w-4 h-4" />,
          },
          ...(isMyReviews
            ? [
              {
                tooltip: t("reviewsSection.actions.delete"),
                leftIcon: <Trash2Icon className="w-4 h-4" />,
                colorScheme: "danger" as const,
              },
            ]
            : []),
          {
            tooltip: t("reviewsSection.actions.reply"),
            colorScheme: "secondary" as const,
            leftIcon: <ReplyIcon className="w-4 h-4" />,
          },
          {
            tooltip: t("reviewsSection.actions.contactCustomer"),
            colorScheme: "danger" as const,
            leftIcon: <Phone className="w-4 h-4" />,
          },
        ]}
      />
    </section>
  );
}
