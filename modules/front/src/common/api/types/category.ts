export interface CategoryDTO {
  uuid: string;
  nom: string;
  description: string;
  icone: string;
  dateCreation: string;
  dateModification: string;
  nbDeals: number;
}

export interface CreateCategoryDTO {
  nom: string;
  description: string;
  icone: string;
}

export type UpdateCategoryDTO = Partial<
  Omit<CreateCategoryDTO, "uuid" | "dateCreation" | "dateModification">
>;
