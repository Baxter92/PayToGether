import { Store } from "lucide-react";
import { Link } from "react-router-dom";
import type { ICategory } from "../types";
import { Dropdown } from "@components/Dropdown";
import type { ReactNode } from "react";

export interface CategoriesBarProps {
  // Catégories
  categories: ICategory[];

  // Dropdown "Toutes les catégories"
  showDropdown?: boolean;
  dropdownLabel?: string;
  dropdownIcon?: ReactNode;

  // Affichage des catégories
  showCategoryIcons?: boolean;
  maxVisibleCategories?: number;

  // Callbacks
  onCategoryClick?: (category: ICategory) => void;
  onDropdownSelect?: (categoryName: string) => void;

  // Layout
  layout?: "horizontal" | "grid";
  spacing?: "compact" | "normal" | "relaxed";

  // Styles
  className?: string;
  navClassName?: string;
  linkClassName?: string;
  activeClassName?: string;

  // Couleurs
  iconColor?: string;
  textColor?: string;
  hoverColor?: string;

  // Responsive
  hideOnMobile?: boolean;

  // Textes personnalisables
  texts?: {
    allCategories?: string;
    viewAll?: string;
  };

  // Mode de rendu personnalisé
  renderCategory?: (category: ICategory) => ReactNode;
}

const CategoriesBar = ({
  categories,
  showDropdown = true,
  dropdownLabel,
  dropdownIcon,
  showCategoryIcons = true,
  maxVisibleCategories,
  onCategoryClick,
  onDropdownSelect,
  layout = "horizontal",
  spacing = "normal",
  className = "",
  navClassName = "",
  linkClassName = "",
  activeClassName = "",
  iconColor = "text-primary-600",
  textColor = "text-gray-700",
  hoverColor = "hover:text-primary-600",
  hideOnMobile = false,
  texts = {
    allCategories: "Toutes les catégories",
    viewAll: "Voir tout",
  },
  renderCategory,
}: CategoriesBarProps) => {
  // Déterminer les catégories visibles
  const visibleCategories = maxVisibleCategories
    ? categories.slice(0, maxVisibleCategories)
    : categories;

  const hasMoreCategories =
    maxVisibleCategories && categories.length > maxVisibleCategories;

  // Espacement selon le mode
  const spacingClasses = {
    compact: "space-x-4",
    normal: "space-x-8",
    relaxed: "space-x-12",
  };

  // Layout classes
  const layoutClasses = {
    horizontal: `flex items-center ${spacingClasses[spacing]}`,
    grid: "grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4",
  };

  // Hauteur selon le layout
  const heightClass = layout === "horizontal" ? "h-12" : "py-4";

  const handleCategoryClick = (category: ICategory, e?: React.MouseEvent) => {
    if (onCategoryClick) {
      e?.preventDefault();
      onCategoryClick(category);
    }
  };

  const handleDropdownSelect = (value: string) => {
    if (onDropdownSelect) {
      onDropdownSelect(value);
    }
  };

  // Rendu par défaut d'une catégorie
  const defaultRenderCategory = (category: ICategory) => {
    const Icon = category.icon;
    return (
      <Link
        key={category.name}
        to={category.href}
        onClick={(e) => handleCategoryClick(category, e)}
        className={`flex items-center space-x-2 text-sm font-medium ${textColor} ${hoverColor} transition-colors ${linkClassName} ${activeClassName}`}
      >
        {showCategoryIcons && Icon && (
          <Icon className={`w-4 h-4 ${iconColor}`} />
        )}
        <span>{category.name}</span>
      </Link>
    );
  };

  // Rendu par défaut d'un item dropdown
  const defaultRenderDropdownItem = (category: ICategory) => ({
    value: category.name,
    label: category.name,
    icon:
      showCategoryIcons && category.icon ? (
        <category.icon className="w-4 h-4" />
      ) : undefined,
    onClick: () => handleDropdownSelect(category.name),
  });

  // Icône du dropdown
  const defaultDropdownIcon = <Store className={`w-4 h-4 ${iconColor}`} />;

  return (
    <div
      className={`max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 ${
        hideOnMobile ? "hidden lg:block" : ""
      } ${className}`}
    >
      <nav
        className={`${layoutClasses[layout]} ${heightClass} ${navClassName}`}
      >
        {/* Dropdown "Toutes les catégories" */}
        {showDropdown && (
          <Dropdown
            label={dropdownLabel || texts.allCategories}
            triggerOptions={{
              leftIcon: dropdownIcon || defaultDropdownIcon,
            }}
            items={categories.map((category) =>
              defaultRenderDropdownItem(category)
            )}
          />
        )}

        {/* Liste des catégories visibles */}
        {visibleCategories.map((category) =>
          renderCategory
            ? renderCategory(category)
            : defaultRenderCategory(category)
        )}

        {/* Lien "Voir tout" si catégories masquées */}
        {hasMoreCategories && (
          <Link
            to="/categories"
            className={`text-sm font-medium ${textColor} ${hoverColor} transition-colors underline`}
          >
            {texts.viewAll} ({categories.length - maxVisibleCategories!} +)
          </Link>
        )}
      </nav>
    </div>
  );
};

export default CategoriesBar;
