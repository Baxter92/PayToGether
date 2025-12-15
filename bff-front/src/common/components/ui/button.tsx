import * as React from "react";
import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@/common/lib/utils";
import { Tooltip, TooltipContent, TooltipTrigger } from "./tooltip";
import { useNavigate, type NavigateOptions } from "react-router-dom";

const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-sm text-sm font-medium transition-all disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4 shrink-0 [&_svg]:shrink-0 outline-none focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:ring-[3px] aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive cursor-pointer",
  {
    variants: {
      variant: {
        default: "",
        destructive: "",
        outline: "",
        secondary: "",
        ghost: "",
        link: "",
        square: "",
        "round-full": "",
        "square-outline": "",
        "round-full-outline": "",
      },
      size: {
        default: "h-9 px-4 py-2 has-[>svg]:px-3",
        sm: "h-8 rounded-md gap-1.5 px-3 has-[>svg]:px-2.5",
        lg: "h-10 rounded-md px-6 has-[>svg]:px-4",
        icon: "size-9",
        "icon-sm": "size-8",
        "icon-lg": "size-10",
      },
      colorScheme: {
        default: "",
        danger: "",
        info: "",
        warning: "",
        success: "",
        secondary: "",
      },
    },

    compoundVariants: [
      // ---- DEFAULT (solid) ----
      {
        variant: "default",
        colorScheme: "default",
        class: "bg-primary text-primary-foreground hover:bg-primary/90",
      },
      {
        variant: "default",
        colorScheme: "danger",
        class:
          "bg-destructive text-white hover:bg-destructive/90 dark:bg-destructive/60",
      },
      {
        variant: "default",
        colorScheme: "info",
        class: "bg-blue-500 text-blue-foreground hover:bg-blue-500/90",
      },
      {
        variant: "default",
        colorScheme: "warning",
        class: "bg-amber-500 text-amber-foreground hover:bg-amber-500/90",
      },
      {
        variant: "default",
        colorScheme: "success",
        class: "bg-green-500 text-green-foreground hover:bg-green-500/90",
      },

      // ---- SECONDARY (solid) ----
      {
        variant: "secondary",
        colorScheme: "default",
        class: "bg-secondary text-secondary-foreground hover:bg-secondary/90",
      },
      {
        variant: "secondary",
        colorScheme: "danger",
        class:
          "bg-destructive text-white hover:bg-destructive/90 dark:bg-destructive/60",
      },
      {
        variant: "secondary",
        colorScheme: "info",
        class: "bg-blue-500 text-blue-foreground hover:bg-blue-500/90",
      },
      {
        variant: "secondary",
        colorScheme: "warning",
        class: "bg-amber-500 text-amber-foreground hover:bg-amber-500/90",
      },
      {
        variant: "secondary",
        colorScheme: "success",
        class: "bg-green-500 text-green-foreground hover:bg-green-500/90",
      },

      // ---- OUTLINE (regular) ----
      {
        variant: "outline",
        colorScheme: "default",
        class:
          "border bg-background shadow-xs hover:bg-secondary dark:bg-input/30 dark:border-input",
      },
      {
        variant: "outline",
        colorScheme: "danger",
        class:
          "border border-destructive text-destructive hover:bg-destructive/10",
      },
      {
        variant: "outline",
        colorScheme: "info",
        class: "border border-blue-500 text-blue-600 hover:bg-blue-50",
      },
      {
        variant: "outline",
        colorScheme: "success",
        class: "border border-green-500 text-green-600 hover:bg-green-50",
      },
      {
        variant: "outline",
        colorScheme: "warning",
        class: "border border-amber-500 text-amber-600 hover:bg-amber-50",
      },
      {
        variant: "outline",
        colorScheme: "secondary",
        class:
          "border border-secondary-foreground text-secondary-foreground hover:bg-secondary/50",
      },

      // ---- GHOST (regular) ----
      {
        variant: "ghost",
        colorScheme: "default",
        class: "hover:bg-secondary dark:hover:bg-secondary/50",
      },
      {
        variant: "ghost",
        colorScheme: "danger",
        class: "text-destructive hover:bg-destructive/10",
      },
      {
        variant: "ghost",
        colorScheme: "info",
        class: "text-blue-600 hover:bg-blue-50",
      },
      {
        variant: "ghost",
        colorScheme: "success",
        class: "text-green-600 hover:bg-green-50",
      },
      {
        variant: "ghost",
        colorScheme: "warning",
        class: "text-amber-600 hover:bg-amber-50",
      },
      {
        variant: "ghost",
        colorScheme: "secondary",
        class: "text-secondary-foreground hover:bg-secondary/50",
      },

      // ---- SQUARE (solid) ----
      {
        variant: "square",
        colorScheme: "default",
        class:
          "bg-primary text-primary-foreground hover:bg-primary/90 rounded-none",
      },
      {
        variant: "square",
        colorScheme: "danger",
        class:
          "bg-destructive text-white hover:bg-destructive/90 dark:bg-destructive/60 rounded-none",
      },
      {
        variant: "square",
        colorScheme: "info",
        class:
          "bg-blue-500 text-blue-foreground hover:bg-blue-500/90  rounded-none",
      },
      {
        variant: "square",
        colorScheme: "warning",
        class:
          "bg-amber-500 text-amber-foreground hover:bg-amber-500/90  rounded-none",
      },
      {
        variant: "square",
        colorScheme: "success",
        class:
          "bg-green-500 text-green-foreground hover:bg-green-500/90  rounded-none",
      },
      {
        variant: "square",
        colorScheme: "secondary",
        class:
          "bg-secondary-foreground text-secondary-foreground hover:bg-secondary/90  rounded-none",
      },

      // ---- ROUND-FULL (solid) ----
      {
        variant: "round-full",
        colorScheme: "default",
        class:
          "bg-primary text-primary-foreground hover:bg-primary/90 rounded-full",
      },
      {
        variant: "round-full",
        colorScheme: "danger",
        class:
          "bg-destructive text-white hover:bg-destructive/90 dark:bg-destructive/60 rounded-full",
      },
      {
        variant: "round-full",
        colorScheme: "info",
        class:
          "bg-blue-500 text-blue-foreground hover:bg-blue-500/90 rounded-full",
      },
      {
        variant: "round-full",
        colorScheme: "warning",
        class:
          "bg-amber-500 text-amber-foreground hover:bg-amber-500/90 rounded-full",
      },
      {
        variant: "round-full",
        colorScheme: "success",
        class:
          "bg-green-500 text-green-foreground hover:bg-green-500/90 rounded-full",
      },
      {
        variant: "round-full",
        colorScheme: "secondary",
        class:
          "bg-secondary-foreground text-secondary-foreground hover:bg-secondary/90 rounded-full",
      },

      // ---- SQUARE-OUTLINE (nouveau) ----
      {
        variant: "square-outline",
        colorScheme: "default",
        class:
          "border bg-background shadow-xs hover:bg-secondary dark:bg-input/30 dark:border-input rounded-none",
      },
      {
        variant: "square-outline",
        colorScheme: "danger",
        class:
          "border border-destructive text-destructive hover:bg-destructive/10 rounded-none",
      },
      {
        variant: "square-outline",
        colorScheme: "info",
        class:
          "border border-blue-500 text-blue-600 hover:bg-blue-50 rounded-none",
      },
      {
        variant: "square-outline",
        colorScheme: "success",
        class:
          "border border-green-500 text-green-600 hover:bg-green-50 rounded-none",
      },
      {
        variant: "square-outline",
        colorScheme: "warning",
        class:
          "border border-amber-500 text-amber-600 hover:bg-amber-50 rounded-none",
      },
      {
        variant: "square-outline",
        colorScheme: "secondary",
        class:
          "border border-secondary-foreground text-secondary-foreground hover:bg-secondary/50 rounded-none",
      },

      // ---- ROUND-FULL-OUTLINE (nouveau) ----
      {
        variant: "round-full-outline",
        colorScheme: "default",
        class:
          "border bg-background shadow-xs hover:bg-secondary dark:bg-input/30 dark:border-input rounded-full",
      },
      {
        variant: "round-full-outline",
        colorScheme: "danger",
        class:
          "border border-destructive text-destructive hover:bg-destructive/10 rounded-full",
      },
      {
        variant: "round-full-outline",
        colorScheme: "info",
        class:
          "border border-blue-500 text-blue-600 hover:bg-blue-50 rounded-full",
      },
      {
        variant: "round-full-outline",
        colorScheme: "success",
        class:
          "border border-green-500 text-green-600 hover:bg-green-50 rounded-full",
      },
      {
        variant: "round-full-outline",
        colorScheme: "warning",
        class:
          "border border-amber-500 text-amber-600 hover:bg-amber-50 rounded-full",
      },
      {
        variant: "round-full-outline",
        colorScheme: "secondary",
        class:
          "border border-secondary-foreground text-secondary-foreground hover:bg-secondary/50 rounded-full",
      },

      // ---- LINK (solid) ----
      {
        variant: "link",
        colorScheme: "default",
        class: "text-primary underline-offset-4 hover:underline",
      },
      {
        variant: "link",
        colorScheme: "danger",
        class: "text-destructive underline-offset-4 hover:underline",
      },
      {
        variant: "link",
        colorScheme: "warning",
        class: "text-amber-600 underline-offset-4 hover:underline",
      },
      {
        variant: "link",
        colorScheme: "secondary",
        class: "text-secondary-foreground underline-offset-4 hover:underline",
      },
      {
        variant: "link",
        colorScheme: "success",
        class: "text-green-600 underline-offset-4 hover:underline",
      },
      {
        variant: "link",
        colorScheme: "info",
        class: "text-blue-600 underline-offset-4 hover:underline",
      },
    ],

    defaultVariants: {
      variant: "default",
      size: "default",
      colorScheme: "default",
    },
  }
);

