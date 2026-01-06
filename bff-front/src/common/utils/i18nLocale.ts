let currentLocale = "en-CA";

export function setLocale(locale: string): void {
  currentLocale = locale;
}

export function getLocale(): string {
  return currentLocale;
}
