import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import { Star, MapPin, Calendar, Package, TrendingUp, DollarSign } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import { Badge } from "@/common/components/ui/badge";
import type { MarchandAvecDeals } from "@/common/api/types/merchant.types";
import { StatutDeal, StatutCommande } from "@/common/api/types/merchant.types";

interface ViewDetailMerchantModalProps {
  open: boolean;
  onClose: () => void;
  merchant: MarchandAvecDeals | null;
}

const statutDealColors = {
  [StatutDeal.BROUILLON]: "secondary",
  [StatutDeal.PUBLIE]: "success",
  [StatutDeal.EXPIRE]: "danger",
} as const;

const statutCommandeColors = {
  [StatutCommande.EN_COURS]: "warning",
  [StatutCommande.COMPLETEE]: "success",
  [StatutCommande.PAYOUT]: "info",
  [StatutCommande.INVOICE_SELLER]: "info",
  [StatutCommande.INVOICE_CUSTOMER]: "info",
  [StatutCommande.CONFIRMEE]: "success",
  [StatutCommande.ANNULEE]: "danger",
  [StatutCommande.REMBOURSEE]: "danger",
  [StatutCommande.FACTURE_MARCHAND_RECUE]: "info",
  [StatutCommande.FACTURES_CLIENT_ENVOYEES]: "info",
  [StatutCommande.TERMINEE]: "success",
} as const;

export default function ViewDetailMerchantModal({
  open,
  onClose,
  merchant,
}: ViewDetailMerchantModalProps) {
  if (!merchant) return null;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[900px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-2xl">Détails du marchand</DialogTitle>
        </DialogHeader>

        <div className="py-4 space-y-6">
          {/* Informations du marchand */}
          <div className="grid gap-4">
            <div className="space-y-2">
              <h3 className="text-2xl font-bold">
                {merchant.prenom} {merchant.nom}
              </h3>
              <p className="text-muted-foreground">{merchant.email}</p>
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Star
                      key={i}
                      className={`w-4 h-4 ${
                        i < Math.floor(merchant.moyenneGlobale)
                          ? "fill-yellow-400 text-yellow-400"
                          : "text-muted-foreground"
                      }`}
                    />
                  ))}
                </div>
                <span className="font-semibold">
                  {merchant.moyenneGlobale.toFixed(1)} / 5.0
                </span>
              </div>
            </div>

            {/* Statistiques du marchand */}
            <div className="grid grid-cols-3 gap-4">
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm font-medium text-muted-foreground">
                    Deals publiés
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Package className="h-5 w-5 text-primary" />
                    <span className="text-2xl font-bold text-primary">
                      {merchant.nombreDeals}
                    </span>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm font-medium text-muted-foreground">
                    Note moyenne
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <TrendingUp className="h-5 w-5 text-yellow-500" />
                    <span className="text-2xl font-bold text-yellow-500">
                      {merchant.moyenneGlobale.toFixed(1)}
                    </span>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm font-medium text-muted-foreground">
                    Membre depuis
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center gap-2">
                    <Calendar className="h-5 w-5 text-primary" />
                    <span className="font-semibold">
                      {new Date(merchant.dateCreation).toLocaleDateString("fr-FR")}
                    </span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>

          {/* Liste des deals */}
          <div className="space-y-4">
            <h4 className="text-lg font-semibold">Deals du marchand ({merchant.deals.length})</h4>

            {merchant.deals.length === 0 ? (
              <Card>
                <CardContent className="py-8 text-center text-muted-foreground">
                  Aucun deal publié pour le moment
                </CardContent>
              </Card>
            ) : (
              <div className="grid gap-4">
                {merchant.deals.map((deal) => (
                  <Card key={deal.uuid} className="hover:shadow-md transition-shadow">
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="space-y-1 flex-1">
                          <CardTitle className="text-base">{deal.titre}</CardTitle>
                          <CardDescription className="line-clamp-2">
                            {deal.description}
                          </CardDescription>
                        </div>
                        {deal.imageUrl && (
                          <img
                            src={deal.imageUrl}
                            alt={deal.titre}
                            className="w-20 h-20 rounded-md object-cover ml-4"
                          />
                        )}
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      {/* Prix et participants */}
                      <div className="flex items-center gap-4 text-sm">
                        <div className="flex items-center gap-2">
                          <DollarSign className="h-4 w-4 text-green-600" />
                          <span className="font-semibold text-green-600">
                            {deal.prixPart.toFixed(2)} CAD / part
                          </span>
                        </div>
                        <div className="text-muted-foreground">
                          {deal.nbParticipants} participants max
                        </div>
                      </div>

                      {/* Statistiques */}
                      <div className="grid grid-cols-3 gap-3 text-sm">
                        <div className="flex items-center gap-2">
                          <Star className="h-4 w-4 text-yellow-500" />
                          <span>{deal.moyenneCommentaires.toFixed(1)} / 5</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <MapPin className="h-4 w-4 text-blue-500" />
                          <span>{deal.ville}</span>
                        </div>
                        <div className="text-muted-foreground">
                          {new Date(deal.dateCreation).toLocaleDateString("fr-FR")}
                        </div>
                      </div>

                      {/* Badges statut */}
                      <div className="flex items-center gap-2 flex-wrap">
                        <Badge colorScheme={statutDealColors[deal.statut]}>
                          {deal.statut}
                        </Badge>
                        {deal.statutCommande && (
                          <Badge
                            colorScheme={
                              statutCommandeColors[deal.statutCommande] || "secondary"
                            }
                          >
                            Commande: {deal.statutCommande}
                          </Badge>
                        )}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
