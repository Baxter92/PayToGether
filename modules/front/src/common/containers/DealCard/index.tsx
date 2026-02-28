import {
  Clock,
  Users,
  TrendingUp,
  Edit2,
  Trash2,
  FileEdit,
  Globe,
} from "lucide-react";
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
import { useI18n } from "@hooks/useI18n";
import { useGetDealImageUrl } from "@/common/api";
import { cn } from "@/common/lib/utils";

export default function DealCard({
  deal,
  isAdmin,
  onEdit,
  onDelete,
  onToggleStatus,
}: IDealCardProps): JSX.Element {
  const { t } = useI18n();
  const { t: tDeals } = useI18n("deals");

  const { data: imageUrl } = useGetDealImageUrl(
    deal?.id || deal?.uuid || "",
    deal?.image?.imageUuid ?? undefined,
  );

  const percentage = (deal.sold / deal.total) * 100;
  const isHot = percentage > 70;

  const currentStatus = (deal.status || deal.statut || "").toUpperCase();
  const isPublished = ["PUBLIE", "PUBLISHED"].includes(currentStatus);

  return (
    <Card className="overflow-hidden hover:shadow-[var(--shadow-card-hover)] transition-all duration-500 flex flex-col p-0 border-0 bg-card/80 backdrop-blur-sm hover:-translate-y-1 group">
      <Link to={`/deals/${deal.id || deal.uuid}`} className="block">
        {/* Image Container */}
        <CardHeader className="relative w-full h-52 bg-muted p-0 overflow-hidden">
          <div className="w-full h-full overflow-hidden">
            <img
              src={imageUrl?.url || "/placeholder.svg"}
              alt={deal.title || deal.titre}
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
                {tDeals("popular")}
              </span>
            )}
            {isAdmin && (
              <span
                className={cn(
                  "inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold shadow-lg uppercase",
                  isPublished
                    ? "bg-green-500 text-white"
                    : "bg-yellow-500 text-white",
                )}
              >
                {isPublished ? "Publié" : "Brouillon"}
              </span>
            )}
          </div>

          {/* Quick view on hover */}
          <div className="absolute bottom-3 left-3 right-3 opacity-0 group-hover:opacity-100 translate-y-2 group-hover:translate-y-0 transition-all duration-300">
            <span className="text-white text-sm font-medium drop-shadow-lg">
              {tDeals("viewDetails")} →
            </span>
          </div>
        </CardHeader>
      </Link>

      {/* Content */}
      <CardContent className="p-5 flex-1 flex flex-col relative">
        {isAdmin && (
          <div className="absolute -top-6 right-3 flex gap-2 z-10">
            <Button
              size="icon-sm"
              variant="secondary"
              className="shadow-lg h-9 w-9 bg-white hover:bg-gray-100 border border-gray-100"
              title={isPublished ? "Remettre en brouillon" : "Publier"}
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                onToggleStatus?.(
                  deal.id || deal.uuid || "",
                  deal.title || deal.titre || "",
                  currentStatus,
                );
              }}
            >
              {isPublished ? (
                <FileEdit className="w-4 h-4 text-gray-700" />
              ) : (
                <Globe className="w-4 h-4 text-blue-600" />
              )}
            </Button>
            <Button
              size="icon-sm"
              variant="secondary"
              className="shadow-lg h-9 w-9 bg-white hover:bg-gray-100 border border-gray-100"
              title="Modifier"
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                onEdit?.(deal.id || deal.uuid || "");
              }}
            >
              <Edit2 className="w-4 h-4 text-gray-700" />
            </Button>
            <Button
              size="icon-sm"
              variant="destructive"
              className="shadow-lg h-9 w-9"
              title="Supprimer"
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                onDelete?.(
                  deal.id || deal.uuid || "",
                  deal.title || deal.titre || "",
                );
              }}
            >
              <Trash2 className="w-4 h-4" />
            </Button>
          </div>
        )}

        <Link to={`/deals/${deal.id || deal.uuid}`} className="block mb-3">
          <CardTitle className="font-semibold text-foreground line-clamp-1 text-lg group-hover:text-primary transition-colors duration-300">
            {deal.title || deal.titre}
          </CardTitle>
        </Link>

        <CardDescription className="flex-1">
          {/* Prices */}
          <div className="mb-4">
            <div className="flex items-baseline gap-3 mb-1">
              <span className="text-3xl font-extrabold bg-gradient-to-r from-primary to-primary-600 bg-clip-text text-transparent">
                {formatCurrency(deal.groupPrice)}
              </span>
            </div>
          </div>

          {/* Progress */}
          <div className="mb-4">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs text-foreground font-semibold">
                {deal.sold}/{deal.total} {tDeals("sold")}
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
              <span className="font-medium">
                {deal.deadline} {t("days")}
              </span>
            </div>
            <div className="flex items-center gap-1.5 bg-muted/50 px-2.5 py-1.5 rounded-lg">
              <Users className="w-3.5 h-3.5 text-primary" />
              <span className="font-medium">{deal.sold}</span>
            </div>
          </div>
        </CardDescription>

        {/* CTA Button */}
        <Link to={`/deals/${deal.id || deal.uuid}`} className="block w-full">
          <Button className="w-full mt-4 bg-primary hover:bg-primary-600 text-primary-foreground font-semibold rounded-xl h-11 shadow-md hover:shadow-lg transition-all duration-300 group-hover:scale-[1.02]">
            {tDeals("viewDeal")}
          </Button>
        </Link>
      </CardContent>
    </Card>
  );
}
