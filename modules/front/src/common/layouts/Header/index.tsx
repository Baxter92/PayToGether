import { useState, type JSX, type ReactNode } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@context/AuthContext";
import { useI18n } from "@hooks/useI18n";
import {
  MapPin,
  Heart,
  User,
  LogOut,
  Settings,
  ShoppingBag,
} from "lucide-react";
import { Button } from "@components/ui/button";
import { Badge } from "@components/ui/badge";
import CategoriesBar from "./components/CategoriesBar";
import SidebarMenu from "./components/SidebarMenu";
import LanguageSelector from "@components/LanguageSelector";
import { Dropdown } from "@components/Dropdown";
import SearchBar from "./components/SearchBar";
import { PATHS } from "@/common/constants/path";
import { toast } from "sonner";
import type { ICategory } from "@/common/containers/CategoryCard/type";

// Types pour les props du Header
export interface HeaderProps {
  appName?: string;
  appLogo?: ReactNode;
  topBanner?: ReactNode;
  showLocationSelector?: boolean;
  defaultLocation?: string;
  locations?: Array<{ label: string; value: string }>;
  onLocationChange?: (location: string) => void;
  showSearchBar?: boolean;
  searchPlaceholder?: string;
  onSearch?: (query: string) => void;
  showFavorites?: boolean;
  favoritesCount?: number;
  favoritesLink?: string;
  showCart?: boolean;
  cartCount?: number;
  cartLink?: string;
  categories?: ICategory[];
  showCategoriesBar?: boolean;
  showLanguageSelector?: boolean;
  sticky?: boolean;
  className?: string;
  headerBgColor?: string;
  primaryColor?: string;
  customUserMenuItems?: Array<{
    value: string;
    label: string;
    icon?: ReactNode;
    onClick?: () => void;
    href?: string;
  }>;
  onLoginClick?: () => void;
  onRegisterClick?: () => void;
  onLogoutClick?: () => void;
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
  appName = "DealToGether",
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
  favoritesLink = PATHS.FAVORITES,
  showCart = true,
  cartCount = 0,
  cartLink = "/cart",
  categories = [],
  showCategoriesBar = true,
  showLanguageSelector = true,
  sticky = false,
  className = "",
  headerBgColor = "bg-white",
  customUserMenuItems,
  onLoginClick,
  onRegisterClick,
  onLogoutClick,
  texts = {},
}: HeaderProps): JSX.Element => {
  const { t: tNav } = useI18n("nav");
  const { t: tHeader } = useI18n("header");
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const locationRoute = useLocation();
  const [location, setLocation] = useState(defaultLocation);

  // Merge default texts with i18n translations
  const mergedTexts = {
    login: texts.login || tNav("login"),
    register: texts.register || tNav("register"),
    myAccount: texts.myAccount || tNav("myAccount"),
    myProfile: texts.myProfile || tNav("myProfile"),
    myOrders: texts.myOrders || tNav("myOrders"),
    myFavorites: texts.myFavorites || tNav("myFavorites"),
    settings: texts.settings || tNav("settings"),
    logout: texts.logout || tNav("logout"),
  };

  const handleLogout = async (): Promise<void> => {
    if (onLogoutClick) {
      onLogoutClick();
    } else {
      await logout();
      navigate(PATHS.LOGIN);
    }
    toast.success(tHeader("logoutSuccess"));
  };

  const handleLogin = (): void => {
    if (onLoginClick) {
      onLoginClick();
    } else {
      navigate(PATHS.LOGIN, { state: { from: locationRoute } });
    }
  };

  const handleRegister = (): void => {
    if (onRegisterClick) {
      onRegisterClick();
    } else {
      navigate("/register");
    }
  };

  const handleLocationChange = (newLocation: string): void => {
    setLocation(newLocation);
    if (onLocationChange) {
      onLocationChange(newLocation);
    }
  };

  // Menu utilisateur par défaut ou personnalisé
  const userMenuItems = customUserMenuItems || [
    {
      value: "profile",
      label: mergedTexts.myProfile,
      icon: <User className="w-4 h-4 mr-2" />,
      onClick: () => navigate(PATHS.PROFILE),
    },
    {
      value: "orders",
      label: mergedTexts.myOrders,
      icon: <ShoppingBag className="w-4 h-4 mr-2" />,
      onClick: () => navigate(PATHS.ORDERS),
    },
    {
      value: "favorites",
      label: mergedTexts.myFavorites,
      icon: <Heart className="w-4 h-4 mr-2" />,
      onClick: () => navigate(PATHS.FAVORITES),
    },
    {
      value: "settings",
      label: mergedTexts.settings,
      icon: <Settings className="w-4 h-4 mr-2" />,
      onClick: () => navigate(PATHS.USERSITTINGS),
    },
    {
      value: "logout",
      label: mergedTexts.logout,
      icon: <LogOut className="w-4 h-4 mr-2" />,
      onClick: handleLogout,
    }
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
          <Link to="/" className="flex items-center space-x-2 shrink-0 group">
            {appLogo || (
              <span className="text-2xl font-extrabold font-[family-name:var(--font-heading)] bg-gradient-to-r from-primary-500 to-primary-700 bg-clip-text text-transparent group-hover:from-primary-600 group-hover:to-primary-800 transition-all duration-300">
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
                onChange={(value) =>
                  handleLocationChange(Array.isArray(value) ? value[0] : value)
                }
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
            {user ? (
              <Dropdown
                label={
                  <>
                    <User className="w-5 h-5 mr-2" />
                    <span className="hidden xl:inline">
                      {mergedTexts.myAccount}
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
                  {mergedTexts.login}
                </Button>
                <Button
                  size="sm"
                  className="bg-primary-600 hover:bg-primary-700"
                  onClick={handleRegister}
                >
                  {mergedTexts.register}
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
              <LanguageSelector variant="icon" />
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
          <CategoriesBar
            categories={categories}
            maxVisibleCategories={5}
            onCategoryClick={(c) => {
              navigate(PATHS.CATEGORIES(c.href));
            }}
          />
        </div>
      )}
    </header>
  );
};

export default Header;
