export const SUPPORTED_LANGUAGES = ["ca", "es", "en", "fr", "de", "it"] as const;
export type SupportedLanguage = (typeof SUPPORTED_LANGUAGES)[number];

export const LANGUAGE_LABELS: Record<SupportedLanguage, string> = {
  ca: "Catala",
  es: "Espanol",
  en: "English",
  fr: "Francais",
  de: "Deutsch",
  it: "Italiano",
};

export const POI_TYPES = [
  "BEACH",
  "NATURAL",
  "HISTORIC",
  "COMMERCIAL",
  "DANGER",
] as const;

export const POI_TYPE_COLORS: Record<string, string> = {
  BEACH: "#6FBAFF",
  NATURAL: "#7FD17F",
  HISTORIC: "#FF8080",
  COMMERCIAL: "#FFB85C",
  DANGER: "#FF5252",
};

export const POI_TYPE_LABELS: Record<string, string> = {
  BEACH: "Coastal Zone",
  NATURAL: "Natural Area",
  HISTORIC: "Historic Site",
  COMMERCIAL: "Commercial",
  DANGER: "Danger / Alert",
};
