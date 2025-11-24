import { Link, useNavigate } from "react-router-dom";
import { Sheet, SheetContent, SheetTrigger } from "@components/ui/sheet";
import { Button } from "@components/ui/button";
import {
  Heart,
  LogOut,
  MapPin,
  Menu,
  Settings,
  ShoppingBag,
  User,
} from "lucide-react";
import { useState, type ReactNode } from "react";
import { useAuth } from "@/common/context/AuthContext";
import type { ICategory } from "../types";
import VStack from "@components/VStack";
import { Dropdown } from "@components/Dropdown";

export interface SidebarMenuProps {
  // Compteurs
  favoritesCount?: number;
  cartCount?: number;

  // Localisation
  location?: string;
  locations?: Array<{ label: string; value: string }>;
  showLocationSelector?: boolean;
  onLocationChange?: (location: string) => void;

  // Catégories
  categories?: ICategory[];
  showCategories?: boolean;

  // Navigation links
  profileLink?: string;
  ordersLink?: string;
  favoritesLink?: string;
  settingsLink?: string;
  loginLink?: string;
  registerLink?: string;

  // User info
  userName?: string;
  userEmail?: string;
  userAvatar?: ReactNode;

  // Callbacks
  onLogout?: () => void;
  onLoginClick?: () => void;
  onRegisterClick?: () => void;

  // Menu items personnalisés
  customMenuItems?: Array<{
    label: string;
    icon?: ReactNode;
    href?: string;
    onClick?: () => void;
  }>;

  // Style
  className?: string;
  width?: string;
  side?: "left" | "right";

  // Textes personnalisables
  texts?: {
    myProfile?: string;
    myOrders?: string;
    myFavorites?: string;
    settings?: string;
    logout?: string;
    login?: string;
    register?: string;
    yourCity?: string;
    categories?: string;
  };
}

