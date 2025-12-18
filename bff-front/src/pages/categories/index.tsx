import { VStack } from "@/common/components";
import { categories } from "@/common/constants/data";
import CategoriesList from "@/common/containers/CategoriesList";
import { type JSX } from "react";

export default function Categories(): JSX.Element {
  return (
    <VStack className="p-4">
      <CategoriesList categories={categories} />
    </VStack>
  );
}
