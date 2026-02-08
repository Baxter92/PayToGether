import HStack from "@/common/components/HStack";
import { Tag } from "lucide-react";
import { Outlet } from "react-router-dom";
import Header from "../Header";
import Footer from "../Footer";
import { categories } from "@/common/constants/data";

export const MainLayout = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <Header
        appName="DealToGether"
        topBanner={
          <HStack
            className={
              "bg-linear-to-r from-primary-500 to-teal-500 text-white py-2"
            }
          >
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
              <div className="flex items-center justify-center text-sm font-medium">
                <Tag className="w-4 h-4 mr-2" />
                Jusqu'à 70% de réduction sur les activités locales
              </div>
            </div>
          </HStack>
        }
        categories={categories}
        locations={[
          { label: "Douala", value: "Douala" },
          { label: "Yaoundé", value: "Yaoundé" },
          { label: "Bafoussam", value: "Bafoussam" },
          { label: "Garoua", value: "Garoua" }
        ]}
      />
      <main className="flex-1">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
};
