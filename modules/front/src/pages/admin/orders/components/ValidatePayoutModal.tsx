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
import { Input } from "@/common/components/ui/input";
import { Label } from "@/common/components/ui/label";
import { Calendar } from "lucide-react";
import { toast } from "sonner";

interface ValidatePayoutModalProps {
  open: boolean;
  onClose: () => void;
  order: any;
  onConfirm: (dateDepotPayout: string) => Promise<void>;
}

export default function ValidatePayoutModal({
  open,
  onClose,
  order,
  onConfirm,
}: ValidatePayoutModalProps) {
  const { t } = useI18n("admin");
  const [dateDepotPayout, setDateDepotPayout] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!dateDepotPayout) {
      toast.error(t("orders.payout.dateRequired"));
      return;
    }

    setIsLoading(true);
    try {
      await onConfirm(dateDepotPayout);
      toast.success(t("orders.payout.validated"));
      onClose();
      setDateDepotPayout("");
    } catch (error) {
      console.error("Erreur validation payout:", error);
      toast.error(t("orders.payout.error"));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
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
              <Label htmlFor="dateDepotPayout">
                {t("orders.payout.dateLabel")}
              </Label>
              <div className="relative">
                <Calendar className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  id="dateDepotPayout"
                  type="datetime-local"
                  value={dateDepotPayout}
                  onChange={(e) => setDateDepotPayout(e.target.value)}
                  className="pl-10"
                  required
                />
              </div>
              <p className="text-sm text-muted-foreground">
                {t("orders.payout.dateHelp")}
              </p>
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isLoading}
            >
              {t("orders.payout.cancel")}
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? t("orders.payout.loading") : t("orders.payout.validate")}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

