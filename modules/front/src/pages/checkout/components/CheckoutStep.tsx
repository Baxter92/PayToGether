import { HStack, VStack } from "@/common/components";
import { CheckIcon } from "lucide-react";
import type { JSX } from "react";

export interface ICheckoutStepProps {
  stepNumber: number;
  title: string;
  description: string;
  isActive: boolean;
  isCompleted: boolean;
  children?: React.ReactNode;
}

export default function CheckoutStep({
  stepNumber,
  title,
  description,
  isActive,
  isCompleted,
  children,
}: ICheckoutStepProps): JSX.Element {
  return (
    <div className="checkout-step">
      <HStack spacing={4} align="start">
        <div
          className={`flex items-center justify-center w-10 h-10 rounded-full border-2 flex-shrink-0 transition-colors ${
            isCompleted
              ? "bg-primary border-primary text-primary-foreground"
              : isActive
                ? "border-primary text-primary"
                : "border-muted text-muted-foreground"
          }`}
        >
          {isCompleted ? (
            <CheckIcon className="w-6 h-6" />
          ) : (
            <span className="font-semibold">{stepNumber}</span>
          )}
        </div>
        <VStack spacing={2} className="flex-1">
          <h3 className="text-lg font-semibold text-foreground">{title}</h3>
          <p className="text-sm text-muted-foreground">{description}</p>
          {isActive && <div className="mt-4">{children}</div>}
        </VStack>
      </HStack>
    </div>
  );
}
