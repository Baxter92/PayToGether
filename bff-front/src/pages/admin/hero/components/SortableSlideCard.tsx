import { useRef, useState, useCallback, type JSX } from "react";
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
import { Dialog, DialogContent } from "@/common/components/ui/dialog";
import {
  Trash2,
  GripVertical,
  Eye,
  EyeOff,
  Upload,
  ImageIcon,
  Crop,
} from "lucide-react";
import Cropper from "react-easy-crop";
import { getCroppedImg } from "@/common/utils/image";

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

  // Crop states
  const [cropSrc, setCropSrc] = useState<string | null>(null);
  const [currentFile, setCurrentFile] = useState<File | null>(null);
  const [croppedArea, setCroppedArea] = useState<any>(null);
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);

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

  const onCropComplete = useCallback((_area: any, pixels: any) => {
    setCroppedArea(pixels);
  }, []);

  const handleFileSelect = (file: File) => {
    setCurrentFile(file);
    setCropSrc(URL.createObjectURL(file));
    setCrop({ x: 0, y: 0 });
    setZoom(1);
  };

  const applyCrop = async () => {
    if (!cropSrc || !currentFile || !croppedArea) return;

    try {
      const croppedFile = await getCroppedImg(
        cropSrc,
        croppedArea,
        currentFile
      );
      onImageUpload(slide.id, croppedFile);

      setCropSrc(null);
      setCurrentFile(null);
    } catch (error) {
      console.error("Erreur lors du recadrage:", error);
    }
  };

  const openCropModal = () => {
    // Ouvrir le crop avec l'image actuelle
    fetch(slide.image)
      .then((res) => res.blob())
      .then((blob) => {
        const file = new File([blob], "current-image.jpg", { type: blob.type });
        handleFileSelect(file);
      });
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
        <CardTitle className="pb-3">
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
        </CardTitle>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Preview image */}
            <div className="space-y-2">
              <Label>Image de fond</Label>
              <div className="relative aspect-video rounded-lg overflow-hidden bg-muted group border border-gray-200">
                <img
                  src={slide.image}
                  alt={slide.title}
                  className="w-full h-full object-cover"
                />
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
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => fileInputRef.current?.click()}
                  className="flex-1"
                >
                  <ImageIcon className="h-4 w-4 mr-2" />
                  Uploader une image
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={openCropModal}
                >
                  <Crop className="h-4 w-4" />
                </Button>
              </HStack>
            </div>

            {/* Form fields */}
            <VStack spacing={10} className="items-stretch">
              <Input
                label="Titre"
                value={slide.title}
                onChange={(e) =>
                  onSlideChange(slide.id, "title", e.target.value)
                }
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
                onChange={(e) =>
                  onSlideChange(slide.id, "badge", e.target.value)
                }
                placeholder="Ex: Nouveau, Populaire..."
              />
            </VStack>
          </div>
        </CardContent>
      </Card>

      {/* Modal de crop */}
      <Dialog open={!!cropSrc} onOpenChange={() => setCropSrc(null)}>
        <DialogContent className="max-w-3xl">
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold">Recadrer l'image</h3>
              <p className="text-sm text-muted-foreground">
                Ajustez le cadrage et le zoom de votre image de slide
              </p>
            </div>

            {cropSrc && (
              <>
                <div className="relative h-[400px] bg-gray-900 rounded-lg overflow-hidden">
                  <Cropper
                    image={cropSrc}
                    crop={crop}
                    zoom={zoom}
                    aspect={16 / 9}
                    onCropChange={setCrop}
                    onCropComplete={onCropComplete}
                    onZoomChange={setZoom}
                  />
                </div>

                <div className="space-y-3">
                  <div>
                    <label className="text-sm font-medium mb-2 block">
                      Zoom: {Math.round(zoom * 100)}%
                    </label>
                    <input
                      type="range"
                      min={1}
                      max={3}
                      step={0.1}
                      value={zoom}
                      onChange={(e) => setZoom(Number(e.target.value))}
                      className="w-full"
                    />
                  </div>
                  <p className="text-xs text-muted-foreground">
                    💡 Astuce: Utilisez la molette de la souris pour zoomer ou
                    déplacer l'image avec le curseur
                  </p>
                </div>

                <div className="flex justify-end gap-3 pt-4 border-t">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => {
                      setCropSrc(null);
                      setCurrentFile(null);
                    }}
                  >
                    Annuler
                  </Button>
                  <Button type="button" onClick={applyCrop}>
                    Appliquer le recadrage
                  </Button>
                </div>
              </>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
