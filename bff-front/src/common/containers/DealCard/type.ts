export interface IDealCardProps {
  deal: {
    id: number;
    title: string;
    image: string;
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
