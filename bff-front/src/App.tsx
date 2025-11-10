import { useState } from "react";
import reactLogo from "./assets/react.svg";
import viteLogo from "/vite.svg";
import { useI18n } from "./context/I18nProvider";
import LanguageSelector from "./components/LanguageSelector";

function App() {
  const [count, setCount] = useState(0);
  const { t } = useI18n();

  return (
    <>
      <div>
        <a href="https://vite.dev" target="_blank">
          <img src={viteLogo} className="logo" alt="Vite logo" />
        </a>
        <a href="https://react.dev" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>
      <h1 className="bg-amber-600">Vite + React</h1>
      <div className="p-4">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <p>
          Edit <code>src/App.tsx</code> and save to test HMR
        </p>
      </div>
      <LanguageSelector />
      <p className="read-the-docs">{t("hello")}</p>
    </>
  );
}

export default App;
