import { cn } from "@/common/lib/utils";
import type { JSX } from "react";

/**
 * StarRating
 * - display & optional interactive rating
 * - supports half stars
 * - customizable size, color, readonly
 */

export type StarRatingSize = "xs" | "sm" | "md" | "lg";

export interface IStarRatingProps {
  value: number; // rating value (ex: 3.5)
  max?: number; // number of stars (default 5)
  size?: StarRatingSize;
  readOnly?: boolean;
  allowHalf?: boolean;
  onChange?: (value: number) => void; // only if interactive
  className?: string;
  showValue?: boolean; // display numeric value
}

const SIZE_MAP: Record<StarRatingSize, string> = {
  xs: "w-3 h-3",
  sm: "w-4 h-4",
  md: "w-5 h-5",
  lg: "w-6 h-6",
};

function StarIcon({
  filled,
  className,
}: {
  filled: boolean;
  className?: string;
}) {
  return (
    <svg
      viewBox="0 0 20 20"
      fill={filled ? "currentColor" : "none"}
      stroke="currentColor"
      strokeWidth="1.5"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.967a1 1 0 00.95.69h4.173c.969 0 1.371 1.24.588 1.81l-3.377 2.455a1 1 0 00-.364 1.118l1.287 3.966c.3.922-.755 1.688-1.54 1.118l-3.377-2.454a1 1 0 00-1.176 0l-3.377 2.454c-.784.57-1.838-.196-1.539-1.118l1.286-3.966a1 1 0 00-.364-1.118L2.002 9.394c-.783-.57-.38-1.81.588-1.81h4.174a1 1 0 00.95-.69l1.286-3.967z"
      />
    </svg>
  );
}

function StarRating({
  value,
  max = 5,
  size = "md",
  readOnly = true,
  allowHalf = true,
  onChange,
  className,
  showValue = false,
}: IStarRatingProps): JSX.Element {
  const stars = Array.from({ length: max });

  const getFill = (index: number): number => {
    const diff = value - index;
    if (diff >= 1) return 1; // full
    if (allowHalf && diff >= 0.5) return 0.5; // half
    return 0; // empty
  };

  return (
    <div className={cn("flex items-center gap-1 text-yellow-500", className)}>
      {stars.map((_, i) => {
        const fill = getFill(i);

        return (
          <button
            key={i}
            type="button"
            disabled={readOnly}
            onClick={() => !readOnly && onChange?.(i + 1)}
            className={cn(
              "relative",
              !readOnly && "cursor-pointer",
              readOnly && "cursor-default"
            )}
            aria-label={`Rate ${i + 1} star`}
          >
            {/* empty star */}
            <StarIcon
              filled={false}
              className={cn(SIZE_MAP[size], "text-yellow-500")}
            />

            {/* filled star (full or half) */}
            {fill > 0 && (
              <span
                className={cn(
                  "absolute inset-0 overflow-hidden",
                  SIZE_MAP[size]
                )}
                style={{ width: fill === 1 ? "100%" : "50%" }}
              >
                <StarIcon
                  filled
                  className={cn(SIZE_MAP[size], "text-yellow-500")}
                />
              </span>
            )}
          </button>
        );
      })}

      {showValue && (
        <span className="ml-2 text-sm text-muted-foreground">
          {value.toFixed(1)} / {max}
        </span>
      )}
    </div>
  );
}

export default StarRating;
