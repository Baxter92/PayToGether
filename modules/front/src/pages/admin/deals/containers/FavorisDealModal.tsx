import { AlertCircle, Heart, HeartOff } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import { Button } from "@/common/components/ui/button";
import { Alert, AlertDescription } from "@/common/components/ui/alert";
import { useI18n } from "@/common/hooks/useI18n";

interface FavorisDealModalProps {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  dealTitre: string;
  isFavori: boolean;
  isLoading?: boolean;
}

export function FavorisDealModal({
  open,
  onClose,
  onConfirm,
  dealTitre,
  isFavori,
  isLoading = false,
}: FavorisDealModalProps) {
  const { t } = useI18n("admin");

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            {isFavori ? (
              <HeartOff className="h-5 w-5 text-muted-foreground" />
            ) : (
              <Heart className="h-5 w-5 text-red-500" />
            )}
            {isFavori ? t("deals.removeFavorite") : t("deals.addFavorite")}
          </DialogTitle>
          <DialogDescription>
            {isFavori
              ? t("deals.removeFavoriteDescription")
              : t("deals.addFavoriteDescription")}
          </DialogDescription>
        </DialogHeader>

        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            <strong>{t("deals.dealLabel")}:</strong> {dealTitre}
          </AlertDescription>
        </Alert>

        {!isFavori && (
          <div className="rounded-lg border border-yellow-200 dark:border-yellow-800 bg-yellow-50 dark:bg-yellow-950/30 p-4">
            <p className="text-sm text-yellow-800 dark:text-yellow-200">
              {t("deals.favoriteInfo")}
            </p>
          </div>
        )}

        <DialogFooter className="flex gap-2 sm:gap-0">
          <Button
            variant="outline"
            onClick={onClose}
            disabled={isLoading}
            className="flex-1 sm:flex-initial"
          >
            {t("common.cancel")}
          </Button>
          <Button
            onClick={onConfirm}
            disabled={isLoading}
            variant={isFavori ? "destructive" : "default"}
            className="flex-1 sm:flex-initial"
          >
            {isLoading ? (
              <span className="flex items-center gap-2">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                {t("common.loading")}
              </span>
            ) : (
              <>
                {isFavori ? (
                  <HeartOff className="h-4 w-4 mr-2" />
                ) : (
                  <Heart className="h-4 w-4 mr-2" />
                )}
                {isFavori ? t("deals.remove") : t("deals.add")}
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

