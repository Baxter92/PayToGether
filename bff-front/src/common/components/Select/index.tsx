import * as React from "react";

import {
  Select as ShadcnSelect,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from "@components/ui/select";

import { cn } from "@/common/lib/utils"; // adapte le path si besoin

export type ISelectOption = {
  value: string;
  label: React.ReactNode;
  disabled?: boolean;
  [key: string]: any;
};

type Group = {
  label: string;
  items: ISelectOption[];
};

export type ISelectProps = {
  items?: ISelectOption[];
  groups?: Group[];
  value?: string;
  onValueChange?: (value: string) => void;
  placeholder?: string;
  triggerClassName?: string;
  contentClassName?: string;
  renderItem?: (item: ISelectOption) => React.ReactNode;
  maxContentHeight?: string;
  disabled?: boolean;

  /** --- Nouveautés pour la gestion d'erreur --- */
  label?: React.ReactNode;
  helperText?: React.ReactNode;
  error?: React.ReactNode;
  errorClassName?: string;
  helperClassName?: string;
  wrapperClassName?: string;
} & Omit<React.ComponentProps<typeof ShadcnSelect>, "value" | "onValueChange">;

export default function Select({
  items = [],
  groups,
  value,
  onValueChange,
  placeholder = "Select an option",
  triggerClassName,
  contentClassName,
  renderItem,
  maxContentHeight = "max-h-[240px]",
  disabled = false,
  label,
  helperText,
  error,
  errorClassName,
  helperClassName,
  wrapperClassName,
  ...rest
}: ISelectProps) {
  const hasGroups = Array.isArray(groups) && groups.length > 0;

  const selectId = React.useId();
  const helperId = helperText ? `${selectId}-helper` : undefined;
  const errorId = error ? `${selectId}-error` : undefined;
  const describedBy =
    [errorId, helperId].filter(Boolean).join(" ") || undefined;

  // visual class when error exists
  const triggerErrorClass = error
    ? "border-destructive/80 focus-within:ring-destructive/30"
    : "border-input focus-within:ring-ring/30";

  return (
    <div className={cn("flex flex-col", wrapperClassName)}>
      {label ? (
        <label className="mb-1 text-sm font-medium">{label}</label>
      ) : null}

      <ShadcnSelect value={value} onValueChange={onValueChange} {...rest}>
        <SelectTrigger
          className={cn(
            "relative rounded-md border bg-transparent",
            triggerErrorClass,
            disabled ? "opacity-60 pointer-events-none" : "opacity-100",
            triggerClassName
          )}
          disabled={disabled}
          aria-invalid={!!error || undefined}
          aria-describedby={describedBy}
        >
          <SelectValue placeholder={placeholder} />
        </SelectTrigger>

        <SelectContent className={contentClassName}>
          <div className={cn(maxContentHeight, "overflow-auto")}>
            <SelectItem disabled>{placeholder}</SelectItem>
            {hasGroups
              ? groups!.map((g) => (
                  <SelectGroup key={g.label}>
                    <SelectLabel>{g.label}</SelectLabel>
                    {g.items.map((it) => (
                      <SelectItem
                        key={it.value}
                        value={it.value}
                        disabled={it.disabled}
                      >
                        {renderItem ? renderItem(it) : it.label}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                ))
              : items.map((it) => (
                  <SelectItem
                    key={it.value}
                    value={it.value}
                    disabled={it.disabled}
                  >
                    {renderItem ? renderItem(it) : it.label}
                  </SelectItem>
                ))}
          </div>
        </SelectContent>
      </ShadcnSelect>

      {/* helper / error */}
      {error || helperText ? (
        <div className="mt-1 min-h-[1rem]">
          {error ? (
            <p
              id={errorId}
              className={cn("text-xs text-destructive", errorClassName)}
            >
              {error}
            </p>
          ) : helperText ? (
            <p
              id={helperId}
              className={cn("text-xs text-muted-foreground", helperClassName)}
            >
              {helperText}
            </p>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
