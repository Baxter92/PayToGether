import React from "react";
import clsx from "clsx";

type HeadingLevel = 1 | 2 | 3 | 4 | 5 | 6;

type Align = "left" | "center" | "right";

type Justify = "start" | "center" | "end" | "between";

type DescriptionSize = "xs" | "sm" | "base" | "lg" | "xl";

type UnderlineStyle = "text" | "line" | "bar";

const levelMap: Record<HeadingLevel, string> = {
  1: "text-4xl md:text-5xl font-extrabold tracking-tight",
  2: "text-3xl md:text-4xl font-bold tracking-tight",
  3: "text-2xl md:text-3xl font-bold",
  4: "text-xl md:text-2xl font-semibold",
  5: "text-lg md:text-xl font-semibold",
  6: "text-base md:text-lg font-medium",
};

const descriptionSizeMap: Record<DescriptionSize, string> = {
  xs: "text-xs",
  sm: "text-sm",
  base: "text-base",
  lg: "text-lg",
  xl: "text-xl",
};

const justifyMap: Record<Justify, string> = {
  start: "justify-start",
  center: "justify-center",
  end: "justify-end",
  between: "justify-between",
};

export type HeadingProps = {
  title: string | React.ReactNode;
  level?: HeadingLevel;
  description?: string | React.ReactNode;
  descriptionSize?: DescriptionSize;
  actions?: React.ReactNode;
  spacing?: number;
  align?: Align;
  justify?: Justify;

  // NEW
  underline?: boolean;
  underlineStyle?: UnderlineStyle; // "text" | "line" | "bar"
  underlineColor?: string; // Tailwind class (ex: "border-primary")
  underlineWidth?: string; // ex: "w-12", "w-full"

  className?: string;
};

export const Heading = ({
  title,
  level = 2,
  description,
  descriptionSize = "sm",
  actions,
  spacing = 12,
  align = "left",
  justify = "between",

  underline = false,
  underlineStyle = "line",
  underlineColor = "border-primary",
  underlineWidth = "w-12",

  className = "",
}: HeadingProps) => {
  const Tag = `h${level}` as any;
  const gap = `${spacing}px`;

  return (
    <div className={clsx("flex flex-col w-full", className)} style={{ gap }}>
      {/* Titre + actions */}
      <div className={clsx("flex w-full items-center", justifyMap[justify])}>
        <div className="flex flex-col w-full">
          <Tag
            className={clsx(
              levelMap[level],
              underline &&
                underlineStyle === "text" &&
                "underline underline-offset-4",
              align === "center" && "text-center",
              align === "right" && "text-right"
            )}
          >
            {title}
          </Tag>

          {/* Underline style = line / bar */}
          {underline && underlineStyle !== "text" && (
            <div
              className={clsx(
                "mt-1 border-b-2",
                underlineColor,
                underlineWidth,
                align === "center" && "mx-auto",
                align === "right" && "ml-auto"
              )}
            />
          )}
        </div>

        {actions && <div className="flex items-center gap-2">{actions}</div>}
      </div>

      {/* Description */}
      {description && (
        <p
          className={clsx(
            descriptionSizeMap[descriptionSize],
            "text-muted-foreground",
            align === "center" && "text-center",
            align === "right" && "text-right"
          )}
        >
          {description}
        </p>
      )}
    </div>
  );
};
