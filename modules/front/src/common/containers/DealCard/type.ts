import type { ImageResponse } from "@/common/api/hooks/useImageUpload";

export interface IDealCardProps {
  deal: {
    id: string;
    uuid?: string;
    title: string;
    titre?: string;
    image: ImageResponse;
    originalPrice: number;
    groupPrice: number;
    unit: number;
    sold: number;
    total: number;
    deadline: string;
    discount: number;
    popular: boolean;
    status?: string;
    statut?: string;
  };
  isAdmin?: boolean;
  onEdit?: (id: string) => void;
  onDelete?: (id: string, title: string) => void;
  onToggleStatus?: (id: string, title: string, currentStatus: string) => void;
}
