import { Input } from "@components/ui/input";
import { Search, Loader2 } from "lucide-react";
import { useState, useRef, useEffect } from "react";
import { useRechercheDeals } from "@common/api/hooks/useRechercheDeals";
import { useNavigate } from "react-router-dom";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@components/ui/card";

export interface SearchBarWithResultsProps {
  placeholder?: string;
  debounceDelay?: number;
  className?: string;
  inputClassName?: string;
  autoFocus?: boolean;
  onResultClick?: (uuid: string) => void;
}

/**
 * Barre de recherche avec résultats en temps réel
 * Utilise Elasticsearch pour la recherche globale de deals
 */
const SearchBarWithResults = ({
  placeholder = "Rechercher des deals...",
  debounceDelay = 500,
  className = "",
  inputClassName = "",
  autoFocus = false,
  onResultClick,
}: SearchBarWithResultsProps) => {
  const [query, setQuery] = useState("");
  const [showResults, setShowResults] = useState(false);
  const searchRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  // Hook de recherche avec debounce
  const { data: results, isLoading, isFetching } = useRechercheDeals(query, debounceDelay);

  // Fermer les résultats quand on clique en dehors
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setShowResults(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newQuery = e.target.value;
    setQuery(newQuery);
    setShowResults(newQuery.trim().length > 0);
  };

  const handleResultClick = (uuid: string) => {
    setShowResults(false);
    setQuery("");

    if (onResultClick) {
      onResultClick(uuid);
    } else {
      // Par défaut, naviguer vers le détail du deal
      navigate(`/deals/${uuid}`);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Escape") {
      setShowResults(false);
    }
  };

  return (
    <div className={`relative ${className}`} ref={searchRef}>
      {/* Barre de recherche */}
      <div className="relative">
        <Input
          type="search"
          placeholder={placeholder}
          value={query}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          onFocus={() => query.trim().length > 0 && setShowResults(true)}
          autoFocus={autoFocus}
          className={`w-full pr-10 ${inputClassName}`}
        />
        <div className="absolute right-3 top-1/2 -translate-y-1/2">
          {isFetching ? (
            <Loader2 className="w-4 h-4 animate-spin text-muted-foreground" />
          ) : (
            <Search className="w-4 h-4 text-muted-foreground" />
          )}
        </div>
      </div>

      {/* Résultats de recherche */}
      {showResults && results && results.length > 0 && (
        <Card className="absolute top-full mt-2 w-full max-h-96 overflow-y-auto z-50 shadow-lg">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm">
              {results.length} résultat{results.length > 1 ? "s" : ""} trouvé{results.length > 1 ? "s" : ""}
            </CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <div className="divide-y">
              {results.map((deal) => (
                <button
                  key={deal.uuid}
                  onClick={() => handleResultClick(deal.uuid)}
                  className="w-full text-left p-4 hover:bg-accent transition-colors"
                >
                  <div className="flex gap-3">
                    {/* Image */}
                    {deal.imagePrincipaleUrl && (
                      <div className="flex-shrink-0">
                        <img
                          src={deal.imagePrincipaleUrl}
                          alt={deal.titre}
                          className="w-16 h-16 object-cover rounded"
                        />
                      </div>
                    )}

                    {/* Contenu */}
                    <div className="flex-1 min-w-0">
                      <h4 className="font-semibold text-sm truncate">
                        {deal.titre}
                      </h4>
                      <p className="text-xs text-muted-foreground truncate">
                        {deal.description}
                      </p>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="text-xs font-medium text-primary">
                          {deal.prixPart}€
                        </span>
                        {deal.ville && (
                          <span className="text-xs text-muted-foreground">
                            • {deal.ville}
                          </span>
                        )}
                        {deal.categorieNom && (
                          <span className="text-xs text-muted-foreground">
                            • {deal.categorieNom}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Message "Aucun résultat" */}
      {showResults && results && results.length === 0 && !isLoading && query.trim().length > 0 && (
        <Card className="absolute top-full mt-2 w-full z-50 shadow-lg">
          <CardContent className="p-4">
            <p className="text-sm text-muted-foreground text-center">
              Aucun deal trouvé pour "{query}"
            </p>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default SearchBarWithResults;

