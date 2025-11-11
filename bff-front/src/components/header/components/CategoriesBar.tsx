import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { ChevronDown, Store } from "lucide-react";
import { Link } from "react-router-dom";
import type { ICategory } from "../types";

export type CategoriesBarProps = {
  categories: ICategory[];
};

const CategoriesBar = ({ categories }: CategoriesBarProps) => {
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <nav className="flex items-center space-x-8 h-12">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" className="flex items-center space-x-2">
              <Store className="w-4 h-4 text-primary-600" />
              <span className="font-medium">Toutes les catégories</span>
              <ChevronDown className="w-4 h-4 text-primary-600" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" className="w-80">
            <DropdownMenuLabel>Liste des catégories</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {categories.map((category) => {
              const Icon = category.icon;
              return (
                <DropdownMenuItem key={category.name} asChild>
                  <Link
                    to={category.href}
                    className="flex items-center space-x-2"
                  >
                    <Icon className="w-4 h-4 text-primary-600" />
                    <span>{category.name}</span>
                  </Link>
                </DropdownMenuItem>
              );
            })}
          </DropdownMenuContent>
        </DropdownMenu>
        {categories.map((category) => {
          const Icon = category.icon;
          return (
            <Link
              key={category.name}
              to={category.href}
              className="flex items-center space-x-2 text-sm font-medium text-gray-700 hover:text-primary-600 transition-colors"
            >
              <Icon className="w-4 h-4" />
              <span>{category.name}</span>
            </Link>
          );
        })}
      </nav>
    </div>
  );
};

export default CategoriesBar;
