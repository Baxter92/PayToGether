export interface CategoryDTO {
  uuid: string;
  nom: string;
  description: string;
  icone: string;
  dateCreation: string;
  dateModification: string;
}

export interface CreateCategoryDTO {
  nom: string;
  description: string;
  icone: string;
}

export type UpdateCategoryDTO = Omit<
  CreateCategoryDTO,
  "uuid" | "dateCreation" | "dateModification"
>;
