import { type ILanguages } from "@/common/context/I18nContext";
import { type JSX } from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "../ui/dropdown-menu";
import { Button } from "../ui/button";
import { ChevronDown, Globe } from "lucide-react";
import { useI18n } from "@hooks/useI18n";

const languageConfig: Record<ILanguages, { label: string }> = {
  "fr-CA": { label: "Français" },
  "en-CA": { label: "English" },
};

interface LanguageSelectorProps {
  variant?: "icon" | "full" | "compact";
  className?: string;
}

export default function LanguageSelector({
  variant = "icon",
  className = "",
}: LanguageSelectorProps): JSX.Element {
  const { changeLanguage, language, availableLanguages } = useI18n();

  const handleChange = (lng: ILanguages): void => {
    changeLanguage(lng);
    // Force re-render by triggering a state update event
    window.dispatchEvent(new CustomEvent("languageChanged", { detail: lng }));
  };

  const currentConfig = languageConfig[language] || {
    label: language,
    flag: "🌐",
  };

  return (
    <div className={`cursor-pointer ${className}`}>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          {variant === "icon" ? (
            <Button variant="ghost" size="icon" className="h-9 w-9">
              <Globe className="h-5 w-5" />
            </Button>
          ) : variant === "full" ? (
            <Button variant="outline" className="flex items-center gap-2 px-3">
              <span className="text-sm font-medium">{currentConfig.label}</span>
              <ChevronDown className="w-4 h-4 text-muted-foreground" />
            </Button>
          ) : (
            <Button variant="ghost" className="flex items-center gap-1.5 px-2">
              <span className="text-sm font-medium hidden sm:inline">
                {language.split("-")[0].toUpperCase()}
              </span>
              <ChevronDown className="w-3.5 h-3.5 text-muted-foreground" />
            </Button>
          )}
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="min-w-35">
          {availableLanguages?.map?.((lng) => {
            const config = languageConfig[lng as ILanguages] || {
              label: lng,
              flag: "🌐",
            };
            return (
              <DropdownMenuItem
                key={lng}
                onClick={() => handleChange(lng as ILanguages)}
                className={`cursor-pointer flex items-center gap-2 ${
                  language === lng ? "bg-primary" : ""
                }`}
              >
                <span>{config.label}</span>
              </DropdownMenuItem>
            );
          })}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}
