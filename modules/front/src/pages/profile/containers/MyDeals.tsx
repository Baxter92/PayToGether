import { HStack } from "@/common/components";
import { Button } from "@/common/components/ui/button";
import { useDeals } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { Plus } from "lucide-react";
import { useState, type JSX } from "react";
import { CreateDealModal } from "../components/CreateDealModal";
import type { IColumnFilter } from "@/common/components/DataTable";

export default function MyDeals(): JSX.Element {
  const [addDealModalOpen, setAddDealModalOpen] = useState(false);
  const { data: dealsData, refetch } = useDeals();
  const deals = (dealsData ?? []).map(mapDealToView);

  // Configuration des filtres pour le DataTable
  const columnFiltersConfig: IColumnFilter[] = [
    {
      id: "category",
      label: "Catégorie",
      type: "select",
      options: [
        { label: "Climatiseurs", value: "clim" },
        { label: "Ventilateurs", value: "ventilo" },
        { label: "Électroménager", value: "electromenager" }
      ],
    },
    {
      id: "city",
      label: "Ville",
      type: "select",
      options: [
        { label: "Yaoundé", value: "yaounde" },
        { label: "Douala", value: "douala" }
      ],
    },
    {
      id: "groupPrice",
      label: "Prix max",
      type: "number",
    }
  ];

  return (
    <section>
      <HStack spacing={4} align="center" justify="between" className="py-4">
        <Heading
          level={2}
          title="Toutes mes offres"
          description="Parcourez les deals que vous avez créés"
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
          enableExport: true,
          enableSorting: true,
        }}
        isAdmin
      />
      <CreateDealModal
        open={addDealModalOpen}
        onClose={() => setAddDealModalOpen(false)}
        onSuccess={() => refetch()}
      />
    </section>
  );
}
