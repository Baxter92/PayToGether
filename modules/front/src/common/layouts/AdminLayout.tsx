import type { ReactElement } from "react";
import { Navigate, Outlet } from "react-router-dom";
import {
  SidebarProvider,
  SidebarInset,
  SidebarTrigger,
} from "@/common/components/ui/sidebar";
import LanguageSelector from "@/common/components/LanguageSelector";
import { AdminSidebar } from "@/pages/admin/components/AdminSidebar";
import { useAuth } from "../context/AuthContext";
import { PATHS } from "../constants/path";

export function AdminLayout(): ReactElement {
  const { user, role, roles, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-primary-500 border-t-transparent" />
      </div>
    );
  }
  if (!user && !loading) {
    return <Navigate to={PATHS.LOGIN} replace />;
  }
  if (role !== "ADMIN" && !roles.includes("ADMIN")) {
    return <Navigate to={PATHS.HOME} replace />;
  }
  return (
    <SidebarProvider>
      <div className="flex min-h-screen w-full">
        <AdminSidebar />
        <SidebarInset className="flex-1">
          <header className="flex h-16 shrink-0 items-center justify-between border-b px-4 bg-background">
            <SidebarTrigger className="-ml-1" />
            <LanguageSelector />
          </header>
          <main className="flex-1 p-6">
            <Outlet />
          </main>
        </SidebarInset>
      </div>
    </SidebarProvider>
  );
}
