import { useEffect, useState, useCallback, type JSX } from "react";
import { useI18n } from "@/common/hooks/useI18n";
import {
  DndContext,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { HStack, VStack } from "@/common/components";
import { Button } from "@/common/components/ui/button";
import { Card, CardContent } from "@/common/components/ui/card";
import { Switch } from "@/common/components/ui/switch";
import { Label } from "@/common/components/ui/label";
import { Heading } from "@/common/containers/Heading";
import { Save, Plus } from "lucide-react";
import { toast } from "sonner";
import { SortableSlideCard } from "./components/SortableSlideCard";
import {
  useCreatePublicite,
  useDeletePublicite,
  usePublicites,
  useUpdatePublicite,
} from "@/common/api";

interface HeroSlide {
  id: number;
  uuid?: string;
  title: string;
  subtitle: string;
  description: string;
  buttonText: string;
  buttonLink: string;
  image: string;
  gradient: string;
  textColor: string;
  badge?: string;
  isActive: boolean;
  imageFile?: File;
}

export default function AdminHero(): JSX.Element {
  const [slides, setSlides] = useState<HeroSlide[]>([]);
  const [heroEnabled, setHeroEnabled] = useState(true);
  const { t: tAdmin } = useI18n("admin");
  const {
    data: publicitesData,
    isLoading: isLoadingPublicites,
    isError: isErrorPublicites,
  } = usePublicites();
  const {
    mutateAsync: createPublicite,
    isPending,
    isUploading,
  } = useCreatePublicite();
  const { mutateAsync: updatePublicite, isPending: isUpdatingPublicite } =
    useUpdatePublicite();
  const { mutateAsync: deletePublicite, isPending: isDeletingPublicite } =
    useDeletePublicite();

  // ✅ Désactive le PointerSensor sur les inputs pour éviter les conflits drag
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 8 },
    }),
  );

  useEffect(() => {
    if (!publicitesData) return;

    setSlides(
      publicitesData.map((publicite, index) => {
        const parts = (publicite.description ?? "")
          .split(" • ")
          .map((p) => p.trim())
          .filter(Boolean);

        return {
          id: index + 1,
          uuid: publicite.uuid,
          title: publicite.titre,
          subtitle: parts[0] ?? "",
          description: parts[1] ?? publicite.description ?? "",
          buttonText: "Voir l'offre",
          buttonLink: publicite.lienExterne ?? "/deals",
          image: publicite.listeImages?.[0]?.urlImage || "/placeholder.svg",
          gradient: "from-blue-600/50 to-indigo-600/50",
          textColor: "text-white",
          badge: parts[2],
          isActive: Boolean(publicite.active ?? true),
        };
      }),
    );
  }, [publicitesData]);

  // ✅ useCallback sur tous les handlers pour éviter les re-renders en cascade
  const handleSlideChange = useCallback(
    (id: number, field: keyof HeroSlide, value: string | boolean) => {
      setSlides((prev) =>
        prev.map((slide) =>
          slide.id === id ? { ...slide, [field]: value } : slide,
        ),
      );
    },
    [],
  );

  const handleImageUpload = useCallback(
    (id: number, file: File) => {
      if (!file.type.startsWith("image/")) {
        toast.error(tAdmin("hero.invalidImage"));
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        toast.error(tAdmin("hero.imageTooLarge"));
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        const base64 = e.target?.result as string;
        setSlides((prev) =>
          prev.map((slide) =>
            slide.id === id
              ? { ...slide, image: base64, imageFile: file }
              : slide,
          ),
        );
        toast.success(tAdmin("hero.imageUploaded"));
      };
      reader.onerror = () => toast.error(tAdmin("hero.uploadError"));
      reader.readAsDataURL(file);
    },
    [tAdmin],
  );

  const handleToggleSlide = useCallback(
    async (id: number) => {
      const targetSlide = slides.find((slide) => slide.id === id);
      if (!targetSlide) return;

      const nextActive = !targetSlide.isActive;

      if (!targetSlide.uuid) {
        setSlides((prev) =>
          prev.map((slide) =>
            slide.id === id ? { ...slide, isActive: nextActive } : slide,
          ),
        );
        return;
      }

      try {
        await updatePublicite({
          id: targetSlide.uuid,
          data: {
            active: nextActive,
          },
        });

        setSlides((prev) =>
          prev.map((slide) =>
            slide.id === id ? { ...slide, isActive: nextActive } : slide,
          ),
        );
      } catch (error) {
        toast.error("Erreur lors de la mise a jour de la publicite", {
          description:
            error instanceof Error ? error.message : "Une erreur est survenue",
        });
      }
    },
    [slides, updatePublicite],
  );

  const handleDeleteSlide = useCallback(
    async (id: number) => {
      const targetSlide = slides.find((slide) => slide.id === id);
      if (!targetSlide) return;

      if (slides.length <= 1) {
        toast.error(tAdmin("hero.keepAtLeastOne"));
        return;
      }

      try {
        if (targetSlide.uuid) {
          await deletePublicite(targetSlide.uuid);
        }

        setSlides((prev) => prev.filter((slide) => slide.id !== id));
        toast.success(tAdmin("hero.slideDeleted"));
      } catch (error) {
        toast.error("Erreur lors de la suppression de la publicite", {
          description:
            error instanceof Error ? error.message : "Une erreur est survenue",
        });
      }
    },
    [slides, tAdmin, deletePublicite],
  );

  const handleAddSlide = useCallback(() => {
    setSlides((prev) => {
      const newId =
        prev.length > 0 ? Math.max(...prev.map((s) => s.id)) + 1 : 1;
      return [
        ...prev,
        {
          id: newId,
          title: tAdmin("hero.newSlideTitle"),
          subtitle: tAdmin("hero.newSlideSubtitle"),
          description: tAdmin("hero.newSlideDescription"),
          buttonText: tAdmin("hero.newSlideButtonText"),
          buttonLink: "/deals",
          image:
            "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200&q=80",
          gradient: "from-blue-600/50 to-indigo-600/50",
          textColor: "text-white",
          badge: tAdmin("hero.newBadge"),
          isActive: true,
        },
      ];
    });
    toast.success(tAdmin("hero.slideAdded"));
  }, [tAdmin]);

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      const { active, over } = event;
      if (over && active.id !== over.id) {
        setSlides((items) => {
          const oldIndex = items.findIndex((item) => item.id === active.id);
          const newIndex = items.findIndex((item) => item.id === over.id);
          return arrayMove(items, oldIndex, newIndex);
        });
        toast.success(tAdmin("hero.orderUpdated"));
      }
    },
    [tAdmin],
  );

  const handleSave = useCallback(async () => {
    const activeSlides = slides.filter((s) => s.isActive);
    const slidesToCreate = activeSlides.filter((s) => s.imageFile);

    if (slidesToCreate.length === 0) {
      toast.error(tAdmin("hero.invalidImage"), {
        description:
          "Ajoute au moins une image locale sur un slide actif avant de sauvegarder.",
      });
      return;
    }

    const skippedCount = activeSlides.length - slidesToCreate.length;

    try {
      await Promise.all(
        slidesToCreate.map(async (slide) => {
          const now = new Date();
          const dateDebut = now.toISOString().slice(0, 19);
          const dateFin = new Date(now.getTime() + 30 * 24 * 60 * 60 * 1000)
            .toISOString()
            .slice(0, 19);

          await createPublicite({
            titre: slide.title,
            description: [slide.subtitle, slide.description, slide.badge]
              .filter(Boolean)
              .join(" • "),
            lienExterne: slide.buttonLink || null,
            listeImages: [
              {
                urlImage: slide.imageFile?.name || "hero-image.jpg",
                statut: "PENDING",
                presignUrl: null,
                file: slide.imageFile,
              },
            ],
            dateDebut,
            dateFin,
            active: true,
          });
        }),
      );

      toast.success(tAdmin("hero.saved"), {
        description:
          skippedCount > 0
            ? `${slidesToCreate.length} slide(s) enregistré(s), ${skippedCount} ignoré(s) (image non uploadée).`
            : tAdmin("hero.saveDescription"),
      });
    } catch (error) {
      toast.error("Erreur lors de la création des publicités", {
        description:
          error instanceof Error ? error.message : "Une erreur est survenue",
      });
    }
  }, [slides, createPublicite, tAdmin]);

  // ✅ Mémoïser les slide IDs pour éviter un recalcul à chaque render
  const slideIds = slides.map((s) => s.id);

  return (
    <main className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <Heading
          title={tAdmin("hero.title")}
          description={tAdmin("hero.description")}
          level={2}
          underline
        />
        <HStack spacing={3}>
          <Button
            onClick={handleSave}
            leftIcon={<Save className="h-4 w-4" />}
            disabled={isPending || isUploading}
          >
            {tAdmin("hero.save")}
          </Button>
        </HStack>
      </div>

      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label htmlFor="hero-toggle" className="text-base font-medium">
                {tAdmin("hero.enableHero")}
              </Label>
              <p className="text-sm text-muted-foreground">
                {tAdmin("hero.enableHeroDescription")}
              </p>
            </div>
            <Switch
              id="hero-toggle"
              checked={heroEnabled}
              onCheckedChange={setHeroEnabled}
            />
          </div>
        </CardContent>
      </Card>

      <VStack spacing={4}>
        <HStack justify="between">
          <h3 className="font-semibold text-lg">
            {tAdmin("hero.slides")} ({slides.filter((s) => s.isActive).length}{" "}
            {tAdmin("hero.active")} {tAdmin("hero.of")} {slides.length})
          </h3>
          <Button
            variant="outline"
            size="sm"
            onClick={handleAddSlide}
            leftIcon={<Plus className="h-4 w-4" />}
          >
            {tAdmin("hero.addSlide")}
          </Button>
        </HStack>

        {isLoadingPublicites && slides.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            Chargement...
          </div>
        ) : isErrorPublicites && slides.length === 0 ? (
          <div className="text-center py-8 text-destructive">
            Erreur lors du chargement des publicites.
          </div>
        ) : (
          <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
          >
            <SortableContext
              items={slideIds}
              strategy={verticalListSortingStrategy}
            >
              <VStack spacing={10}>
                {slides.map((slide, index) => (
                  <SortableSlideCard
                    key={slide.id}
                    slide={slide}
                    index={index}
                    onSlideChange={handleSlideChange}
                    onToggleSlide={handleToggleSlide}
                    onDeleteSlide={handleDeleteSlide}
                    onImageUpload={handleImageUpload}
                    isProcessingActions={
                      isUpdatingPublicite || isDeletingPublicite
                    }
                  />
                ))}
              </VStack>
            </SortableContext>
          </DndContext>
        )}
      </VStack>
    </main>
  );
}
