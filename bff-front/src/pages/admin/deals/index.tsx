import { useState, type ReactElement } from "react";
import { Plus } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import DealsList from "@/common/containers/DealList";
import { mockDeals } from "@/common/constants/data";
import { CreateDealModal } from "@/pages/profile/components/CreateDealModal";

export default function AdminDeals(): ReactElement {
  const [open, setOpen] = useState(false);
  return (
    <section className="space-y-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold font-heading">Gestion des Deals</h1>
          <p className="text-muted-foreground">
            Créez et gérez vos offres promotionnelles
          </p>
        </div>
        <Button
          leftIcon={<Plus className="h-4 w-4" />}
          onClick={() => setOpen(true)}
        >
          Nouveau Deal
        </Button>
      </header>

      <DealsList
        deals={mockDeals}
        viewMode="list"
        viewModeToggleable
        showFilters
        filterPosition="top"
        availableFilters={["search", "category", "status"]}
        showPagination
        itemsPerPage={10}
      />

      <CreateDealModal open={open} onClose={() => setOpen(false)} />
    </section>
  );
}
