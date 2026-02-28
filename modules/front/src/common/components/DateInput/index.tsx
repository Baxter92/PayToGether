import React from "react";
import { Calendar as CalendarIcon } from "lucide-react";
import { Popover, PopoverContent, PopoverTrigger } from "../ui/popover";
import { cn } from "@/common/lib/utils";
import { Input } from "../ui/input";
import { Calendar } from "../ui/calendar";

type Size = "sm" | "md" | "lg";

export type IDateInputProps = Omit<
  React.ComponentPropsWithoutRef<typeof Input>,
  "onChange" | "value" | "type" | "min" | "max"
> & {
  value?: Date | string | null;
  onChange?: (date: Date | null) => void;
  min?: Date;
  max?: Date;
  size?: Size;
  dateFormat?: Intl.DateTimeFormatOptions; // optional formatting options
};

function formatDateValue(
  value?: Date | string | null,
  opts?: Intl.DateTimeFormatOptions,
) {
  if (!value) return "";

  let d: Date;
  if (value instanceof Date) {
    d = value;
  } else if (typeof value === "string") {
    // Handle ISO strings or other formats
    d = new Date(value);
  } else {
    return "";
  }

  if (Number.isNaN(d.getTime())) return "";

  return new Intl.DateTimeFormat(
    "fr-FR",
    opts ?? { year: "numeric", month: "2-digit", day: "2-digit" },
  ).format(d);
}

const DateInput = React.forwardRef<HTMLInputElement, IDateInputProps>(
  (
    {
      value,
      onChange,
      min,
      max,
      label,
      helperText,
      error,
      size = "md",
      dateFormat,
      // ...props
    },
    ref,
  ) => {
    const [open, setOpen] = React.useState(false);

    const selectedDate = React.useMemo(() => {
      if (!value) return null;

      if (value instanceof Date) {
        return Number.isNaN(value.getTime()) ? null : value;
      }

      if (typeof value === "string") {
        // Tentative de parsing (ISO ou autre format valide)
        const d = new Date(value);
        if (!Number.isNaN(d.getTime())) return d;

        // Si c'est notre format display "jj/mm/aaaa", on ne veut pas l'interpréter
        // Cela évite que l'input s'efface si Input.tsx déclenche un onChange par erreur
        if (value.match(/^\d{2}\/\d{2}\/\d{4}$/)) return null;
      }

      return null;
    }, [value]);

    const displayString = React.useMemo(() => {
      return formatDateValue(selectedDate, dateFormat);
    }, [selectedDate, dateFormat]);

    return (
      <Popover open={open} onOpenChange={setOpen}>
        <div className="w-full">
          {label && (
            <label className={cn("mb-1 text-sm font-medium")}>{label}</label>
          )}
          <PopoverTrigger asChild>
            <div className="w-full">
              <Input
                ref={ref}
                readOnly
                rightIcon={<CalendarIcon className="w-4 h-4" />}
                error={error}
                helperText={helperText}
                size={size}
                // {...(props as any)}
                value={displayString}
                onClick={() => setOpen(true)}
                placeholder="jj/mm/aaaa"
              />
            </div>
          </PopoverTrigger>

          <PopoverContent
            side="bottom"
            align="start"
            className="w-auto p-0"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="p-2">
              <Calendar
                mode="single"
                selected={selectedDate ?? undefined}
                onSelect={(d) => {
                  onChange?.(d ?? null);
                  setOpen(false);
                }}
                disabled={(date) => {
                  if (min && date < min) return true;
                  if (max && date > max) return true;
                  return false;
                }}
              />
            </div>
          </PopoverContent>
        </div>
      </Popover>
    );
  },
);

export default DateInput;

DateInput.displayName = "DateInput";
