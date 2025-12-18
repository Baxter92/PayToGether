import React, { useMemo } from "react";
import Form, { type IFieldConfig } from "@/common/containers/Form";
import { formatCurrency } from "@/common/utils/formatCurrency";
import { useState } from "react";
import type { PaymentData } from "../types";
import * as z from "zod";
import { Card, CardContent } from "@components/ui/card";
import { Button } from "@components/ui/button";
import HStack from "@components/HStack";
import VStack from "@components/VStack";
import { InfoIcon } from "lucide-react";

/** --- Schema Zod --- */
const paymentSchema = z.object({
  paymentMethod: z.literal("card"),
  cardNumber: z.string().min(12, "Numéro de carte invalide"),
  cardExpiry: z.string().min(4, "Date d'expiration invalide"),
  cardCvv: z.string().min(3, "CVV invalide"),
  saveCard: z.boolean().optional(),
});

/** --- Types --- */
interface PaymentFormProps {
  total: number;
  onSubmit: (data: PaymentData) => void;
  onBack?: () => void;
  isSubmitting?: boolean;
}

/** --- Helpers : card detection + Luhn --- */
function luhnValid(cardNumber: string) {
  const digits = cardNumber.replace(/\D/g, "");
  let sum = 0;
  let shouldDouble = false;
  for (let i = digits.length - 1; i >= 0; i--) {
    let d = parseInt(digits.charAt(i), 10);
    if (shouldDouble) {
      d *= 2;
      if (d > 9) d -= 9;
    }
    sum += d;
    shouldDouble = !shouldDouble;
  }
  return sum % 10 === 0;
}

type CardBrand = "visa" | "mastercard" | "amex" | "maestro" | "unknown";

function detectCardBrand(number = ""): CardBrand {
  const n = number.replace(/\D/g, "");
  if (/^4/.test(n)) return "visa";
  if (/^5[1-5]/.test(n) || /^2(2[2-9]|[3-6]\d|7[0-1]|720)/.test(n))
    return "mastercard";
  if (/^3[47]/.test(n)) return "amex";
  if (/^(50|5[6-9]|6)/.test(n)) return "maestro";
  return "unknown";
}

function formatCardNumber(value = "") {
  const digits = value.replace(/\D/g, "");
  // AMEX groups differently: 4-6-5
  if (/^3[47]/.test(digits)) {
    return digits.replace(/(\d{1,4})(\d{1,6})?(\d{1,5})?/, (m, a, b, c) =>
      [a, b, c].filter(Boolean).join(" ")
    );
  }
  return digits.replace(/(\d{1,4})/g, "$1 ").trim();
}

/** --- Icon set (inline SVGs) --- */
const IconVisa = () => (
  <svg width="40" height="25" viewBox="0 0 40 25" aria-hidden>
    <rect rx="4" width="40" height="25" fill="#1A1F71" />
    <text x="8" y="17" fill="#fff" fontSize="11" fontWeight="700">
      VISA
    </text>
  </svg>
);

const IconMastercard = () => (
  <svg width="40" height="25" viewBox="0 0 40 25" aria-hidden>
    <rect rx="4" width="40" height="25" fill="#000" />
    <circle cx="16" cy="12.5" r="7.5" fill="#ff5f00" />
    <circle cx="24" cy="12.5" r="7.5" fill="#eb001b" />
  </svg>
);

const IconAmex = () => (
  <svg width="40" height="25" viewBox="0 0 40 25" aria-hidden>
    <rect rx="4" width="40" height="25" fill="#2e77bb" />
    <text x="6" y="17" fill="#fff" fontSize="9" fontWeight="700">
      AMEX
    </text>
  </svg>
);

const IconMaestro = () => (
  <svg width="40" height="25" viewBox="0 0 40 25" aria-hidden>
    <rect rx="4" width="40" height="25" fill="#142f88" />
    <circle cx="15" cy="12.5" r="6.5" fill="#cc0000" />
    <circle cx="24" cy="12.5" r="6.5" fill="#00a0df" />
  </svg>
);

const SupportedCard = ({
  brand,
  active,
}: {
  brand: CardBrand;
  active?: boolean;
}) => (
  <div
    className={`flex items-center gap-3 px-3 py-2 rounded-md border ${
      active ? "bg-primary/10 border-primary" : "border-gray-200"
    }`}
  >
    <div className="w-10 h-6 flex items-center justify-center">
      {brand === "visa" && <IconVisa />}
      {brand === "mastercard" && <IconMastercard />}
      {brand === "amex" && <IconAmex />}
      {brand === "maestro" && <IconMaestro />}
      {brand === "unknown" && (
        <div className="text-xs text-muted-foreground">Card</div>
      )}
    </div>
    <div className="text-sm text-muted-foreground">
      {brand === "unknown" ? "Carte" : brand.toUpperCase()}
    </div>
  </div>
);

