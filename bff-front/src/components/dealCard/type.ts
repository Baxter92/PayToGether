export interface IDealCardProps {
  deal: {
    id: number;
    title: string;
    image: string;
    originalPrice: number;
    groupPrice: number;
    unit: string;
    sold: number;
    total: number;
    deadline: string;
    discount: number;
  };
}
