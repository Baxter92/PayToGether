import { memo, useRef, useCallback, type JSX } from "react";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { useI18n } from "@/common/hooks/useI18n";
import { HStack, VStack } from "@/common/components";
import { Button } from "@/common/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import { Input } from "@/common/components/ui/input";
import { Label } from "@/common/components/ui/label";
import {
  Trash2,
  GripVertical,
  Eye,
  EyeOff,
  Upload,
  ImageIcon,
} from "lucide-react";

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

interface SortableSlideCardProps {
  slide: HeroSlide;
  index: number;
  onSlideChange: (
    id: number,
    field: keyof HeroSlide,
    value: string | boolean,
  ) => void;
  onToggleSlide: (id: number) => void | Promise<void>;
  onDeleteSlide: (id: number) => void | Promise<void>;
  onImageUpload: (id: number, file: File) => void;
  isProcessingActions?: boolean;
}

// ✅ memo = re-render seulement si les props changent réellement
export const SortableSlideCard = memo(function SortableSlideCard({
  slide,
  index,
  onSlideChange,
  onToggleSlide,
  onDeleteSlide,
  onImageUpload,
  isProcessingActions = false,
}: SortableSlideCardProps): JSX.Element {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const { t: tAdmin } = useI18n("admin");

  const {
    attributes,
    listeners,
    setNodeRef,
    setActivatorNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: slide.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  // ✅ Handlers locaux stables grâce à useCallback + id capturé en closure
  const handleToggle = useCallback(
    () => onToggleSlide(slide.id),
    [onToggleSlide, slide.id],
  );
  const handleDelete = useCallback(
    () => onDeleteSlide(slide.id),
    [onDeleteSlide, slide.id],
  );
  const handleImageClick = useCallback(() => fileInputRef.current?.click(), []);

  const handleFileChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file) onImageUpload(slide.id, file);
      e.target.value = "";
    },
    [onImageUpload, slide.id],
  );

  // ✅ Un handler par champ pour éviter des closures inline dans le JSX
  const handleTitleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) =>
      onSlideChange(slide.id, "title", e.target.value),
    [onSlideChange, slide.id],
  );
  const handleSubtitleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) =>
      onSlideChange(slide.id, "subtitle", e.target.value),
    [onSlideChange, slide.id],
  );
  const handleDescriptionChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) =>
      onSlideChange(slide.id, "description", e.target.value),
    [onSlideChange, slide.id],
  );
  const handleButtonTextChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) =>
      onSlideChange(slide.id, "buttonText", e.target.value),
    [onSlideChange, slide.id],
  );
  const handleButtonLinkChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) =>
      onSlideChange(slide.id, "buttonLink", e.target.value),
    [onSlideChange, slide.id],
  );
  const handleBadgeChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) =>
      onSlideChange(slide.id, "badge", e.target.value),
    [onSlideChange, slide.id],
  );

  return (
    <Card
      ref={setNodeRef}
      style={style}
      className={`transition-opacity ${!slide.isActive ? "opacity-60" : ""} ${
        isDragging ? "opacity-50 shadow-lg z-50" : ""
      }`}
    >
      <CardHeader className="pb-3">
        <HStack justify="between">
          <HStack spacing={3}>
            <button
              ref={setActivatorNodeRef}
              {...attributes}
              {...listeners}
              className="cursor-grab active:cursor-grabbing touch-none"
              type="button"
            >
              <GripVertical className="h-5 w-5 text-muted-foreground" />
            </button>
            <CardTitle className="text-base">
              {tAdmin("hero.slide")} {index + 1}
            </CardTitle>
            {!slide.isActive && (
              <span className="text-xs bg-muted px-2 py-1 rounded">
                {tAdmin("hero.disabled")}
              </span>
            )}
          </HStack>
          <HStack spacing={2}>
            <Button
              variant="ghost"
              size="icon"
              onClick={handleToggle}
              title={
                slide.isActive ? tAdmin("hero.disable") : tAdmin("hero.enable")
              }
              disabled={isProcessingActions}
            >
              {slide.isActive ? (
                <Eye className="h-4 w-4" />
              ) : (
                <EyeOff className="h-4 w-4" />
              )}
            </Button>
            <Button
              variant="ghost"
              size="icon"
              onClick={handleDelete}
              className="text-destructive hover:text-destructive"
              disabled={isProcessingActions}
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          </HStack>
        </HStack>
      </CardHeader>

      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label>{tAdmin("hero.backgroundImage")}</Label>
            <div className="relative aspect-video rounded-lg overflow-hidden bg-muted group">
              <img
                src={slide.image}
                alt={slide.title}
                className="w-full h-full object-cover"
                // ✅ Évite le layout shift et force le navigateur à ne pas re-décoder si src identique
                loading="lazy"
              />
              <div
                className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center cursor-pointer"
                onClick={handleImageClick}
              >
                <VStack spacing={2} className="text-white" align="center">
                  <Upload className="h-8 w-8" />
                  <span className="text-sm font-medium">
                    {tAdmin("hero.changeImage")}
                  </span>
                </VStack>
              </div>
              <input
                type="file"
                accept="image/*"
                className="hidden"
                ref={fileInputRef}
                onChange={handleFileChange}
              />
            </div>
            <HStack spacing={2}>
              <Button
                variant="outline"
                size="sm"
                onClick={handleImageClick}
                className="flex-1"
              >
                <ImageIcon className="h-4 w-4 mr-2" />
                {tAdmin("hero.uploadImage")}
              </Button>
            </HStack>
          </div>

          <VStack spacing={10} className="items-stretch">
            <Input
              label={tAdmin("hero.title")}
              value={slide.title}
              onChange={handleTitleChange}
            />
            <Input
              label={tAdmin("hero.subtitle")}
              value={slide.subtitle}
              onChange={handleSubtitleChange}
            />
            <Input
              label={tAdmin("hero.description")}
              value={slide.description}
              onChange={handleDescriptionChange}
            />
            <div className="grid grid-cols-2 gap-3">
              <Input
                label={tAdmin("hero.buttonText")}
                value={slide.buttonText}
                onChange={handleButtonTextChange}
              />
              <Input
                label={tAdmin("hero.buttonLink")}
                value={slide.buttonLink}
                onChange={handleButtonLinkChange}
              />
            </div>
            <Input
              label={tAdmin("hero.badge")}
              value={slide.badge || ""}
              onChange={handleBadgeChange}
              placeholder={tAdmin("hero.badgePlaceholder")}
            />
          </VStack>
        </div>
      </CardContent>
    </Card>
  );
});
