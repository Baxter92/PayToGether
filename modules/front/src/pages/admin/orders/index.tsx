import type { ReactElement } from "react";
import { useState } from "react";
import { Eye, Download, Loader2, DollarSign, Upload, CheckCircle2 } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import { Badge } from "@/common/components/ui/badge";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { DataTable, VStack } from "@/common/components";
import ViewOrderDetailsModal from "./components/ViewOrderDetailsModal";
import ValidatePayoutModal from "./components/ValidatePayoutModal";
import UploadSellerInvoiceModal from "./components/UploadSellerInvoiceModal";
import ValidateCustomerInvoicesModal from "./components/ValidateCustomerInvoicesModal";
import { ViewDetailDealModal } from "../deals/containers/ViewDetailDealModal";
import {
  useAdminOrders,
  useAdminValidatePayout,
  useAdminUploadSellerInvoice,
  useAdminValidateCustomerInvoices,
  useOrderCustomers,
} from "@/common/api/hooks/useOrders";
import { useI18n } from "@/common/hooks/useI18n";
import { StatutCommande } from "@/common/api/types/order";
import { useDeal } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import { useAuth } from "@/common/context/AuthContext";

export default function AdminOrders(): ReactElement {
  const [openViewDetails, setOpenViewDetails] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<any>(null);
  const [openDealDetails, setOpenDealDetails] = useState(false);
  const [openPayoutModal, setOpenPayoutModal] = useState(false);
  const [openInvoiceModal, setOpenInvoiceModal] = useState(false);
  const [openValidationModal, setOpenValidationModal] = useState(false);

  const { data: dealData } = useDeal(selectedOrder?.dealUuid);
  const dealForModal = dealData ? mapDealToView(dealData) : null;

  const { data, isLoading, error } = useAdminOrders();
  const orders = data?.commandes ?? [];
  const statsData = data?.statistiques;

  const { t: tAdmin } = useI18n("admin");
  const { t: tStatus } = useI18n("status");
  const { isAdmin } = useAuth();

  // Hooks pour les mutations
  const validatePayoutMutation = useAdminValidatePayout();
  const uploadInvoiceMutation = useAdminUploadSellerInvoice();
  const validateCustomersMutation = useAdminValidateCustomerInvoices();

  // Hook pour récupérer les clients (seulement si le modal est ouvert)
  const { data: customers = [] } = useOrderCustomers(
    openValidationModal ? selectedOrder?.uuid : "",
  );

  const handleValidatePayout = async (dateDepotPayout: string): Promise<void> => {
    if (!selectedOrder?.uuid) return;
    await validatePayoutMutation.mutateAsync({
      uuid: selectedOrder.uuid,
      dateDepotPayout,
    });
  };

  const handleUploadInvoice = async (file: File): Promise<void> => {
    if (!selectedOrder?.uuid) return;
    await uploadInvoiceMutation.mutateAsync({
      uuid: selectedOrder.uuid,
      file,
    });
  };

  // ✅ Signature alignée avec useAdminValidateCustomerInvoices : { uuid, utilisateurUuids }
  const handleValidateCustomers = async (
    utilisateurUuids: string[],
  ): Promise<void> => {
    if (!selectedOrder?.uuid) return;
    await validateCustomersMutation.mutateAsync({
      uuid: selectedOrder.uuid,
      utilisateurUuids,
    });
  };

  // ✅ getStatusBadge avec valeurs enum correctes (TERMINEE, ANNULEE, REMBOURSEE, COMPLETEE)
  const getStatusBadge = (status: string): ReactElement => {
    switch (status) {
      case StatutCommande.TERMINEE:
        return (
          <Badge className="bg-green-100 text-green-800 hover:bg-green-100">
            {tStatus("completed")}
          </Badge>
        );
      case StatutCommande.EN_COURS:
        return (
          <Badge className="bg-yellow-100 text-yellow-800 hover:bg-yellow-100">
            {tStatus("pending")}
          </Badge>
        );
      case StatutCommande.COMPLETEE:
        return (
          <Badge className="bg-sky-100 text-sky-800 hover:bg-sky-100">
            {tStatus("complete")}
          </Badge>
        );
      case StatutCommande.CONFIRMEE:
        return (
          <Badge className="bg-blue-100 text-blue-800 hover:bg-blue-100">
            {tStatus("complete")}
          </Badge>
        );
      case StatutCommande.PAYOUT:
        return (
          <Badge className="bg-purple-100 text-purple-800 hover:bg-purple-100">
            {tStatus("payout")}
          </Badge>
        );
      case StatutCommande.INVOICE_SELLER:
        return (
          <Badge className="bg-indigo-100 text-indigo-800 hover:bg-indigo-100">
            {tStatus("invoiceSeller")}
          </Badge>
        );
      case StatutCommande.INVOICE_CUSTOMER:
        return (
          <Badge className="bg-cyan-100 text-cyan-800 hover:bg-cyan-100">
            {tStatus("invoiceCustomer")}
          </Badge>
        );
      case StatutCommande.FACTURE_MARCHAND_RECUE:
        return (
          <Badge className="bg-teal-100 text-teal-800 hover:bg-teal-100">
            {tStatus("factureMarchandRecue")}
          </Badge>
        );
      case StatutCommande.FACTURES_CLIENT_ENVOYEES:
        return (
          <Badge className="bg-emerald-100 text-emerald-800 hover:bg-emerald-100">
            {tStatus("facturesClientEnvoyees")}
          </Badge>
        );
      case StatutCommande.REMBOURSEE:
        return (
          <Badge className="bg-blue-100 text-blue-800 hover:bg-blue-100">
            {tStatus("refunded")}
          </Badge>
        );
      case StatutCommande.ANNULEE:
        return (
          <Badge className="bg-destructive/10 text-destructive hover:bg-destructive/10">
            {tStatus("cancelled")}
          </Badge>
        );
      default:
        return <Badge>{status}</Badge>;
    }
  };

  const columns = [
    {
      id: "numeroCommande",
      header: tAdmin("orders.numeroCommande"),
      accessorKey: "numeroCommande",
    },
    {
      id: "marchandNom",
      header: tAdmin("orders.merchant"),
      cell: ({ row }: { row: any }) =>
        `${row.original.marchandPrenom} ${row.original.marchandNom}`,
    },
    {
      id: "dealTitre",
      header: tAdmin("orders.deal"),
      accessorKey: "dealTitre",
      cell: ({ row }: { row: any }) => (
        <Button
          variant="link"
          size="sm"
          className="p-0 h-auto"
          onClick={() => {
            setSelectedOrder(row.original);
            setOpenDealDetails(true);
          }}
        >
          {row.original.dealTitre}
        </Button>
      ),
    },
    {
      id: "dateCreation",
      header: tAdmin("orders.date"),
      accessorKey: "dateCreation",
      cell: ({ row }: { row: any }) =>
        row.original.dateCreation
          ? new Date(row.original.dateCreation).toLocaleDateString()
          : "-",
    },
    {
      id: "montantTotalPaiements",
      header: tAdmin("orders.amount"),
      accessorKey: "montantTotalPaiements",
      cell: ({ row }: { row: any }) =>
        formatCurrency(row.original.montantTotalPaiements),
    },
    {
      id: "statut",
      header: tAdmin("orders.status"),
      accessorKey: "statut",
      cell: ({ row }: { row: any }) => getStatusBadge(row.original.statut),
    },
    {
      id: "actions",
      header: tAdmin("orders.actions"),
      cell: ({ row }: { row: any }) => {
        const order = row.original;
        const statut = order.statut;

        return (
          <div className="flex items-center gap-2">
            {/* Voir les détails */}
            <Button
              variant="ghost"
              size="icon"
              onClick={() => {
                setSelectedOrder(order);
                setOpenViewDetails(true);
              }}
            >
              <Eye className="h-4 w-4" />
            </Button>

            {/* Valider Payout — admin, statut COMPLETEE ou CONFIRMEE */}
            {isAdmin &&
              (statut === StatutCommande.COMPLETEE ||
                statut === StatutCommande.CONFIRMEE) && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setSelectedOrder(order);
                    setOpenPayoutModal(true);
                  }}
                >
                  <DollarSign className="h-4 w-4 mr-1" />
                  Payout
                </Button>
              )}

            {/*
             * Upload Facture Vendeur :
             * - Tout vendeur peut uploader quand statut = PAYOUT
             * - L'ADMIN peut aussi uploader, y compris quand statut = INVOICE_SELLER
             *   (pour remplacer une facture incorrecte)
             */}
            {(statut === StatutCommande.PAYOUT ||
              (isAdmin && statut === StatutCommande.INVOICE_SELLER)) && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setSelectedOrder(order);
                  setOpenInvoiceModal(true);
                }}
              >
                <Upload className="h-4 w-4 mr-1" />
                {statut === StatutCommande.INVOICE_SELLER
                  ? tAdmin("orders.invoice.reupload")
                  : tAdmin("orders.invoice.upload")}
              </Button>
            )}

            {/* Valider Factures Clients */}
            {(statut === StatutCommande.INVOICE_CUSTOMER ||
              statut === StatutCommande.TERMINEE) && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setSelectedOrder(order);
                  setOpenValidationModal(true);
                }}
              >
                <CheckCircle2 className="h-4 w-4 mr-1" />
                {statut === StatutCommande.TERMINEE
                  ? tAdmin("orders.invoice.view")
                  : tAdmin("orders.invoice.validate")}
              </Button>
            )}
          </div>
        );
      },
    },
  ];

  const stats = [
    {
      label: tAdmin("orders.stats.totalOrders"),
      value: statsData?.totalCommandes || 0,
    },
    {
      label: tAdmin("orders.stats.completed"),
      value: statsData?.commandesConfirmees || 0,
    },
    {
      label: tAdmin("orders.stats.pending"),
      value: statsData?.commandesEnCours || 0,
    },
    {
      label: tAdmin("orders.stats.refunded"),
      value: statsData?.commandesRemboursees || 0,
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
        Erreur lors du chargement des commandes
      </div>
    );
  }

  return (
    <VStack spacing={6} className="p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            {tAdmin("orders.title")}
          </h1>
          <p className="text-muted-foreground">
            {tAdmin("orders.description")}
          </p>
        </div>
        <Button variant="outline">
          <Download className="mr-2 h-4 w-4" />
          {tAdmin("orders.export")}
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, index) => (
          <Card key={index}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.label}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardContent>
          <DataTable
            columns={columns}
            data={orders}
            enableSelection={false}
            enableExport={false}
          />
        </CardContent>
      </Card>

      {selectedOrder && (
        <>
          <ViewOrderDetailsModal
            open={openViewDetails}
            onClose={() => {
              setOpenViewDetails(false);
              setSelectedOrder(null);
            }}
            order={selectedOrder}
          />
          <ViewDetailDealModal
            open={openDealDetails}
            onClose={() => {
              setOpenDealDetails(false);
              setSelectedOrder(null);
            }}
            deal={dealForModal}
          />
          <ValidatePayoutModal
            open={openPayoutModal}
            onClose={() => setOpenPayoutModal(false)}
            order={selectedOrder}
            onConfirm={handleValidatePayout}
          />
          <UploadSellerInvoiceModal
            open={openInvoiceModal}
            onClose={() => setOpenInvoiceModal(false)}
            order={selectedOrder}
            onUpload={handleUploadInvoice}
          />
          <ValidateCustomerInvoicesModal
            open={openValidationModal}
            onClose={() => setOpenValidationModal(false)}
            order={selectedOrder}
            customers={customers}
            onValidate={handleValidateCustomers}
            isReadOnly={selectedOrder?.statut === StatutCommande.TERMINEE}
          />
        </>
      )}
    </VStack>
  );
}
