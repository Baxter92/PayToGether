import type { ReactElement } from "react";
import { Outlet } from "react-router-dom";
import {
  SidebarProvider,
  SidebarInset,
  SidebarTrigger,
} from "@/common/components/ui/sidebar";
import LanguageSelector from "@/common/components/LanguageSelector";
import { AdminSidebar } from "@/pages/admin/components/AdminSidebar";

export function AdminLayout(): ReactElement {
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