const SidebarMenu = ({
  favoritesCount = 0,
  cartCount = 0,
  location = "Douala",
  locations = [
    { label: "Douala", value: "Douala" },
    { label: "Yaoundé", value: "Yaoundé" },
    { label: "Bafoussam", value: "Bafoussam" },
    { label: "Garoua", value: "Garoua" },
  ],
  showLocationSelector = true,
  onLocationChange,
  categories = [],
  showCategories = true,
  profileLink = "/profile",
  ordersLink = "/orders",
  favoritesLink = "/favorites",
  settingsLink = "/settings",
  loginLink = "/login",
  registerLink = "/register",
  userName,
  userEmail,
  userAvatar,
  onLogout,
  onLoginClick,
  onRegisterClick,
  customMenuItems,
  className = "",
  width = "w-80",
  side = "right",
  texts = {
    myProfile: "Mon profil",
    myOrders: "Mes commandes",
    myFavorites: "Mes favoris",
    settings: "Paramètres",
    logout: "Se déconnecter",
    login: "Se connecter",
    register: "S'inscrire",
    yourCity: "Votre ville",
    categories: "Catégories",
  },
}: SidebarMenuProps) => {
  const { user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = async () => {
    setMobileMenuOpen(false);
    if (onLogout) {
      onLogout();
    } else {
      await logout();
      navigate(loginLink);
    }
  };

  const handleLogin = () => {
    setMobileMenuOpen(false);
    if (onLoginClick) {
      onLoginClick();
    } else {
      navigate(loginLink);
    }
  };

  const handleRegister = () => {
    setMobileMenuOpen(false);
    if (onRegisterClick) {
      onRegisterClick();
    } else {
      navigate(registerLink);
    }
  };

  const handleLocationSelect = (newLocation: string) => {
    if (onLocationChange) {
      onLocationChange(newLocation);
    }
  };

  const closeMenu = () => setMobileMenuOpen(false);

  // Avatar par défaut
  const defaultAvatar = (
    <div className="w-12 h-12 bg-gradient-to-br from-primary-500 to-teal-600 rounded-full flex items-center justify-center">
      <User className="w-6 h-6 text-white" />
    </div>
  );

  return (
    <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
      <SheetTrigger asChild>
        <Button variant="ghost" size="icon">
          <Menu className="w-6 h-6" />
        </Button>
      </SheetTrigger>
      <SheetContent
        side={side}
        className={`${width} overflow-y-auto p-6 ${className}`}
      >
        <VStack className="space-y-6">
          {/* User Section - Si connecté */}
          {user ? (
            <div className="pb-6 border-b w-full">
              <div className="flex items-center space-x-3 mb-4">
                {userAvatar || defaultAvatar}
                <div>
                  <p className="font-medium">
                    {userName || user?.name || "Utilisateur"}
                  </p>
                  <p className="text-sm text-gray-500">
                    {userEmail || user?.email}
                  </p>
                </div>
              </div>
              <VStack className="space-y-2 w-full">
                <Button
                  variant="ghost"
                  onClick={closeMenu}
                  title={texts.myProfile}
                  leftIcon={<User className="w-4 h-4" />}
                />
                <Button
                  variant="ghost"
                  onClick={closeMenu}
                  title={texts.myOrders}
                  leftIcon={<ShoppingBag className="w-4 h-4" />}
                />
                <Button
                  variant="ghost"
                  onClick={closeMenu}
                  title={texts.myFavorites}
                  leftIcon={<Heart className="w-4 h-4" />}
                />
              </VStack>
            </div>
          ) : (
            /* Login/Register - Si non connecté */
            <VStack className="pb-6 border-b space-y-2 w-full mt-3">
              <Button
                className="w-full bg-primary-600 hover:bg-primary-700"
                onClick={handleRegister}
              >
                {texts.register}
              </Button>
              <Button
                variant="outline"
                className="w-full"
                onClick={handleLogin}
              >
                {texts.login}
              </Button>
            </VStack>
          )}

          {/* Location Selector */}
          {showLocationSelector && (
            <VStack className="pb-6 border-b w-full mt-3">
              <p className="text-sm font-medium text-gray-500 mb-3">
                {texts.yourCity}
              </p>
              <Dropdown
                label={location}
                items={locations.map((loc) => ({
                  value: loc.value,
                  label: loc.label,
                  icon: <MapPin className="w-4 h-4 text-primary-600" />,
                }))}
              />
            </VStack>
          )}

          {/* Categories */}
          {showCategories && categories.length > 0 && (
            <div className="pb-6 border-b">
              <p className="text-sm font-medium text-gray-500 mb-3">
                {texts.categories}
              </p>
              <div className="space-y-2">
                {categories.map((category) => {
                  const Icon = category.icon;
                  return (
                    <Button
                      key={category.name}
                      variant="ghost"
                      className="w-full justify-start"
                      onClick={closeMenu}
                      title={category.name}
                      leftIcon={
                        <Icon className="w-4 h-4 mr-2 text-primary-600" />
                      }
                    />
                  );
                })}
              </div>
            </div>
          )}

          {/* Custom Menu Items */}
          {customMenuItems && customMenuItems.length > 0 && (
            <div className="pb-6 border-b">
              <div className="space-y-2">
                {customMenuItems.map((item, index) => (
                  <Button
                    key={index}
                    variant="ghost"
                    className="w-full justify-start"
                    title={item.label}
                    leftIcon={item.icon}
                    onClick={() => {
                      closeMenu();
                      if (item.onClick) item.onClick();
                    }}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Settings & Logout - Si connecté */}
          {user && (
            <div className="space-y-2">
              <Button
                variant="ghost"
                className="w-full justify-start"
                asChild
                onClick={closeMenu}
              >
                <Link to={settingsLink}>
                  <Settings className="w-4 h-4 mr-2" />
                  {texts.settings}
                </Link>
              </Button>
              <Button
                variant="ghost"
                className="w-full justify-start text-red-600 hover:text-red-700 hover:bg-red-50"
                onClick={handleLogout}
              >
                <LogOut className="w-4 h-4 mr-2" />
                {texts.logout}
              </Button>
            </div>
          )}
        </VStack>
      </SheetContent>
    </Sheet>
  );
};

export default SidebarMenu;
