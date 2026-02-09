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
    fullName: z.string().min(2, t("checkout.nameRequired")),
    phone: z.string().min(6, t("checkout.phoneInvalid")),
    address: z.string().min(5, t("checkout.addressTooShort")),
    city: z.string().min(2, t("checkout.cityRequired")),
    postalCode: z.string().optional(),
  });

  const fields: IFieldConfig[] = [
    {
      name: "fullName",
      label: t("checkout.fullName"),
      type: "text",
      placeholder: defaultValues?.fullName || t("checkout.fullNamePlaceholder"),
    },
    {
      name: "phone",
      label: t("checkout.phone"),
      type: "text",
      placeholder: defaultValues?.phone || "+237 6XX XXX XXX",
    },
    {
      name: "address",
      label: t("checkout.address"),
      type: "text",
      placeholder: t("checkout.addressPlaceholder"),
    },
    {
      name: "city",
      label: t("checkout.city"),
      type: "text",
      placeholder: t("checkout.cityPlaceholder"),
    },
    {
      name: "postalCode",
      label: t("checkout.postalCodeOptional"),
      type: "text",
      placeholder: "67000",
    }
  ];

  return (
    <Form
      fields={fields}
      schema={shippingSchema}
      onSubmit={({ data }) => onSubmit(data as ShippingData)}
      submitLabel={
        isSubmitting ? t("checkout.processing") : t("checkout.continue")
      }
      resetLabel={onBack ? t("checkout.back") : undefined}
      onReset={onBack}
    />
  );
}
