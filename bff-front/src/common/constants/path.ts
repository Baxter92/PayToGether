export const PATHS = {
  HOME: "/",
  LOGIN: "/login",
  REGISTER: "/register",
  FORGOT_PASSWORD: "/forgot-password",
  PROFILE: "/profile",
  CHECKOUT: (id: string | number = ":id") => `/deals/${id}/checkout`,
  SUCCESS_SUBSCRIPTION: (id: string | number = ":id") =>
    `/subscription/${id}/success`,
  DEAL_DETAIL: (id: string | number = ":id") => `/deals/${id}`,
  ORDERS: "/orders",
  FAVORITES: "/profile#favorites",
  SEARCH: "/search",
  USERSITTINGS: "/profile#settings",
  CATEGORIES: (id: string | number = ":id") => `/categories/${id}`,
  ALL_CATEGORIES: "/categories",
  NOT_FOUND: "/404",
};
