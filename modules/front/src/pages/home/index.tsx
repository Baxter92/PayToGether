import { publiciteService, useDealsByStatut, usePublicitesActives } from "@/common/api";
import { mapDealToView } from "@/common/api/mappers/catalog";
import { StatutDeal } from "@/common/api/types/deal";
import { useI18n } from "@hooks/useI18n";
import Hero from "@containers/Hero";
import { useMemo, type ComponentProps, type JSX } from "react";
import DealsList from "@/common/containers/DealList";
import { Heading } from "@/common/containers/Heading";
import { VStack } from "@/common/components";
import { Button } from "@/common/components/ui/button";
import { ArrowRight, Sparkles } from "lucide-react";
import { PATHS } from "@/common/constants/path";
import { useQueries } from "@tanstack/react-query";

export default function Home(): JSX.Element {
  const { t } = useI18n("home");
  const { data: dealsData, isLoading } = useDealsByStatut(StatutDeal.PUBLIE);
  const { data: publicitesData = [] } = usePublicitesActives();
  const allDeals = (dealsData ?? []).map(mapDealToView);
  type HeroSlide = ComponentProps<typeof Hero>["slides"][number];

  const heroImageQueries = useQueries({
    queries: publicitesData.map((publicite) => {
      const imageUuid = publicite.listeImages?.[0]?.imageUuid;
      return {
        queryKey: ["publicites", "detail", publicite.uuid, "image-url", imageUuid],
        queryFn: () => publiciteService.getImageUrl(publicite.uuid, imageUuid as string),
        enabled: !!publicite.uuid && !!imageUuid,
      };
    }),
  });

  const heroSlides = useMemo<HeroSlide[]>(
    () =>
      publicitesData.map((publicite, index) => ({
        id: index + 1,
        title: publicite.titre ?? "",
        subtitle: "",
        description: publicite.description ?? "",
        buttonText: t("exploreAll"),
        buttonLink: publicite.lienExterne ?? PATHS.DEALS,
        image: heroImageQueries[index]?.data?.url ?? "/placeholder.svg",
        gradient: "from-blue-600/50 to-indigo-600/50",
        textColor: "text-white",
      })),
    [heroImageQueries, publicitesData, t],
  );

  const featuredDeals = useMemo(() => allDeals.slice(0, 4), [allDeals]);
  const popularDeals = useMemo(
    () =>
      [...allDeals]
        .sort((a, b) => (b.discount || 0) - (a.discount || 0))
        .slice(0, 4),
    [allDeals],
  );

  return (
    <div className="mx-auto">
      {heroSlides.length > 0 ? <Hero slides={heroSlides} /> : null}

      {/* Promotional Deals Section */}
      <section className="py-16 bg-background">
        <VStack spacing={10} className="max-w-7xl mx-auto px-4">
          <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4 w-full">
            <Heading
              title={t("promoTitle")}
              level={2}
              spacing={8}
              description={t("promoDescription")}
              underline
              underlineStyle="bar"
              underlineWidth="w-16"
            />
            <Button
              to={PATHS.DEALS}
              variant="ghost"
              className="text-primary hover:text-primary-600 font-semibold gap-2 group"
            >
              {t("viewAllDeals")}
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </Button>
          </div>
          <DealsList
            deals={featuredDeals}
            showFilters={false}
            showPagination={false}
            itemsPerPage={4}
          />
        </VStack>
      </section>

      {/* Popular Section */}
      <section className="py-16 bg-gradient-to-b from-muted/50 to-background relative">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,oklch(0.75_0.15_230_/_0.05),transparent_50%)]" />
        <VStack spacing={10} className="max-w-7xl mx-auto px-4 relative z-10">
          <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4 w-full">
            <div className="flex items-start gap-3">
              <div className="p-2 bg-accent/10 rounded-xl">
                <Sparkles className="w-6 h-6 text-accent" />
              </div>
              <Heading
                title={t("popularTitle")}
                level={2}
                spacing={8}
                description={t("popularDescription")}
                underline
                underlineStyle="bar"
                underlineWidth="w-16"
              />
            </div>
            <Button
              to={PATHS.DEALS}
              variant="ghost"
              className="text-primary hover:text-primary-600 font-semibold gap-2 group"
            >
              {t("exploreAll")}
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </Button>
          </div>
          <DealsList
            deals={popularDeals}
            showFilters={false}
            showPagination={false}
            itemsPerPage={4}
          />
        </VStack>
      </section>

      {/* All Deals Section */}
      <section className="py-16 bg-gradient-to-b from-muted/30 to-background">
        <VStack spacing={10} className="max-w-7xl mx-auto px-4">
          <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4 w-full">
            <div className="flex items-start gap-3">
              <Heading
                title={t("savingsTitle")}
                level={2}
                spacing={8}
                description={t("savingsDescription")}
                underline
                underlineStyle="bar"
                underlineWidth="w-20"
              />
            </div>
            <Button
              to={PATHS.DEALS}
              variant="ghost"
              className="text-primary hover:text-primary-600 font-semibold gap-2 group"
            >
              {t("exploreAll")}
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </Button>
          </div>

          {isLoading ? (
            <div className="text-center py-8 text-muted-foreground">
              Chargement...
            </div>
          ) : (
            <DealsList deals={allDeals} showFilters={false} />
          )}
        </VStack>
      </section>
    </div>
  );
}
