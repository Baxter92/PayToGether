import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuCheckboxItem,
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
  onClick?: () => void;
};

type DropdownProps = {
  label: React.ReactNode;
  items: DropdownItem[];
  selectedValue?: string | string[];
  onChange?: (value: string | string[]) => void;
  className?: string;
  contentClassName?: string;
  triggerOptions?: IButtonProps;
  multiple?: boolean;
  renderItem?: (item: DropdownItem) => React.ReactNode;
};

export function Dropdown({
  label,
  items,
  selectedValue,
  onChange,
  renderItem,
  className,
  contentClassName,
  triggerOptions,
  multiple = false,
}: DropdownProps) {
  const isSelected = (value: string) =>
    multiple
      ? Array.isArray(selectedValue) && selectedValue.includes(value)
      : selectedValue === value;

  const toggleMulti = (value: string) => {
    if (!Array.isArray(selectedValue)) {
      onChange?.([value]);
      return;
    }
    if (selectedValue.includes(value)) {
      onChange?.(selectedValue.filter((v) => v !== value));
    } else {
      onChange?.([...selectedValue, value]);
    }
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="outline"
          className={cn("flex justify-between items-center", className)}
          rightIcon={<ChevronDown className="w-4 h-4" />}
          {...triggerOptions}
        >
          {label}
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="start" className={contentClassName}>
        <DropdownMenuLabel>Choisir une option</DropdownMenuLabel>
        <DropdownMenuSeparator />

        {items.map((item) =>
          multiple ? (
            <DropdownMenuCheckboxItem
              key={item.value}
              checked={isSelected(item.value)}
              disabled={item.disabled}
              onCheckedChange={() => toggleMulti(item.value)}
            >
              {renderItem ? (
                renderItem(item)
              ) : (
                <div className="flex items-center gap-2">
                  {item.icon}
                  {item.label}
                </div>
              )}
            </DropdownMenuCheckboxItem>
          ) : (
            <DropdownMenuItem
              key={item.value}
              disabled={item.disabled}
              onClick={() => {
                if (item.onClick) {
                  item.onClick();
                } else onChange?.(item.value);
              }}
              className={selectedValue === item.value ? "bg-secondary" : ""}
            >
              {renderItem ? (
                renderItem(item)
              ) : (
                <div className="flex items-center gap-2">
                  {item.icon}
                  {item.label}
                </div>
              )}
            </DropdownMenuItem>
          )
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
