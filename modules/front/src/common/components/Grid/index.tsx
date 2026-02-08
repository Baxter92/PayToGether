import * as React from "react";
import { cn } from "@lib/utils";

type IBreakpoint = "base" | "sm" | "md" | "lg" | "xl";
export type IColsProp = Partial<Record<IBreakpoint, number>>;

export type IGridProps = {
  cols?: IColsProp;
  gap?: string | number;
  rowGap?: string | number;
  colGap?: string | number;
  as?: any;
  className?: string;
  style?: React.CSSProperties;
  itemWrapperClassName?: string;
  items?: any[];
  renderItem?: (item: any, index: number) => React.ReactNode;
  children?: React.ReactNode;
};

/**
 * Map exact tailwind classes for each breakpoint + column combination
 * This ensures Tailwind can detect and include these classes
 */
const GRID_COLS_CLASSES: Record<string, string> = {
  // Base (no prefix)
  "base-1": "grid-cols-1",
  "base-2": "grid-cols-2",
  "base-3": "grid-cols-3",
  "base-4": "grid-cols-4",
  "base-5": "grid-cols-5",
  "base-6": "grid-cols-6",
  "base-7": "grid-cols-7",
  "base-8": "grid-cols-8",
  "base-9": "grid-cols-9",
  "base-10": "grid-cols-10",
  "base-11": "grid-cols-11",
  "base-12": "grid-cols-12",
  // SM
  "sm-1": "sm:grid-cols-1",
  "sm-2": "sm:grid-cols-2",
  "sm-3": "sm:grid-cols-3",
  "sm-4": "sm:grid-cols-4",
  "sm-5": "sm:grid-cols-5",
  "sm-6": "sm:grid-cols-6",
  "sm-7": "sm:grid-cols-7",
  "sm-8": "sm:grid-cols-8",
  "sm-9": "sm:grid-cols-9",
  "sm-10": "sm:grid-cols-10",
  "sm-11": "sm:grid-cols-11",
  "sm-12": "sm:grid-cols-12",
  // MD
  "md-1": "md:grid-cols-1",
  "md-2": "md:grid-cols-2",
  "md-3": "md:grid-cols-3",
  "md-4": "md:grid-cols-4",
  "md-5": "md:grid-cols-5",
  "md-6": "md:grid-cols-6",
  "md-7": "md:grid-cols-7",
  "md-8": "md:grid-cols-8",
  "md-9": "md:grid-cols-9",
  "md-10": "md:grid-cols-10",
  "md-11": "md:grid-cols-11",
  "md-12": "md:grid-cols-12",
  // LG
  "lg-1": "lg:grid-cols-1",
  "lg-2": "lg:grid-cols-2",
  "lg-3": "lg:grid-cols-3",
  "lg-4": "lg:grid-cols-4",
  "lg-5": "lg:grid-cols-5",
  "lg-6": "lg:grid-cols-6",
  "lg-7": "lg:grid-cols-7",
  "lg-8": "lg:grid-cols-8",
  "lg-9": "lg:grid-cols-9",
  "lg-10": "lg:grid-cols-10",
  "lg-11": "lg:grid-cols-11",
  "lg-12": "lg:grid-cols-12",
  // XL
  "xl-1": "xl:grid-cols-1",
  "xl-2": "xl:grid-cols-2",
  "xl-3": "xl:grid-cols-3",
  "xl-4": "xl:grid-cols-4",
  "xl-5": "xl:grid-cols-5",
  "xl-6": "xl:grid-cols-6",
  "xl-7": "xl:grid-cols-7",
  "xl-8": "xl:grid-cols-8",
  "xl-9": "xl:grid-cols-9",
  "xl-10": "xl:grid-cols-10",
  "xl-11": "xl:grid-cols-11",
  "xl-12": "xl:grid-cols-12",
};

function buildColsClasses(cols?: IColsProp) {
  if (!cols) return [];

  const classes: string[] = [];

  (Object.keys(cols) as IBreakpoint[]).forEach((bp) => {
    const n = cols[bp];
    if (!n || n < 1 || n > 12) return;

    const key = `${bp}-${n}`;
    const className = GRID_COLS_CLASSES[key];
    if (className) {
      classes.push(className);
    }
  });

  return classes;
}

function gapToClassOrStyle(gap?: string | number) {
  if (gap === undefined) return { cls: undefined, css: undefined };

  if (typeof gap === "number") return { cls: undefined, css: `${gap}px` };

  if (/^gap(-[xy])?-\d+$/.test(gap)) {
    return { cls: gap, css: undefined };
  }

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

  const colsClasses = buildColsClasses(cols);

  const gapClsOrStyle = gapToClassOrStyle(gap);
  const rowGapClsOrStyle = gapToClassOrStyle(rowGap);
  const colGapClsOrStyle = gapToClassOrStyle(colGap);

  const twClasses = cn(
    "grid",
    ...colsClasses,
    gapClsOrStyle.cls,
    rowGapClsOrStyle.cls,
    colGapClsOrStyle.cls,
    className
  );

  const inlineStyle: React.CSSProperties = {
    ...(gapClsOrStyle.css ? { gap: gapClsOrStyle.css } : {}),
    ...(rowGapClsOrStyle.css ? { rowGap: rowGapClsOrStyle.css } : {}),
    ...(colGapClsOrStyle.css ? { columnGap: colGapClsOrStyle.css } : {}),
    ...style,
  };

  const content =
    items && renderItem
      ? items.map((it, index) => {
        const itemContent = renderItem(it, index);
        return itemWrapperClassName ? (
          <div key={index} className={itemWrapperClassName}>
            {itemContent}
          </div>
        ) : (
          <React.Fragment key={index}>{itemContent}</React.Fragment>
        );
      })
      : itemWrapperClassName
        ? React.Children.map(children, (child, idx) =>
          child ? (
            <div key={idx} className={itemWrapperClassName}>
              {child}
            </div>
          ) : null
        )
        : children;

  return (
    <Element className={twClasses} style={inlineStyle} {...rest}>
      {content}
    </Element>
  );
}
