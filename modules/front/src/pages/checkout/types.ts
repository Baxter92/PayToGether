export type CheckoutState = {
  deal?: {
    id: string;
    title?: string;
    pricePerPart: number;
    partWeightKg?: number;
  };
  qty?: number;
  dealId?: string;
};

export type ShippingData = {
  fullName: string;
  phone: string;
  address: string;
  city: string;
  postalCode?: string;
};

export type DeliveryData = {
  deliveryMethod: "home" | "pickup";
};

export type PaymentData = {
  paymentMethod: "card" | "mobile_money" | "cash" | "square";
  cardNumber?: string;
  cardExpiry?: string;
  cardCvv?: string;
  saveCard?: boolean;
  squarePaymentId?: string; // ID du paiement Square après succès
};

export type Deal = {
  uuid: string;
  id?: string;
  title?: string;
  pricePerPart: number;
  partWeightKg?: number;
};
