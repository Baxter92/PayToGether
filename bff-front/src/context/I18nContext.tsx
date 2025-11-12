import { createContext, useContext, useMemo } from "react";
import { useTranslation } from "react-i18next";
import type { TFunction, i18n as I18nType } from "i18next";

export type ILanguages = "fr" | "en";

export type I18nContextProps = {
  t: TFunction;
  i18n: I18nType;
  /** code langue courant */
  language: ILanguages;
  /** change la langue */
  changeLanguage: (lng: ILanguages) => void;
  /** langues disponibles (supportedLngs) */
  availableLanguages: readonly string[];
};

/** default safe stubs */
const defaultT: TFunction = ((k: string) => k) as TFunction;

export const I18nContext = createContext<I18nContextProps>({
  t: defaultT,
  i18n: {} as unknown as I18nType,
  language: "fr",
  changeLanguage: () => {},
  availableLanguages: [],
});

export function I18nProvider({ children }: { children: React.ReactNode }) {
  const { t, i18n } = useTranslation();
  const language = (i18n.language ?? "fr") as ILanguages;
  const changeLanguage = (lng: ILanguages) => {
    i18n.changeLanguage(lng);
  };
  const availableLanguages = i18n.options?.supportedLngs || [];

  const value = useMemo(
    () => ({
      t,
      i18n,
      language,
      changeLanguage,
      availableLanguages: availableLanguages.filter((lng) => lng !== "cimode"),
    }),
    [t, i18n, language, availableLanguages]
  );

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

/**
 * useT(namespace?)
 *
 * - si namespace fourni : renvoie une fonction tNamespaced(key, opts?) -> t('namespace:key', opts)
 * - sinon : renvoie la fonction t du context
 */
export function useT(namespace?: string) {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error("useT must be used inside I18nProvider");

  const { t } = ctx;

  if (!namespace) return t;

  // wrapper qui préfixe la clé si elle n'est pas déjà nommée "ns:key"
  const tNamespaced: TFunction = ((key: string, opts?: any) => {
    const k =
      typeof key === "string" && key.indexOf(":") === -1
        ? `${namespace}:${key}`
        : key;
    return t(k, opts);
  }) as TFunction;

  // expose aussi les méthodes utiles (comme t.exists) si disponibles
  // @ts-ignore - propager les méthodes dynamiques si besoin
  tNamespaced.exists = (k: string, opts?: any) => {
    const key = k.indexOf(":") === -1 ? `${namespace}:${k}` : k;
    // @ts-ignore
    return t.exists ? t.exists(key, opts) : false;
  };

  return tNamespaced;
}

/**
 * useI18n(namespace?)
 *
 * renvoie { i18n, t } où t est soit i18n.t (non prefixed) soit i18n.t lié au namespace.
 */
export function useI18n(namespace?: string) {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error("useI18n must be used inside I18nProvider");

  const { t, ...rest } = ctx;

  if (!namespace) {
    return { t: t, ...rest };
  }

  const tNs: TFunction = ((key: string, opts?: any) => {
    const k =
      typeof key === "string" && key.indexOf(":") === -1
        ? `${namespace}:${key}`
        : key;
    return t(k, opts);
  }) as TFunction;

  return { t: tNs, ...rest };
}