/** --- Le composant principal --- */
export default function PaymentFormCard({
  total,
  onSubmit,
  onBack,
  isSubmitting,
}: PaymentFormProps) {
  const [detectedBrand, setDetectedBrand] = useState<CardBrand>("unknown");
  const [luhnOk, setLuhnOk] = useState<boolean | null>(null);

  const handleSubmit = (data: any) => {
    // renforce validation côté UI
    const rawNumber = (data.cardNumber || "").toString();
    const digits = rawNumber.replace(/\D/g, "");
    if (!luhnValid(digits)) {
      alert("Numéro de carte invalide (Luhn).");
      return;
    }
    if (!data.cardExpiry || !/^\d{2}\/?\d{2,4}$/.test(data.cardExpiry)) {
      alert("Date d'expiration invalide (MM/AA ou MM/YYYY).");
      return;
    }
    if (!data.cardCvv || data.cardCvv.toString().length < 3) {
      alert("CVV invalide.");
      return;
    }

    const payload: PaymentData = {
      paymentMethod: "card",
      cardNumber: digits,
      cardExpiry: data.cardExpiry,
      cardCvv: data.cardCvv,
      saveCard: !!data.saveCard,
    };

    onSubmit(payload);
  };

  const supported: CardBrand[] = ["visa", "mastercard", "amex", "maestro"];

  /** --- Fields : on utilise `render` pour custom UI (form param est fourni) --- */
  const fields: IFieldConfig[] = [
    {
      name: "paymentMethod",
      label: "Moyen de paiement",
      type: "text",
      hidden: true,
      value: "card",
      colSpan: 12,
    },
    {
      name: "cardNumber",
      label: "Numéro de carte",
      type: "text",
      colSpan: 12,
      render: (field, form) => {
        const { register, watch, setValue, formState } = form;
        const val = watch(field.name) ?? "";
        const formatted = formatCardNumber(val);
        const brand = detectCardBrand(val);
        // update detectedBrand and Luhn check (local state)
        React.useEffect(() => {
          setDetectedBrand(brand);
          const ok =
            val.replace(/\D/g, "").length >= 12 ? luhnValid(val) : null;
          setLuhnOk(ok === null ? null : !!ok);
        }, [val]);

        return (
          <div>
            <label className="mb-1 block text-sm font-medium">
              {field.label}
            </label>

            <div className="flex items-center gap-3">
              <div className="flex-1">
                <input
                  {...register(field.name)}
                  value={formatted}
                  onChange={(e) => {
                    // keep only digits in the form value
                    const raw = e.target.value;
                    const digits = raw.replace(/\D/g, "");
                    // set raw digits to form (so validation can use digits)
                    setValue(field.name, digits, {
                      shouldValidate: false,
                      shouldDirty: true,
                    });
                  }}
                  inputMode="numeric"
                  className={`w-full px-3 py-2 border rounded-md ${
                    formState.errors[field.name]
                      ? "border-destructive"
                      : "border-gray-300"
                  }`}
                  placeholder="4242 4242 4242 4242"
                />
                {formState.errors[field.name] && (
                  <p className="text-xs text-destructive mt-1">
                    {(formState.errors as any)[field.name]?.message}
                  </p>
                )}
              </div>

              <div className="w-28">
                <SupportedCard brand={brand} active={brand === detectedBrand} />
              </div>
            </div>

            <div className="mt-2 flex items-center justify-between text-xs text-muted-foreground">
              <div>
                {luhnOk === null ? (
                  "Entrez le numéro"
                ) : luhnOk ? (
                  <span className="text-green-600">Numéro valide</span>
                ) : (
                  <span className="text-destructive">Numéro invalide</span>
                )}
              </div>
              <div className="flex items-center gap-2">
                {supported.map((b) => (
                  <div key={b} title={b} className="opacity-80">
                    <div className="inline-block">
                      {b === "visa" ? (
                        <IconVisa />
                      ) : b === "mastercard" ? (
                        <IconMastercard />
                      ) : b === "amex" ? (
                        <IconAmex />
                      ) : (
                        <IconMaestro />
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        );
      },
    },
    {
      name: "cardExpiry",
      label: "Date d'expiration (MM/AA)",
      type: "text",
      colSpan: 6,
      placeholder: "MM/AA",
      format: (raw: string) => {
        const digits = raw.replace(/\D/g, "");
        if (digits.length <= 2) return digits;
        return `${digits.slice(0, 2)}/${digits.slice(2, 6)}`;
      },
    },
    {
      name: "cardCvv",
      label: "CVV",
      type: "text",
      colSpan: 6,
      maxLength: 4,
      placeholder: "123",
      inputMode: "numeric",
    },
    {
      name: "saveCard",
      label: "Enregistrer la carte pour une prochaine fois",
      type: "checkbox",
      colSpan: 12,
    },
  ];

  return (
    <Card>
      <CardContent>
        <VStack spacing={6}>
          <div>
            <h3 className="text-lg font-semibold">Paiement par carte</h3>
            <p className="text-sm text-muted-foreground mt-1">
              Nous acceptons les principales cartes bancaires. Les paiements
              sont sécurisés.
            </p>
          </div>

          <Form
            fields={fields}
            columns={2}
            schema={paymentSchema}
            onSubmit={handleSubmit}
            submitLabel={
              isSubmitting ? "Traitement..." : `Payer ${formatCurrency(total)}`
            }
            resetLabel={onBack ? "Retour" : undefined}
            onReset={onBack}
          />

          <div className="text-xs text-muted-foreground">
            <HStack spacing={2} align="center">
              <InfoIcon className="w-4 h-4 text-muted-foreground" />
              <span>
                Les données de carte ne sont pas stockées sur nos serveurs
                (exemple de démo).
              </span>
            </HStack>
          </div>
        </VStack>
      </CardContent>
    </Card>
  );
}
