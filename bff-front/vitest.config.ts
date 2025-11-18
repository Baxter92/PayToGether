import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react/dist";

export default defineConfig({
    plugins: [react()],
    test: {
        environment: "jsdom", // simulateur de navigateur
        globals: true, // permet d’utiliser expect() sans import manuel
        setupFiles: "./src/setupTests.ts", // fichier de config des tests
        coverage: {
            provider: "v8",
            reporter: ["text", "json", "html"],
        },
    },
});
