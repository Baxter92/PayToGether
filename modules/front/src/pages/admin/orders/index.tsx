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
  // const [searchQuery, setSearchQuery] = useState("");
  // const [statusFilter, setStatusFilter] = useState("all");
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

  const handleValidateCustomers = async (
    validations: { customerUuid: string; valide: boolean }[],
  ): Promise<void> => {
    if (!selectedOrder?.uuid) return;
    await validateCustomersMutation.mutateAsync({
      uuid: selectedOrder.uuid,
      validations,
    });
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
            {/* Bouton Voir les détails */}
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

            {/* Bouton Valider Payout (admin seulement, statut COMPLETE) */}
            {isAdmin && (statut === StatutCommande.CONFIRMEE || statut === StatutCommande.COMPLETE) && (
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

            {/* Bouton Upload Facture Vendeur (statut PAYOUT) */}
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
                Invoice
              </Button>
            )}

            {/* Bouton Valider Factures Clients (statut INVOICE_CUSTOMER ou TERMINE) */}
            {(statut === StatutCommande.INVOICE_CUSTOMER ||
              statut === StatutCommande.TERMINE) && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setSelectedOrder(order);
                  setOpenValidationModal(true);
                }}
              >
                <CheckCircle2 className="h-4 w-4 mr-1" />
                {statut === StatutCommande.TERMINE ? "Voir" : "Valider"}
              </Button>
            )}
          </div>
        );
      },
    },
  ];

  // const filteredOrders = orders.filter((order) => {
  //   const matchesSearch =
  //     order.numeroCommande
  //       ?.toLowerCase?.()
  //       .includes(searchQuery.toLowerCase?.()) ||
  //     order.marchandNom
  //       ?.toLowerCase?.()
  //       .includes(searchQuery.toLowerCase?.()) ||
  //     order.dealTitre?.toLowerCase?.().includes(searchQuery.toLowerCase?.());
  //   const matchesStatus =
  //     statusFilter === "all" || order.statut === statusFilter;
  //   return matchesSearch && matchesStatus;
  // });

  const getStatusBadge = (status: string): ReactElement => {
    switch (status) {
      case StatutCommande.TERMINE:
      case StatutCommande.LIVRÉE:
        return (
          <Badge className="bg-green-100 text-green-800 hover:bg-green-100">
            {tStatus("completed")}
          </Badge>
        );
      case StatutCommande.EN_ATTENTE:
      case StatutCommande.EN_COURS:
        return (
          <Badge className="bg-yellow-100 text-yellow-800 hover:bg-yellow-100">
            {tStatus("pending")}
          </Badge>
        );
      case StatutCommande.COMPLETE:
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
      case StatutCommande.REMBOURSÉE:
        return (
          <Badge className="bg-blue-100 text-blue-800 hover:bg-blue-100">
            {tStatus("refunded")}
          </Badge>
        );
      case StatutCommande.ANNULÉE:
        return (
          <Badge className="bg-destructive/10 text-destructive hover:bg-destructive/10">
            {tStatus("cancelled")}
          </Badge>
        );
      default:
        return <Badge>{tStatus(status.toLowerCase())}</Badge>;
    }
  };

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
          {/* <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder={tAdmin("orders.search")}
                className="pl-8"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="flex items-center gap-2">
              <Select
                value={statusFilter}
                onValueChange={(val) => setStatusFilter(val)}
                items={[
                  { label: tAdmin("orders.filter.all"), value: "all" },
                  { label: tStatus("completed"), value: StatutCommande.LIVRÉE },
                  { label: tStatus("pending"), value: StatutCommande.EN_COURS },
                  {
                    label: tStatus("cancelled"),
                    value: StatutCommande.ANNULÉE,
                  },
                  {
                    label: tStatus("refunded"),
                    value: StatutCommande.REMBOURSÉE,
                  },
                ]}
                wrapperClassName="w-[200px]"
              />
            </div>
          </div> */}

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
            onClose={() => {
              setOpenPayoutModal(false);
            }}
            order={selectedOrder}
            onConfirm={handleValidatePayout}
          />
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
    </VStack>
  );
}
