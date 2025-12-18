import { type ILanguages } from "@/common/context/I18nContext";
import { type JSX } from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "../ui/dropdown-menu";
import { Button } from "../ui/button";
import { ChevronDown } from "lucide-react";
import { useI18n } from "@hooks/useI18n";

export default function LanguageSelector(): JSX.Element {
  const { changeLanguage, language, availableLanguages } = useI18n();

  const change = (lng: ILanguages): void => {
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
            <ChevronDown className="w-4 h-4 " />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-10">
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
