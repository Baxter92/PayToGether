import React from "react";
import clsx from "clsx";

export type VStackProps = React.DetailedHTMLProps<
  React.HTMLAttributes<HTMLDivElement>,
  HTMLDivElement
> & {
  spacing?: number;
  align?: string;
  justify?: string;
  wrap?: boolean;
};

const VStack = ({
  children,
  spacing = 4,
  align = "start",
  justify = "start",
  className = "",
  ...props
}: VStackProps) => {
  return (
    <div
      className={clsx(
        "flex flex-col",
        `items-${align}`,
        `justify-${justify}`,
        `space-y-[${spacing}px]`,
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export default VStack;
