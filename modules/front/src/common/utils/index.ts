import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Combine plusieurs classes CSS de manière optimale
 * Fusionne les classes Tailwind en évitant les doublons
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export * from "./date";
export * from "./formatCurrency";
export * from "./i18nLocale";
export * from "./image";
export * from "./string";
export * from "./ScrollToTop";

