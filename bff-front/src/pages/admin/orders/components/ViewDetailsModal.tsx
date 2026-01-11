import React, { useEffect } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/common/components/ui/dialog";
import Form, {
  type IFieldConfig,
  type IFieldGroup,
} from "@/common/containers/Form";
import { useForm } from "react-hook-form";
import * as z from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

// ==============================
// Types
// ==============================
export type OrderDetailsInput = {
  id: string;
  customer: string;
  deal: string;
  date: string;
  amount: number;
  status: "completed" | "pending" | "refunded" | "cancelled";
};

// ==============================
// Schema Zod
// ==============================
const orderDetailsSchema = z.object({
  id: z.string(),
  customer: z.string(),
  deal: z.string(),
  date: z.string(),
  amount: z.number(),
  status: z.enum(["completed", "pending", "refunded", "cancelled"]),
});

// ==============================
// Component
// ==============================
interface ViewDetailsModalProps {
  open: boolean;
  onClose: () => void;
  order: OrderDetailsInput | null;
}

export default function ViewDetailsModal({
  open,
  onClose,
  order,
}: ViewDetailsModalProps) {
  const form = useForm<OrderDetailsInput>({
    resolver: zodResolver(orderDetailsSchema),
    defaultValues: order || {
      id: "",
      customer: "",
      deal: "",
      date: "",
      amount: 0,
      status: "pending",
    },
  });

  useEffect(() => {
    if (order) {
      form.reset(order);
    }
  }, [order, form]);

  const formFields: IFieldConfig[] = [
    {
      type: "text",
      name: "id",
      label: "Identifiant",
      disabled: true,
    },
    {
      type: "text",
      name: "date",
      label: "Date de commande",
      disabled: true,
    },
    {
      type: "text",
      name: "customer",
      label: "Client",
      disabled: true,
    },
    {
      type: "text",
      name: "deal",
      label: "Deal acheté",
      disabled: true,
    },
    {
      type: "number",
      name: "amount",
      label: "Montant",
      disabled: true,
    },
    {
      type: "select",
      name: "status",
      label: "Statut",
      items: [
        { label: "Complété", value: "completed" },
        { label: "En attente", value: "pending" },
        { label: "Remboursé", value: "refunded" },
        { label: "Annulé", value: "cancelled" },
      ],
    },
  ];

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Détails de la commande</DialogTitle>
        </DialogHeader>
        <div className="py-4">
          <Form<OrderDetailsInput>
            form={form}
            fields={formFields}
            submitLabel="Mettre à jour"
            showResetButton={false}
            showSubmitButton={false}
            onSubmit={async ({ data }) => {
              console.log("Updated order:", data);
              // Ici, vous pourriez appeler une API pour mettre à jour le statut
              onClose();
            }}
          />
        </div>
      </DialogContent>
    </Dialog>
  );
}
