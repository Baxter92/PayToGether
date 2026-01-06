import type { ReactElement } from "react";
import {
  BarChart3,
  TrendingUp,
  TrendingDown,
  DollarSign,
  Users,
  ShoppingCart,
  Tag,
  Download,
} from "lucide-react";
import { Button } from "@/common/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import Select from "@/common/components/Select";
import { formatCurrency } from "@/common/utils/formatCurrency";

interface StatCard {
  title: string;
  value: string;
  change: number;
  icon: React.ComponentType<{ className?: string }>;
}

const statsCards: StatCard[] = [
  {
    title: "Revenus totaux",
    value: formatCurrency(2450000),
    change: 12.5,
    icon: DollarSign,
  },
  {
    title: "Nouveaux utilisateurs",
    value: "1,234",
    change: 8.2,
    icon: Users,
  },
  {
    title: "Commandes",
    value: "456",
    change: -3.1,
    icon: ShoppingCart,
  },
  {
    title: "Deals actifs",
    value: "89",
    change: 15.7,
    icon: Tag,
  },
];

const topDeals = [
  { name: "Spa Premium - 2h de soins", sales: 125, revenue: 625000 },
  { name: "Menu Gastronomique pour 2", sales: 98, revenue: 490000 },
  { name: "Lavage auto complet", sales: 87, revenue: 261000 },
  { name: "Coiffure + Maquillage", sales: 76, revenue: 228000 },
  { name: "Week-end Kribi", sales: 45, revenue: 675000 },
];

const topMerchants = [
  { name: "Restaurant Le Gourmet", deals: 12, revenue: 850000 },
  { name: "Spa Wellness", deals: 8, revenue: 680000 },
  { name: "Boutique Mode", deals: 15, revenue: 520000 },
  { name: "Auto Service Pro", deals: 6, revenue: 390000 },
];

export default function AdminReports(): ReactElement {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-heading font-bold flex items-center gap-2">
            <BarChart3 className="h-8 w-8" />
            Rapports
          </h1>
          <p className="text-muted-foreground mt-1">
            Analytics et statistiques détaillées
          </p>
        </div>
        <div className="flex gap-2">
          <Select
            defaultValue="30d"
            triggerClassName="w-40"
            placeholder="Période"
            items={[
              { value: "7d", label: "7 derniers jours" },
              { value: "30d", label: "30 derniers jours" },
              { value: "90d", label: "90 derniers jours" },
              { value: "1y", label: "Cette année" },
            ]}
          />
          <Button variant="outline" leftIcon={<Download className="h-4 w-4" />}>
            Exporter
          </Button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {statsCards.map((stat) => (
          <Card key={stat.title}>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardDescription>{stat.title}</CardDescription>
              <stat.icon className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
              <div className="flex items-center text-sm mt-1">
                {stat.change > 0 ? (
                  <TrendingUp className="h-4 w-4 text-green-600 mr-1" />
                ) : (
                  <TrendingDown className="h-4 w-4 text-destructive mr-1" />
                )}
                <span
                  className={
                    stat.change > 0 ? "text-green-600" : "text-destructive"
                  }
                >
                  {stat.change > 0 ? "+" : ""}
                  {stat.change}%
                </span>
                <span className="text-muted-foreground ml-1">
                  vs mois dernier
                </span>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Top Deals</CardTitle>
            <CardDescription>Les deals les plus vendus ce mois</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {topDeals.map((deal, index) => (
                <div key={deal.name} className="flex items-center gap-4">
                  <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 text-primary font-medium text-sm">
                    {index + 1}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium truncate">{deal.name}</p>
                    <p className="text-sm text-muted-foreground">
                      {deal.sales} ventes
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-medium">
                      {formatCurrency(deal.revenue)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Top Marchands</CardTitle>
            <CardDescription>
              Les marchands les plus performants
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {topMerchants.map((merchant, index) => (
                <div key={merchant.name} className="flex items-center gap-4">
                  <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 text-primary font-medium text-sm">
                    {index + 1}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium truncate">{merchant.name}</p>
                    <p className="text-sm text-muted-foreground">
                      {merchant.deals} deals actifs
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="font-medium">
                      {formatCurrency(merchant.revenue)}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Évolution des ventes</CardTitle>
          <CardDescription>
            Graphique des ventes sur la période sélectionnée
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-64 flex items-center justify-center border-2 border-dashed rounded-lg text-muted-foreground">
            <div className="text-center">
              <BarChart3 className="h-12 w-12 mx-auto mb-2 opacity-50" />
              <p>Graphique à implémenter</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
