import { Input } from "@components/ui/input";
import { Search } from "lucide-react";
import { useState } from "react";

// Types pour les props du SearchBar
export interface SearchBarProps {
  // Placeholder
  placeholder?: string;

  // Valeur contrôlée
  value?: string;
  defaultValue?: string;

  // Callbacks
  onChange?: (value: string) => void;
  onSearch?: (value: string) => void;
  onFocus?: () => void;
  onBlur?: () => void;
  onClear?: () => void;

  // Icône
  icon?: React.ReactNode;
  iconPosition?: "left" | "right";
  showIcon?: boolean;

  // Style
  className?: string;
  inputClassName?: string;
  focusRingColor?: string;

  // Comportement
  autoFocus?: boolean;
  disabled?: boolean;
  readOnly?: boolean;

  // Recherche en temps réel
  debounceDelay?: number;
  liveSearch?: boolean;
}

const SearchBar = ({
  placeholder = "Rechercher des deals, restaurants, activités...",
  value,
  defaultValue = "",
  onChange,
  onSearch,
  onFocus,
  onBlur,
  icon,
  iconPosition = "left",
  showIcon = true,
  className = "",
  inputClassName = "",
  focusRingColor = "ring-primary-500",
  autoFocus = false,
  disabled = false,
  readOnly = false,
  debounceDelay = 0,
  liveSearch = false,
}: SearchBarProps) => {
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [internalValue, setInternalValue] = useState(defaultValue);
  const [debounceTimeout, setDebounceTimeout] = useState<number | null>(null);

  // Utiliser la valeur contrôlée si fournie, sinon la valeur interne
  const currentValue = value !== undefined ? value : internalValue;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;

    // Mettre à jour la valeur interne si non contrôlée
    if (value === undefined) {
      setInternalValue(newValue);
    }

    // Appeler onChange si fourni
    if (onChange) {
      onChange(newValue);
    }

    // Recherche en temps réel avec debounce
    if (liveSearch && onSearch) {
      // Annuler le timeout précédent
      if (debounceTimeout) {
        clearTimeout(debounceTimeout);
      }

      // Créer un nouveau timeout
      if (debounceDelay > 0) {
        const timeout = setTimeout(() => {
          onSearch(newValue);
        }, debounceDelay);
        setDebounceTimeout(timeout);
      } else {
        onSearch(newValue);
      }
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && onSearch) {
      onSearch(currentValue);
    }
  };

  const handleFocus = () => {
    setIsSearchFocused(true);
    if (onFocus) {
      onFocus();
    }
  };

  const handleBlur = () => {
    setIsSearchFocused(false);
    if (onBlur) {
      onBlur();
    }
  };

  // Icône par défaut
  const defaultIcon = <Search className="w-4 h-4" />;
  const displayIcon = showIcon ? icon || defaultIcon : null;

  return (
    <div className={`relative ${className}`}>
      <Input
        type="search"
        placeholder={placeholder}
        value={currentValue}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        onFocus={handleFocus}
        onBlur={handleBlur}
        autoFocus={autoFocus}
        disabled={disabled}
        readOnly={readOnly}
        className={`w-full transition-all ${
          isSearchFocused ? `ring-2 ${focusRingColor}` : ""
        } ${inputClassName}`}
        leftIcon={iconPosition === "left" ? displayIcon : undefined}
        rightIcon={iconPosition === "right" ? displayIcon : undefined}
      />
    </div>
  );
};

export default SearchBar;
