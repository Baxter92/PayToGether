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
import { Upload, FileText, Loader2 } from "lucide-react";
import { toast } from "sonner";

interface UploadSellerInvoiceModalProps {
  open: boolean;
  onClose: () => void;
  order: any;
  onUpload: (file: File) => Promise<void>;
}

export default function UploadSellerInvoiceModal({
  open,
  onClose,
  order,
  onUpload,
}: UploadSellerInvoiceModalProps) {
  const { t } = useI18n("admin");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // Vérifier le type de fichier
      if (file.type !== "application/pdf") {
        toast.error(t("orders.invoice.invalidFileType"));
        return;
      }

      // Vérifier la taille (max 10MB)
      if (file.size > 10 * 1024 * 1024) {
        toast.error(t("orders.invoice.fileTooLarge"));
        return;
      }

      setSelectedFile(file);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedFile) {
      toast.error(t("orders.invoice.fileRequired"));
      return;
    }

    setIsLoading(true);
    try {
      await onUpload(selectedFile);
      toast.success(t("orders.invoice.uploaded"));
      onClose();
      setSelectedFile(null);
    } catch (error) {
      console.error("Erreur upload facture:", error);
      toast.error(t("orders.invoice.uploadError"));
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    if (!isLoading) {
      onClose();
      setSelectedFile(null);
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("orders.invoice.uploadTitle")}</DialogTitle>
          <DialogDescription>
            {t("orders.invoice.uploadDescription", {
              orderNumber: order?.numeroCommande,
            })}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="invoice">{t("orders.invoice.fileLabel")}</Label>
              <div className="flex items-center gap-2">
                <Input
                  id="invoice"
                  type="file"
                  accept="application/pdf"
                  onChange={handleFileChange}
                  disabled={isLoading}
                  className="cursor-pointer"
                />
              </div>
              {selectedFile && (
                <div className="flex items-center gap-2 p-2 bg-muted rounded-md">
                  <FileText className="h-4 w-4 text-muted-foreground" />
                  <span className="text-sm">{selectedFile.name}</span>
                  <span className="text-xs text-muted-foreground ml-auto">
                    ({(selectedFile.size / 1024).toFixed(2)} KB)
                  </span>
                </div>
              )}
              <p className="text-sm text-muted-foreground">
                {t("orders.invoice.fileHelp")}
              </p>
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isLoading}
            >
              {t("orders.invoice.cancel")}
            </Button>
            <Button type="submit" disabled={isLoading || !selectedFile}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  {t("orders.invoice.uploading")}
                </>
              ) : (
                <>
                  <Upload className="mr-2 h-4 w-4" />
                  {t("orders.invoice.upload")}
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

