import { useEffect, useState } from "react";

/**
 * Hook pour debouncer une valeur
 * Retarde la mise à jour de la valeur jusqu'à ce que l'utilisateur arrête de taper
 *
 * @param value Valeur à debouncer
 * @param delay Délai en millisecondes (défaut: 500ms)
 * @returns Valeur debouncée
 *
 * @example
 * const [searchQuery, setSearchQuery] = useState("");
 * const debouncedQuery = useDebounce(searchQuery, 500);
 *
 * // debouncedQuery sera mis à jour 500ms après le dernier changement de searchQuery
 */
export function useDebounce<T>(value: T, delay = 500): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    // Créer un timer pour mettre à jour la valeur après le délai
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // Nettoyer le timer si la valeur change avant la fin du délai
    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);

  return debouncedValue;
}

