import { Link, useNavigate } from "react-router-dom";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import {
  ChevronDown,
  Heart,
  LogOut,
  MapPin,
  Menu,
  Settings,
  ShoppingBag,
  User,
} from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useState } from "react";
import { useAuth } from "@/context/AuthContext";
import type { ICategory } from "../types";

export type SidebarMenuProps = {
  favoritesCount?: number;
  location?: string;
  categories?: ICategory[];
  setLocation?: (location: string) => void;
};

const SidebarMenu = ({
  favoritesCount,
  location,
  categories,
  setLocation,
}: SidebarMenuProps) => {
  const { user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };
  return (
    <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
      <SheetTrigger asChild>
        <Button variant="ghost" size="icon">
          <Menu className="w-6 h-6" />
        </Button>
      </SheetTrigger>
      <SheetContent side="right" className="w-80 overflow-y-auto p-2">
        <div className="space-y-6">
          {/* User Section */}
          {!user ? (
            <div className="pb-6 border-b">
              <div className="flex items-center space-x-3 mb-4">
                <div className="w-12 h-12 bg-linear-to-br from-primary-500 to-teal-600 rounded-full flex items-center justify-center">
                  <User className="w-6 h-6 text-white" />
                </div>
                <div>
                  <p className="font-medium">John</p>
                  <p className="text-sm text-gray-500">test@test.com</p>
                </div>
              </div>
              <div className="space-y-2">
                <Button
                  variant="ghost"
                  className="w-full justify-start"
                  asChild
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Link to="/profile">
                    <User className="w-4 h-4 mr-2" />
                    Mon profil
                  </Link>
                </Button>
                <Button
                  variant="ghost"
                  className="w-full justify-start"
                  asChild
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Link to="/orders">
                    <ShoppingBag className="w-4 h-4 mr-2" />
                    Mes commandes
                  </Link>
                </Button>
                <Button
                  variant="ghost"
                  className="w-full justify-start"
                  asChild
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Link to="/favorites">
                    <Heart className="w-4 h-4 mr-2" />
                    Mes favoris ({favoritesCount})
                  </Link>
                </Button>
              </div>
            </div>
          ) : (
            <div className="pb-6 border-b space-y-2">
              <Button
                className="w-full bg-primary-600 hover:bg-primary-700"
                asChild
                onClick={() => setMobileMenuOpen(false)}
              >
                <Link to="/register">S'inscrire</Link>
              </Button>
              <Button
                variant="outline"
                className="w-full"
                asChild
                onClick={() => setMobileMenuOpen(false)}
              >
                <Link to="/login">Se connecter</Link>
              </Button>
            </div>
          )}

          {/* Location */}
          <div className="pb-6 border-b">
            <p className="text-sm font-medium text-gray-500 mb-3">
              Votre ville
            </p>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" className="w-full justify-between">
                  <div className="flex items-center">
                    <MapPin className="w-4 h-4 text-primary-600 mr-2" />
                    <span>{location}</span>
                  </div>
                  <ChevronDown className="w-4 h-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-full">
                <DropdownMenuItem onClick={() => setLocation?.("Douala")}>
                  Douala
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setLocation?.("Yaoundé")}>
                  Yaoundé
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setLocation?.("Bafoussam")}>
                  Bafoussam
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setLocation?.("Garoua")}>
                  Garoua
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>

          {/* Categories */}
          <div className="pb-6 border-b">
            <p className="text-sm font-medium text-gray-500 mb-3">Catégories</p>
            <div className="space-y-2">
              {categories &&
                categories.map((category) => {
                  const Icon = category.icon;
                  return (
                    <Button
                      key={category.name}
                      variant="ghost"
                      className="w-full justify-start"
                      asChild
                      onClick={() => setMobileMenuOpen(false)}
                    >
                      <Link to={category.href}>
                        <Icon className="w-4 h-4 mr-2 text-primary-600" />
                        {category.name}
                      </Link>
                    </Button>
                  );
                })}
            </div>
          </div>

          {/* Settings & Logout */}
          {!user && (
            <div className="space-y-2">
              <Button
                variant="ghost"
                className="w-full justify-start"
                asChild
                onClick={() => setMobileMenuOpen(false)}
              >
                <Link to="/settings">
                  <Settings className="w-4 h-4 mr-2" />
                  Paramètres
                </Link>
              </Button>
              <Button
                variant="ghost"
                className="w-full justify-start text-red-600 hover:text-red-700 hover:bg-red-50"
                onClick={() => {
                  handleLogout();
                  setMobileMenuOpen(false);
                }}
              >
                <LogOut className="w-4 h-4 mr-2" />
                Se déconnecter
              </Button>
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
};

export default SidebarMenu;
