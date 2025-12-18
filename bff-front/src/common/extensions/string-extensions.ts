import { capitalize, capitalizeWords } from "../utils/string";

export {}; // permet de déclarer le global scope
declare global {
  interface String {
    capitalize(): string;
    capitalizeWords(): string;
  }
}

String.prototype.capitalize = function (): string {
  return capitalize(this);
};

String.prototype.capitalizeWords = function (): string {
  return capitalizeWords(this);
};
