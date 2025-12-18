import type { LucideIcon } from "lucide-react";

export interface ICategory {
  id: string | number;
  name: string;
  href?: string;
  image?: string;
  count?: number; // nombre d'items dans la catégorie (optionnel)
  description?: string;
  icon?: LucideIcon;
  subcategories?: string[];
}
