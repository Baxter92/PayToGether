import { useRef, type JSX } from "react";
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
    value: string | boolean
  ) => void;
  onToggleSlide: (id: number) => void;
  onDeleteSlide: (id: number) => void;
  onImageUpload: (id: number, file: File) => void;
}

export function SortableSlideCard({
  slide,
  index,
  onSlideChange,
  onToggleSlide,
  onDeleteSlide,
  onImageUpload,
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
              onClick={() => onToggleSlide(slide.id)}
              title={
                slide.isActive ? tAdmin("hero.disable") : tAdmin("hero.enable")
              }
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
              onClick={() => onDeleteSlide(slide.id)}
              className="text-destructive hover:text-destructive"
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          </HStack>
        </HStack>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Preview image */}
          <div className="space-y-2">
            <Label>{tAdmin("hero.backgroundImage")}</Label>
            <div className="relative aspect-video rounded-lg overflow-hidden bg-muted group">
              <img
                src={slide.image}
                alt={slide.title}
                className="w-full h-full object-cover"
              />
              <div
                className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center cursor-pointer"
                onClick={() => fileInputRef.current?.click()}
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
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (file) onImageUpload(slide.id, file);
                  e.target.value = "";
                }}
              />
            </div>
            <HStack spacing={2}>
              <Button
                variant="outline"
                size="sm"
                onClick={() => fileInputRef.current?.click()}
                className="flex-1"
              >
                <ImageIcon className="h-4 w-4 mr-2" />
                {tAdmin("hero.uploadImage")}
              </Button>
            </HStack>
          </div>

          {/* Form fields */}
          <VStack spacing={10} className="items-stretch">
            <Input
              label={tAdmin("hero.title")}
              value={slide.title}
              onChange={(e) => onSlideChange(slide.id, "title", e.target.value)}
            />
            <Input
              label={tAdmin("hero.subtitle")}
              value={slide.subtitle}
              onChange={(e) =>
                onSlideChange(slide.id, "subtitle", e.target.value)
              }
            />
            <Input
              label={tAdmin("hero.description")}
              value={slide.description}
              onChange={(e) =>
                onSlideChange(slide.id, "description", e.target.value)
              }
            />
            <div className="grid grid-cols-2 gap-3">
              <Input
                label={tAdmin("hero.buttonText")}
                value={slide.buttonText}
                onChange={(e) =>
                  onSlideChange(slide.id, "buttonText", e.target.value)
                }
              />
              <Input
                label={tAdmin("hero.buttonLink")}
                value={slide.buttonLink}
                onChange={(e) =>
                  onSlideChange(slide.id, "buttonLink", e.target.value)
                }
              />
            </div>
            <Input
              label={tAdmin("hero.badge")}
              value={slide.badge || ""}
              onChange={(e) => onSlideChange(slide.id, "badge", e.target.value)}
              placeholder={tAdmin("hero.badgePlaceholder")}
            />
          </VStack>
        </div>
      </CardContent>
    </Card>
  );
}
