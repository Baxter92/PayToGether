import type { ImageResponse } from "@/common/api/hooks/useImageUpload";

export interface IDealCardProps {
  deal: {
    id: string;
    title: string;
    image: ImageResponse;
    originalPrice: number;
    groupPrice: number;
    unit: number;
    sold: number;
    total: number;
    deadline: string;
    discount: number;
    popular: boolean;
  };
}
