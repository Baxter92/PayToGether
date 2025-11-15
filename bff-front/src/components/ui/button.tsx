import * as React from "react";
import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@/lib/utils";
import { Tooltip, TooltipContent, TooltipTrigger } from "./tooltip";

const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-sm text-sm font-medium transition-all disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg:not([class*='size-'])]:size-4 shrink-0 [&_svg]:shrink-0 outline-none focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:ring-[3px] aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive cursor-pointer",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive:
          "bg-destructive text-white hover:bg-destructive/90 focus-visible:ring-destructive/20 dark:focus-visible:ring-destructive/40 dark:bg-destructive/60",
        outline:
          "border bg-background shadow-xs hover:bg-secondary hover:text-accent-foreground dark:bg-input/30 dark:border-input dark:hover:bg-input/50",
        secondary:
          "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost:
          "hover:bg-secondary hover:text-accent-foreground dark:hover:bg-secondary/50",
        link: "text-primary underline-offset-4 hover:underline",
      },
      size: {
        default: "h-9 px-4 py-2 has-[>svg]:px-3",
        sm: "h-8 rounded-md gap-1.5 px-3 has-[>svg]:px-2.5",
        lg: "h-10 rounded-md px-6 has-[>svg]:px-4",
        icon: "size-9",
        "icon-sm": "size-8",
        "icon-lg": "size-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
);

export type IButtonProps = React.ComponentProps<"button"> &
  VariantProps<typeof buttonVariants> & {
    asChild?: boolean;
    tooltip?: string | null;
    title?: string;
    loading?: boolean;
    leftIcon?: React.ReactNode;
    rightIcon?: React.ReactNode;
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
  leftIcon,
  rightIcon,
  ...props
}: IButtonProps) {
  const Comp: any = asChild ? Slot : "button";

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
      <span className="inline-flex items-center">{children ?? title}</span>

      {/* right icon (hidden when loading) */}
      {!loading && rightIcon ? (
        <span className="flex items-center">{rightIcon}</span>
      ) : null}
    </>
  );

  const buttonElement = (
    <Comp
      data-slot="button"
      className={cn(buttonVariants({ variant, size, className }))}
      {...props}
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
