import { useI18n, type ILanguages } from "@/context/I18nContext";
import React from "react";
import { Button } from "./ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";

export default function LanguageSelector() {
  const { changeLanguage, language, availableLanguages } = useI18n();

  const change = (lng: ILanguages) => {
    changeLanguage(lng);
  };

  return (
    <div className="cursor-pointer">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" className="flex items-center space-x-2">
            <span className="text-sm font-medium">
              {language.toUpperCase()}
            </span>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-56">
          <DropdownMenuLabel>Changer de langue</DropdownMenuLabel>
          <DropdownMenuSeparator />
          {availableLanguages?.map?.((lng) => (
            <DropdownMenuItem
              key={lng}
              onClick={() => change(lng as ILanguages)}
              className="cursor-pointer"
            >
              {lng.toUpperCase()}
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}
