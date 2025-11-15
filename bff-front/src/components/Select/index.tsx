import * as React from "react";

import {
  Select as ShadcnSelect,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

type Item = {
  value: string;
  label: React.ReactNode;
  disabled?: boolean;
  // metadata libre si besoin
  [key: string]: any;
};

type Group = {
  label: string;
  items: Item[];
};

export type ISelectProps = {
  /** Liste plate d'items (utilisée si `groups` non fournis) */
  items?: Item[];
  /** Liste groupée (utilisée si fournie) */
  groups?: Group[];
  /** Valeur contrôlée */
  value?: string;
  /** Callback on change */
  onValueChange?: (value: string) => void;
  /** Placeholder affiché dans le trigger */
  placeholder?: string;
  /** largeur/ classes pour le trigger */
  triggerClassName?: string;
  /** classes pour SelectContent */
  contentClassName?: string;
  /** Permet de personnaliser le rendu d'un item (label, icones, etc.) */
  renderItem?: (item: Item) => React.ReactNode;
  /** Max height du content pour rendre scrollable */
  maxContentHeight?: string;
  disabled?: boolean;
} & Omit<React.ComponentProps<typeof ShadcnSelect>, "value" | "onValueChange">;

export function Select({
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
  ...rest
}: ISelectProps) {
  const hasGroups = Array.isArray(groups) && groups.length > 0;

  return (
    <ShadcnSelect value={value} onValueChange={onValueChange} {...rest}>
      <SelectTrigger className={triggerClassName} disabled={disabled}>
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>

      <SelectContent className={contentClassName}>
        <div className={maxContentHeight + " overflow-auto"}>
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
  );
}
