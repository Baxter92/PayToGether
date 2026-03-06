import { useI18n } from "@hooks/useI18n";
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

export default function ShippingForm({
  defaultValues,
  onSubmit,
  onBack,
  isSubmitting,
}: IShippingFormProps) {
  const { t } = useI18n("checkout");

  const shippingSchema = z.object({
    // fullName: z.string().min(2, t("nameRequired")),
    // phone: z.string().min(6, t("phoneInvalid")),
    // address: z.string().min(5, t("addressTooShort")),
    // city: z.string().min(2, t("cityRequired")),
    // postalCode: z.string().optional(),
  });

  const fields: IFieldConfig[] = [
    {
      name: "fullName",
      label: t("fullName"),
      type: "text",
      placeholder: defaultValues?.fullName || t("fullNamePlaceholder"),
    },
    {
      name: "phone",
      label: t("phone"),
      type: "text",
      placeholder: defaultValues?.phone || "+1 XXX XXX XXX",
    },
    {
      name: "address",
      label: t("address"),
      type: "text",
      placeholder: t("addressPlaceholder"),
    },
    {
      name: "city",
      label: t("city"),
      type: "text",
      placeholder: t("cityPlaceholder"),
    },
    {
      name: "postalCode",
      label: t("postalCodeOptional"),
      type: "text",
      placeholder: "67000",
    },
  ];

  return (
    <Form
      fields={fields}
      schema={shippingSchema}
      onSubmit={({ data }) => onSubmit(data as ShippingData)}
      submitLabel={isSubmitting ? t("processing") : t("continue")}
      resetLabel={onBack ? t("back") : undefined}
      onReset={onBack}
    />
  );
}
