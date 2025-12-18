import { HStack } from "@/common/components";
import { Button } from "@/common/components/ui/button";
import { mockDeals } from "@/common/constants/data";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { Plus } from "lucide-react";
import { useState, type JSX } from "react";
import { CreateDealModal } from "../components/CreateDealModal";

export default function MyDeals(): JSX.Element {
  const [addDealModalOpen, setAddDealModalOpen] = useState(false);
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
        deals={mockDeals}
        cols={{ md: 2, base: 1, lg: 3 }}
        filterPosition="top"
        viewMode="list"
      />
      <CreateDealModal
        open={addDealModalOpen}
        onClose={() => setAddDealModalOpen(false)}
      />
    </section>
  );
}
