import { type JSX } from "react";
import { useNavigate } from "react-router-dom";
import type { ICategory } from "./type";
import { PATHS } from "@/common/constants/path";

type ICategoryCardProps = {
  category: ICategory;
};

export default function CategoryCard({
  category,
}: ICategoryCardProps): JSX.Element {
  const navigate = useNavigate();
  return (
    <div
      onClick={() => navigate(PATHS.CATEGORIES(category.href))}
      className="group relative max-h-70 rounded-xl overflow-hidden border border-border bg-card shadow-sm hover:shadow-md transition-shadow cursor-pointer"
    >
      <div className="aspect-[4/3] overflow-hidden">
        <img
          src={
            category.image ??
            "https://images.unsplash.com/photo-1521335629791-ce4aec67dd47?auto=format&fit=crop&w=800&q=60"
          }
          alt={category.name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
        />
      </div>

      <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent pointer-events-none" />

      <div className="absolute bottom-4 left-4">
        <h3 className="text-white text-lg font-semibold drop-shadow-md">
          {category.name}
        </h3>
        {typeof category.count === "number" && (
          <p className="text-white/80 text-xs drop-shadow-sm">
            {category.count} produits
          </p>
        )}
      </div>
    </div>
  );
}
