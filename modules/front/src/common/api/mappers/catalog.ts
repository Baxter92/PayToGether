import type { CategoryDTO, DealDTO } from "@/common/api/types";
import type { ICategory } from "@/common/containers/CategoryCard/type";

const DAY_MS = 1000 * 60 * 60 * 24;

export const mapCategoryToView = (category: CategoryDTO): ICategory => {
  return {
    id: category.uuid,
    name: category.nom,
    href: category.uuid,
    description: category.description,
  };
};

export const mapDealToView = (deal: DealDTO) => {
  const expiration = deal.dateExpiration ? new Date(deal.dateExpiration) : null;
  const now = new Date();
  const deadline =
    expiration && !Number.isNaN(expiration.getTime())
      ? Math.max(0, Math.ceil((expiration.getTime() - now.getTime()) / DAY_MS))
      : 0;

  const firstImage = deal.listeImages?.[0];
  const image =
    typeof firstImage?.urlImage === "string" &&
    firstImage.urlImage.trim().length > 0
      ? firstImage.urlImage
      : "/placeholder.svg";

  const discount =
    deal.prixDeal > 0
      ? Math.max(0, Math.round((1 - deal.prixPart / deal.prixDeal) * 100))
      : 0;

  return {
    id: deal.uuid,
    title: deal.titre,
    subtitle: deal.description,
    image,
    originalPrice: Number(deal.prixDeal) || 0,
    groupPrice: Number(deal.prixPart) || 0,
    unit: 1,
    sold: 0,
    total: Number(deal.nbParticipants) || 0,
    deadline: String(deadline),
    category: deal.categorieNom || deal.categorieUuid,
    city: deal.ville || "",
    discount,
    popular: false,
    status: deal.statut,
    raw: deal,
  };
};
