import type React from "react";
import { useState, type ReactElement } from "react";
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

import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import { mockDeals } from "@/common/constants/data";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";
import { Button } from "@/common/components/ui/button";
import { Dropdown } from "@/common/components/Dropdown";
import { formatCurrency } from "@/common/utils/formatCurrency";

const totalRevenue = mockDeals.reduce(
  (sum, deal) => sum + deal.originalPrice * deal.sold,
  0
);
const totalSold = mockDeals.reduce((sum, deal) => sum + deal.sold, 0);
const totalDeals = mockDeals.length;
const bovinDeals = mockDeals.filter((deal) => deal.category === "bovins");
const poissonDeals = mockDeals.filter((deal) => deal.category === "poissons");
const bovinRevenue = bovinDeals.reduce(
  (sum, deal) => sum + deal.originalPrice * deal.sold,
  0
);
const poissonRevenue = poissonDeals.reduce(
  (sum, deal) => sum + deal.originalPrice * deal.sold,
  0
);

interface StatCard {
  title: string;
  value: string;
  change: number;
  icon: React.ComponentType<{ className?: string }>;
}

const statsCards: StatCard[] = [
  {
    title: "Revenus totaux",
    value: formatCurrency(totalRevenue),
    change: 12.5,
    icon: DollarSign,
  },
  {
    title: "Unités vendues",
    value: totalSold.toString(),
    change: 8.2,
    icon: ShoppingCart,
  },
  {
    title: "Deals actifs",
    value: totalDeals.toString(),
    change: 15.7,
    icon: Tag,
  },
  {
    title: "Villes",
    value: [...new Set(mockDeals.map((d) => d.city))].length.toString(),
    change: 5.0,
    icon: Users,
  },
];

const topDeals = mockDeals
  .sort((a, b) => b.sold - a.sold)
  .slice(0, 5)
  .map((deal) => ({
    name: deal.title,
    sales: deal.sold,
    revenue: deal.originalPrice * deal.sold,
  }));

const cityStats = mockDeals.reduce((acc, deal) => {
  const existing = acc.find((d) => d.city === deal.city);
  if (existing) {
    existing.deals += 1;
    existing.revenue += deal.originalPrice * deal.sold;
    existing.sold += deal.sold;
  } else {
    acc.push({
      city: deal.city,
      deals: 1,
      revenue: deal.originalPrice * deal.sold,
      sold: deal.sold,
    });
  }
  return acc;
}, [] as Array<{ city: string; deals: number; revenue: number; sold: number }>);

const categoryData = [
  {
    name: "Bovins",
    value: Math.round((bovinRevenue / totalRevenue) * 100),
    revenue: bovinRevenue,
  },
  {
    name: "Poissons",
    value: Math.round((poissonRevenue / totalRevenue) * 100),
    revenue: poissonRevenue,
  },
];

const COLORS = ["#3b82f6", "#10b981"];

const monthlySalesData = [
  { month: "Jan", revenue: 800000, orders: 120, sold: 450 },
  { month: "Fév", revenue: 950000, orders: 145, sold: 520 },
  { month: "Mar", revenue: 1100000, orders: 160, sold: 580 },
  { month: "Avr", revenue: 1250000, orders: 175, sold: 620 },
  { month: "Mai", revenue: 1180000, models: 168, sold: 610 },
  { month: "Juin", revenue: totalRevenue, orders: totalSold, sold: totalSold },
];

