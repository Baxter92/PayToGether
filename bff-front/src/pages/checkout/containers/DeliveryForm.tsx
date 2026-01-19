import { useTranslation } from "react-i18next";
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
  const { t } = useTranslation();

  const fields: IFieldConfig[] = [
    {
      name: "deliveryMethod",
      label: t("checkout.deliveryTitle"),
      type: "radio",
      items: [
        { label: `${t("checkout.homeDelivery")} (+3.50€)`, value: "home" },
        { label: t("checkout.pickupDelivery"), value: "pickup" },
      ],
    },
  ];

  return (
    <Form
      fields={fields}
      schema={deliverySchema}
      onSubmit={({ data }) => onSubmit(data as DeliveryData)}
      submitLabel={isSubmitting ? t("checkout.processing") : t("checkout.continue")}
      resetLabel={onBack ? t("checkout.back") : undefined}
      onReset={() => {
        onBack?.();
      }}
    />
  );
}
