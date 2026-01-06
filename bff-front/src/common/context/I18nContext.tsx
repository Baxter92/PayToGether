import { createContext, useMemo } from "react";
import { useTranslation } from "react-i18next";
import type { TFunction, i18n as I18nType } from "i18next";

export type ILanguages = "fr-CA" | "en-CA";

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
  language: "fr-CA",
  changeLanguage: () => {},
  availableLanguages: [],
});

export function I18nProvider({ children }: { children: React.ReactNode }) {
  const { t, i18n } = useTranslation();
  const language = (i18n.language ?? "fr-CA") as ILanguages;
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
    [t, i18n, language, availableLanguages, changeLanguage]
  );

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}
