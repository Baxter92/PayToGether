import { useState } from "react";
import { Card } from "@components/ui/card";
import { cn } from "@/common/lib/utils";
import { Button } from "@/common/components/ui/button";
import { HStack } from "@/common/components";

export default function Gallery({ images }: { images: string[] }) {
  const [main, setMain] = useState(images[0]);

  return (
    <div>
      <Card className="overflow-hidden">
        <img src={main} alt="produit" className="w-full h-96 object-cover" />
      </Card>

      <HStack className="mt-3" spacing={8}>
        {images.map((src, i) => (
          <Button
            variant={"ghost"}
            key={src}
            className={cn(
              "w-28 h-20 p-0 rounded overflow-hidden border",
              main === src ? "border-primary-600" : "border-gray-200"
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
    </div>
  );
}
