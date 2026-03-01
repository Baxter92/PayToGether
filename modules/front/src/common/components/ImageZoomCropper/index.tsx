import { useState, useRef, useEffect } from "react";
import { Button } from "@/common/components/ui/button";
import { ZoomIn, ZoomOut, RotateCw, Move, Maximize2, X } from "lucide-react";
import { cn } from "@/common/lib/utils";

interface ImageZoomCropperProps {
  imageSrc: string;
  isOpen: boolean;
  onClose: () => void;
}

export function ImageZoomCropper({ imageSrc, isOpen, onClose }: ImageZoomCropperProps) {
  const [scale, setScale] = useState(1);
  const [rotation, setRotation] = useState(0);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const [fitMode, setFitMode] = useState<"contain" | "cover">("contain");
  const containerRef = useRef<HTMLDivElement>(null);

  // Reset state when image changes or modal opens
  useEffect(() => {
    if (isOpen) {
      setScale(1);
      setRotation(0);
      setPosition({ x: 0, y: 0 });
      setFitMode("contain");
    }
  }, [imageSrc, isOpen]);

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent): void => {
      if (!isOpen) return;

      if (e.key === "Escape") onClose();
      if (e.key === "+" || e.key === "=") handleZoomIn();
      if (e.key === "-") handleZoomOut();
      if (e.key === "r" || e.key === "R") handleRotate();
      if (e.key === "0") handleReset();
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isOpen]);

  const handleZoomIn = (): void => {
    setScale((prev) => Math.min(prev + 0.25, 5));
  };

  const handleZoomOut = (): void => {
    setScale((prev) => Math.max(prev - 0.25, 0.5));
  };

  const handleRotate = (): void => {
    setRotation((prev) => (prev + 90) % 360);
  };

  const handleReset = (): void => {
    setScale(1);
    setRotation(0);
    setPosition({ x: 0, y: 0 });
  };

  const handleToggleFit = (): void => {
    setFitMode((prev) => (prev === "contain" ? "cover" : "contain"));
    setScale(1);
    setPosition({ x: 0, y: 0 });
  };

  const handleMouseDown = (e: React.MouseEvent): void => {
    if (e.button !== 0) return; // Only left click
    setIsDragging(true);
    setDragStart({
      x: e.clientX - position.x,
      y: e.clientY - position.y,
    });
  };

  const handleMouseMove = (e: React.MouseEvent): void => {
    if (!isDragging) return;
    setPosition({
      x: e.clientX - dragStart.x,
      y: e.clientY - dragStart.y,
    });
  };

  const handleMouseUp = (): void => {
    setIsDragging(false);
  };

  const handleWheel = (e: React.WheelEvent): void => {
    e.preventDefault();
    const delta = e.deltaY > 0 ? -0.1 : 0.1;
    setScale((prev) => Math.max(0.5, Math.min(5, prev + delta)));
  };

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 z-50 bg-black/95 backdrop-blur-sm"
      onClick={onClose}
    >
      {/* Header */}
      <div className="absolute top-0 left-0 right-0 z-10 bg-black/50 border-b border-white/10">
        <div className="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <h3 className="text-white font-semibold">Zoom & Recadrage</h3>
            <div className="text-white/60 text-sm hidden sm:block">
              Molette pour zoomer • Glisser pour déplacer • R pour rotation
            </div>
          </div>
          <Button
            variant="ghost"
            size="icon"
            className="text-white hover:bg-white/20"
            onClick={onClose}
          >
            <X className="w-5 h-5" />
          </Button>
        </div>
      </div>

      {/* Toolbar */}
      <div className="absolute bottom-0 left-0 right-0 z-10 bg-black/50 border-t border-white/10">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex flex-wrap items-center justify-center gap-2">
            {/* Zoom controls */}
            <div className="flex items-center gap-1 bg-black/50 rounded-lg p-1">
              <Button
                variant="ghost"
                size="icon-sm"
                className="text-white hover:bg-white/20"
                onClick={handleZoomOut}
                disabled={scale <= 0.5}
              >
                <ZoomOut className="w-4 h-4" />
              </Button>

              <div className="px-3 text-white text-sm min-w-[60px] text-center">
                {Math.round(scale * 100)}%
              </div>

              <Button
                variant="ghost"
                size="icon-sm"
                className="text-white hover:bg-white/20"
                onClick={handleZoomIn}
                disabled={scale >= 5}
              >
                <ZoomIn className="w-4 h-4" />
              </Button>
            </div>

            {/* Rotation */}
            <Button
              variant="ghost"
              size="icon"
              className="text-white hover:bg-white/20 bg-black/50"
              onClick={handleRotate}
              title="Rotation (R)"
            >
              <RotateCw className="w-4 h-4" />
            </Button>

            {/* Toggle fit mode */}
            <Button
              variant="ghost"
              size="icon"
              className="text-white hover:bg-white/20 bg-black/50"
              onClick={handleToggleFit}
              title={fitMode === "contain" ? "Mode remplissage" : "Mode ajusté"}
            >
              <Maximize2 className="w-4 h-4" />
            </Button>

            {/* Pan indicator */}
            <div className="flex items-center gap-2 px-3 py-2 bg-black/50 rounded-lg text-white/60 text-sm">
              <Move className="w-4 h-4" />
              <span className="hidden sm:inline">Déplacer</span>
            </div>

            {/* Reset */}
            <Button
              variant="secondary"
              size="sm"
              onClick={handleReset}
              className="bg-black/50 hover:bg-white/20"
            >
              Réinitialiser
            </Button>
          </div>

          {/* Info text */}
          <div className="mt-2 text-center text-white/40 text-xs">
            Position: X {position.x.toFixed(0)}, Y {position.y.toFixed(0)} • Rotation: {rotation}°
          </div>
        </div>
      </div>

      {/* Image Container */}
      <div
        ref={containerRef}
        className="absolute inset-0 flex items-center justify-center overflow-hidden pt-16 pb-32"
        onClick={(e) => e.stopPropagation()}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
        onWheel={handleWheel}
      >
        <div
          className={cn(
            "relative select-none",
            isDragging ? "cursor-grabbing" : "cursor-grab",
          )}
          style={{
            transform: `translate(${position.x}px, ${position.y}px) scale(${scale}) rotate(${rotation}deg)`,
            transition: isDragging ? "none" : "transform 0.1s ease-out",
          }}
        >
          <img
            src={imageSrc}
            alt="Image à manipuler"
            className={cn(
              "max-w-[90vw] max-h-[calc(100vh-200px)] pointer-events-none",
              fitMode === "contain" ? "object-contain" : "object-cover",
            )}
            style={{
              width: fitMode === "cover" ? "100vw" : "auto",
              height: fitMode === "cover" ? "calc(100vh - 200px)" : "auto",
            }}
            draggable={false}
          />
        </div>
      </div>

      {/* Grid overlay (optional, for reference) */}
      {scale > 1.5 && (
        <div
          className="absolute inset-0 pointer-events-none"
          style={{
            backgroundImage: `
              linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px),
              linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px)
            `,
            backgroundSize: "50px 50px",
            opacity: 0.3,
          }}
        />
      )}
    </div>
  );
}

