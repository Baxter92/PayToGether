import { type JSX, useMemo } from "react";
import { type ColumnDef } from "@tanstack/react-table";
import { Link } from "react-router-dom";
import { DataTable, StarRating } from "@/common/components";
import { Badge } from "@/common/components/ui/badge";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { Heading } from "@/common/containers/Heading";
import { PATHS } from "@/common/constants/path";
import {
  useMyPayments,
  useCommentaires,
  type MyPaymentsDTO,
  type CommentaireDTO,
  StatutPaiement,
  type StatutPaiementType,
  StatutCommande,
  type StatutCommandeType,
  type MethodePaiementType,
} from "@/common/api";
import { useAuth } from "@/common/context/AuthContext";
import { useI18n } from "@/common/hooks/useI18n";

type MyPaymentRow = MyPaymentsDTO & {
  dealTitre: string;
  categorieNom: string;
  dealImageUrl: string;
};

const paymentStatusConfig: Record<
  StatutPaiementType,
  { labelKey: string; colorScheme: "success" | "warning" | "danger" | "info" }
> = {
  [StatutPaiement.CONFIRME]: { labelKey: "completed", colorScheme: "success" },
  [StatutPaiement.EN_ATTENTE]: { labelKey: "pending", colorScheme: "warning" },
  [StatutPaiement.ECHOUE]: { labelKey: "failed", colorScheme: "danger" },
  [StatutPaiement.PROCESSING]: { labelKey: "processing", colorScheme: "info" },
  [StatutPaiement.REFUNDED]: { labelKey: "refunded", colorScheme: "info" },
  [StatutPaiement.CANCELLED]: { labelKey: "cancelled", colorScheme: "info" },
};

const orderStatusConfig: Record<
  StatutCommandeType,
  { labelKey: string; colorScheme: "success" | "warning" | "danger" | "info" }
> = {
  [StatutCommande.EN_ATTENTE]: { labelKey: "pending", colorScheme: "warning" },
  [StatutCommande.EN_COURS]: { labelKey: "pending", colorScheme: "info" },
  [StatutCommande.COMPLETE]: { labelKey: "complete", colorScheme: "info" },
  [StatutCommande.PAYOUT]: { labelKey: "payout", colorScheme: "info" },
  [StatutCommande.INVOICE_SELLER]: { labelKey: "invoiceSeller", colorScheme: "info" },
  [StatutCommande.INVOICE_CUSTOMER]: { labelKey: "invoiceCustomer", colorScheme: "info" },
  [StatutCommande.TERMINE]: { labelKey: "completed", colorScheme: "success" },
  [StatutCommande.LIVRÉE]: { labelKey: "completed", colorScheme: "success" },
  [StatutCommande.ANNULÉE]: { labelKey: "cancelled", colorScheme: "danger" },
  [StatutCommande.REMBOURSÉE]: { labelKey: "refunded", colorScheme: "info" },
};

