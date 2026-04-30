import { useState, type JSX } from "react";
import { DataTable } from "@/common/components";
import { Badge } from "@/common/components/ui/badge";
import { Button } from "@/common/components/ui/button";
import { Heading } from "@/common/containers/Heading";
import { timeAgo } from "@/common/utils/date";
import { formatCurrency } from "@/common/utils/formatCurrency";
import type { ColumnDef } from "@tanstack/react-table";
import {
  Upload,
  CheckCircle2,
  EyeIcon,
  Loader2,
} from "lucide-react";
import { useMerchantOrders, useAdminUploadSellerInvoice, useAdminValidateCustomerInvoices, useOrderCustomers } from "@/common/api/hooks/useOrders";
import { StatutCommande } from "@/common/api/types/order";
import { useI18n } from "@/common/hooks/useI18n";
import { useAuth } from "@/common/context/AuthContext";
import UploadSellerInvoiceModal from "@/pages/admin/orders/components/UploadSellerInvoiceModal";
import ValidateCustomerInvoicesModal from "@/pages/admin/orders/components/ValidateCustomerInvoicesModal";

export default function OrdersReceivedList(): JSX.Element {
  const { t } = useI18n("profile");
  const { t: tStatus } = useI18n("status");
  const { user } = useAuth();

  const [selectedOrder, setSelectedOrder] = useState<any>(null);
  const [openInvoiceModal, setOpenInvoiceModal] = useState(false);
  const [openValidationModal, setOpenValidationModal] = useState(false);

  // Récupérer les commandes du marchand connecté
  const { data, isLoading, error } = useMerchantOrders(user?.id ?? "");
  const orders = data?.commandes ?? [];

  // Hooks pour les mutations
  const uploadInvoiceMutation = useAdminUploadSellerInvoice();
  const validateCustomersMutation = useAdminValidateCustomerInvoices();

  // Hook pour récupérer les clients
  const { data: customers = [] } = useOrderCustomers(
    openValidationModal ? selectedOrder?.uuid : "",
  );

  const handleUploadInvoice = async (file: File): Promise<void> => {
    if (!selectedOrder?.uuid) return;
    await uploadInvoiceMutation.mutateAsync({
      uuid: selectedOrder.uuid,
      file,
    });
  };

  const handleValidateCustomers = async (
    validations: { utilisateurUuids: string }[],
  ): Promise<void> => {
    if (!selectedOrder?.uuid) return;
    await validateCustomersMutation.mutateAsync({
      uuid: selectedOrder.uuid,
      validations,
    });
  };

  const getStatusBadge = (status: string): JSX.Element => {
    switch (status) {
      case StatutCommande.TERMINE:
        return (
          <Badge variant="outline" className="bg-green-100 text-green-800">
            {tStatus("completed")}
          </Badge>
        );
      case StatutCommande.COMPLETE:
      case StatutCommande.CONFIRMEE:
        return (
          <Badge variant="outline" className="bg-blue-100 text-blue-800">
            {tStatus("complete")}
          </Badge>
        );
      case StatutCommande.PAYOUT:
        return (
          <Badge variant="outline" className="bg-purple-100 text-purple-800">
            {tStatus("payout")}
          </Badge>
        );
      case StatutCommande.INVOICE_SELLER:
        return (
          <Badge variant="outline" className="bg-indigo-100 text-indigo-800">
            {tStatus("invoiceSeller")}
          </Badge>
        );
      case StatutCommande.INVOICE_CUSTOMER:
        return (
          <Badge variant="outline" className="bg-cyan-100 text-cyan-800">
            {tStatus("invoiceCustomer")}
          </Badge>
        );
      case StatutCommande.EN_ATTENTE:
      case StatutCommande.EN_COURS:
        return (
          <Badge variant="outline" className="bg-yellow-100 text-yellow-800">
            {tStatus("pending")}
          </Badge>
        );
      case StatutCommande.ANNULÉE:
        return (
          <Badge variant="outline" className="bg-red-100 text-red-800">
            {tStatus("cancelled")}
          </Badge>
        );
      case StatutCommande.REMBOURSÉE:
        return (
          <Badge variant="outline" className="bg-gray-100 text-gray-800">
            {tStatus("refunded")}
          </Badge>
        );
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  const columns: ColumnDef<any, any>[] = [
    {
      header: t("ordersReceivedSection.table.order"),
      accessorKey: "numeroCommande",
      cell: ({ getValue }) => (
        <span className="font-medium">{getValue<string>()}</span>
      ),
    },
    {
      header: t("ordersReceivedSection.table.deal"),
      accessorKey: "dealTitre",
    },
    {
      header: t("ordersReceivedSection.table.amount"),
      accessorKey: "montantTotalPaiements",
      cell: ({ getValue }) => formatCurrency(getValue<number>()),
    },
    {
      header: t("ordersReceivedSection.table.date"),
      accessorKey: "dateCreation",
      cell: ({ getValue }) => {
        const v = getValue<string>();
        return (
          <div className="flex flex-col">
            <span className="text-sm">{new Date(v).toLocaleDateString()}</span>
            <span className="text-xs text-muted-foreground">
              {timeAgo(v)}
            </span>
          </div>
        );
      },
    },
    {
      header: t("ordersReceivedSection.table.status"),
      accessorKey: "statut",
      cell: ({ getValue }) => getStatusBadge(getValue<string>()),
    },
    {
      header: t("ordersReceivedSection.table.actions"),
      cell: ({ row }) => {
        const order = row.original;
        const statut = order.statut;

        return (
          <div className="flex items-center gap-2">
            {/* Bouton Upload Facture (statut PAYOUT) */}
            {statut === StatutCommande.PAYOUT && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setSelectedOrder(order);
                  setOpenInvoiceModal(true);
                }}
              >
                <Upload className="h-4 w-4 mr-1" />
                {t("ordersReceivedSection.actions.invoice")}
              </Button>
            )}

            {/* Bouton Valider Factures Clients (statut INVOICE_CUSTOMER) */}
            {statut === StatutCommande.INVOICE_CUSTOMER && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setSelectedOrder(order);
                  setOpenValidationModal(true);
                }}
              >
                <CheckCircle2 className="h-4 w-4 mr-1" />
                {t("ordersReceivedSection.actions.validate")}
              </Button>
            )}

            {/* Bouton Voir validations (statut TERMINE) */}
            {statut === StatutCommande.TERMINE && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => {
                  setSelectedOrder(order);
                  setOpenValidationModal(true);
                }}
              >
                <EyeIcon className="h-4 w-4 mr-1" />
                {t("ordersReceivedSection.actions.view")}
              </Button>
            )}
          </div>
        );
      },
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
        {t("ordersReceivedSection.loadError")}
      </div>
    );
  }

  return (
    <section>
      <Heading
        level={2}
        title={t("ordersReceivedSection.title")}
        description={t("ordersReceivedSection.description")}
        underline
      />

      <DataTable
        columns={columns}
        data={orders}
        searchKey={["numeroCommande", "dealTitre"]}
        searchPlaceholder={t("ordersReceivedSection.searchPlaceholder")}
        pageSizeOptions={[10, 25, 50]}
        enableSelection={false}
        enableRowNumber
      />

      {/* Modals */}
      {selectedOrder && (
        <>
          <UploadSellerInvoiceModal
            open={openInvoiceModal}
            onClose={() => {
              setOpenInvoiceModal(false);
            }}
            order={selectedOrder}
            onUpload={handleUploadInvoice}
          />
          <ValidateCustomerInvoicesModal
            open={openValidationModal}
            onClose={() => {
              setOpenValidationModal(false);
            }}
            order={selectedOrder}
            customers={customers}
            onValidate={handleValidateCustomers}
            isReadOnly={selectedOrder?.statut === StatutCommande.TERMINE}
          />
        </>
      )}
    </section>
  );
}