export type IButtonProps = React.ComponentProps<"button"> &
  VariantProps<typeof buttonVariants> & {
    asChild?: boolean;
    tooltip?: string | null;
    to?: string;
    linkOptions?: NavigateOptions;
    title?: string;
    loading?: boolean;
    leftIcon?: React.ReactNode;
    rightIcon?: React.ReactNode;
    colorScheme?:
      | "default"
      | "danger"
      | "info"
      | "warning"
      | "success"
      | "secondary";
  };

function Button({
  className,
  variant,
  size,
  asChild = false,
  children,
  title,
  tooltip,
  loading = false,
  to,
  linkOptions,
  leftIcon,
  rightIcon,
  colorScheme = "default",
  ...props
}: IButtonProps) {
  const Comp: any = asChild ? Slot : "button";
  const navigate = useNavigate();

  const isExternalLink = to && to.startsWith("http");

  // compute disabled state (prop disabled override possible)
  const isDisabled = !!(props.disabled || loading);

  // spinner size based on size variant
  const spinnerSizeClass =
    size === "sm"
      ? "h-3 w-3 border-2"
      : size === "lg"
      ? "h-5 w-5 border-2"
      : size === "icon-sm"
      ? "h-3 w-3 border-2"
      : size === "icon-lg"
      ? "h-6 w-6 border-2"
      : "h-4 w-4 border-2";

  const spinner = (
    <span
      className={cn(
        "inline-flex items-center justify-center animate-spin rounded-full border-current border-t-transparent",
        spinnerSizeClass
      )}
      aria-hidden="true"
    />
  );

  // Le contenu du bouton
  const content = (
    <>
      {/* left icon or spinner */}
      {loading ? (
        <span className=" flex items-center">{spinner}</span>
      ) : leftIcon ? (
        <span className=" flex items-center">{leftIcon}</span>
      ) : null}

      {/* texte / enfants */}
      {children || title ? (
        <span className="inline-flex items-center">{children ?? title}</span>
      ) : null}

      {/* right icon (hidden when loading) */}
      {!loading && rightIcon ? (
        <span className="flex items-center">{rightIcon}</span>
      ) : null}
    </>
  );

  const buttonElement = (
    <Comp
      data-slot="button"
      className={cn(buttonVariants({ variant, size, colorScheme, className }))}
      {...props}
      onClick={(e) => {
        if (to) {
          if (isExternalLink) {
            window.open(to, "_blank");
          } else {
            navigate(to, linkOptions);
          }
        } else {
          props.onClick?.(e);
        }
      }}
      disabled={isDisabled}
      aria-disabled={isDisabled}
      aria-busy={loading || undefined}
      tabIndex={isDisabled ? -1 : props.tabIndex}
    >
      {content}
    </Comp>
  );

  // Si pas de tooltip -> renvoyer directement le bouton
  if (!tooltip) {
    return buttonElement;
  }

  // Sinon wrapper tooltip
  return (
    <Tooltip disableHoverableContent>
      <TooltipTrigger asChild>{buttonElement}</TooltipTrigger>
      <TooltipContent>
        <p>{tooltip}</p>
      </TooltipContent>
    </Tooltip>
  );
}

export { Button, buttonVariants };