export default function MyPurchases(): JSX.Element {
  const { t: tProfile } = useI18n("profile");
  const { t: tCommon } = useI18n();
  const { t: tStatus } = useI18n("status");
  const { user } = useAuth();
  const { data, isLoading, error, refetch } = useMyPayments();
  const { data: commentaires = [] } = useCommentaires();

  const payments: MyPaymentRow[] = (data ?? []).map((payment) => ({
    ...payment,
    dealTitre: payment.deal?.titre || "",
    categorieNom: payment.categorie?.nom || "",
    dealImageUrl: payment.deal?.imagePrincipaleUrlPresignee || "",
  }));

  const reviewsByDeal = useMemo(() => {
    const map = new Map<string, CommentaireDTO>();
    commentaires
      .filter(
        (commentaire) =>
          commentaire.utilisateurUuid === user?.id &&
          !commentaire.commentaireParentUuid,
      )
      .forEach((commentaire) => {
        const existing = map.get(commentaire.dealUuid);
        const currentDate = new Date(commentaire.dateCreation ?? 0).getTime();
        const existingDate = new Date(existing?.dateCreation ?? 0).getTime();
        if (!existing || currentDate > existingDate) {
          map.set(commentaire.dealUuid, commentaire);
        }
      });
    return map;
  }, [commentaires, user?.id]);

  const methodLabels: Record<MethodePaiementType, string> = {
    CARTE_CREDIT: tProfile("paymentMethods.card"),
    INTERAC: tProfile("paymentMethods.interac"),
    VIREMENT_BANCAIRE: tProfile("paymentMethods.bankTransfer"),
    SQUARE_CARD: tProfile("paymentMethods.squareCard"),
    SQUARE_GOOGLE_PAY: tProfile("paymentMethods.googlePay"),
    SQUARE_APPLE_PAY: tProfile("paymentMethods.applePay"),
    SQUARE_CASH_APP_PAY: tProfile("paymentMethods.cashAppPay"),
  };

  const columns = useMemo<ColumnDef<MyPaymentRow>[]>(() => {
    return [
      {
        id: "deal",
        header: tProfile("purchasesTable.deal"),
        cell: ({ row }) => {
          const deal = row.original.deal;
          const dealTitle = deal?.titre || "-";
          const dealUuid = deal?.uuid;
          const imageUrl = row.original.dealImageUrl || "/placeholder.svg";
          return (
            <div className="flex items-center gap-3 min-w-[220px]">
              <div className="h-12 w-12 rounded-md overflow-hidden bg-muted flex-shrink-0">
                <img
                  src={imageUrl}
                  alt={dealTitle}
                  className="h-full w-full object-cover"
                  loading="lazy"
                />
              </div>
              <div className="min-w-0">
                {dealUuid ? (
                  <Link
                    to={PATHS.DEAL_DETAIL(dealUuid)}
                    className="font-medium text-primary hover:underline truncate block"
                  >
                    {dealTitle}
                  </Link>
                ) : (
                  <span className="font-medium truncate block">
                    {dealTitle}
                  </span>
                )}
                <div className="text-xs text-muted-foreground truncate">
                  {row.original.categorieNom || "-"}
                </div>
              </div>
            </div>
          );
        },
      },
      {
        header: tProfile("purchasesTable.order"),
        accessorKey: "numeroCommande",
        cell: ({ row }) => (
          <div>
            <div className="font-medium">{row.original.numeroCommande}</div>
            <Badge
              variant="outline"
              colorScheme={
                orderStatusConfig[row.original.statutCommande]?.colorScheme ||
                "info"
              }
              size="sm"
            >
              {tStatus(
                orderStatusConfig[row.original.statutCommande]?.labelKey ||
                  row.original.statutCommande.toLowerCase(),
              )}
            </Badge>
          </div>
        ),
      },
      {
        header: tProfile("purchasesTable.payment"),
        accessorKey: "paiementUuid",
        cell: ({ row }) => (
          <div className="space-y-1">
            <div className="font-mono text-xs truncate max-w-[160px]">
              {row.original.paiementUuid}
            </div>
            <div className="text-xs text-muted-foreground truncate max-w-[160px]">
              {row.original.transactionId || "-"}
            </div>
          </div>
        ),
      },
      {
        header: tProfile("purchasesTable.amountPaid"),
        accessorKey: "montantPaiement",
        cell: ({ row }) => (
          <div className="font-medium">
            {formatCurrency(row.original.montantPaiement)}
          </div>
        ),
      },
      {
        header: tProfile("purchasesTable.amountTotal"),
        accessorKey: "montantTotal",
        cell: ({ row }) => (
          <div className="font-medium">
            {formatCurrency(row.original.montantTotal)}
          </div>
        ),
      },
      {
        header: tProfile("purchasesTable.parts"),
        accessorKey: "nbPartsAchetees",
        cell: ({ row }) => (
          <span className="font-medium">{row.original.nbPartsAchetees}</span>
        ),
      },
      {
        header: tProfile("purchasesTable.method"),
        accessorKey: "methodePaiement",
        cell: ({ row }) => (
          <span className="text-sm">
            {methodLabels[row.original.methodePaiement] ||
              row.original.methodePaiement}
          </span>
        ),
      },
      {
        header: tProfile("purchasesTable.paymentStatus"),
        accessorKey: "statutPaiement",
        cell: ({ row }) => (
          <Badge
            variant="outline"
            colorScheme={
              paymentStatusConfig[row.original.statutPaiement]?.colorScheme ||
              "info"
            }
            size="sm"
          >
            {tStatus(
              paymentStatusConfig[row.original.statutPaiement]?.labelKey ||
                row.original.statutPaiement.toLowerCase(),
            )}
          </Badge>
        ),
      },
      {
        id: "review",
        header: tProfile("purchasesTable.review"),
        cell: ({ row }) => {
          const dealUuid = row.original.deal?.uuid;
          if (!dealUuid) {
            return <span className="text-xs text-muted-foreground">-</span>;
          }

          const review = reviewsByDeal.get(dealUuid);
          if (!review) {
            return (
              <Link
                to={PATHS.DEAL_DETAIL(dealUuid)}
                className="text-primary text-sm hover:underline"
              >
                {tProfile("purchasesTable.leaveReview")}
              </Link>
            );
          }

          return (
            <div className="flex items-center gap-2">
              <StarRating value={review.note} size="sm" />
              <span className="text-xs text-muted-foreground">
                {tProfile("purchasesTable.reviewDone")}
              </span>
            </div>
          );
        },
      },
    ];
  }, [methodLabels, reviewsByDeal, tProfile, tStatus]);

  return (
    <section>
      <Heading
        level={2}
        title={tProfile("purchases")}
        description={tProfile("purchasesDescription")}
        underline
      />

      {isLoading ? (
        <div className="text-center py-8 text-muted-foreground">
          {tCommon("loading")}
        </div>
      ) : error ? (
        <div className="text-center py-8 text-destructive">
          {tProfile("purchasesLoadError")}
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={payments}
          searchKey={[
            "numeroCommande",
            "paiementUuid",
            "transactionId",
            "dealTitre",
            "categorieNom",
          ]}
          searchPlaceholder={tProfile("purchasesTable.searchPlaceholder")}
          pageSizeOptions={[10, 25, 50]}
          enableSelection={false}
          showSelectionCount={true}
          enableRowNumber={true}
          enableExport={false}
          onRefresh={() => refetch()}
        />
      )}
    </section>
  );
}
