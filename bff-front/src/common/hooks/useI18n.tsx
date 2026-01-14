import { useContext, useEffect, useState, useCallback } from "react";
import { I18nContext } from "../context/I18nContext";
import type { TFunction } from "i18next";

/**
 * useT(namespace?)
 *
 * Hook pour récupérer une fonction de traduction `t`.
 *
 * - Si `namespace` est fourni : renvoie une fonction `tNamespaced(key, opts?)`
 *   qui préfixe automatiquement les clés avec `namespace:` si nécessaire.
 * - Sinon : renvoie directement la fonction `t` du contexte.
 *
 * Exemples :
 *   const t = useT();            // t('hello')
 *   const t = useT('auth');      // t('auth:login')
 */
export function useT(namespace?: string) {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error("useT must be used inside I18nProvider");

  const { t } = ctx;

  if (!namespace) return t;

  // wrapper qui préfixe les clés si elles ne contiennent pas de namespace
  const tNamespaced: TFunction = ((key: string, opts?: any) => {
    const k =
      typeof key === "string" && key.indexOf(":") === -1
        ? `${namespace}:${key}`
        : key;
    return t(k, opts);
  }) as TFunction;

  // Propagation des méthodes dynamiques (comme t.exists) si elles existent
  // @ts-expect-error - tNamespaced est étendu dynamiquement
  tNamespaced.exists = (k: string, opts?: any) => {
    const key = k.indexOf(":") === -1 ? `${namespace}:${k}` : k;
    // @ts-expect-error - t.exists peut ne pas être typé sur TFunction
    return t.exists ? t.exists(key, opts) : false;
  };

  return tNamespaced;
}

/**
 * useI18n(namespace?)
 *
 * Hook pour récupérer un objet I18n complet.
 *
 * - Renvoie `{ t, ...rest }` :
 *    - `t` : fonction de traduction (namespacée si `namespace` fourni)
 *    - `rest` : reste des propriétés du contexte I18n (i18n, language, etc.)
 *
 * Exemples :
 *   const { t, i18n } = useI18n();         // t('hello'), i18n.changeLanguage(...)
 *   const { t, i18n } = useI18n('auth');   // t('auth:login'), i18n.changeLanguage(...)
 */
export function useI18n(namespace?: string) {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error("useI18n must be used inside I18nProvider");

  const { t, ...rest } = ctx;

  if (!namespace) {
    return { t, ...rest };
  }

  // wrapper pour préfixer les clés si namespace fourni
  const tNs: TFunction = ((key: string, opts?: any) => {
    const k =
      typeof key === "string" && key.indexOf(":") === -1
        ? `${namespace}:${key}`
        : key;
    return t(k, opts);
  }) as TFunction;

  return { t: tNs, ...rest };
}

/**
 * useLanguageRefresh
 * 
 * Hook that forces a re-render when the language changes.
 * Use this in components that depend on locale-sensitive formatting
 * (like formatCurrency) to ensure they update when language changes.
 */
export function useLanguageRefresh(): number {
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    const handleLanguageChange = () => {
      setRefreshKey((prev) => prev + 1);
    };

    window.addEventListener("languageChanged", handleLanguageChange);
    return () => {
      window.removeEventListener("languageChanged", handleLanguageChange);
    };
  }, []);

  return refreshKey;
}

/**
 * useFormattedCurrency
 * 
 * Hook that returns a memoized currency formatter that updates when language changes.
 */
export function useFormattedCurrency() {
  const { language } = useI18n();
  useLanguageRefresh();

  const formatCurrency = useCallback(
    (amount: number, currency: string = "XAF"): string => {
      return new Intl.NumberFormat(language, {
        style: "currency",
        currency,
      }).format(amount);
    },
    [language]
  );

  return formatCurrency;
}
