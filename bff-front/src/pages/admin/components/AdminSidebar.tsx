import type { ReactElement } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  Tag,
  Users,
  ShoppingCart,
  Settings,
  LogOut,
  Home,
  Store,
  CreditCard,
  FolderTree,
  BarChart3,
} from "lucide-react";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  useSidebar,
} from "@/common/components/ui/sidebar";
import { cn } from "@/common/lib/utils";
import { ADMIN_PATHS } from "../constants/adminPaths";
import { toast } from "sonner";

const mainNavItems = [
  {
    title: "Dashboard",
    url: ADMIN_PATHS.DASHBOARD,
    icon: LayoutDashboard,
  },
  {
    title: "Deals",
    url: ADMIN_PATHS.DEALS,
    icon: Tag,
  },
  {
    title: "Marchands",
    url: ADMIN_PATHS.MERCHANTS,
    icon: Store,
  },
  {
    title: "Utilisateurs",
    url: ADMIN_PATHS.USERS,
    icon: Users,
  },
  {
    title: "Commandes",
    url: ADMIN_PATHS.ORDERS,
    icon: ShoppingCart,
  },
  {
    title: "Paiements",
    url: ADMIN_PATHS.PAYMENTS,
    icon: CreditCard,
  },
  {
    title: "Catégories",
    url: ADMIN_PATHS.CATEGORIES,
    icon: FolderTree,
  },
  {
    title: "Rapports",
    url: ADMIN_PATHS.REPORTS,
    icon: BarChart3,
  },
];

const settingsNavItems = [
  {
    title: "Paramètres",
    url: ADMIN_PATHS.SETTINGS,
    icon: Settings,
  },
];

export function AdminSidebar(): ReactElement {
  const location = useLocation();
  const navigate = useNavigate();
  const { state } = useSidebar();
  const isCollapsed = state === "collapsed";

  const isActive = (path: string): boolean => location.pathname === path;

  const handleLogout = () => {
    sessionStorage.removeItem("adminAuthenticated");
    toast.success("Déconnexion", {
      description: "Vous avez été déconnecté",
    });
    navigate(ADMIN_PATHS.LOGIN);
  };

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader className="border-b">
        <div className="flex items-center gap-2 px-2 py-3">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <LayoutDashboard className="h-4 w-4" />
          </div>
          {!isCollapsed && (
            <span className="font-heading font-semibold text-lg">Admin</span>
          )}
        </div>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>Navigation</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {mainNavItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton
                    asChild
                    isActive={isActive(item.url)}
                    tooltip={item.title}
                  >
                    <Link to={item.url}>
                      <item.icon className="h-4 w-4" />
                      <span>{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        <SidebarGroup>
          <SidebarGroupLabel>Configuration</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {settingsNavItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton
                    asChild
                    isActive={isActive(item.url)}
                    tooltip={item.title}
                  >
                    <Link to={item.url}>
                      <item.icon className="h-4 w-4" />
                      <span>{item.title}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter className="border-t">
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton asChild tooltip="Retour au site">
              <Link to="/">
                <Home className="h-4 w-4" />
                <span>Retour au site</span>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>
          <SidebarMenuItem>
            <SidebarMenuButton
              onClick={handleLogout}
              className={cn(
                "text-destructive hover:bg-destructive/10 hover:text-destructive"
              )}
              tooltip="Déconnexion"
            >
              <LogOut className="h-4 w-4" />
              <span>Déconnexion</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>

      <SidebarRail />
    </Sidebar>
  );
}
