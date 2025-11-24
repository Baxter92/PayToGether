import React from "react";
import clsx from "clsx";

export type HStackProps = React.DetailedHTMLProps<
  React.HTMLAttributes<HTMLDivElement>,
  HTMLDivElement
> & {
  spacing?: number;
  align?: string;
  justify?: string;
  wrap?: boolean;
};

const HStack = ({
  children,
  spacing = 4,
  align = "center",
  justify = "start",
  wrap = false,
  className = "",
  ...props
}: HStackProps) => {
  return (
    <div
      className={clsx(
        "flex",
        wrap && "flex-wrap",
        `items-${align}`,
        `justify-${justify}`,
        `space-x-[${spacing}px]`,
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export default HStack;
