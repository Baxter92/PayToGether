export const PATHS = {
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  PROFILE: "/profile",
  CHECKOUT: (id: string | number = ":id") => `/deals/${id}/checkout`,
  DEAL_DETAIL: (id: string | number = ":id") => `/deals/${id}`,
  ORDERS: "/orders",
  FAVORITES: "/profile#favorites",
  USERSITTINGS: "/profile#settings",
};
