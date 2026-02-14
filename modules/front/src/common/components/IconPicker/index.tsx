import type { ISelectProps } from "../Select";
import Select from "../Select";
import HStack from "../HStack";
import { useMemo } from "react";
import { LazyIcon } from "../LazyIcon";

export type IIconPickerProps = Omit<ISelectProps, "items" | "renderItem"> & {
  icons?: string[];
};

const ICON_LIST = [
  "Fish",
  "Anchor",
  "Waves",
  "Drumstick",
  "Beef",
  "Utensils",
  "Flame",
  "Carrot",
  "Leaf",
  "Apple",
  "Cherry",
  "Croissant",
  "Cake",
  "Wheat",
  "Coffee",
  "Wine",
  "Beer",
  "Milk",
];

export default function IconPicker({
  icons = ICON_LIST,
  ...props
}: IIconPickerProps) {
  const items = useMemo(
    () => icons.map((name) => ({ value: name, label: name })),
    [icons],
  );

  return (
    <Select
      {...props}
      items={items}
      renderItem={(item) => {
        return (
          <HStack className="items-center">
            <div>
              <LazyIcon name={item.value} size={20} />
            </div>
            <span>{item.label}</span>
          </HStack>
        );
      }}
    />
  );
}
