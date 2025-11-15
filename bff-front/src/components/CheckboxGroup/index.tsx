import * as React from "react";
import { cn } from "@/lib/utils";
import { Checkbox, type ICheckboxProps } from "../Checkbox";

export type CheckboxItem = Omit<
  ICheckboxProps,
  "name" | "checked" | "onChange"
> & {
  value: string;
};

export type ICheckboxGroupProps = {
  items: CheckboxItem[];
  value?: string;
  defaultValue?: string;
  onChange?: (value: string) => void;
  name?: string;
  groupLabel?: React.ReactNode;
  orientation?: "vertical" | "horizontal";
  loading?: boolean;
  disabled?: boolean;
  className?: string;
  checkboxSize?: "sm" | "md" | "lg";
};

export function CheckboxGroup({
  items,
  value,
  defaultValue,
  onChange,
  name,
  groupLabel,
  orientation = "vertical",
  loading = false,
  disabled = false,
  className,
  checkboxSize = "md",
}: ICheckboxGroupProps) {
  const generatedName = React.useId();
  const groupName = name ?? `checkbox-group-${generatedName}`;

  const [internal, setInternal] = React.useState(defaultValue);

  const currentValue = value !== undefined ? value : internal;

  const _size =
    checkboxSize === "sm"
      ? "h-4 w-4"
      : checkboxSize === "lg"
      ? "h-6 w-6"
      : "h-5 w-5";

  const handleSelect = (v: string) => {
    if (value === undefined) setInternal(v);
    onChange?.(v);
  };

  return (
    <div className={cn("flex flex-col", className)}>
      {groupLabel && (
        <div className="mb-2 text-sm font-medium">{groupLabel}</div>
      )}

      <div
        className={cn(
          "flex",
          orientation === "vertical"
            ? "flex-col space-y-2"
            : "flex-row gap-4 flex-wrap"
        )}
      >
        {items.map((item) => (
          <Checkbox
            key={item.value}
            {...item}
            name={groupName}
            className={cn(_size, item.className)}
            disabled={disabled || item.disabled}
            loading={loading}
            checked={currentValue === item.value}
            onChange={(v) => handleSelect(v.target.value)}
          />
        ))}
      </div>
    </div>
  );
}
