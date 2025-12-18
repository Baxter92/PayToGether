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
  opts?: Intl.DateTimeFormatOptions
) {
  if (!value) return "";
  const d = typeof value === "string" ? new Date(value) : value;
  if (Number.isNaN(d.getTime())) return "";
  return new Intl.DateTimeFormat(
    "fr-FR",
    opts ?? { year: "numeric", month: "2-digit", day: "2-digit" }
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
      ...props
    },
    ref
  ) => {
    const [open, setOpen] = React.useState(false);
    const selectedDate = React.useMemo(() => {
      if (!value) return null;
      const d = typeof value === "string" ? new Date(value) : value;
      return Number.isNaN(d.getTime()) ? null : d;
    }, [value]);

    return (
      <Popover open={open} onOpenChange={setOpen}>
        <div>
          {label && (
            <label className={cn("mb-1 text-sm font-medium")}>{label}</label>
          )}
          <PopoverTrigger asChild>
            <div>
              <Input
                ref={ref}
                readOnly
                value={formatDateValue(selectedDate, dateFormat)}
                onClick={() => setOpen(true)}
                placeholder="jj/mm/aaaa"
                rightIcon={<CalendarIcon className="w-4 h-4" />}
                error={error}
                helperText={helperText}
                size={size}
                {...(props as any)}
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
  }
);

export default DateInput;

DateInput.displayName = "DateInput";
