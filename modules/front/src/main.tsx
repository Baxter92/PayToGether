import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import "./common/lib/i18n/index.ts";
import App from "./App.tsx";
import { I18nProvider } from "./common/context/I18nContext.tsx";
import "./common/extensions/string-extensions";
import { Toaster } from "./common/components/ui/sonner.tsx";
import { ReactQueryProvider } from "./common/api";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ReactQueryProvider>
      <I18nProvider>
        <App />
        <Toaster />
      </I18nProvider>
    </ReactQueryProvider>
  </StrictMode>
);
