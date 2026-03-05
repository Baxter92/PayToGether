export interface CommentaireDTO {
  uuid?: string;
  contenu: string;
  note: number;
  utilisateurUuid: string;
  dealUuid: string;
  commentaireParentUuid?: string | null;
  estPertinent?: boolean | null;
  dateCreation?: string;
  dateModification?: string;
}

export type CreateCommentaireDTO = Omit<
  CommentaireDTO,
  "uuid" | "dateCreation" | "dateModification"
>;

export type UpdateCommentaireDTO = Partial<CreateCommentaireDTO>;

