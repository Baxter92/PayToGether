import { useState, type ReactElement } from "react";
import { Eye, Plus } from "lucide-react";
import { Button } from "@/common/components/ui/button";
import { useI18n } from "@/common/hooks/useI18n";
import DealsList from "@/common/containers/DealList";
import { mockDeals } from "@/common/constants/data";
import { CreateDealModal } from "@/pages/profile/components/CreateDealModal";
import { ViewDetailDealModal } from "./containers/ViewDetailDealModal";

export default function AdminDeals(): ReactElement {
  const [open, setOpen] = useState(false);
  const [openDetail, setOpenDetail] = useState(false);
  const [selectedDeal, setSelectedDeal] = useState<any>();
  const { t: tAdmin } = useI18n("admin");
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

      <DealsList
        deals={mockDeals}
        viewMode="list"
        viewModeToggleable={false}
        showFilters
        filterPosition="top"
        availableFilters={["search", "category", "status"]}
        showPagination
        itemsPerPage={10}
        isAdmin
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

      <CreateDealModal open={open} onClose={() => setOpen(false)} />
      <ViewDetailDealModal
        open={openDetail}
        onClose={() => setOpenDetail(false)}
        deal={selectedDeal}
      />
    </section>
  );
}
