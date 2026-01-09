import React from "react";
import DateInput from "../DateInput";
import TimeInput from "../TimeInput";

export type IDateTimeInputProps = {
  value?: Date | string | null;
  onChange?: (dt: Date | null) => void;
  dateProps?: React.ComponentProps<typeof DateInput>;
  timeProps?: React.ComponentProps<typeof TimeInput>;
  label?: React.ReactNode;
  helperText?: React.ReactNode;
  error?: React.ReactNode;
  disabled?: boolean;
};

function toDate(value?: Date | string | null) {
  if (!value) return null;
  const d = typeof value === "string" ? new Date(value) : value;
  return Number.isNaN(d.getTime()) ? null : d;
}

const DateTimeInput: React.FC<IDateTimeInputProps> = ({
  value,
  onChange,
  dateProps,
  timeProps,
  label,
  helperText,
  disabled,
  error,
}) => {
  const dt = toDate(value);
  const [localDate, setLocalDate] = React.useState<Date | null>(
    dt ? new Date(dt) : null
  );
  const [localTime, setLocalTime] = React.useState<string | null>(() => {
    if (!dt) return null;
    const hh = String(dt.getHours()).padStart(2, "0");
    const mm = String(dt.getMinutes()).padStart(2, "0");
    return `${hh}:${mm}`;
  });

  React.useEffect(() => {
    // sync when parent value changes
    const d = toDate(value);
    setLocalDate(d ? new Date(d) : null);
    if (d) {
      setLocalTime(
        `${String(d.getHours()).padStart(2, "0")}:${String(
          d.getMinutes()
        ).padStart(2, "0")}`
      );
    }
  }, [value]);

  const emitChange = React.useCallback(
    (date: Date | null, time: string | null) => {
      if (!date) {
        onChange?.(null);
        return;
      }
      const [hh = "00", mm = "00"] = (time ?? "00:00").split(":");
      const newD = new Date(date);
      newD.setHours(Number(hh), Number(mm), 0, 0);
      onChange?.(newD);
    },
    [onChange]
  );

  return (
    <div className="flex flex-col">
      {label && <label className="mb-1 text-sm font-medium">{label}</label>}
      <div className="flex gap-2">
        <div className="flex-1">
          <DateInput
            {...(dateProps as any)}
            value={localDate}
            disabled={disabled || dateProps?.disabled}
            onChange={(d) => {
              setLocalDate(d);
              emitChange(d, localTime);
            }}
          />
        </div>
        <div style={{ width: 140 }}>
          <TimeInput
            {...(timeProps as any)}
            value={localTime}
            disabled={disabled || timeProps?.disabled}
            onChange={(t) => {
              setLocalTime(t);
              emitChange(localDate, t);
            }}
          />
        </div>
      </div>

      {error ? (
        <p className="text-xs text-destructive mt-1">{error}</p>
      ) : helperText ? (
        <p className="text-xs text-muted-foreground mt-1">{helperText}</p>
      ) : null}
    </div>
  );
};

export default DateTimeInput;
