import type { IFieldConfig } from "@/common/containers/Form";
import Form from "@/common/containers/Form";
import type { DeliveryData } from "../types";
import type { JSX } from "react";
import * as z from "zod";

export interface IDeliveryFormProps {
  defaultValue?: "home" | "pickup";
  onSubmit: (data: DeliveryData) => void;
  onBack?: () => void;
  isSubmitting?: boolean;
}

const deliverySchema = z.object({
  deliveryMethod: z.enum(["home", "pickup"]).default("home"),
});

export function DeliveryForm({
  onSubmit,
  onBack,
  isSubmitting,
}: IDeliveryFormProps): JSX.Element {
  const fields: IFieldConfig[] = [
    {
      name: "deliveryMethod",
      label: "Méthode de livraison",
      type: "radio",
      items: [
        { label: "📍 Livraison à domicile (+3.50€)", value: "home" },
        { label: "📦 Retrait au point relais (Gratuit)", value: "pickup" },
      ],
    },
  ];

  return (
    <Form
      fields={fields}
      schema={deliverySchema}
      onSubmit={({ data }) => onSubmit(data as DeliveryData)}
      submitLabel={isSubmitting ? "Traitement..." : "Continuer"}
      resetLabel={onBack ? "Retour" : undefined}
      onReset={() => {
        console.log("rr");
        onBack?.();
      }}
    />
  );
}
