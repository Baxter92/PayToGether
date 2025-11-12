import { useState } from "react";
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
  ChevronDown,
} from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import CategoriesBar from "./components/CategoriesBar";
import type { ICategory } from "./types";
import SearchBar from "./components/SearchBar";
import SidebarMenu from "./components/SidebarMenu";

const Header = () => {
  const { user, logout } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();
  const [location, setLocation] = useState("Douala");

  // Nombre de favoris et panier (à remplacer par vos vraies données)
  const favoritesCount = 3;
  const cartCount = 2;

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  const categories: ICategory[] = [
    { name: "Restaurants", icon: Utensils, href: "/deals/restaurants" },
    { name: "Beauté & Spa", icon: Sparkles, href: "/deals/beauty" },
    { name: "Sport & Fitness", icon: Dumbbell, href: "/deals/fitness" },
    { name: "Cadeaux", icon: Gift, href: "/deals/gifts" },
    { name: "Shopping", icon: ShoppingBag, href: "/deals/shopping" },
  ];

  return (
    <header className="top-0 z-50 w-full border-b bg-white shadow-sm">
      {/* Top Bar - Promo Banner */}
      <div className="bg-linear-to-r from-primary-500 to-teal-500 text-white py-2">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-center text-sm font-medium">
            <Tag className="w-4 h-4 mr-2" />
            Jusqu'à 70% de réduction sur les activités locales
          </div>
        </div>
      </div>

      {/* Main Header */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-2">
        <div className="flex items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2 shrink-0">
            <span className="text-xl font-bold bg-linear-to-r from-primary-600 to-primary-900 bg-clip-text text-transparent">
              PayToGether
            </span>
          </Link>

          {/* Location Selector - Desktop */}
          <div className="hidden lg:flex items-center ml-6">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="flex items-center space-x-2">
                  <MapPin className="w-4 h-4 text-primary-600" />
                  <span className="font-medium">{location}</span>
                  <ChevronDown className="w-4 h-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="start" className="w-48">
                <DropdownMenuLabel>Changer de ville</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => setLocation("Douala")}>
                  Douala
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setLocation("Yaoundé")}>
                  Yaoundé
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setLocation("Bafoussam")}>
                  Bafoussam
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setLocation("Garoua")}>
                  Garoua
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>

          {/* Search Bar - Desktop Only */}
          <div className="hidden lg:flex flex-1 max-w-2xl  flex-col space-y-2 py-1">
            <SearchBar />
          </div>

          {/* Desktop Navigation */}
          <nav className="hidden lg:flex items-center space-x-1">
            {/* Favorites */}
            <Button variant="ghost" size="sm" className="relative" asChild>
              <Link to="/favorites">
                <Heart className="w-5 h-5" />
                {favoritesCount > 0 && (
                  <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs bg-red-500">
                    {favoritesCount}
                  </Badge>
                )}
              </Link>
            </Button>
            {/* Cart */}
            <Button variant="ghost" size="sm" className="relative" asChild>
              <Link to="/cart">
                <ShoppingBag className="w-5 h-5" />
                {cartCount > 0 && (
                  <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs bg-primary-600">
                    {cartCount}
                  </Badge>
                )}
              </Link>
            </Button>
            {/* User Menu */}
            {!user ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="sm" className="ml-2">
                    <User className="w-5 h-5 mr-2" />
                    <span className="hidden xl:inline">Mon compte</span>
                    <ChevronDown className="w-4 h-4 ml-1" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56">
                  <DropdownMenuLabel>
                    <div className="flex flex-col">
                      <span className="font-medium">John</span>
                      <span className="text-xs text-gray-500">
                        test@test.com
                      </span>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem asChild>
                    <Link to="/profile" className="cursor-pointer">
                      <User className="w-4 h-4 mr-2" />
                      Mon profil
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link to="/orders" className="cursor-pointer">
                      <ShoppingBag className="w-4 h-4 mr-2" />
                      Mes commandes
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link to="/favorites" className="cursor-pointer">
                      <Heart className="w-4 h-4 mr-2" />
                      Mes favoris
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuItem asChild>
                    <Link to="/settings" className="cursor-pointer">
                      <Settings className="w-4 h-4 mr-2" />
                      Paramètres
                    </Link>
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onClick={handleLogout}
                    className="cursor-pointer text-red-600"
                  >
                    <LogOut className="w-4 h-4 mr-2" />
                    Se déconnecter
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <div className="flex items-center space-x-2 ml-2">
                <Button variant="ghost" size="sm" asChild>
                  <Link to="/login">Se connecter</Link>
                </Button>
                <Button
                  size="sm"
                  className="bg-primary-600 hover:bg-primary-700"
                  asChild
                >
                  <Link to="/register">S'inscrire</Link>
                </Button>
              </div>
            )}
          </nav>

          {/* Mobile Menu Button */}
          <div className="flex lg:hidden items-center space-x-2">
            {/* Mobile Cart */}
            <Button variant="ghost" size="icon" className="relative" asChild>
              <Link to="/cart">
                <ShoppingBag className="w-5 h-5" />
                {cartCount > 0 && (
                  <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs bg-primary-600">
                    {cartCount}
                  </Badge>
                )}
              </Link>
            </Button>

            {/* Mobile Menu */}
            <SidebarMenu
              categories={categories}
              location={location}
              setLocation={setLocation}
              favoritesCount={favoritesCount}
            />
          </div>
        </div>

        {/* Search Bar - Mobile Only */}
        <div className="lg:hidden">
          <SearchBar />
        </div>
      </div>

      {/* Categories Bar - Desktop Only */}
      <div className="hidden lg:block border-t bg-gray-50">
        <CategoriesBar categories={categories} />
      </div>
    </header>
  );
};

export default Header;
