import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import HttpBackend from "i18next-http-backend";
import LanguageDetector from "i18next-browser-languagedetector";
import { setLocale } from "@/common/utils/i18nLocale";

i18n
  // charge les fichiers de traduction depuis /locales/{{lng}}/{{ns}}.json
  .use(HttpBackend)
  // detecte la langue du navigateur / localStorage / querystring
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: "fr-CA",
    supportedLngs: ["fr-CA", "en-CA"],
    ns: [
      "app",
      "nav",
      "header",
      "footer",
      "home",
      "deals",
      "search",
      "auth",
      "checkout",
      "orders",
      "orderSuccess",
      "profile",
      "favorites",
      "notFound",
      "common",
      "admin",
      "status",
      "roles",
      "form",
      "validation",
      "filters",
      "table",
    ],
    defaultNS: "common",
    debug: false,
    interpolation: {
      escapeValue: false,
    },
    backend: {
      loadPath: "/locales/{{lng}}/{{ns}}.json",
    },
    react: {
      useSuspense: true,
    },
  });

i18n.on("languageChanged", (lng) => {
  setLocale(lng);
});

export default i18n;
