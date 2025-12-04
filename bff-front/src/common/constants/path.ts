export const PATHS = {
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  PROFILE: "/profile",
  CHECKOUT: (id: string | number = ":id") => `/deals/${id}/checkout`,
  SUCCESS_SUBSCRIPTION: (id: string | number = ":id") =>
    `/subscription/${id}/success`,
  DEAL_DETAIL: (id: string | number = ":id") => `/deals/${id}`,
  ORDERS: "/orders",
  FAVORITES: "/profile#favorites",
  USERSITTINGS: "/profile#settings",
};
