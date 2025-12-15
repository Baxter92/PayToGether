import * as React from "react";
import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@/common/lib/utils";

/**
 * Badge variants
 * - variant: style (contained / outline / subtle / ghost)
 * - colorScheme: semantic color
 * - size: visual size
 * - rounded: border radius
 */
const badgeVariants = cva(
  "inline-flex items-center justify-center border font-medium w-fit whitespace-nowrap shrink-0 [&>svg]:size-3 gap-1 [&>svg]:pointer-events-none focus-visible:ring-[3px] transition-[color,background-color,border-color,box-shadow]",
  {
    variants: {
      variant: {
        contained: "border-transparent",
        outline: "bg-transparent",
        subtle: "border-transparent",
        ghost: "border-transparent bg-transparent",
      },
      colorScheme: {
        default: "text-primary bg-primary/10 border-primary/20",
        secondary:
          "text-secondary-foreground bg-secondary/80 border-secondary/30",
        success: "text-green-700 bg-green-100 border-green-200",
        info: "text-blue-700 bg-blue-100 border-blue-200",
        warning: "text-amber-800 bg-amber-100 border-amber-200",
        danger: "text-red-700 bg-red-100 border-red-200",
      },
      size: {
        xs: "text-xs px-2 py-0.5",
        sm: "text-xs px-2.5 py-0.5",
        md: "text-sm px-3 py-1",
        lg: "text-sm px-4 py-1.5",
      },
      rounded: {
        none: "rounded-none",
        sm: "rounded-md",
        md: "rounded-lg",
        full: "rounded-full",
      },
    },
    compoundVariants: [
      // OUTLINE
      {
        variant: "outline",
        colorScheme: "default",
        className: "text-primary border-primary bg-transparent",
      },
      {
        variant: "outline",
        colorScheme: "success",
        className: "text-green-700 border-green-400 bg-transparent",
      },
      {
        variant: "outline",
        colorScheme: "info",
        className: "text-blue-700 border-blue-400 bg-transparent",
      },
      {
        variant: "outline",
        colorScheme: "warning",
        className: "text-amber-800 border-amber-400 bg-transparent",
      },
      {
        variant: "outline",
        colorScheme: "danger",
        className: "text-red-700 border-red-400 bg-transparent",
      },

      // CONTAINED
      {
        variant: "contained",
        colorScheme: "default",
        className: "bg-primary text-primary-foreground",
      },
      {
        variant: "contained",
        colorScheme: "secondary",
        className: "bg-secondary text-secondary-foreground",
      },
      {
        variant: "contained",
        colorScheme: "success",
        className: "bg-green-600 text-white",
      },
      {
        variant: "contained",
        colorScheme: "info",
        className: "bg-blue-600 text-white",
      },
      {
        variant: "contained",
        colorScheme: "warning",
        className: "bg-amber-500 text-black",
      },
      {
        variant: "contained",
        colorScheme: "danger",
        className: "bg-red-600 text-white",
      },

      // SUBTLE
      {
        variant: "subtle",
        colorScheme: "default",
        className: "bg-primary/10 text-primary",
      },
      {
        variant: "subtle",
        colorScheme: "secondary",
        className: "bg-secondary/20 text-secondary-foreground",
      },
    ],
    defaultVariants: {
      variant: "subtle",
      colorScheme: "default",
      size: "md",
      rounded: "full",
    },
  }
);

export interface BadgeProps
  extends React.ComponentProps<"span">,
    VariantProps<typeof badgeVariants> {
  asChild?: boolean;
}

function Badge({
  className,
  variant,
  colorScheme,
  size,
  rounded,
  asChild = false,
  ...props
}: BadgeProps) {
  const Comp = asChild ? Slot : "span";

  return (
    <Comp
      data-slot="badge"
      className={cn(
        badgeVariants({ variant, colorScheme, size, rounded }),
        className
      )}
      {...props}
    />
  );
}

export { Badge, badgeVariants };
