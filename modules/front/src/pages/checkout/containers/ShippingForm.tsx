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
    phone: z.string().refine((val) => {
      const cleaned = val.replace(/\D/g, "");
      if (cleaned.length <= 0) return true;
      return cleaned.length >= 10;
    }, t("phoneInvalid")),
    address: z.string().min(5, t("addressTooShort")),
    complementAddress: z.string().optional(),
    city: z.string().min(2, t("cityRequired")),
    postalCode: z.string().min(3, t("postalCodeRequired")),
  });

  const fields: IFieldConfig[] = [
    {
      name: "fullName",
      label: t("fullName"),
      type: "text",
      autoComplete: "name",
      defaultValue: defaultValues?.fullName,
      placeholder: defaultValues?.fullName || t("fullNamePlaceholder"),
    },
    {
      name: "phone",
      label: t("phone"),
      type: "text",
      autoComplete: "tel",
      placeholder: defaultValues?.phone || "+1 XXX XXX XXX",
    },
    {
      name: "address",
      label: t("address"),
      type: "text",
      autoComplete: "address-line1",
      placeholder: t("addressPlaceholder"),
    },
    {
      name: "complementAddress",
      label: t("complementAddress"),
      type: "text",
      autoComplete: "address-line2",
      placeholder: t("complementAddressPlaceholder"),
    },
    {
      name: "city",
      label: t("city"),
      type: "text",
      autoComplete: "address-level2",
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
      defaultValues={defaultValues}
      onSubmit={({ data }) => onSubmit(data as ShippingData)}
      submitLabel={isSubmitting ? t("processing") : t("continue")}
      resetLabel={onBack ? t("back") : undefined}
      onReset={onBack}
    />
  );
}