export default function AdminReports(): ReactElement {
  const [selectedPeriod, setSelectedPeriod] = useState("30d");
  const [selectedCategory, setSelectedCategory] = useState<string | string[]>(
    "all"
  );
  const [selectedCity, setSelectedCity] = useState<string | string[]>("all");

  const cities = [...new Set(mockDeals.map((d) => d.city))];

  const periodItems = [
    { label: "7 derniers jours", value: "7d" },
    { label: "30 derniers jours", value: "30d" },
    { label: "90 derniers jours", value: "90d" },
    { label: "Cette année", value: "1y" },
  ];

  const categoryItems = [
    { label: "Tous", value: "all" },
    { label: "Bovins", value: "bovins" },
    { label: "Poissons", value: "poissons" },
  ];

  const cityItems = [
    { label: "Toutes les villes", value: "all" },
    ...cities.map((city) => ({ label: city, value: city })),
  ];

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
        <div className="flex gap-2 flex-wrap">
          <Dropdown
            label={`Période: ${
              periodItems.find((p) => p.value === selectedPeriod)?.label ||
              "30 derniers jours"
            }`}
            items={periodItems}
            selectedValue={selectedPeriod}
            onChange={(value) => setSelectedPeriod(value as string)}
            className="w-auto"
          />
          <Dropdown
            label={`Catégorie: ${
              categoryItems.find((c) => c.value === selectedCategory)?.label ||
              "Tous"
            }`}
            items={categoryItems}
            selectedValue={selectedCategory as string}
            onChange={(value) => setSelectedCategory(value)}
            className="w-auto"
          />
          <Dropdown
            label={`Ville: ${
              cityItems.find((c) => c.value === selectedCity)?.label ||
              "Toutes les villes"
            }`}
            items={cityItems}
            selectedValue={selectedCity as string}
            onChange={(value) => setSelectedCity(value)}
            className="w-auto"
          />
          <Button
            variant="outline"
            leftIcon={<Download className="h-4 w-4 bg-transparent" />}
          >
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

      <Card>
        <CardHeader>
          <CardTitle>Évolution des ventes</CardTitle>
          <CardDescription>
            Tendance des revenus et unités vendues sur 6 mois
          </CardDescription>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={monthlySalesData}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis dataKey="month" stroke="var(--muted-foreground)" />
              <YAxis stroke="var(--muted-foreground)" />
              <Tooltip
                contentStyle={{
                  backgroundColor: "var(--background)",
                  border: "1px solid var(--border)",
                  borderRadius: "8px",
                }}
              />
              <Legend />
              <Line
                type="monotone"
                dataKey="revenue"
                stroke="#3b82f6"
                strokeWidth={2}
                dot={{ fill: "#3b82f6" }}
                name="Revenus (USD)"
              />
              <Line
                type="monotone"
                dataKey="sold"
                stroke="#10b981"
                strokeWidth={2}
                dot={{ fill: "#10b981" }}
                name="Unités vendues"
              />
            </LineChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Distribution par catégorie</CardTitle>
            <CardDescription>
              Répartition des revenus : Bovins vs Poissons
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={categoryData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, value }: { name?: string; value?: number }) =>
                    `${name || ""} (${value || 0}%)`
                  }
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {categoryData.map((_, index: number) => (
                    <Cell
                      key={`cell-${index}`}
                      fill={COLORS[index % COLORS.length]}
                    />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(value) => `${value ?? 0}%`}
                  contentStyle={{
                    backgroundColor: "var(--background)",
                    border: "1px solid var(--border)",
                    borderRadius: "8px",
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Revenus par catégorie</CardTitle>
            <CardDescription>
              Montant généré par catégorie de produits
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart
                data={categoryData}
                margin={{ top: 20, right: 30, left: 100, bottom: 5 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                <XAxis dataKey="name" stroke="var(--muted-foreground)" />
                <YAxis stroke="var(--muted-foreground)" />
                <Tooltip
                  formatter={(value) => formatCurrency(Number(value ?? 0))}
                  contentStyle={{
                    backgroundColor: "var(--background)",
                    border: "1px solid var(--border)",
                    borderRadius: "8px",
                  }}
                />
                <Bar dataKey="revenue" fill="#3b82f6" name="Commission (USD)" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Top Deals</CardTitle>
            <CardDescription>Les deals les plus vendus</CardDescription>
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
                      {deal.sales} unités vendues
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
            <CardTitle>Performance par ville</CardTitle>
            <CardDescription>Revenus générés par localisation</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {cityStats
                .sort((a, b) => b.revenue - a.revenue)
                .map((city, index) => (
                  <div key={city.city} className="flex items-center gap-4">
                    <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 text-primary font-medium text-sm">
                      {index + 1}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium">{city.city}</p>
                      <p className="text-sm text-muted-foreground">
                        {city.deals} deals · {city.sold} unités
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="font-medium">
                        {formatCurrency(city.revenue)}
                      </p>
                    </div>
                  </div>
                ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
