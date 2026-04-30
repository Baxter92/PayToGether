import { useState } from "react";
import { useI18n } from "@/common/hooks/useI18n";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import { Button } from "@/common/components/ui/button";
import { Badge } from "@/common/components/ui/badge";
import { CheckCircle2, Circle, Loader2, User } from "lucide-react";
import { toast } from "sonner";
import type { CommandeUtilisateurDTO } from "@/common/api/types/order";

interface ValidateCustomerInvoicesModalProps {
  open: boolean;
  onClose: () => void;
  order: any;
  customers: CommandeUtilisateurDTO[];
  /** Reçoit la liste des UUIDs des utilisateurs à valider */
  onValidate: (utilisateurUuids: string[]) => Promise<void>;
  isReadOnly?: boolean;
}

export default function ValidateCustomerInvoicesModal({
  open,
  onClose,
  order,
  customers,
  onValidate,
  isReadOnly = false,
}: ValidateCustomerInvoicesModalProps) {
  const { t } = useI18n("admin");
  const [validations, setValidations] = useState<Map<string, boolean>>(
    // ✅ Clé = utilisateurUuid (UUID de l'utilisateur réel)
    new Map(customers.map((c) => [c.utilisateurUuid, c.valide])),
  );
  const [isLoading, setIsLoading] = useState(false);

  const handleToggleValidation = (utilisateurUuid: string) => {
    if (isReadOnly) return;
    // Bloquer uniquement les utilisateurs déjà validés côté API (customer.valide === true)
    const customer = customers.find((c) => c.utilisateurUuid === utilisateurUuid);
    if (customer?.valide) return; // déjà validé en base → non modifiable
    const currentValue = validations.get(utilisateurUuid) || false;
    setValidations(new Map(validations.set(utilisateurUuid, !currentValue)));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isReadOnly) { onClose(); return; }
    setIsLoading(true);
    try {
      const utilisateurUuids = Array.from(validations.entries())
        .filter(([, checked]) => checked)
        .map(([uuid]) => uuid);
      await onValidate(utilisateurUuids);
      const allValidated = Array.from(validations.values()).every((v) => v);
      toast.success(allValidated ? t("orders.validation.allComplete") : t("orders.validation.saved"));
      onClose();
    } catch (error) {
      console.error("Erreur validation factures:", error);
      toast.error(t("orders.validation.error"));
    } finally {
      setIsLoading(false);
    }
  };

  const allValidated = Array.from(validations.values()).every((v) => v);
  const validatedCount = Array.from(validations.values()).filter((v) => v).length;
  const total = customers.length;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[85vh] flex flex-col gap-0 p-0 overflow-hidden">
        {/* ── Header ── */}
        <div className="px-6 pt-6 pb-4 border-b">
          <DialogHeader>
            <DialogTitle className="text-lg font-semibold">
              {isReadOnly ? t("orders.validation.viewTitle") : t("orders.validation.title")}
            </DialogTitle>
            <DialogDescription className="text-sm text-muted-foreground">
              {isReadOnly
                ? t("orders.validation.viewDescription", { orderNumber: order?.numeroCommande })
                : t("orders.validation.description", { orderNumber: order?.numeroCommande })}
            </DialogDescription>
          </DialogHeader>

          {/* Progress bar */}
          <div className="mt-4 space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground flex items-center gap-1.5">
                <CheckCircle2 className="h-4 w-4 text-green-500" />
                {t("orders.validation.progress", { validated: validatedCount, total })}
              </span>
              {allValidated && (
                <Badge className="bg-green-100 text-green-800 hover:bg-green-100 text-xs">
                  {t("orders.validation.complete")}
                </Badge>
              )}
            </div>
            <div className="h-2 rounded-full bg-muted overflow-hidden">
              <div
                className="h-full rounded-full bg-green-500 transition-all duration-300"
                style={{ width: total > 0 ? `${(validatedCount / total) * 100}%` : "0%" }}
              />
            </div>
          </div>
        </div>

        {/* ── Customer list ── */}
        <div className="flex-1 overflow-y-auto px-6 py-4 space-y-3">
          {customers.map((customer) => {
            const isValidated = validations.get(customer.utilisateurUuid) || false;
            const isAlreadyValidated = customer.valide; // validé côté API → verrouillé
            const isClickable = !isReadOnly && !isAlreadyValidated; // sélectionnable ET désélectionnable

            return (
              <div
                key={customer.uuid}
                onClick={() => isClickable && handleToggleValidation(customer.utilisateurUuid)}
                role={isClickable ? "button" : undefined}
                tabIndex={isClickable ? 0 : undefined}
                onKeyDown={(e) => {
                  if (isClickable && (e.key === "Enter" || e.key === " ")) {
                    e.preventDefault();
                    handleToggleValidation(customer.utilisateurUuid);
                  }
                }}
                className={`
                  relative flex items-center gap-4 p-4 rounded-xl border-2 transition-all duration-200
                  ${isValidated
                    ? "border-green-400 bg-green-50 dark:bg-green-950/40 dark:border-green-700"
                    : "border-border bg-card"
                  }
                  ${isClickable
                    ? "cursor-pointer hover:border-green-300 hover:bg-green-50/50 dark:hover:bg-green-950/20 hover:shadow-sm select-none"
                    : "cursor-default"
                  }
                `}
              >
                {/* Icône état */}
                <div className={`flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center ${
                  isValidated ? "bg-green-500 text-white" : "bg-muted text-muted-foreground"
                }`}>
                  {isValidated
                    ? <CheckCircle2 className="h-5 w-5" />
                    : <User className="h-4 w-4" />
                  }
                </div>

                {/* Infos client */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-semibold text-sm truncate">
                      {customer.prenom} {customer.nom}
                    </span>
                    {isAlreadyValidated && (
                      <Badge className="bg-green-100 text-green-700 hover:bg-green-100 text-xs shrink-0">
                        {t("orders.validation.validated")}
                      </Badge>
                    )}
                  </div>
                  <p className="text-xs text-muted-foreground truncate mt-0.5">{customer.email}</p>
                  <div className="flex items-center gap-4 mt-1.5 text-xs text-muted-foreground">
                    <span>
                      {t("orders.validation.paymentNumber")}:{" "}
                      <span className="font-mono font-medium text-foreground">
                        {customer.numeroPayment ?? "—"}
                      </span>
                    </span>
                    <span>
                      {t("orders.validation.amount")}:{" "}
                      <span className="font-semibold text-foreground">
                        ${customer.montant?.toFixed(2) ?? "—"}
                      </span>
                    </span>
                  </div>
                </div>

                {/* Indicateur toggle (droite) */}
                {!isReadOnly && (
                  <div className="flex-shrink-0">
                    {isValidated
                      ? <CheckCircle2 className="h-5 w-5 text-green-500" />
                      : <Circle className="h-5 w-5 text-muted-foreground/40" />
                    }
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* ── Footer ── */}
        <DialogFooter className="px-6 py-4 border-t gap-2">
          <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>
            {isReadOnly ? t("common.close") : t("orders.validation.cancel")}
          </Button>
          {!isReadOnly && (
            <Button onClick={handleSubmit} disabled={isLoading} className="min-w-[140px]">
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  {t("orders.validation.loading")}
                </>
              ) : (
                t("orders.validation.saveValidations")
              )}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
