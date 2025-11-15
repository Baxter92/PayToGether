// eslint.config.js
import tseslint from "typescript-eslint";
import js from "@eslint/js";
import globals from "globals";
import reactPlugin from "eslint-plugin-react";
import reactHooks from "eslint-plugin-react-hooks";
import testingLibrary from "eslint-plugin-testing-library";
import jsxA11y from "eslint-plugin-jsx-a11y";

export default tseslint.config(
    js.configs.recommended,
    ...tseslint.configs.recommended,
    testingLibrary.configs["flat/react"],
    {
      ignores: [
        "node_modules",
        "dist",
        "coverage",
        "**/webpack.config.*",
        "**/vite.config.*",
        "**/vitest.config.*",
        "eslint.config.js",
      ],
      plugins: {
        react: reactPlugin,
        "react-hooks": reactHooks,
        "jsx-a11y": jsxA11y,
        "testing-library": testingLibrary,
      },
      languageOptions: {
        parser: tseslint.parser,
        parserOptions: {
          project: ["./tsconfig.json"],
          ecmaFeatures: { jsx: true },
        },
        globals: {
          ...globals.browser,
          ...globals.node,
          ...globals.jest,
          ...globals.vi,
        },
      },
      rules: {
        /* Typescript */
        "@typescript-eslint/no-unused-vars": ["error", { argsIgnorePattern: "^_" }],
        "@typescript-eslint/explicit-function-return-type": [
          "warn",
          { allowExpressions: true, allowTypedFunctionExpressions: true },
        ],
        "@typescript-eslint/no-explicit-any": "warn",
        "@typescript-eslint/consistent-type-imports": ["error", { prefer: "type-imports" }],

        /* Style */
        indent: ["error", 2],
        quotes: ["error", "double"],
        semi: ["error", "always"],
        "comma-dangle": ["error", "always-multiline"],
        "object-curly-spacing": ["error", "always"],
        "space-before-blocks": ["error", "always"],
        "keyword-spacing": ["error", { before: true, after: true }],
        "no-trailing-spaces": "error",
        "eol-last": ["error", "always"],

        /* React */
        "react/react-in-jsx-scope": "off",
        "react/jsx-uses-react": "off",
        "react/jsx-uses-vars": "error",
        "react-hooks/rules-of-hooks": "error",
        "react-hooks/exhaustive-deps": "warn",

        /* JSX A11y */
        "jsx-a11y/alt-text": "warn",
        "jsx-a11y/anchor-is-valid": "warn",
        "jsx-a11y/label-has-associated-control": "warn",
      },
    }
);
