import * as React from "react";
import { cn } from "@/common/lib/utils";

type Size = "sm" | "md" | "lg";

export type TextareaProps = Omit<
  React.ComponentPropsWithoutRef<"textarea">,
  "size"
> & {
  label?: React.ReactNode;
  helperText?: React.ReactNode;
  error?: React.ReactNode;
  loading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  size?: Size;
  textareaClassName?: string;
  labelClassName?: string;
  wrapperClassName?: string;
  helperClassName?: string;
  errorClassName?: string;

  /** Debounce delay in ms */
  debounce?: number;
};

const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  (
    {
      id,
      label,
      helperText,
      error,
      loading = false,
      leftIcon,
      rightIcon,
      size = "md",
      labelClassName,
      wrapperClassName,
      helperClassName,
      errorClassName,
      disabled,
      className,

      debounce,
      onChange,
      ...props
    },
    ref,
  ) => {
    const wouldBeDisabled = !!(disabled || loading || props.readOnly);

    const textareaId = id ?? React.useId();
    const helperId = helperText ? `${textareaId}-helper` : undefined;
    const errorId = error ? `${textareaId}-error` : undefined;
    const describedBy =
      [errorId, helperId].filter(Boolean).join(" ") || undefined;

    const sizeClasses =
      size === "sm"
        ? "text-sm p-2"
        : size === "lg"
          ? "text-base p-4"
          : "text-sm p-3"; // md

    // --- Debounce logic ---
    const debounceTimer = React.useRef<number | NodeJS.Timeout | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
      if (!debounce || !onChange) {
        return onChange?.(e);
      }

      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }

      const eventCopy = { ...e };

      debounceTimer.current = setTimeout(() => {
        onChange(eventCopy as any);
      }, debounce);
    };

    return (
      <div className={cn("flex flex-col", wrapperClassName)}>
        {label && (
          <label
            htmlFor={textareaId}
            className={cn("mb-1 text-sm font-medium", labelClassName)}
          >
            {label}
          </label>
        )}

        <div
          className={cn(
            "relative flex w-full rounded-md border bg-transparent transition-shadow",
            error
              ? "border-destructive/80 focus-within:ring-destructive/30"
              : "border-input focus-within:ring-ring/30",
            wouldBeDisabled ? "opacity-60 pointer-events-none" : "opacity-100",
            "min-h-[80px]",
          )}
        >
          {/* Left icon */}
          {leftIcon && (
            <span className="absolute left-3 top-3 text-muted-foreground">
              {leftIcon}
            </span>
          )}

          <textarea
            id={textareaId}
            ref={ref}
            className={cn(
              "w-full bg-transparent outline-none resize-y placeholder:text-muted-foreground",
              sizeClasses,
              leftIcon ? "pl-10" : "",
              rightIcon || loading ? "pr-10" : "",
              className,
            )}
            disabled={wouldBeDisabled}
            aria-invalid={!!error || undefined}
            aria-describedby={describedBy}
            onChange={handleChange}
            {...props}
          />

          {/* Right area: loading spinner OR right icon */}
          <div className="absolute right-2 top-3 inline-flex items-center">
            {loading ? (
              <SpinnerSmall />
            ) : rightIcon ? (
              <span className="text-muted-foreground">{rightIcon}</span>
            ) : null}
          </div>
        </div>

        {/* Helper / Error */}
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
  },
);

Textarea.displayName = "Textarea";

export default Textarea;

function SpinnerSmall() {
  return (
    <svg
      className="animate-spin h-4 w-4 text-current"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <circle
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="3"
        strokeOpacity="0.25"
      />
      <path
        d="M22 12a10 10 0 00-10-10"
        stroke="currentColor"
        strokeWidth="3"
        strokeLinecap="round"
      />
    </svg>
  );
}
