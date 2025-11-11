import { Clock, Users } from "lucide-react";
import { Card } from "../ui/card";
import type { IDealCardProps } from "./type";
import { Button } from "../ui/button";
import { Progress } from "../ui/progress";

export default function DealCard({ deal }: IDealCardProps) {
  const percentage = (deal.sold / deal.total) * 100;

  return (
    <Card className="overflow-hidden hover:shadow-lg transition-shadow duration-300 flex flex-col">
      {/* Image Container */}
      <div className="relative w-full h-48 bg-muted overflow-hidden group">
        <img
          src={deal.image || "/placeholder.svg"}
          alt={deal.title}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />
        {/* Discount Badge */}
        <div className="absolute top-3 right-3 bg-primary text-primary-foreground px-3 py-1 rounded-full text-sm font-bold">
          -{deal.discount}%
        </div>
      </div>

      {/* Content */}
      <div className="p-4 flex-1 flex flex-col">
        <h3 className="font-semibold text-foreground line-clamp-2 mb-2">
          {deal.title}
        </h3>

        {/* Prices */}
        <div className="mb-4">
          <div className="flex items-baseline gap-2 mb-2">
            <span className="text-2xl font-bold text-primary">
              {deal.groupPrice.toFixed(0)}€
            </span>
            <span className="text-sm text-muted-foreground line-through">
              {deal.originalPrice}€
            </span>
          </div>
          <p className="text-xs text-muted-foreground">par {deal.unit}</p>
        </div>

        {/* Progress */}
        <div className="mb-4">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs text-foreground font-medium">
              {deal.sold}/{deal.total} vendus
            </span>
            <span className="text-xs text-muted-foreground">
              {Math.round(percentage)}%
            </span>
          </div>
          <Progress value={percentage} className="h-2" />
        </div>

        {/* Info */}
        <div className="flex items-center gap-4 text-xs text-muted-foreground mb-4">
          <div className="flex items-center gap-1">
            <Clock className="w-4 h-4" />
            {deal.deadline}
          </div>
          <div className="flex items-center gap-1">
            <Users className="w-4 h-4" />
            {deal.sold} acheteurs
          </div>
        </div>

        {/* CTA Button */}
        <Button className="w-full bg-primary hover:bg-primary/90 text-primary-foreground mt-auto">
          Voir le deal
        </Button>
      </div>
    </Card>
  );
}
