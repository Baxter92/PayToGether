import path from "path";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "@common": path.resolve(__dirname, "./src/common"),
      "@components": path.resolve(__dirname, "./src/common/components"),
      "@layouts": path.resolve(__dirname, "./src/common/layouts"),
      "@pages": path.resolve(__dirname, "./src/pages"),
      "@containers": path.resolve(__dirname, "./src/common/containers"),
      "@context": path.resolve(__dirname, "./src/common/context"),
      "@hooks": path.resolve(__dirname, "./src/common/hooks"),
      "@utils": path.resolve(__dirname, "./src/common/utils"),
      "@assets": path.resolve(__dirname, "./src/assets"),
      "@lib": path.resolve(__dirname, "./src/common/lib"),
      "@constants": path.resolve(__dirname, "./src/common/constants"),
    },
  },
});
