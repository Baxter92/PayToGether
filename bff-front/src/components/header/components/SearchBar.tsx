import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";
import { useState } from "react";

const SearchBar = () => {
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  return (
    <div className="relative w-full">
      <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
      <Input
        type="search"
        placeholder="Rechercher des deals, restaurants, activités..."
        className={`pl-10 pr-4 py-2 w-full transition-all ${
          isSearchFocused ? "ring-2 ring-primary-500" : ""
        }`}
        onFocus={() => setIsSearchFocused(true)}
        onBlur={() => setIsSearchFocused(false)}
      />
    </div>
  );
};

export default SearchBar;
