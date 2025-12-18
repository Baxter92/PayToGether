import * as React from "react";
import { cn } from "@/common/lib/utils";
import Radio, { type IRadioProps } from "../Radio";

export type RadioItem = Omit<IRadioProps, "name" | "checked" | "onChange"> & {
  value: string;
};

export type IRadioGroupProps = {
  items: RadioItem[];
  value?: string;
  defaultValue?: string;
  onChange?: (value: string) => void;
  name?: string;
  groupLabel?: React.ReactNode;
  orientation?: "vertical" | "horizontal";
  loading?: boolean;
  disabled?: boolean;
  className?: string;

  /** size for each radio control */
  radioSize?: "sm" | "md" | "lg";

  /** --- new props for error/helper like Input/Select --- */
  error?: React.ReactNode;
  helperText?: React.ReactNode;
  errorClassName?: string;
  helperClassName?: string;
  wrapperClassName?: string;
};

export default function RadioGroup({
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
  radioSize = "md",
  // new props
  error,
  helperText,
  errorClassName,
  helperClassName,
  wrapperClassName,
}: IRadioGroupProps) {
  const generatedName = React.useId();
  const groupName = name ?? `radio-group-${generatedName}`;

  const [internal, setInternal] = React.useState<string | undefined>(
    defaultValue
  );

  const currentValue = value !== undefined ? value : internal;

  const _size =
    radioSize === "sm" ? "h-4 w-4" : radioSize === "lg" ? "h-6 w-6" : "h-5 w-5";

  const handleSelect = (v: string) => {
    if (value === undefined) setInternal(v);
    onChange?.(v);
  };

  const helperId = helperText ? `${groupName}-helper` : undefined;
  const errorId = error ? `${groupName}-error` : undefined;
  const describedBy =
    [errorId, helperId].filter(Boolean).join(" ") || undefined;

  return (
    <div className={cn("flex flex-col", wrapperClassName)}>
      {groupLabel && (
        <div className="mb-2 text-sm font-medium">{groupLabel}</div>
      )}

      <div
        className={cn(
          "flex",
          orientation === "vertical"
            ? "flex-col space-y-2"
            : "flex-row gap-4 flex-wrap",
          className
        )}
        role="radiogroup"
        aria-invalid={!!error || undefined}
        aria-describedby={describedBy}
      >
        {items.map((item) => (
          <Radio
            key={item.value}
            {...item}
            name={groupName}
            className={cn(_size, item.className)}
            disabled={disabled || item.disabled}
            loading={loading}
            checked={currentValue === item.value}
            onChange={(ev) =>
              handleSelect((ev.target as HTMLInputElement).value)
            }
            // accessibility: ensure each radio references the same describedBy
            aria-describedby={describedBy}
            aria-invalid={!!error || undefined}
            // pass group-level error to each radio as well, if your Radio supports it
            error={error ? true : undefined}
          />
        ))}
      </div>

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
