import React, { useId } from "react";
import { cn } from "@/common/lib/utils";

type Size = "sm" | "md" | "lg";

export type InputProps = Omit<
  React.ComponentPropsWithoutRef<"input">,
  "size"
> & {
  label?: React.ReactNode;
  helperText?: React.ReactNode;
  error?: React.ReactNode;
  loading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  size?: Size;
  inputClassName?: string;
  labelClassName?: string;
  wrapperClassName?: string;
  helperClassName?: string;
  errorClassName?: string;
  debounce?: number;
  format?: (raw: string) => string;
  onValueChange?: (payload: {
    formatted: string;
    raw: string;
    event: React.ChangeEvent<HTMLInputElement>;
  }) => void;
};

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  (props, forwardedRef) => {
    const {
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
      onValueChange,
      format,
      value,
      defaultValue,
      name,
      autoComplete,
      ...rest
    } = props;

    const internalRef = React.useRef<HTMLInputElement | null>(null);

    React.useImperativeHandle(
      forwardedRef,
      () => internalRef.current as HTMLInputElement,
      [],
    );

    const computeRaw = (display: string) => {
      try {
        return String(display).replace(/[^\p{L}\p{N}]/gu, "");
      } catch {
        return String(display).replace(/[^0-9A-Za-z]/g, "");
      }
    };

    const controlled = value !== undefined;

    const displayValue = controlled
      ? (() => {
          if (!format) return String(value ?? "");
          const raw = computeRaw(String(value ?? ""));
          return format(raw);
        })()
      : undefined;

    const debounceTimer = React.useRef<number | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const userInput = e.target.value ?? "";

      const raw = format ? computeRaw(userInput) : userInput;
      const formatted = format ? format(raw) : userInput;

      onValueChange?.({
        formatted,
        raw,
        event: e,
      });

      if (!debounce || !onChange) {
        onChange?.(e);
        return;
      }

      if (debounceTimer.current) clearTimeout(debounceTimer.current);

      debounceTimer.current = window.setTimeout(() => {
        onChange(e);
      }, debounce);
    };

    /* =============================
       Autofill detection
    ============================== */

    React.useEffect(() => {
      const el = internalRef.current;
      if (!el) return;

      const triggerChange = () => {
        if (!el.value) return;

        el.dispatchEvent(new Event("input", { bubbles: true }));
      };

      const timer = setTimeout(triggerChange, 200);

      const onAnimationStart = (e: AnimationEvent) => {
        if (e.animationName === "autofill-start") {
          triggerChange();
        }
      };

      el.addEventListener("animationstart", onAnimationStart);

      return () => {
        clearTimeout(timer);
        el.removeEventListener("animationstart", onAnimationStart);
      };
    }, []);

    const inputId = id ?? useId();

    const helperId = helperText ? `${inputId}-helper` : undefined;
    const errorId = error ? `${inputId}-error` : undefined;

    const describedBy =
      [errorId, helperId].filter(Boolean).join(" ") || undefined;

    const sizeClasses =
      size === "sm"
        ? "h-8 text-sm px-2"
        : size === "lg"
          ? "h-11 text-base px-4"
          : "h-9 text-sm px-3";

    const wouldBeDisabled = !!(disabled || loading);

    return (
      <div className={cn("flex flex-col", wrapperClassName)}>
        {label && (
          <label
            htmlFor={inputId}
            className={cn("mb-1 text-sm font-medium", labelClassName)}
          >
            {label}
          </label>
        )}

        <div
          className={cn(
            "relative flex items-center rounded-md border bg-transparent transition-shadow",
            error
              ? "border-destructive/80 focus-within:ring-destructive/30"
              : "border-input focus-within:ring-ring/30",
            wouldBeDisabled ? "opacity-60 pointer-events-none" : "opacity-100",
            size === "sm" ? "rounded-sm" : "rounded-md",
          )}
        >
          {leftIcon && (
            <span className="ml-2 mr-1 flex items-center text-muted-foreground">
              {leftIcon}
            </span>
          )}

          <input
            id={inputId}
            ref={internalRef}
            name={name}
            autoComplete={autoComplete}
            {...(controlled ? { value: displayValue } : { defaultValue })}
            className={cn(
              "flex-1 bg-transparent outline-none placeholder:text-muted-foreground",
              sizeClasses,
              leftIcon ? "pl-1" : "",
              rightIcon || loading ? "pr-10" : "",
              className,
            )}
            disabled={wouldBeDisabled}
            aria-invalid={!!error || undefined}
            aria-describedby={describedBy}
            onChange={handleChange}
            {...rest}
          />

          <div className="absolute right-2 inline-flex items-center">
            {loading ? (
              <SpinnerSmall />
            ) : rightIcon ? (
              <span className="flex items-center text-muted-foreground">
                {rightIcon}
              </span>
            ) : null}
          </div>
        </div>

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

Input.displayName = "Input";

function SpinnerSmall() {
  return (
    <svg
      className="animate-spin h-4 w-4 text-current"
      viewBox="0 0 24 24"
      fill="none"
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
