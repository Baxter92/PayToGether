import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import "./common/lib/i18n/index.ts";
import App from "./App.tsx";
import { I18nProvider } from "./common/context/I18nContext.tsx";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <I18nProvider>
      <App />
    </I18nProvider>
  </StrictMode>
);
