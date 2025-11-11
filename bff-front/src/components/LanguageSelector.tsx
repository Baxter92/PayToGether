import { useI18n, type ILanguages } from "@/context/I18nContext";
import React from "react";
import { Button } from "./ui/button";

export default function LanguageSelector() {
  const { changeLanguage } = useI18n();

  const change = (lng: ILanguages) => {
    changeLanguage(lng);
  };

  return (
    <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
      <Button onClick={() => change("fr")} aria-label="Français">
        FR
      </Button>
      <Button onClick={() => change("en")} aria-label="English">
        EN
      </Button>
    </div>
  );
}
