import type { ReactElement } from "react";
import {
  Users,
  ShoppingCart,
  DollarSign,
  Tag,
  ArrowUpRight,
  ArrowDownRight,
} from "lucide-react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/common/components/ui/card";

interface StatCardProps {
  title: string;
  value: string;
  change: number;
  icon: React.ElementType;
}

function StatCard({
  title,
  value,
  change,
  icon: Icon,
}: StatCardProps): ReactElement {
  const isPositive = change >= 0;

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">
          {title}
        </CardTitle>
        <Icon className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        <p
          className={`text-xs flex items-center gap-1 ${
            isPositive ? "text-green-600" : "text-destructive"
          }`}
        >
          {isPositive ? (
            <ArrowUpRight className="h-3 w-3" />
          ) : (
            <ArrowDownRight className="h-3 w-3" />
          )}
          {Math.abs(change)}% par rapport au mois dernier
        </p>
      </CardContent>
    </Card>
  );
}

const stats = [
  {
    title: "Revenus totaux",
    value: "45 231€",
    change: 20.1,
    icon: DollarSign,
  },
  {
    title: "Commandes",
    value: "2 350",
    change: 15.5,
    icon: ShoppingCart,
  },
  {
    title: "Utilisateurs",
    value: "12 234",
    change: 8.2,
    icon: Users,
  },
  {
    title: "Deals actifs",
    value: "573",
    change: -2.4,
    icon: Tag,
  },
];

export default function AdminDashboard(): ReactElement {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold font-heading">Dashboard</h1>
        <p className="text-muted-foreground">
          Vue d'ensemble de votre activité
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <StatCard key={stat.title} {...stat} />
        ))}
      </div>
    </div>
  );
}
