"use client";

import { useLocale } from "@/lib/locale-context";
import { SUPPORTED_LOCALES, LOCALE_FLAGS, LOCALE_LABELS, type Locale } from "@/lib/i18n";

export default function LocaleSwitcher() {
  const { locale, setLocale } = useLocale();

  return (
    <select
      value={locale}
      onChange={(e) => setLocale(e.target.value as Locale)}
      className="bg-dark-lighter border border-dark-border rounded-lg text-sm text-white px-2 py-1.5 cursor-pointer"
      title="Language"
    >
      {SUPPORTED_LOCALES.map((loc) => (
        <option key={loc} value={loc}>
          {LOCALE_FLAGS[loc]} {LOCALE_LABELS[loc]}
        </option>
      ))}
    </select>
  );
}
