import { useState, type ReactNode } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { useI18n } from "@/context/I18nContext";
import {
  MapPin,
  Tag,
  Gift,
  Utensils,
  Dumbbell,
  Sparkles,
  Heart,
  User,
  LogOut,
  Settings,
  ShoppingBag,
  type LucideIcon,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import CategoriesBar from "./components/CategoriesBar";
import SidebarMenu from "./components/SidebarMenu";
import LanguageSelector from "../LanguageSelector";
import { Dropdown } from "../Dropdown";
import SearchBar from "./components/SearchBar";

// Types pour les props du Header
export interface HeaderProps {
  // Configuration de base
  appName?: string;
  appLogo?: ReactNode;

  // Bannière promo
  topBanner?: ReactNode;

  // Localisation
  showLocationSelector?: boolean;
  defaultLocation?: string;
  locations?: Array<{ label: string; value: string }>;
  onLocationChange?: (location: string) => void;

  // Recherche
  showSearchBar?: boolean;
  searchPlaceholder?: string;
  onSearch?: (query: string) => void;

  // Navigation
  showFavorites?: boolean;
  favoritesCount?: number;
  favoritesLink?: string;

  showCart?: boolean;
  cartCount?: number;
  cartLink?: string;

  // Catégories
  categories?: Array<{
    name: string;
    icon: LucideIcon;
    href: string;
  }>;
  showCategoriesBar?: boolean;

  // Langue
  showLanguageSelector?: boolean;

  // Comportement sticky
  sticky?: boolean;

  // Styles personnalisés
  className?: string;
  headerBgColor?: string;
  primaryColor?: string;

  // Menu utilisateur personnalisé
  customUserMenuItems?: Array<{
    value: string;
    label: string;
    icon?: ReactNode;
    onClick?: () => void;
    href?: string;
  }>;

  // Callbacks
  onLoginClick?: () => void;
  onRegisterClick?: () => void;
  onLogoutClick?: () => void;

  // Textes personnalisables (i18n)
  texts?: {
    login?: string;
    register?: string;
    myAccount?: string;
    myProfile?: string;
    myOrders?: string;
    myFavorites?: string;
    settings?: string;
    logout?: string;
  };
}

const Header = ({
  // Valeurs par défaut
  appName = "PayToGether",
  appLogo,
  topBanner,
  showLocationSelector = true,
  defaultLocation = "Douala",
  locations = [],
  onLocationChange,
  showSearchBar = true,
  searchPlaceholder,
  onSearch,
  showFavorites = true,
  favoritesCount = 0,
  favoritesLink = "/favorites",
  showCart = true,
  cartCount = 0,
  cartLink = "/cart",
  categories = [],
  showCategoriesBar = true,
  showLanguageSelector = true,
  sticky = true,
  className = "",
  headerBgColor = "bg-white",
  customUserMenuItems,
  onLoginClick,
  onRegisterClick,
  onLogoutClick,
  texts = {
    login: "Se connecter",
    register: "S'inscrire",
    myAccount: "Mon compte",
    myProfile: "Mon profil",
    myOrders: "Mes commandes",
    myFavorites: "Mes favoris",
    settings: "Paramètres",
    logout: "Se déconnecter",
  },
}: HeaderProps) => {
  const { user, logout } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();
  const [location, setLocation] = useState(defaultLocation);

  const handleLogout = async () => {
    if (onLogoutClick) {
      onLogoutClick();
    } else {
      await logout();
      navigate("/login");
    }
  };

  const handleLogin = () => {
    if (onLoginClick) {
      onLoginClick();
    } else {
      navigate("/login");
    }
  };

  const handleRegister = () => {
    if (onRegisterClick) {
      onRegisterClick();
    } else {
      navigate("/register");
    }
  };

  const handleLocationChange = (newLocation: string) => {
    setLocation(newLocation);
    if (onLocationChange) {
      onLocationChange(newLocation);
    }
  };

  // Menu utilisateur par défaut ou personnalisé
  const userMenuItems = customUserMenuItems || [
    {
      value: "profile",
      label: texts.myProfile || "Mon profil",
      icon: <User className="w-4 h-4 mr-2" />,
      onClick: () => navigate("/profile"),
    },
    {
      value: "orders",
      label: texts.myOrders || "Mes commandes",
      icon: <ShoppingBag className="w-4 h-4 mr-2" />,
      onClick: () => navigate("/orders"),
    },
    {
      value: "favorites",
      label: texts.myFavorites || "Mes favoris",
      icon: <Heart className="w-4 h-4 mr-2" />,
      onClick: () => navigate("/favorites"),
    },
    {
      value: "settings",
      label: texts.settings || "Paramètres",
      icon: <Settings className="w-4 h-4 mr-2" />,
      onClick: () => navigate("/settings"),
    },
    {
      value: "logout",
      label: texts.logout || "Se déconnecter",
      icon: <LogOut className="w-4 h-4 mr-2" />,
      onClick: handleLogout,
    },
  ];

  return (
    <header
      className={`${
        sticky ? "sm:sticky" : ""
      } top-0 z-50 w-full border-b ${headerBgColor} shadow-sm ${className}`}
    >
      {/* Top Bar - Promo Banner */}
      {topBanner}

      {/* Main Header */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-2">
        <div className="flex items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2 shrink-0">
            {appLogo || (
              <span className="text-xl font-bold bg-linear-to-r from-primary-600 to-primary-900 bg-clip-text text-transparent">
                {appName}
              </span>
            )}
          </Link>

          {/* Location Selector - Desktop */}
          {showLocationSelector && (
            <div className="hidden lg:flex items-center ml-6">
              <Dropdown
                label={
                  <>
                    <MapPin className="w-4 h-4 text-primary-600" />
                    <span className="font-medium">{location}</span>
                  </>
                }
                items={locations}
                selectedValue={location}
                onChange={handleLocationChange}
                className="px-3"
                triggerOptions={{
                  variant: "ghost",
                }}
              />
            </div>
          )}

          {/* Search Bar - Desktop Only */}
          {showSearchBar && (
            <div className="hidden lg:flex flex-1 max-w-2xl flex-col">
              <SearchBar placeholder={searchPlaceholder} onSearch={onSearch} />
            </div>
          )}

          {/* Desktop Navigation */}
          <nav className="hidden lg:flex items-center space-x-4 mx-1">
            {/* Favorites */}
            {showFavorites && (
              <Button variant="ghost" size="sm" asChild>
                <Link
                  to={favoritesLink}
                  className="inline-flex items-center relative"
                >
                  <Heart className="w-5 h-5" />
                  {favoritesCount > 0 && (
                    <span
                      aria-hidden="true"
                      className="absolute -top-2 -right-2 h-5 w-5 flex items-center justify-center p-0 text-xs bg-red-500 rounded-full"
                    >
                      {favoritesCount}
                    </span>
                  )}
                </Link>
              </Button>
            )}

            {/* Cart */}
            {showCart && (
              <Button variant="ghost" size="sm" asChild>
                <Link to={cartLink} className="relative">
                  <ShoppingBag className="w-5 h-5" />
                  {cartCount > 0 && (
                    <Badge className="absolute -top-2 -right-2 h-5 w-5 flex items-center justify-center p-0 text-xs bg-primary-600">
                      {cartCount}
                    </Badge>
                  )}
                </Link>
              </Button>
            )}

            {/* User Menu */}
            {!user ? (
              <Dropdown
                label={
                  <>
                    <User className="w-5 h-5 mr-2" />
                    <span className="hidden xl:inline">
                      {texts.myAccount || "Mon compte"}
                    </span>
                  </>
                }
                contentClassName="w-56"
                triggerOptions={{
                  variant: "ghost",
                }}
                items={userMenuItems}
              />
            ) : (
              <div className="flex items-center space-x-2 ml-2">
                <Button variant="ghost" size="sm" onClick={handleLogin}>
                  {texts.login || "Se connecter"}
                </Button>
                <Button
                  size="sm"
                  className="bg-primary-600 hover:bg-primary-700"
                  onClick={handleRegister}
                >
                  {texts.register || "S'inscrire"}
                </Button>
              </div>
            )}
          </nav>

          {/* Mobile Menu Button */}
          <div className="flex lg:hidden items-center space-x-2">
            {/* Mobile Cart */}
            {showCart && (
              <Button variant="ghost" size="icon" className="relative" asChild>
                <Link to={cartLink}>
                  <ShoppingBag className="w-5 h-5" />
                  {cartCount > 0 && (
                    <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs bg-primary-600">
                      {cartCount}
                    </Badge>
                  )}
                </Link>
              </Button>
            )}

            {/* Language Selector - Mobile Only */}
            {showLanguageSelector && (
              <div className="flex">
                <LanguageSelector />
              </div>
            )}

            {/* Mobile Menu */}
            <SidebarMenu
              categories={categories}
              location={location}
              onLocationChange={handleLocationChange}
              favoritesCount={favoritesCount}
            />
          </div>

          {/* Language Selector - Desktop Only */}
          {showLanguageSelector && (
            <div className="hidden lg:flex ml-4">
              <LanguageSelector />
            </div>
          )}
        </div>

        {/* Search Bar - Mobile Only */}
        {showSearchBar && (
          <div className="lg:hidden">
            <SearchBar placeholder={searchPlaceholder} onSearch={onSearch} />
          </div>
        )}
      </div>

      {/* Categories Bar - Desktop Only */}
      {showCategoriesBar && (
        <div className="hidden lg:block border-t bg-gray-50">
          <CategoriesBar categories={categories} />
        </div>
      )}
    </header>
  );
};

export default Header;
