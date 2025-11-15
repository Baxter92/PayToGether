import * as React from "react";
import { cn } from "@/lib/utils";

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
};

/**
 * Input component with label, helper, error, icons and loading state.
 */
export const Input = React.forwardRef<HTMLInputElement, InputProps>(
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
      ...props
    },
    ref
  ) => {
    const wouldBeDisabled = !!(disabled || loading || props.readOnly);

    const inputId = id ?? React.useId();
    const helperId = helperText ? `${inputId}-helper` : undefined;
    const errorId = error ? `${inputId}-error` : undefined;
    const describedBy =
      [errorId, helperId].filter(Boolean).join(" ") || undefined;

    const sizeClasses =
      size === "sm"
        ? "h-8 text-sm px-2"
        : size === "lg"
        ? "h-11 text-base px-4"
        : "h-9 text-sm px-3"; // md

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
            // border color depends on error state
            error
              ? "border-destructive/80 focus-within:ring-destructive/30"
              : "border-input focus-within:ring-ring/30",
            wouldBeDisabled ? "opacity-60 pointer-events-none" : "opacity-100",
            size === "sm" ? "rounded-sm" : "rounded-md"
          )}
        >
          {/* Left icon */}
          {leftIcon ? (
            <span className="ml-2 mr-1 flex items-center text-muted-foreground">
              {leftIcon}
            </span>
          ) : null}

          {/* Input */}
          <input
            id={inputId}
            ref={ref}
            className={cn(
              "flex-1 bg-transparent outline-none placeholder:text-muted-foreground",
              sizeClasses,
              // ensure padding-left if leftIcon present, padding-right if rightIcon/loading present
              leftIcon ? "pl-1" : "",
              rightIcon || loading ? "pr-10" : "",
              className
            )}
            disabled={wouldBeDisabled}
            aria-invalid={!!error || undefined}
            aria-describedby={describedBy}
            {...props}
          />

          {/* Right area: loading spinner OR rightIcon */}
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
);

Input.displayName = "Input";

/* Small spinner used for loading */
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
