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
  paymentMethod: "card" | "mobile_money" | "cash";
  cardNumber?: string;
  cardExpiry?: string;
  cardCvv?: string;
};
