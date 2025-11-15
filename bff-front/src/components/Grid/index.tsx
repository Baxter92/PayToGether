import * as React from "react";
import { cn } from "@/lib/utils"; // ou utilise classnames / simple concat si tu n'as pas cn

type IBreakpoint = "base" | "sm" | "md" | "lg" | "xl";

type IColsProp = Partial<Record<IBreakpoint, number>>; // e.g. { base:1, md:2, lg:4 }

export type IGridProps = {
  /**
   * Nombre de colonnes par Ibreakpoint.
   * Accepts numbers 1..12. Example: { base:1, md:2, lg:4 }
   */
  cols?: IColsProp;

  /**
   * Gap (tailwind friendly class suffix or raw css value).
   * - If you pass a string that matches "gap-*" it will be appended as class (ex: "gap-8").
   * - Otherwise it will be applied inline as CSS (ex: "24px" or 8).
   */
  gap?: string | number;
  rowGap?: string | number;
  colGap?: string | number;

  /** element type: div, ul, section ... */
  as?: any;

  /** extra classes passthrough */
  className?: string;

  /** inline styles fallback */
  style?: React.CSSProperties;

  /** afficher chaque enfant avec un wrapper (utile pour items list) */
  itemWrapperClassName?: string;

  /** optionally render a list of items with a renderItem */
  items?: any[];
  renderItem?: (item: any, index: number) => React.ReactNode;

  children?: React.ReactNode;
};

/** Helper: allowed columns range to keep things safe */
const ALLOWED_COLS = new Set(Array.from({ length: 12 }, (_, i) => i + 1));

/** Map Ibreakpoint to tailwind prefix */
const BP_PREFIX: Record<IBreakpoint, string> = {
  base: "",
  sm: "sm:",
  md: "md:",
  lg: "lg:",
  xl: "xl:",
};

/**
 * Build tailwind classes for grid columns for given cols prop.
 * Returns { classes: string[], fallbackStyle?: string }.
 */
function buildColsClasses(cols?: IColsProp) {
  if (!cols) return { classes: [] as string[], fallbackStyle: undefined };

  const classes: string[] = [];
  const explicitTemplates: string[] = []; // for fallback style pieces like "repeat(2, 1fr)"

  (Object.keys(cols) as IBreakpoint[]).forEach((bp) => {
    const n = cols[bp];
    if (!n) return;

    if (!ALLOWED_COLS.has(n)) {
      // ignore invalid values
      return;
    }

    // prefer tailwind class when possible
    const prefix = BP_PREFIX[bp];
    classes.push(`${prefix}grid-cols-${n}`);

    // track fallback template for inline style (base only used without prefix)
    // We'll only use base template as a fallback gridTemplateColumns if Tailwind classes not present.
    if (bp === "base") explicitTemplates.push(`repeat(${n}, 1fr)`);
  });

  const fallbackStyle = explicitTemplates.length
    ? explicitTemplates[0]
    : undefined;

  return { classes, fallbackStyle };
}

/**
 * Convert gap prop: if it looks like "gap-8" or "gap-x-4" return as class,
 * else return css value string.
 */
function gapToClassOrStyle(gap?: string | number) {
  if (gap === undefined) return { cls: undefined, css: undefined };

  if (typeof gap === "number") return { cls: undefined, css: `${gap}px` };

  // If user passed a tailwind class like "gap-8" or "gap-x-4" or "gap-y-6"
  if (/^gap(-[xy])?-\d+$/.test(gap)) {
    return { cls: gap, css: undefined };
  }

  // else treat as css value
  return { cls: undefined, css: gap };
}

export default function Grid({
  cols,
  gap,
  rowGap,
  colGap,
  as = "div",
  className,
  style,
  items,
  renderItem,
  children,
  itemWrapperClassName,
  ...rest
}: IGridProps) {
  const Element = as as any;

  const { classes: colsClasses, fallbackStyle } = buildColsClasses(cols);

  const gapClsOrStyle = gapToClassOrStyle(gap);
  const rowGapClsOrStyle = gapToClassOrStyle(rowGap);
  const colGapClsOrStyle = gapToClassOrStyle(colGap);

  // Compose tailwind classes (note: these are *safe* values but Tailwind purge can still remove them;
  // consider adding used classes to safelist in tailwind.config if necessary)
  const twClasses = [
    "grid",
    ...colsClasses,
    gapClsOrStyle.cls,
    rowGapClsOrStyle.cls,
    colGapClsOrStyle.cls,
    className,
  ]
    .filter(Boolean)
    .join(" ");

  // Inline style fallback when user passed raw css gap or when we built a fallback template
  const inlineStyle: React.CSSProperties = {
    ...(fallbackStyle ? { gridTemplateColumns: fallbackStyle } : {}),
    ...(gapClsOrStyle.css ? { gap: gapClsOrStyle.css } : {}),
    ...(rowGapClsOrStyle.css ? { rowGap: rowGapClsOrStyle.css } : {}),
    ...(colGapClsOrStyle.css ? { columnGap: colGapClsOrStyle.css } : {}),
    ...style,
  };

  // Render children or items/renderItem
  const content =
    items && renderItem
      ? items.map((it, index) => (
          <div key={index} className={itemWrapperClassName}>
            {renderItem(it, index)}
          </div>
        ))
      : React.Children.map(children, (child, idx) =>
          child ? (
            <div key={idx} className={itemWrapperClassName}>
              {child}
            </div>
          ) : null
        );

  return (
    <Element className={cn(twClasses)} style={inlineStyle} {...(rest as any)}>
      {content}
    </Element>
  );
}
