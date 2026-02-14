export function buildQueryString(params?: Record<string, any>): string {
  if (!params) return "";
  const entries = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null)
    .flatMap(([k, v]) =>
      Array.isArray(v)
        ? v.map(
            (x) => `${encodeURIComponent(k)}=${encodeURIComponent(String(x))}`,
          )
        : `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`,
    )
    .filter(Boolean);
  return entries.length ? `?${entries.join("&")}` : "";
}
