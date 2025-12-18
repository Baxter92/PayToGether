import React from "react";
import clsx from "clsx";

export type VStackProps = React.DetailedHTMLProps<
  React.HTMLAttributes<HTMLDivElement>,
  HTMLDivElement
> & {
  spacing?: number; // px
  align?: "start" | "center" | "end" | "stretch";
  justify?: "start" | "center" | "end" | "between" | "around" | "evenly";
  wrap?: boolean;
};

const alignMap: Record<VStackProps["align"], string> = {
  start: "items-start",
  center: "items-center",
  end: "items-end",
  stretch: "items-stretch",
};

const justifyMap: Record<VStackProps["justify"], string> = {
  start: "justify-start",
  center: "justify-center",
  end: "justify-end",
  between: "justify-between",
  around: "justify-around",
  evenly: "justify-evenly",
};

const VStack = ({
  children,
  spacing = 16,
  align = "stretch",
  justify = "start",
  className = "",
  wrap = false,
  style,
  ...props
}: VStackProps) => {
  const gap = `${spacing}px`;

  return (
    <div
      className={clsx(
        "flex flex-col w-full",
        alignMap[align],
        justifyMap[justify],
        wrap && "flex-wrap",
        className
      )}
      style={{ gap, ...style }}
      {...props}
    >
      {children}
    </div>
  );
};

export default VStack;
