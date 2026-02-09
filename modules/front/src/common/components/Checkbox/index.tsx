import * as React from "react";
import { cn } from "@/common/lib/utils";
import { Loader2 } from "lucide-react";

export type ICheckboxProps = Omit<
  React.InputHTMLAttributes<HTMLInputElement>,
  "checked"
> & {
  label?: string;
  description?: string;
  error?: string;
  loading?: boolean;
  onCheckedChange?: (value: boolean) => void;
  icon?: React.ReactNode;
  position?: "left" | "right";
  size?: "sm" | "md" | "lg";

  // ⬇️ Ici : checked supporte 3 états
  checked?: boolean | "indeterminate";
};

export default function Checkbox({
  label,
  description,
  error,
  loading,
  disabled,
  icon,
  className,
  position = "right",
  onCheckedChange,
  checked = false,
  ...props
}: ICheckboxProps): React.JSX.Element {
  const ref = React.useRef<HTMLInputElement>(null);

  // Gérer automatiquement l’état indéterminé
  React.useEffect(() => {
    if (!ref.current) return;

    ref.current.indeterminate = checked === "indeterminate";
  }, [checked]);

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
          <input
            ref={ref}
            type="checkbox"
            disabled={disabled || loading}
            className={cn(
              "h-4 w-4 rounded-sm border-input text-primary focus-visible:ring-[3px]",
              "align-middle leading-none",
              "focus-visible:ring-ring/50 focus-visible:border-ring",
              "disabled:opacity-50 disabled:cursor-not-allowed",
              error && "border-destructive",
              className
            )}
            checked={checked === true}
            {...props}
            onChange={(e) => {
              onCheckedChange?.(e.target.checked);
              props.onChange?.(e);
            }}
          />

          {loading && (
            <Loader2 className="absolute inset-0 m-auto h-3 w-3 animate-spin text-primary" />
          )}
        </div>

        {icon && <span className="text-muted-foreground">{icon}</span>}

        {position === "right" && label && (
          <span className="text-sm font-medium">{label}</span>
        )}
      </label>

      {description && (
        <p className="text-xs text-muted-foreground">{description}</p>
      )}

      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  );
}
