import { Skeleton } from "@/common/components/ui/skeleton";

/**
 * Skeleton pour le Hero slider
 */
export function HeroSkeleton() {
  return (
    <div className="relative w-full h-[500px] md:h-[600px] overflow-hidden">
      {/* Background skeleton */}
      <Skeleton className="absolute inset-0 w-full h-full" />

      {/* Content overlay skeleton */}
      <div className="absolute inset-0 flex items-center justify-center">
        <div className="max-w-7xl mx-auto px-4 w-full">
          <div className="max-w-2xl space-y-6">
            {/* Title skeleton */}
            <Skeleton className="h-12 w-3/4" />
            <Skeleton className="h-12 w-2/3" />

            {/* Description skeleton */}
            <div className="space-y-2">
              <Skeleton className="h-6 w-full" />
              <Skeleton className="h-6 w-5/6" />
            </div>

            {/* Button skeleton */}
            <Skeleton className="h-12 w-40 rounded-md" />
          </div>
        </div>
      </div>

      {/* Navigation dots skeleton */}
      <div className="absolute bottom-8 left-1/2 transform -translate-x-1/2 flex gap-2">
        {Array.from({ length: 3 }).map((_, index) => (
          <Skeleton key={index} className="h-2 w-8 rounded-full" />
        ))}
      </div>
    </div>
  );
}

