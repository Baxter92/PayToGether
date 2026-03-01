import { useEffect, useState } from "react";
import { Card } from "@components/ui/card";
import { cn } from "@/common/lib/utils";
import { Button } from "@/common/components/ui/button";
import { HStack } from "@/common/components";
import { ImageLightbox } from "@/common/components/ImageLightbox";
import { ImageZoomCropper } from "@/common/components/ImageZoomCropper";
import { Expand, Crop } from "lucide-react";

export default function Gallery({ images }: { images: string[] }) {
  const [main, setMain] = useState(images[0]);
  const [isLightboxOpen, setIsLightboxOpen] = useState(false);
  const [isCropperOpen, setIsCropperOpen] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  useEffect(() => {
    if (!images?.length) return;

    // Sync when images are loaded asynchronously or changed.
    if (!main || !images.includes(main)) {
      setMain(images[0]);
    }
  }, [images, main]);

  const handleImageClick = (index: number): void => {
    setCurrentImageIndex(index);
    setIsLightboxOpen(true);
  };

  const handleCropperOpen = (): void => {
    setIsCropperOpen(true);
  };

  return (
    <div>
      <Card className="overflow-hidden relative group">
        <img
          src={main || images[0] || "/placeholder.svg"}
          alt="produit"
          className="w-full h-96 object-cover cursor-pointer"
          onClick={() => handleImageClick(images.indexOf(main))}
        />

        {/* Boutons en overlay */}
        <div className="absolute top-4 right-4 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
          <Button
            variant="secondary"
            size="icon"
            className="shadow-lg"
            onClick={handleCropperOpen}
            title="Zoom & Recadrage"
          >
            <Crop className="w-5 h-5" />
          </Button>
          <Button
            variant="secondary"
            size="icon"
            className="shadow-lg"
            onClick={() => handleImageClick(images.indexOf(main))}
            title="Voir en plein écran"
          >
            <Expand className="w-5 h-5" />
          </Button>
        </div>
      </Card>

      <HStack className="mt-3" spacing={8}>
        {images.map((src, i) => (
          <Button
            variant={"ghost"}
            key={src}
            className={cn(
              "w-28 h-20 p-0 rounded overflow-hidden border",
              main === src ? "border-primary-600" : "border-gray-200 dark:border-gray-700",
            )}
            aria-label={`Voir image ${i + 1}`}
            onClick={() => setMain(src)}
          >
            <img
              src={src}
              alt={`thumb ${i + 1}`}
              className="w-full h-full object-cover"
            />
          </Button>
        ))}
      </HStack>

      {/* Lightbox */}
      <ImageLightbox
        images={images}
        currentIndex={currentImageIndex}
        isOpen={isLightboxOpen}
        onClose={() => setIsLightboxOpen(false)}
      />

      {/* Zoom & Cropper */}
      <ImageZoomCropper
        imageSrc={main || images[0] || "/placeholder.svg"}
        isOpen={isCropperOpen}
        onClose={() => setIsCropperOpen(false)}
      />
    </div>
  );
}
