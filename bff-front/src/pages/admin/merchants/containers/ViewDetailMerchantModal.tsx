import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import { Star, MapPin, Phone, Calendar } from "lucide-react";

const supplierMock = {
  name: "Supplier Name",
  rating: 4.5,
  city: "Supplier City",
  contact: "Supplier Contact",
  dealsCount: 10,
  joinedAt: "2022-01-01",
};

export default function ViewDetailMerchantModal({ open, onClose }: any) {
  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Détails du marchand</DialogTitle>
        </DialogHeader>
        <div className="py-4">
          <div className="grid gap-6">
            <div className="space-y-2">
              <h3 className="text-2xl font-bold">{supplierMock.name}</h3>
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-1">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Star
                      key={i}
                      className={`w-4 h-4 ${
                        i < Math.floor(supplierMock.rating)
                          ? "fill-yellow-400 text-yellow-400"
                          : "text-muted-foreground"
                      }`}
                    />
                  ))}
                </div>
                <span className="font-semibold">{supplierMock.rating} / 5</span>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="rounded-lg border border-border/50 bg-muted/30 p-4">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                  Localisation
                </p>
                <div className="mt-2 flex items-center gap-2">
                  <MapPin className="h-5 w-5 text-primary" />
                  <span className="font-semibold">{supplierMock.city}</span>
                </div>
              </div>

              <div className="rounded-lg border border-border/50 bg-muted/30 p-4">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                  Contact
                </p>
                <div className="mt-2 flex items-center gap-2">
                  <Phone className="h-5 w-5 text-primary" />
                  <span className="font-semibold">{supplierMock.contact}</span>
                </div>
              </div>

              <div className="rounded-lg border border-border/50 bg-muted/30 p-4">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                  Deals publiés
                </p>
                <p className="mt-2 text-2xl font-bold text-primary">
                  {supplierMock.dealsCount}
                </p>
              </div>

              <div className="rounded-lg border border-border/50 bg-muted/30 p-4">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                  Membre depuis
                </p>
                <div className="mt-2 flex items-center gap-2">
                  <Calendar className="h-5 w-5 text-primary" />
                  <span className="font-semibold">
                    {new Date(supplierMock.joinedAt).toLocaleDateString(
                      "fr-FR"
                    )}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
