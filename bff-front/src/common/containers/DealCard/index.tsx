import { Clock, Users, TrendingUp } from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@components/ui/card";
import type { IDealCardProps } from "./type";
import { Button } from "@components/ui/button";
import { Progress } from "@components/ui/progress";
import type { JSX } from "react";
import { Link } from "react-router-dom";
import { formatCurrency } from "@/common/utils/formatCurrency";

export default function DealCard({ deal }: IDealCardProps): JSX.Element {
  const percentage = (deal.sold / deal.total) * 100;
  const isHot = percentage > 70;

  return (
    <Link to={`/deals/${deal.id}`} className="group block">
      <Card className="overflow-hidden hover:shadow-[var(--shadow-card-hover)] transition-all duration-500 flex flex-col p-0 border-0 bg-card/80 backdrop-blur-sm hover:-translate-y-1">
        {/* Image Container */}
        <CardHeader className="relative w-full h-52 bg-muted p-0 overflow-hidden">
          <div className="w-full h-full overflow-hidden">
            <img
              src={deal.image || "/placeholder.svg"}
              alt={deal.title}
              className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700 ease-out"
            />
            {/* Gradient overlay */}
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
          </div>

          {/* Badges */}
          <div className="absolute top-3 left-3 flex flex-col gap-2">
            {deal.popular && (
              <span className="inline-flex items-center gap-1 bg-accent text-accent-foreground px-2.5 py-1 rounded-full text-xs font-semibold shadow-lg">
                <TrendingUp className="w-3 h-3" />
                Populaire
              </span>
            )}
          </div>

          {/* Quick view on hover */}
          <div className="absolute bottom-3 left-3 right-3 opacity-0 group-hover:opacity-100 translate-y-2 group-hover:translate-y-0 transition-all duration-300">
            <span className="text-white text-sm font-medium drop-shadow-lg">
              Voir les détails →
            </span>
          </div>
        </CardHeader>

        {/* Content */}
        <CardContent className="p-5 flex-1 flex flex-col">
          <CardTitle className="font-semibold text-foreground line-clamp-1 mb-3 text-lg group-hover:text-primary transition-colors duration-300">
            {deal.title}
          </CardTitle>

          <CardDescription className="flex-1">
            {/* Prices */}
            <div className="mb-4">
              <div className="flex items-baseline gap-3 mb-1">
                <span className="text-3xl font-extrabold bg-gradient-to-r from-primary to-primary-600 bg-clip-text text-transparent">
                  {formatCurrency(deal.groupPrice)}
                </span>
              </div>
              <p className="text-xs text-muted-foreground">
                {formatCurrency(deal.originalPrice)}
              </p>
            </div>

            {/* Progress */}
            <div className="mb-4">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs text-foreground font-semibold">
                  {deal.sold}/{deal.total} vendus
                </span>
                <span
                  className={`text-xs font-bold ${
                    isHot ? "text-accent" : "text-primary"
                  }`}
                >
                  {Math.round(percentage)}%
                </span>
              </div>
              <Progress
                value={percentage}
                className={`h-2.5 rounded-full ${
                  isHot
                    ? "[&>div]:bg-gradient-to-r [&>div]:from-accent [&>div]:to-accent-600"
                    : ""
                }`}
              />
            </div>

            {/* Info */}
            <div className="flex items-center gap-4 text-xs text-muted-foreground">
              <div className="flex items-center gap-1.5 bg-muted/50 px-2.5 py-1.5 rounded-lg">
                <Clock className="w-3.5 h-3.5 text-primary" />
                <span className="font-medium">{deal.deadline}</span>
              </div>
              <div className="flex items-center gap-1.5 bg-muted/50 px-2.5 py-1.5 rounded-lg">
                <Users className="w-3.5 h-3.5 text-primary" />
                <span className="font-medium">{deal.sold}</span>
              </div>
            </div>
          </CardDescription>

          {/* CTA Button */}
          <Button className="w-full mt-4 bg-primary hover:bg-primary-600 text-primary-foreground font-semibold rounded-xl h-11 shadow-md hover:shadow-lg transition-all duration-300 group-hover:scale-[1.02]">
            Voir le deal
          </Button>
        </CardContent>
      </Card>
    </Link>
  );
}
