import type { ReactElement } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  Tag,
  Users,
  ShoppingCart,
  LogOut,
  Home,
  Store,
  CreditCard,
  FolderTree,
  BarChart3,
  Image,
  Wallet,
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
import { useI18n } from "@/common/hooks/useI18n";

export function AdminSidebar(): ReactElement {
  const { t } = useI18n("admin");
  const location = useLocation();
  const navigate = useNavigate();
  const { state } = useSidebar();
  const isCollapsed = state === "collapsed";

  const isActive = (path: string): boolean => location.pathname === path;

  const handleLogout = () => {
    sessionStorage.removeItem("adminAuthenticated");
    toast.success(t("sidebar.logout"), {
      description: t("sidebar.logoutMessage"),
    });
    navigate(ADMIN_PATHS.LOGIN);
  };

  const sidebarItems = {
    dashboard: [
      {
        titleKey: "sidebar.dashboard",
        url: ADMIN_PATHS.DASHBOARD,
        icon: LayoutDashboard,
      },
    ],
    commerce: [
      { titleKey: "sidebar.deals", url: ADMIN_PATHS.DEALS, icon: Tag },
      {
        titleKey: "sidebar.orders",
        url: ADMIN_PATHS.ORDERS,
        icon: ShoppingCart,
      },
      {
        titleKey: "sidebar.payments",
        url: ADMIN_PATHS.PAYMENTS,
        icon: CreditCard,
      },
      {
        titleKey: "sidebar.payouts",
        url: ADMIN_PATHS.PAYOUTS,
        icon: Wallet,
      },
      {
        titleKey: "sidebar.categories",
        url: ADMIN_PATHS.CATEGORIES,
        icon: FolderTree,
      },
    ],
    users: [
      { titleKey: "sidebar.users", url: ADMIN_PATHS.USERS, icon: Users },
      {
        titleKey: "sidebar.merchants",
        url: ADMIN_PATHS.MERCHANTS,
        icon: Store,
      },
    ],
    content: [{ titleKey: "sidebar.hero", url: ADMIN_PATHS.HERO, icon: Image }],
    config: [
      {
        titleKey: "sidebar.reports",
        url: ADMIN_PATHS.REPORTS,
        icon: BarChart3,
      },
    ],
  };

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader className="border-b">
        <div className="flex items-center gap-2 px-2 py-3">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <LayoutDashboard className="h-4 w-4" />
          </div>
          {!isCollapsed && (
            <span className="font-heading font-semibold text-lg">
              {t("title")}
            </span>
          )}
        </div>
      </SidebarHeader>

      <SidebarContent>
        {/* Dashboard */}
        <SidebarGroup>
          <SidebarGroupLabel>{t("sidebar.main")}</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {sidebarItems.dashboard.map((item) => (
                <SidebarMenuItem key={item.titleKey}>
                  <SidebarMenuButton
                    asChild
                    isActive={isActive(item.url)}
                    tooltip={t(item.titleKey)}
                  >
                    <Link to={item.url}>
                      <item.icon className="h-4 w-4" />
                      <span>{t(item.titleKey)}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        {/* Commerce */}
        <SidebarGroup>
          <SidebarGroupLabel>{t("sidebar.commerce")}</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {sidebarItems.commerce.map((item) => (
                <SidebarMenuItem key={item.titleKey}>
                  <SidebarMenuButton
                    asChild
                    isActive={isActive(item.url)}
                    tooltip={t(item.titleKey)}
                  >
                    <Link to={item.url}>
                      <item.icon className="h-4 w-4" />
                      <span>{t(item.titleKey)}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        {/* Utilisateurs */}
        <SidebarGroup>
          <SidebarGroupLabel>{t("sidebar.users")}</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {sidebarItems.users.map((item) => (
                <SidebarMenuItem key={item.titleKey}>
                  <SidebarMenuButton
                    asChild
                    isActive={isActive(item.url)}
                    tooltip={t(item.titleKey)}
                  >
                    <Link to={item.url}>
                      <item.icon className="h-4 w-4" />
                      <span>{t(item.titleKey)}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        {/* Contenu */}
        <SidebarGroup>
          <SidebarGroupLabel>{t("sidebar.content")}</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {sidebarItems.content.map((item) => (
                <SidebarMenuItem key={item.titleKey}>
                  <SidebarMenuButton
                    asChild
                    isActive={isActive(item.url)}
                    tooltip={t(item.titleKey)}
                  >
                    <Link to={item.url}>
                      <item.icon className="h-4 w-4" />
                      <span>{t(item.titleKey)}</span>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        {/* Configuration */}
        <SidebarGroup>
          <SidebarGroupLabel>{t("sidebar.config")}</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {sidebarItems.config.map((item) => (
                <SidebarMenuItem key={item.titleKey}>
                  <SidebarMenuButton
                    asChild
                    isActive={isActive(item.url)}
                    tooltip={t(item.titleKey)}
                  >
                    <Link to={item.url}>
                      <item.icon className="h-4 w-4" />
                      <span>{t(item.titleKey)}</span>
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
            <SidebarMenuButton asChild tooltip={t("sidebar.backToSite")}>
              <Link to="/">
                <Home className="h-4 w-4" />
                <span>{t("sidebar.backToSite")}</span>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>
          <SidebarMenuItem>
            <SidebarMenuButton
              onClick={handleLogout}
              className={cn(
                "text-destructive hover:bg-destructive/10 hover:text-destructive"
              )}
              tooltip={t("sidebar.logout")}
            >
              <LogOut className="h-4 w-4" />
              <span>{t("sidebar.logout")}</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>

      <SidebarRail />
    </Sidebar>
  );
}
