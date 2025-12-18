import { type JSX } from "react";
import { useNavigate } from "react-router-dom";
import type { ICategory } from "./type";
import { PATHS } from "@/common/constants/path";
import { ArrowRight } from "lucide-react";

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
      className="group relative max-h-80 rounded-2xl overflow-hidden bg-card shadow-[var(--shadow-card)] hover:shadow-[var(--shadow-card-hover)] transition-all duration-500 cursor-pointer hover:-translate-y-1"
    >
      <div className="aspect-[4/3] overflow-hidden">
        <img
          src={
            category.image ??
            "https://images.unsplash.com/photo-1521335629791-ce4aec67dd47?auto=format&fit=crop&w=800&q=60"
          }
          alt={category.name}
          className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700 ease-out"
        />
      </div>

      {/* Gradient overlay */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />
      
      {/* Hover overlay */}
      <div className="absolute inset-0 bg-primary/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

      {/* Content */}
      <div className="absolute bottom-0 left-0 right-0 p-5">
        <div className="flex items-end justify-between">
          <div>
            <h3 className="text-white text-xl font-bold font-[family-name:var(--font-heading)] drop-shadow-lg mb-1 group-hover:text-primary-100 transition-colors duration-300">
              {category.name}
            </h3>
            {typeof category.count === "number" && (
              <p className="text-white/80 text-sm font-medium">
                {category.count} offres disponibles
              </p>
            )}
          </div>
          
          {/* Arrow indicator */}
          <div className="bg-white/20 backdrop-blur-sm p-2.5 rounded-xl group-hover:bg-primary group-hover:scale-110 transition-all duration-300">
            <ArrowRight className="w-5 h-5 text-white" />
          </div>
        </div>
      </div>
    </div>
  );
}
