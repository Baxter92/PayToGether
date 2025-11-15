import * as React from "react";
import { cn } from "@/lib/utils";
import { Loader2 } from "lucide-react";

// Reprend la base du Input mais adapté au type radio
export type IRadioProps = React.InputHTMLAttributes<HTMLInputElement> & {
  label?: string;
  description?: string;
  error?: string;
  loading?: boolean;
  icon?: React.ReactNode; // Icône à gauche ou droite (optionnelle)
  position?: "left" | "right"; // Position du label
  size?: "sm" | "md" | "lg";
};

export function Radio({
  label,
  description,
  error,
  loading,
  disabled,
  icon,
  className,
  position = "right",
  ...props
}: IRadioProps) {
  return (
    <div className="flex flex-col space-y-1">
      <label
        className={cn(
          "flex items-center gap-2 cursor-pointer",
          disabled && "opacity-50 cursor-not-allowed"
        )}
      >
        {position === "left" && label && (
          <span className="text-sm font-medium">{label}</span>
        )}

        <div className="relative">
          {/* Le radio lui-même */}
          <input
            type="radio"
            disabled={disabled || loading}
            className={cn(
              "h-4 w-4 rounded-full border-input text-primary focus-visible:ring-[3px]",
              "align-middle leading-none",
              "focus-visible:ring-ring/50 focus-visible:border-ring",
              "disabled:opacity-50 disabled:cursor-not-allowed",
              error && "border-destructive",
              className
            )}
            {...props}
          />

          {/* Icône loading si activé */}
          {loading && (
            <Loader2 className="absolute inset-0 m-auto h-3 w-3 animate-spin text-primary" />
          )}
        </div>

        {icon && <span className="text-muted-foreground">{icon}</span>}

        {position === "right" && label && (
          <span className="text-sm font-medium">{label}</span>
        )}
      </label>

      {/* Description */}
      {description && (
        <p className="text-xs text-muted-foreground">{description}</p>
      )}

      {/* Erreur */}
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  );
}
