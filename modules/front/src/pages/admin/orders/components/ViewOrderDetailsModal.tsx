import { type JSX } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import { Badge } from "@/common/components/ui/badge";
import { Separator } from "@/common/components/ui/separator";
import { Button } from "@/common/components/ui/button";
import {
  Package,
  User,
  Store,
  Calendar,
  DollarSign,
  FileText,
  Clock,
  CheckCircle,
  XCircle,
  AlertCircle,
  Mail,
  Users,
  Loader2,
} from "lucide-react";
import { useI18n } from "@/common/hooks/useI18n";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { StatutCommande } from "@/common/api/types/order";
import { timeAgo } from "@/common/utils/date";
import { useOrderCustomers } from "@/common/api/hooks/useOrders";

interface ViewOrderDetailsModalProps {
  open: boolean;
  onClose: () => void;
  order: any;
}

export default function ViewOrderDetailsModal({
  open,
  onClose,
  order,
}: ViewOrderDetailsModalProps): JSX.Element {
  const { t: tStatus } = useI18n("status");

  // Récupérer les clients de la commande
  const { data: customers = [], isLoading: isLoadingCustomers } = useOrderCustomers(
    open ? order?.uuid : "",
  );

  if (!order) return <></>;

  const getStatusIcon = (status: string) => {
    switch (status) {
      case StatutCommande.TERMINE:
      case StatutCommande.LIVRÉE:
        return <CheckCircle className="h-5 w-5 text-green-600" />;
      case StatutCommande.ANNULÉE:
        return <XCircle className="h-5 w-5 text-red-600" />;
      case StatutCommande.EN_ATTENTE:
      case StatutCommande.EN_COURS:
        return <Clock className="h-5 w-5 text-yellow-600" />;
      default:
        return <AlertCircle className="h-5 w-5 text-blue-600" />;
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case StatutCommande.TERMINE:
      case StatutCommande.LIVRÉE:
        return (
          <Badge className="bg-green-100 text-green-800">
            {tStatus("completed")}
          </Badge>
        );
      case StatutCommande.EN_ATTENTE:
      case StatutCommande.EN_COURS:
        return (
          <Badge className="bg-yellow-100 text-yellow-800">
            {tStatus("pending")}
          </Badge>
        );
      case StatutCommande.COMPLETE:
      case StatutCommande.CONFIRMEE:
        return (
          <Badge className="bg-blue-100 text-blue-800">
            {tStatus("complete")}
          </Badge>
        );
      case StatutCommande.PAYOUT:
        return (
          <Badge className="bg-purple-100 text-purple-800">
            {tStatus("payout")}
          </Badge>
        );
      case StatutCommande.INVOICE_SELLER:
        return (
          <Badge className="bg-indigo-100 text-indigo-800">
            {tStatus("invoiceSeller")}
          </Badge>
        );
      case StatutCommande.INVOICE_CUSTOMER:
        return (
          <Badge className="bg-cyan-100 text-cyan-800">
            {tStatus("invoiceCustomer")}
          </Badge>
        );
      case StatutCommande.REMBOURSÉE:
        return (
          <Badge className="bg-blue-100 text-blue-800">
            {tStatus("refunded")}
          </Badge>
        );
      case StatutCommande.ANNULÉE:
        return (
          <Badge className="bg-red-100 text-red-800">
            {tStatus("cancelled")}
          </Badge>
        );
      default:
        return <Badge>{status}</Badge>;
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return new Intl.DateTimeFormat("fr-CA", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date);
  };

  const validatedCount = customers.filter((c) => c.valide).length;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[900px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-start justify-between">
            <div className="space-y-1">
              <DialogTitle className="text-2xl font-bold">
                Détails de la commande
              </DialogTitle>
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <FileText className="h-4 w-4" />
                <span className="font-mono font-semibold text-foreground">
                  {order.numeroCommande}
                </span>
              </div>
            </div>
            <div className="flex items-center gap-2">
              {getStatusIcon(order.statut)}
              {getStatusBadge(order.statut)}
            </div>
          </div>
        </DialogHeader>

        <div className="grid gap-6 py-4 md:grid-cols-2">
          {/* Colonne gauche */}
          <div className="space-y-6">
            {/* Section Deal */}
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
                <Package className="h-4 w-4" />
                <span>OFFRE ACHETÉE</span>
              </div>
              <div className="rounded-lg border bg-muted/50 p-4">
                <h3 className="font-semibold text-lg">{order.dealTitre}</h3>
                {order.dealUuid && (
                  <p className="text-xs text-muted-foreground mt-1 font-mono">
                    ID: {order.dealUuid}
                  </p>
                )}
              </div>
            </div>

            {/* Section Marchand */}
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
                <Store className="h-4 w-4" />
                <span>VENDEUR</span>
              </div>
              <div className="rounded-lg border p-4">
                <div className="flex items-center gap-3">
                  <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
                    <User className="h-6 w-6 text-primary" />
                  </div>
                  <div className="flex-1">
                    <p className="font-semibold text-lg">
                      {order.marchandPrenom} {order.marchandNom}
                    </p>
                    {order.marchandEmail && (
                      <div className="flex items-center gap-1 text-sm text-muted-foreground">
                        <Mail className="h-3 w-3" />
                        <span>{order.marchandEmail}</span>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>

            {/* Section Montant */}
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
                <DollarSign className="h-4 w-4" />
                <span>MONTANT TOTAL</span>
              </div>
              <div className="rounded-lg border bg-gradient-to-br from-green-50 to-emerald-50 dark:from-green-950 dark:to-emerald-950 p-5">
                <p className="text-4xl font-bold text-green-700 dark:text-green-400">
                  {formatCurrency(order.montantTotalPaiements)}
                </p>
                <p className="text-sm text-muted-foreground mt-1">
                  Total collecté
                </p>
              </div>
            </div>

            {/* Date de dépôt payout */}
            {order.dateDepotPayout && (
              <div className="space-y-3">
                <div className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
                  <Clock className="h-4 w-4" />
                  <span>DATE PAYOUT</span>
                </div>
                <div className="rounded-lg border bg-purple-50 dark:bg-purple-950/30 p-3">
                  <p className="font-medium">
                    {formatDate(order.dateDepotPayout)}
                  </p>
                </div>
              </div>
            )}

            {/* Facture marchand */}
            {order.factureMarchandUrl && (
              <div className="space-y-3">
                <div className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
                  <FileText className="h-4 w-4" />
                  <span>FACTURE VENDEUR</span>
                </div>
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => window.open(order.factureMarchandUrl, "_blank")}
                >
                  <FileText className="mr-2 h-4 w-4" />
                  Télécharger la facture
                </Button>
              </div>
            )}
          </div>

          {/* Colonne droite */}
          <div className="space-y-6">
            {/* Dates */}
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
                <Calendar className="h-4 w-4" />
                <span>DATES</span>
              </div>
              <div className="space-y-2">
                <div className="rounded-lg border bg-muted/30 p-3">
                  <p className="text-xs text-muted-foreground">Créée le</p>
                  <p className="font-medium">{formatDate(order.dateCreation)}</p>
                  <p className="text-xs text-muted-foreground mt-1">
                    {timeAgo(order.dateCreation)}
                  </p>
                </div>
                {order.dateModification && (
                  <div className="rounded-lg border bg-muted/30 p-3">
                    <p className="text-xs text-muted-foreground">Modifiée le</p>
                    <p className="font-medium">
                      {formatDate(order.dateModification)}
                    </p>
                  </div>
                )}
              </div>
            </div>

            {/* Timeline de progression */}
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm font-semibold text-muted-foreground">
                <Clock className="h-4 w-4" />
                <span>PROGRESSION</span>
              </div>
              <div className="space-y-2">
                <TimelineItem
                  label="Commande créée"
                  completed={true}
                  current={order.statut === StatutCommande.EN_COURS}
                />
                <TimelineItem
                  label="Paiements complétés"
                  completed={
                    order.statut !== StatutCommande.EN_ATTENTE &&
                    order.statut !== StatutCommande.EN_COURS
                  }
                  current={order.statut === StatutCommande.CONFIRMEE}
                />
                <TimelineItem
                  label="Payout effectué"
                  completed={
                    order.statut === StatutCommande.PAYOUT ||
                    order.statut === StatutCommande.INVOICE_SELLER ||
                    order.statut === StatutCommande.INVOICE_CUSTOMER ||
                    order.statut === StatutCommande.TERMINE
                  }
                  current={order.statut === StatutCommande.PAYOUT}
                />
                <TimelineItem
                  label="Facture vendeur reçue"
                  completed={
                    order.statut === StatutCommande.INVOICE_SELLER ||
                    order.statut === StatutCommande.INVOICE_CUSTOMER ||
                    order.statut === StatutCommande.TERMINE
                  }
                  current={order.statut === StatutCommande.INVOICE_SELLER}
                />
                <TimelineItem
                  label="Factures clients validées"
                  completed={order.statut === StatutCommande.TERMINE}
                  current={order.statut === StatutCommande.INVOICE_CUSTOMER}
                />
                <TimelineItem
                  label="Commande terminée"
                  completed={order.statut === StatutCommande.TERMINE}
                  current={false}
                />
              </div>
            </div>
          </div>
        </div>

        {/* Section Participants/Clients */}
        {customers.length > 0 && (
          <>
            <Separator className="my-4" />
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Users className="h-5 w-5 text-muted-foreground" />
                  <h3 className="text-lg font-semibold">
                    Participants ({customers.length})
                  </h3>
                </div>
                {order.statut === StatutCommande.INVOICE_CUSTOMER ||
                order.statut === StatutCommande.TERMINE ? (
                  <Badge
                    variant="outline"
                    className={
                      validatedCount === customers.length
                        ? "bg-green-100 text-green-800"
                        : "bg-yellow-100 text-yellow-800"
                    }
                  >
                    {validatedCount}/{customers.length} validé(s)
                  </Badge>
                ) : null}
              </div>

              <div className="grid gap-3 max-h-[300px] overflow-y-auto pr-2">
                {isLoadingCustomers ? (
                  <div className="flex items-center justify-center py-8">
                    <Loader2 className="h-6 w-6 animate-spin text-primary" />
                  </div>
                ) : (
                  customers.map((customer) => (
                    <div
                      key={customer.uuid}
                      className={`flex items-center justify-between rounded-lg border p-3 ${
                        customer.valide
                          ? "bg-green-50 border-green-200 dark:bg-green-950 dark:border-green-800"
                          : "bg-background"
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
                          <User className="h-5 w-5 text-primary" />
                        </div>
                        <div>
                          <p className="font-medium">
                            {customer.prenom} {customer.nom}
                          </p>
                          <div className="flex items-center gap-1 text-xs text-muted-foreground">
                            <Mail className="h-3 w-3" />
                            <span>{customer.email}</span>
                          </div>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="font-semibold text-lg">
                          {formatCurrency(customer.montant)}
                        </p>
                        <p className="text-xs text-muted-foreground font-mono">
                          {customer.numeroPayment}
                        </p>
                        {customer.valide && (
                          <Badge
                            variant="outline"
                            className="mt-1 bg-green-100 text-green-800"
                          >
                            <CheckCircle className="h-3 w-3 mr-1" />
                            Validé
                          </Badge>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </>
        )}

        <div className="flex justify-end gap-2 pt-4 border-t">
          <Button variant="outline" onClick={onClose}>
            Fermer
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}

// Composant Timeline Item
function TimelineItem({
  label,
  completed,
  current,
}: {
  label: string;
  completed: boolean;
  current: boolean;
}) {
  return (
    <div className="flex items-center gap-3">
      <div
        className={`flex h-8 w-8 items-center justify-center rounded-full border-2 transition-all ${
          completed
            ? "border-green-600 bg-green-600 shadow-sm"
            : current
              ? "border-blue-600 bg-blue-600 shadow-sm ring-2 ring-blue-200"
              : "border-gray-300 bg-white dark:bg-gray-800 dark:border-gray-600"
        }`}
      >
        {completed ? (
          <CheckCircle className="h-4 w-4 text-white" />
        ) : current ? (
          <Clock className="h-4 w-4 text-white animate-pulse" />
        ) : (
          <div className="h-2 w-2 rounded-full bg-gray-300 dark:bg-gray-600" />
        )}
      </div>
      <span
        className={`text-sm ${
          completed
            ? "font-semibold text-foreground"
            : current
              ? "font-semibold text-blue-600"
              : "text-muted-foreground"
        }`}
      >
        {label}
      </span>
      {current && (
        <Badge variant="outline" className="ml-auto text-xs">
          En cours
        </Badge>
      )}
    </div>
  );
}





