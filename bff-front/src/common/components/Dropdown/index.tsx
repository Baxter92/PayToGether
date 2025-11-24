import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from "@components/ui/dropdown-menu";
import { ChevronDown } from "lucide-react";
import React from "react";
import { Button, type IButtonProps } from "../ui/button";
import { cn } from "@/common/lib/utils";

type DropdownItem = {
  label: string | React.ReactNode;
  value: string;
  disabled?: boolean;
  icon?: React.ReactNode;
  [key: string]: any;
};

type IDropdownProps = {
  label: React.ReactNode;
  items: DropdownItem[];
  selectedValue?: string;
  onChange?: (value: string) => void;
  className?: string;
  contentClassName?: string;
  renderItem?: (item: DropdownItem) => React.ReactNode;
  triggerOptions?: IButtonProps;
};

export function Dropdown({
  label,
  items,
  selectedValue,
  onChange,
  className = "",
  contentClassName = "",
  renderItem,
  triggerOptions,
}: IDropdownProps) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="outline"
          className={cn(
            `flex justify-between items-center space-x-2`,
            className
          )}
          rightIcon={<ChevronDown className="w-4 h-4" />}
          {...triggerOptions}
        >
          {label}
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="start" className={contentClassName}>
        <DropdownMenuLabel>Choisir une option</DropdownMenuLabel>
        <DropdownMenuSeparator />

        {items.map((item) => (
          <DropdownMenuItem
            key={item.value}
            disabled={item.disabled}
            onClick={() => {
              item.onClick?.();
              onChange?.(item.value);
            }}
            className={selectedValue === item.value ? "bg-secondary" : ""}
          >
            {renderItem ? (
              renderItem(item)
            ) : (
              <div className="flex items-center gap-2">
                {item.icon}
                <span>{item.label}</span>
              </div>
            )}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
