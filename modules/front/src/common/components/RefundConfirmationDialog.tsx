import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import { Button } from "@/common/components/ui/button";
import { Textarea } from "@/common/components/ui/textarea";
import { Label } from "@/common/components/ui/label";
import { AlertCircle, RefreshCw } from "lucide-react";
import { Alert, AlertDescription } from "@/common/components/ui/alert";

interface RefundConfirmationDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onConfirm: (reason: string) => void;
  isLoading?: boolean;
  participantCount: number;
  participantNames?: string[];
  totalAmount: number;
}

export function RefundConfirmationDialog({
  open,
  onOpenChange,
  onConfirm,
  isLoading = false,
  participantCount,
  participantNames = [],
  totalAmount,
}: RefundConfirmationDialogProps): React.ReactElement {
  const [reason, setReason] = useState("");

  const handleConfirm = (): void => {
    onConfirm(reason);
    setReason("");
  };

  const handleCancel = (): void => {
    onOpenChange(false);
    setReason("");
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[550px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-xl">
            <RefreshCw className="w-5 h-5 text-orange-600" />
            Confirm Refund
          </DialogTitle>
          <DialogDescription>
            You are about to refund {participantCount} participant{participantCount > 1 ? "s" : ""}.
            This action cannot be undone.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* Alert d'avertissement */}
          <Alert className="border-orange-200 bg-orange-50 dark:bg-orange-950/20">
            <AlertCircle className="h-4 w-4 text-orange-600" />
            <AlertDescription className="text-orange-800 dark:text-orange-200">
              <strong>Warning:</strong> This will:
              <ul className="list-disc list-inside mt-2 space-y-1 text-sm">
                <li>Refund payments via Square</li>
                <li>Remove participants from the deal</li>
                <li>Delete their payments</li>
                <li>Send refund confirmation emails</li>
              </ul>
            </AlertDescription>
          </Alert>

          {/* Résumé */}
          <div className="bg-gray-50 dark:bg-gray-900 rounded-lg p-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-gray-600 dark:text-gray-400">Participants:</span>
              <span className="font-semibold text-gray-900 dark:text-gray-100">
                {participantCount}
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-600 dark:text-gray-400">Total Amount:</span>
              <span className="font-semibold text-green-600 dark:text-green-400">
                ${totalAmount.toFixed(2)} CAD
              </span>
            </div>
          </div>

          {/* Liste des noms (si peu de participants) */}
          {participantNames.length > 0 && participantNames.length <= 5 && (
            <div className="bg-blue-50 dark:bg-blue-950/20 rounded-lg p-4">
              <p className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Participants:
              </p>
              <ul className="space-y-1">
                {participantNames.map((name, index) => (
                  <li key={index} className="text-sm text-gray-600 dark:text-gray-400">
                    • {name}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Raison du remboursement */}
          <div className="space-y-2">
            <Label htmlFor="reason">Refund Reason (Optional)</Label>
            <Textarea
              id="reason"
              placeholder="e.g., Deal cancelled by admin, Product unavailable..."
              value={reason}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setReason(e.target.value)}
              rows={3}
              className="resize-none"
              disabled={isLoading}
            />
            <p className="text-xs text-gray-500 dark:text-gray-400">
              This reason will be included in the refund confirmation email.
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            onClick={handleCancel}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button
            variant="destructive"
            onClick={handleConfirm}
            disabled={isLoading}
            className="min-w-[120px]"
          >
            {isLoading ? (
              <>
                <RefreshCw className="w-4 h-4 mr-2 animate-spin" />
                Processing...
              </>
            ) : (
              `Refund ${participantCount}`
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

