import { getLocale } from "./i18nLocale";

export function formatCurrency(
  amount: number,
  currency: string = "CAD",
  locale?: string
): string {
  return new Intl.NumberFormat(locale ?? getLocale(), {
    style: "currency",
    currency,
  }).format(amount);
}
