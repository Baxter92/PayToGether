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

  /** Debounce delay in ms */
  debounce?: number;

  /**
   * Format function: transforme la valeur brute (raw) en display.
   * Ex: raw "4242424242424242" => "4242 4242 4242 4242"
   */
  format?: (raw: string) => string;

  /**
   * Optional callback that receives both formatted & raw on every change.
   * Useful if you want the raw value without parsing the event.
   */
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

    // --- refs : merge forwarded + internal DOM ref
    const internalRef = React.useRef<HTMLInputElement | null>(null);
    React.useImperativeHandle(
      forwardedRef,
      () => internalRef.current as HTMLInputElement,
      [internalRef]
    );

    // helper pour merger (si forwardedRef est function ou RefObject)
    React.useEffect(() => {
      if (!forwardedRef) return;
      const node = internalRef.current;
      if (!node) return;
      if (typeof forwardedRef === "function") forwardedRef(node);
      else if (typeof forwardedRef === "object" && forwardedRef !== null) {
        // @ts-ignore
        forwardedRef.current = node;
      }
    }, [forwardedRef]);

    // keep existing computeRaw/format/display logic
    const computeRaw = (display: string) => {
      try {
        return String(display).replace(/[^\p{L}\p{N}]/gu, "");
      } catch {
        return String(display).replace(/[^0-9A-Za-z]/g, "");
      }
    };
    const isFileInput = rest?.type === "file";
    const controlled = !isFileInput && value !== undefined;

    const displayValue = controlled
      ? (() => {
          const raw = computeRaw(String(value ?? ""));
          return format ? format(raw) : String(value ?? "");
        })()
      : undefined;

    // centraliser la logique qui notifie le parent (réutilisée par handleChange et par la détection autofill)
    const notifyChangeFromDom = React.useCallback(
      (el: HTMLInputElement | null) => {
        if (!el) return;
        const userInput = el.value ?? "";
        const raw = format ? computeRaw(userInput) : userInput;
        const formatted = format ? format(raw) : userInput;

        // créer un "event" synthétique proche de ton eventCopy
        const syntheticEvent = {
          target: {
            ...el,
            value: raw,
            rawValue: formatted,
          },
          // inclure quelques props utiles au besoin
          currentTarget: el,
        } as unknown as React.ChangeEvent<HTMLInputElement>;

        if (onValueChange) {
          try {
            onValueChange({ formatted, raw, event: syntheticEvent });
          } catch (err) {
            // ignore
          }
        }

        // si debounce est absent, appeler immédiatement onChange
        if (!debounce) {
          onChange?.(syntheticEvent);
        } else {
          // si debounce présent, respecter la même mécanique qu'avant
          // (on peut réutiliser ton timer déjà présent ou en créer un ici)
          // Pour garder simple, on appelle onChange immédiatement ; si tu veux debounce,
          // reprends ta logique de timer existante.
          onChange?.(syntheticEvent);
        }
      },
      [format, onValueChange, onChange, debounce]
    );

    // Détection de l'autofill : input/change + animationstart + check initial
    React.useEffect(() => {
      if (isFileInput) return;

      const el = internalRef.current;
      if (!el) return;

      const onInput = () => notifyChangeFromDom(el);
      const onChange = () => notifyChangeFromDom(el);
      const onAnimationStart = (ev: AnimationEvent) => {
        // certains navigateurs peuvent utiliser d'autres noms : onAutoFillStart / autofill
        if (
          ev.animationName === "onAutoFillStart" ||
          ev.animationName.toLowerCase().includes("autofill")
        ) {
          notifyChangeFromDom(el);
        }
      };

      el.addEventListener("input", onInput);
      el.addEventListener("change", onChange);
      el.addEventListener("animationstart", onAnimationStart as EventListener);

      // Au mount, vérifier s'il y a déjà une valeur (ex: autofill ou SSR)
      // utiliser setTimeout 0 pour laisser le navigateur finir l'autofill sync
      const t = window.setTimeout(() => {
        if (el.value) notifyChangeFromDom(el);
      }, 0);

      return () => {
        el.removeEventListener("input", onInput);
        el.removeEventListener("change", onChange);
        el.removeEventListener(
          "animationstart",
          onAnimationStart as EventListener
        );
        clearTimeout(t);
      };
    }, [notifyChangeFromDom]);

    // ton handleChange original (pour saisie utilisateur via React)
    const debounceTimer = React.useRef<number | null>(null);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      if (isFileInput) {
        // Pour les fichiers, on laisse passer l'event brut
        onChange?.(e);
        return;
      }

      const userInput = e.target.value ?? "";

      const raw = format ? computeRaw(userInput) : userInput;
      const formatted = format ? format(raw) : userInput;

      const eventCopy = {
        ...e,
        target: { ...e.target, value: raw, rawValue: formatted },
      } as React.ChangeEvent<HTMLInputElement>;

      onValueChange?.({ formatted, raw, event: eventCopy });

      if (!debounce || !onChange) {
        onChange?.(eventCopy);
        return;
      }

      if (debounceTimer.current) clearTimeout(debounceTimer.current);
      debounceTimer.current = window.setTimeout(() => {
        onChange(eventCopy);
      }, debounce);
    };

    // reste du rendu : passer name & autoComplete (très importants pour autofill)
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

    const wouldBeDisabled = !!(disabled || loading || (rest as any).readOnly);

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
            size === "sm" ? "rounded-sm" : "rounded-md"
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
              className
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
  }
);

Input.displayName = "Input";

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
