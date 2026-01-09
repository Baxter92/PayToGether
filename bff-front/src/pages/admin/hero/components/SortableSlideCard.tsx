import { useRef, useState, type JSX } from "react";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
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
  Crop,
} from "lucide-react";
import { ImageCropper } from "@/common/components/ImageCropper";

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
  onImageUpload: _onImageUpload,
}: SortableSlideCardProps): JSX.Element {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [showCropper, setShowCropper] = useState(false);
  const [tempImageSrc, setTempImageSrc] = useState<string>("");

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

  const handleFileSelect = (file: File) => {
    if (!file.type.startsWith("image/")) {
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      const base64 = e.target?.result as string;
      setTempImageSrc(base64);
      setShowCropper(true);
    };
    reader.readAsDataURL(file);
  };

  const handleCropComplete = (croppedImageUrl: string) => {
    onSlideChange(slide.id, "image", croppedImageUrl);
    setShowCropper(false);
    setTempImageSrc("");
  };

  const handleOpenCropperWithCurrentImage = () => {
    setTempImageSrc(slide.image);
    setShowCropper(true);
  };

  return (
    <>
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
              <CardTitle className="text-base">Slide {index + 1}</CardTitle>
              {!slide.isActive && (
                <span className="text-xs bg-muted px-2 py-1 rounded">
                  Désactivé
                </span>
              )}
            </HStack>
            <HStack spacing={2}>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => onToggleSlide(slide.id)}
                title={slide.isActive ? "Désactiver" : "Activer"}
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
              <Label>Image de fond</Label>
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
                    <span className="text-sm font-medium">Changer l'image</span>
                  </VStack>
                </div>
                <input
                  type="file"
                  accept="image/*"
                  className="hidden"
                  ref={fileInputRef}
                  onChange={(e) => {
                    const file = e.target.files?.[0];
                    if (file) handleFileSelect(file);
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
                  Uploader
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleOpenCropperWithCurrentImage}
                  className="flex-1"
                >
                  <Crop className="h-4 w-4 mr-2" />
                  Recadrer
                </Button>
              </HStack>
            </div>

            {/* Form fields */}
            <VStack spacing={10} className="items-stretch">
              <Input
                label="Titre"
                value={slide.title}
                onChange={(e) => onSlideChange(slide.id, "title", e.target.value)}
              />
              <Input
                label="Sous-titre"
                value={slide.subtitle}
                onChange={(e) =>
                  onSlideChange(slide.id, "subtitle", e.target.value)
                }
              />
              <Input
                label="Description"
                value={slide.description}
                onChange={(e) =>
                  onSlideChange(slide.id, "description", e.target.value)
                }
              />
              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="Texte du bouton"
                  value={slide.buttonText}
                  onChange={(e) =>
                    onSlideChange(slide.id, "buttonText", e.target.value)
                  }
                />
                <Input
                  label="Lien du bouton"
                  value={slide.buttonLink}
                  onChange={(e) =>
                    onSlideChange(slide.id, "buttonLink", e.target.value)
                  }
                />
              </div>
              <Input
                label="Badge (optionnel)"
                value={slide.badge || ""}
                onChange={(e) => onSlideChange(slide.id, "badge", e.target.value)}
                placeholder="Ex: Nouveau, Populaire..."
              />
            </VStack>
          </div>
        </CardContent>
      </Card>

      {/* Image Cropper Modal */}
      <ImageCropper
        open={showCropper}
        onClose={() => {
          setShowCropper(false);
          setTempImageSrc("");
        }}
        imageSrc={tempImageSrc}
        onCropComplete={handleCropComplete}
        aspectRatio={16 / 9}
      />
    </>
  );
}
