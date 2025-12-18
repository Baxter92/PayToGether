import type { IFieldConfig } from "@/common/containers/Form";
import Form from "@/common/containers/Form";
import * as z from "zod";
import type { ShippingData } from "../types";

export interface IShippingFormProps {
  defaultValues?: Partial<ShippingData>;
  onSubmit: (data: ShippingData) => void;
  onBack?: () => void;
  isSubmitting?: boolean;
}

const shippingSchema = z.object({
  fullName: z.string().min(2, "Nom requis"),
  phone: z.string().min(6, "Téléphone invalide"),
  address: z.string().min(5, "Adresse trop courte"),
  city: z.string().min(2, "Ville requise"),
  postalCode: z.string().optional(),
});

export default function ShippingForm({
  defaultValues,
  onSubmit,
  onBack,
  isSubmitting,
}: IShippingFormProps) {
  const fields: IFieldConfig[] = [
    {
      name: "fullName",
      label: "Nom complet",
      type: "text",
      placeholder: defaultValues?.fullName || "Votre nom complet",
    },
    {
      name: "phone",
      label: "Téléphone",
      type: "text",
      placeholder: defaultValues?.phone || "+237 6XX XXX XXX",
    },
    {
      name: "address",
      label: "Adresse",
      type: "text",
      placeholder: "Rue, quartier, n°",
    },
    {
      name: "city",
      label: "Ville",
      type: "text",
      placeholder: "Douala, Yaoundé...",
    },
    {
      name: "postalCode",
      label: "Code postal (optionnel)",
      type: "text",
      placeholder: "67000",
    },
  ];

  return (
    <Form
      fields={fields}
      schema={shippingSchema}
      onSubmit={({ data }) => onSubmit(data as ShippingData)}
      submitLabel={isSubmitting ? "Traitement..." : "Continuer"}
      resetLabel={onBack ? "Retour" : undefined}
      onReset={onBack}
    />
  );
}
