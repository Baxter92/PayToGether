import { useState, type ReactElement } from "react";
import { Eye, Plus, Heart } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { useI18n } from "@/common/hooks/useI18n";
import DealsList from "@/common/containers/DealList";
import Pagination from "@/common/components/Pagination";
import { DealTableSkeleton } from "@/common/components/skeletons";
import { CreateDealModal } from "@/pages/profile/components/CreateDealModal";
import { ViewDetailDealModal } from "./containers/ViewDetailDealModal";
import { FavorisDealModal } from "./containers/FavorisDealModal";
import {
  useDealsPaginated,
  useToggleDealFavoris,
  type DealDTO,
} from "@/common/api";
import { toast } from "sonner";

export default function AdminDeals(): ReactElement {
  const [open, setOpen] = useState(false);
  const [openDetail, setOpenDetail] = useState(false);
  const [openFavoris, setOpenFavoris] = useState(false);
  const [selectedDeal, setSelectedDeal] = useState<any>();
  const { t: tAdmin } = useI18n("admin");

  // Utiliser le hook paginé
  const {
    deals: dealsData,
    isLoading,
    page,
    size,
    totalElements,
    totalPages,
    setPage,
    refetch,
  } = useDealsPaginated();

  const toggleFavoris = useToggleDealFavoris();

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

    const firstImage = deal.listeImages?.find?.((img) => img.isPrincipal);

    return {
      id: deal.uuid,
      title: deal.titre,
      description: deal.description,
      image: firstImage || "/placeholder.svg",
      originalPrice: deal.prixDeal,
      groupPrice: deal.prixPart,
      strikePrice: deal.prixPartNonReel || 0,
      unit: 1,
      sold: deal.nombrePartsAchetees,
      participants: deal.nombreParticipantsReel,
      total: deal.nbParticipants,
      deadline,
      category: deal.categorieNom,
      city: deal.ville,
      discount:
        deal.prixDeal > 0
          ? Math.max(0, Math.round((1 - deal.prixPart / deal.prixDeal) * 100))
          : 0,
      status: deal.statut,
      favoris: deal.favoris,
      raw: deal,
    };
  });

  const handleToggleFavoris = async () => {
    if (!selectedDeal?.raw?.uuid) return;

    try {
      await toggleFavoris.mutateAsync(selectedDeal.raw.uuid);
      toast.success(
        selectedDeal.raw.favoris
          ? tAdmin("deals.removeFavoriteSuccess")
          : tAdmin("deals.addFavoriteSuccess")
      );
      refetch();
    } catch (error) {
      toast.error(tAdmin("deals.favoriteError"));
    } finally {
      setOpenFavoris(false);
    }
  };

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
        <DealTableSkeleton rows={size} />
      ) : (
        <>
          <DealsList
            deals={mappedDeals}
            viewMode="list"
            viewModeToggleable={false}
            showFilters
            filterPosition="top"
            availableFilters={["search", "category", "status"]}
            showPagination={false} // Désactiver la pagination interne
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
                {
                  leftIcon: <Heart />,
                  onClick: () => {
                    setSelectedDeal(row.original);
                    setOpenFavoris(true);
                  },
                  tooltip: row.original.favoris
                    ? tAdmin("deals.removeFavorite")
                    : tAdmin("deals.addFavorite"),
                  variant: row.original.favoris ? "default" : "ghost",
                  className: row.original.favoris
                    ? "text-red-500 hover:text-red-600"
                    : "",
                },
              ],
            }}
          />

          {/* Pagination externe */}
          <div className="mt-6">
            <Pagination
              page={page + 1} // Composant attend page 1-based
              totalPages={totalPages}
              onChange={(newPage) => setPage(newPage - 1)} // Convertir en 0-based
              perPage={size}
              totalItems={totalElements}
              showSummary={true}
              align="center"
            />
          </div>
        </>
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
      <FavorisDealModal
        open={openFavoris}
        onClose={() => setOpenFavoris(false)}
        onConfirm={handleToggleFavoris}
        dealTitre={selectedDeal?.title || ""}
        isFavori={selectedDeal?.raw?.favoris || false}
        isLoading={toggleFavoris.isPending}
      />
    </section>
  );
}
