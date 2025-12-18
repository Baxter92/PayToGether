import React from "react";
import { Clock } from "lucide-react";
import { Input } from "../ui/input";

export type ITimeInputProps = Omit<
  React.ComponentPropsWithoutRef<typeof Input>,
  "onChange" | "value" | "type"
> & {
  value?: string | null; // "HH:MM"
  onChange?: (time: string | null) => void;
  step?: number; // minutes, e.g. 5
  size?: "sm" | "md" | "lg";
};

function normalizeTime(val?: string | null) {
  if (!val) return "";
  // accept "HH:MM" or Date string — try to coerce
  if (val.includes(":")) return val;
  const d = new Date(val);
  if (Number.isNaN(d.getTime())) return "";
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  return `${hh}:${mm}`;
}

const TimeInput = React.forwardRef<HTMLInputElement, ITimeInputProps>(
  (
    {
      value,
      onChange,
      step = 15,
      label,
      helperText,
      error,
      size = "md",
      ...props
    },
    ref
  ) => {
    const formatted = normalizeTime(value);

    return (
      <div className="flex flex-col">
        {label && <label className="mb-1 text-sm font-medium">{label}</label>}
        <div>
          <Input
            ref={ref}
            type="time"
            value={formatted}
            onChange={(e) => onChange?.(e.target.value || null)}
            step={String(step * 60)}
            rightIcon={<Clock className="w-4 h-4" />}
            error={error}
            helperText={helperText}
            size={size}
            {...(props as any)}
          />
        </div>
      </div>
    );
  }
);

export default TimeInput;

TimeInput.displayName = "TimeInput";
