import React from "react";
import { Button } from "../ui/button";
import HStack from "../HStack";

export type ICounterProps = React.ComponentProps<typeof HStack> & {
  qty: number;
  setQty: (value: number) => void;
  max: number;
  min: number;
};

export default function Counter({
  qty,
  setQty,
  max,
  min = 1,
  ...props
}: ICounterProps) {
  function handleAdd() {
    const newQty = Math.min(qty + 1, max);

    if (newQty >= min && newQty <= max) {
      setQty(newQty);
    }
  }

  function handleRemove() {
    const newQty = Math.max(qty - 1, min);

    if (newQty <= max && newQty >= min) {
      setQty(newQty);
    }
  }

  return (
    <HStack {...props}>
      <Button
        variant="outline"
        onClick={() => handleRemove()}
        disabled={qty <= min}
      >
        -
      </Button>
      <div className="px-4 py-2 border rounded">{qty}</div>
      <Button
        variant="outline"
        onClick={() => handleAdd()}
        disabled={qty >= max}
      >
        +
      </Button>
    </HStack>
  );
}
