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
import { Label } from "@/common/components/ui/label";
import { toast } from "sonner";
import DateTimeInput from "@/common/components/DateTimeInput";

interface ValidatePayoutModalProps {
  open: boolean;
  onClose: () => void;
  order: any;
  onConfirm: (dateDepotPayout: string) => Promise<void>;
}

/** Formate un Date en "yyyy-MM-dd'T'HH:mm:ss" pour le backend */
function formatForBackend(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, "0");
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  );
}

export default function ValidatePayoutModal({
  open,
  onClose,
  order,
  onConfirm,
}: ValidatePayoutModalProps) {
  const { t } = useI18n("admin");
  const [dateDepotPayout, setDateDepotPayout] = useState<Date | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!dateDepotPayout) {
      toast.error(t("orders.payout.dateRequired"));
      return;
    }

    setIsLoading(true);
    try {
      await onConfirm(formatForBackend(dateDepotPayout));
      toast.success(t("orders.payout.validated"));
      onClose();
      setDateDepotPayout(null);
    } catch (error) {
      console.error("Erreur validation payout:", error);
      toast.error(t("orders.payout.error"));
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    if (!isLoading) {
      onClose();
      setDateDepotPayout(null);
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("orders.payout.title")}</DialogTitle>
          <DialogDescription>
            {t("orders.payout.description", {
              orderNumber: order?.numeroCommande,
            })}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>{t("orders.payout.dateLabel")}</Label>

              {/* Sélecteur date + heure avec calendar shadcn */}
              <DateTimeInput
                value={dateDepotPayout}
                onChange={setDateDepotPayout}
                disabled={isLoading}
                helperText={t("orders.payout.dateHelp")}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isLoading}
            >
              {t("orders.payout.cancel")}
            </Button>
            <Button type="submit" disabled={isLoading || !dateDepotPayout}>
              {isLoading
                ? t("orders.payout.loading")
                : t("orders.payout.validate")}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
