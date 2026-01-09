import { useState, type JSX } from "react";
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
import { slides as initialSlides } from "@/common/constants/data";
import { Save, Plus } from "lucide-react";
import { toast } from "sonner";
import { SortableSlideCard } from "./components/SortableSlideCard";

interface HeroSlide {
  id: number;
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
}

export default function AdminHero(): JSX.Element {
  const [slides, setSlides] = useState<HeroSlide[]>(
    initialSlides.map((slide) => ({ ...slide, isActive: true }))
  );
  const [heroEnabled, setHeroEnabled] = useState(true);

  const sensors = useSensors(useSensor(PointerSensor));

  const handleSlideChange = (
    id: number,
    field: keyof HeroSlide,
    value: string | boolean
  ) => {
    setSlides((prev) =>
      prev.map((slide) =>
        slide.id === id ? { ...slide, [field]: value } : slide
      )
    );
  };

  const handleImageUpload = (id: number, file: File) => {
    if (!file.type.startsWith("image/")) {
      toast.error("Veuillez sélectionner une image valide");
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      toast.error("L'image ne doit pas dépasser 5 Mo");
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      const base64 = e.target?.result as string;
      handleSlideChange(id, "image", base64);
      toast.success("Image uploadée");
    };
    reader.onerror = () => {
      toast.error("Erreur lors du chargement de l'image");
    };
    reader.readAsDataURL(file);
  };

  const handleToggleSlide = (id: number) => {
    setSlides((prev) =>
      prev.map((slide) =>
        slide.id === id ? { ...slide, isActive: !slide.isActive } : slide
      )
    );
  };

  const handleDeleteSlide = (id: number) => {
    if (slides.length <= 1) {
      toast.error("Vous devez garder au moins un slide");
      return;
    }
    setSlides((prev) => prev.filter((slide) => slide.id !== id));
    toast.success("Slide supprimé");
  };

  const handleAddSlide = () => {
    const ids = slides.map((s) => s.id);
    const newId = ids.length > 0 ? Math.max(...ids) + 1 : 1;

    setSlides((prev) => [
      ...prev,
      {
        id: newId,
        title: "Nouveau slide",
        subtitle: "Sous-titre",
        description: "Description du slide",
        buttonText: "Découvrir",
        buttonLink: "/deals",
        image:
          "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=1200&q=80",
        gradient: "from-blue-600/50 to-indigo-600/50",
        textColor: "text-white",
        badge: "Nouveau",
        isActive: true,
      },
    ]);
    toast.success("Nouveau slide ajouté");
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      setSlides((items) => {
        const oldIndex = items.findIndex((item) => item.id === active.id);
        const newIndex = items.findIndex((item) => item.id === over.id);
        return arrayMove(items, oldIndex, newIndex);
      });
      toast.success("Ordre des slides mis à jour");
    }
  };

  const handleSave = () => {
    toast.success("Modifications enregistrées", {
      description: "Les changements seront visibles sur la page d'accueil",
    });
  };

  return (
    <main className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <Heading
          title="Gestion des Pub"
          description="Configurez le carrousel de la page d'accueil"
          level={2}
          underline
        />
        <HStack spacing={3}>
          <Button onClick={handleSave} leftIcon={<Save className="h-4 w-4" />}>
            Enregistrer
          </Button>
        </HStack>
      </div>

      {/* Toggle global */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <Label htmlFor="hero-toggle" className="text-base font-medium">
                Activer la Pub
              </Label>
              <p className="text-sm text-muted-foreground">
                Afficher le carrousel sur la page d'accueil
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

      {/* Liste des slides */}
      <VStack spacing={4}>
        <HStack justify="between">
          <h3 className="font-semibold text-lg">
            Slides ({slides.filter((s) => s.isActive).length} actifs sur{" "}
            {slides.length})
          </h3>
          <Button
            variant="outline"
            size="sm"
            onClick={handleAddSlide}
            leftIcon={<Plus className="h-4 w-4" />}
          >
            Ajouter un slide
          </Button>
        </HStack>

        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext
            items={slides.map((s) => s.id)}
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
                />
              ))}
            </VStack>
          </SortableContext>
        </DndContext>
      </VStack>
    </main>
  );
}
