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
  const { t } = useTranslation("checkout");

  const fields: IFieldConfig[] = [
    {
      name: "deliveryMethod",
      label: t("deliveryTitle"),
      type: "radio",
      defaultValue: "pickup",
      items: [
        { label: `${t("homeDelivery")}`, value: "home" },
        { label: t("pickupDelivery"), value: "pickup" },
      ],
    },
  ];

  return (
    <Form<DeliveryData>
      fields={fields}
      schema={deliverySchema}
      defaultValues={{
        deliveryMethod: "pickup",
      }}
      onSubmit={({ data }) => onSubmit(data as DeliveryData)}
      submitLabel={isSubmitting ? t("processing") : t("continue")}
      resetLabel={onBack ? t("back") : undefined}
      resetBtnProps={{ onClick: onBack, disabled: isSubmitting }}
    />
  );
}
