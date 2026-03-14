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
import { Checkbox } from "@/common/components/ui/checkbox";
import { Badge } from "@/common/components/ui/badge";
import { CheckCircle2, Loader2 } from "lucide-react";
import { toast } from "sonner";

interface Customer {
  uuid: string;
  nom: string;
  prenom: string;
  email: string;
  montant: number;
  numeroPayment: string;
  valide: boolean;
}

interface ValidateCustomerInvoicesModalProps {
  open: boolean;
  onClose: () => void;
  order: any;
  customers: Customer[];
  onValidate: (validations: { customerUuid: string; valide: boolean }[]) => Promise<void>;
  isReadOnly?: boolean; // Pour l'admin qui peut seulement consulter
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
    new Map(customers.map((c) => [c.uuid, c.valide])),
  );
  const [isLoading, setIsLoading] = useState(false);

  const handleToggleValidation = (customerUuid: string) => {
    if (isReadOnly) return;

    const currentValue = validations.get(customerUuid) || false;

    // Ne pas permettre de dévalider un client déjà validé
    if (currentValue) {
      toast.warning(t("orders.validation.cannotUnvalidate"));
      return;
    }

    setValidations(new Map(validations.set(customerUuid, !currentValue)));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (isReadOnly) {
      onClose();
      return;
    }

    setIsLoading(true);
    try {
      const validationsList = Array.from(validations.entries()).map(
        ([customerUuid, valide]) => ({
          customerUuid,
          valide,
        }),
      );

      await onValidate(validationsList);

      const allValidated = Array.from(validations.values()).every((v) => v);
      if (allValidated) {
        toast.success(t("orders.validation.allComplete"));
      } else {
        toast.success(t("orders.validation.saved"));
      }

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

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {isReadOnly
              ? t("orders.validation.viewTitle")
              : t("orders.validation.title")}
          </DialogTitle>
          <DialogDescription>
            {isReadOnly
              ? t("orders.validation.viewDescription", {
                orderNumber: order?.numeroCommande,
              })
              : t("orders.validation.description", {
                orderNumber: order?.numeroCommande,
              })}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* Statistiques de validation */}
          <div className="flex items-center gap-2 p-3 bg-muted rounded-lg">
            <CheckCircle2 className="h-5 w-5 text-green-600" />
            <span className="text-sm font-medium">
              {t("orders.validation.progress", {
                validated: validatedCount,
                total: customers.length,
              })}
            </span>
            {allValidated && (
              <Badge className="ml-auto bg-green-100 text-green-800">
                {t("orders.validation.complete")}
              </Badge>
            )}
          </div>

          {/* Liste des clients */}
          <div className="space-y-3">
            {customers.map((customer) => {
              const isValidated = validations.get(customer.uuid) || false;
              const isAlreadyValidated = customer.valide;

              return (
                <div
                  key={customer.uuid}
                  className={`flex items-start gap-3 p-4 border rounded-lg transition-colors ${
                    isValidated
                      ? "bg-green-50 border-green-200 dark:bg-green-950 dark:border-green-800"
                      : "bg-background"
                  }`}
                >
                  {!isReadOnly && (
                    <Checkbox
                      checked={isValidated}
                      onCheckedChange={() =>
                        handleToggleValidation(customer.uuid)
                      }
                      disabled={isAlreadyValidated || isReadOnly}
                      className="mt-1"
                    />
                  )}

                  <div className="flex-1 space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-medium">
                        {customer.prenom} {customer.nom}
                      </span>
                      {isAlreadyValidated && (
                        <Badge
                          variant="outline"
                          className="bg-green-100 text-green-800"
                        >
                          {t("orders.validation.validated")}
                        </Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground">
                      {customer.email}
                    </p>
                    <div className="flex items-center gap-4 text-sm">
                      <span className="text-muted-foreground">
                        {t("orders.validation.paymentNumber")}:{" "}
                        <span className="font-mono font-medium text-foreground">
                          {customer.numeroPayment}
                        </span>
                      </span>
                      <span className="text-muted-foreground">
                        {t("orders.validation.amount")}:{" "}
                        <span className="font-medium text-foreground">
                          ${customer.montant.toFixed(2)}
                        </span>
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={onClose}
            disabled={isLoading}
          >
            {isReadOnly ? t("common.close") : t("orders.validation.cancel")}
          </Button>
          {!isReadOnly && (
            <Button onClick={handleSubmit} disabled={isLoading}>
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

