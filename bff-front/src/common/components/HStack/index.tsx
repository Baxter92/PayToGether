import React from "react";
import clsx from "clsx";

export type HStackProps = React.DetailedHTMLProps<
  React.HTMLAttributes<HTMLDivElement>,
  HTMLDivElement
> & {
  spacing?: number; // px
  align?: "start" | "center" | "end" | "stretch";
  justify?: "start" | "center" | "end" | "between" | "around" | "evenly";
  wrap?: boolean;
};

const alignMap: Record<NonNullable<HStackProps["align"]>, string> = {
  start: "items-start",
  center: "items-center",
  end: "items-end",
  stretch: "items-stretch",
};

const justifyMap: Record<NonNullable<HStackProps["justify"]>, string> = {
  start: "justify-start",
  center: "justify-center",
  end: "justify-end",
  between: "justify-between",
  around: "justify-around",
  evenly: "justify-evenly",
};

const HStack = ({
  children,
  spacing = 4,
  align = "center",
  justify = "start",
  wrap = false,
  className = "",
  style,
  ...props
}: HStackProps) => {
  const gap = `${spacing}px`;

  return (
    <div
      className={clsx(
        "flex",
        wrap && "flex-wrap",
        alignMap[align],
        justifyMap[justify],
        className
      )}
      style={{ gap, ...style }}
      {...props}
    >
      {children}
    </div>
  );
};

export default HStack;
