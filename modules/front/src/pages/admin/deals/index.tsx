import { useState, type ReactElement } from "react";
import { Eye, Plus } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { useI18n } from "@/common/hooks/useI18n";
import DealsList from "@/common/containers/DealList";
import { CreateDealModal } from "@/pages/profile/components/CreateDealModal";
import { ViewDetailDealModal } from "./containers/ViewDetailDealModal";
import { useDeals, type DealDTO } from "@/common/api";

export default function AdminDeals(): ReactElement {
  const [open, setOpen] = useState(false);
  const [openDetail, setOpenDetail] = useState(false);
  const [selectedDeal, setSelectedDeal] = useState<any>();
  const { t: tAdmin } = useI18n("admin");
  const { data: dealsData, isLoading, refetch } = useDeals();

  const mappedDeals = (dealsData ?? []).map((deal: DealDTO) => {
    const expirationDate = deal.dateExpiration
      ? new Date(deal.dateExpiration)
      : null;
    const now = new Date();
    const deadline =
      expirationDate && !Number.isNaN(expirationDate.getTime())
        ? Math.max(
            0,
            Math.ceil(
              (expirationDate.getTime() - now.getTime()) /
                (1000 * 60 * 60 * 24),
            ),
          ).toString()
        : "0";

    const firstImage = deal.listeImages?.[0];

    return {
      id: deal.uuid,
      title: deal.titre,
      description: deal.description,
      image: firstImage?.urlImage || "/placeholder.svg",
      originalPrice: deal.prixDeal,
      groupPrice: deal.prixPart,
      unit: 1,
      sold: 0,
      total: deal.nbParticipants,
      deadline,
      category: deal.categorieNom,
      city: deal.ville,
      discount:
        deal.prixDeal > 0
          ? Math.max(0, Math.round((1 - deal.prixPart / deal.prixDeal) * 100))
          : 0,
      status: deal.statut,
      raw: deal,
    };
  });

  return (
    <section className="space-y-6">
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold font-heading">
            {tAdmin("deals.title")}
          </h1>
          <p className="text-muted-foreground">{tAdmin("deals.description")}</p>
        </div>
        <Button
          leftIcon={<Plus className="h-4 w-4" />}
          onClick={() => setOpen(true)}
        >
          {tAdmin("deals.newDeal")}
        </Button>
      </header>

      {isLoading ? (
        <div className="text-center py-8 text-muted-foreground">
          Chargement...
        </div>
      ) : (
        <DealsList
          deals={mappedDeals}
          viewMode="list"
          viewModeToggleable={false}
          showFilters
          filterPosition="top"
          availableFilters={["search", "category", "status"]}
          showPagination
          itemsPerPage={10}
          isAdmin={true}
          tableProps={{
            actionsRow: ({ row }) => [
              {
                leftIcon: <Eye />,
                onClick: () => {
                  setSelectedDeal(row.original);
                  setOpenDetail(true);
                },
                tooltip: tAdmin("deals.viewDetail"),
              },
            ],
          }}
        />
      )}

      <CreateDealModal
        open={open}
        onClose={() => setOpen(false)}
        onSuccess={() => {
          refetch();
        }}
      />
      <ViewDetailDealModal
        open={openDetail}
        onClose={() => setOpenDetail(false)}
        deal={selectedDeal}
      />
    </section>
  );
}
