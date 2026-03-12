import { HStack } from "@/common/components";
import { Button } from "@/common/components/ui/button";
import {
  useCategories,
  useDealsByCreateur,
  useDealVilles,
  type DealDTO,
} from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { Eye, Plus } from "lucide-react";
import { useState, type JSX } from "react";
import { CreateDealModal } from "../components/CreateDealModal";
import type { IColumnFilter } from "@/common/components/DataTable";
import { useAuth } from "@/common/context/AuthContext";
import { ViewDetailDealModal } from "@/pages/admin/deals/containers/ViewDetailDealModal";
import { useI18n } from "@/common/hooks/useI18n";

export default function MyDeals(): JSX.Element {
  const [addDealModalOpen, setAddDealModalOpen] = useState(false);
  const { t } = useI18n("profile");
  const { user } = useAuth();
  const { data: villesData } = useDealVilles();
  const { data: dealsData, refetch } = useDealsByCreateur(user?.id || "");
  const deals = (dealsData ?? []).map(mapDealToView);
  const { data: categoriesData } = useCategories();
  const [openDetail, setOpenDetail] = useState(false);
  const [selectedDeal, setSelectedDeal] = useState<DealDTO | null>(null);

  // Configuration des filtres pour le DataTable
  const columnFiltersConfig: IColumnFilter[] = [
    {
      id: "category",
      label: "Catégorie",
      type: "select",
      options: (categoriesData ?? []).map((category) => ({
        label: category.nom,
        value: category.uuid,
      })),
    },
    {
      id: "city",
      label: "Ville",
      type: "select",
      options: (villesData ?? []).map((ville) => ({
        label: ville,
        value: ville,
      })),
    },
    {
      id: "groupPrice",
      label: "Prix max",
      type: "number",
    },
  ];

  return (
    <section>
      <HStack spacing={4} align="center" justify="between" className="py-4">
        <Heading
          level={2}
          title={t("deals")}
          description={t("dealsDescription")}
          underline
        />

        <Button
          leftIcon={<Plus className="w-4 h-4" />}
          onClick={() => setAddDealModalOpen(true)}
        >
          Ajouter une offre
        </Button>
      </HStack>

      <DealsList
        deals={deals}
        cols={{ md: 2, base: 1, lg: 3 }}
        showFilters={false}
        viewMode="list"
        tableProps={{
          columnFiltersConfig,
          enableExport: false,
          enableSorting: true,
          actionsRow: ({ row }) => [
            {
              leftIcon: <Eye />,
              onClick: () => {
                setSelectedDeal(row.original);
                setOpenDetail(true);
              },
              tooltip: t("admin:deals.viewDetail"),
            },
          ],
        }}
        isAdmin
      />
      <CreateDealModal
        open={addDealModalOpen}
        onClose={() => setAddDealModalOpen(false)}
        onSuccess={() => refetch()}
        connectedMerchantUuid={user?.id}
      />

      <ViewDetailDealModal
        open={openDetail}
        onClose={() => setOpenDetail(false)}
        deal={selectedDeal}
      />
    </section>
  );
}
