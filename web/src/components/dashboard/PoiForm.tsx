"use client";

import { useState, useTransition, useMemo, lazy, Suspense } from "react";
import { useRouter } from "next/navigation";
import { Save, X, ChevronDown, ChevronUp, Languages } from "lucide-react";
import {
  SUPPORTED_LANGUAGES,
  LANGUAGE_LABELS,
  POI_TYPES,
  POI_TYPE_COLORS,
  type SupportedLanguage,
} from "@/lib/constants";
import { useLocale } from "@/lib/locale-context";
import ImageUpload from "./ImageUpload";
import type { PoiFormData } from "@/lib/actions/poi-actions";

const MapPicker = lazy(() => import("./MapPicker"));

// English first, then the rest in original order
const ORDERED_LANGUAGES = ["en", ...SUPPORTED_LANGUAGES.filter((l) => l !== "en")] as const;

interface PoiFormProps {
  initialData?: PoiFormData & { id?: string };
  onSubmit: (data: PoiFormData) => Promise<void>;
}

export default function PoiForm({ initialData, onSubmit }: PoiFormProps) {
  const router = useRouter();
  const { t } = useLocale();
  const [isPending, startTransition] = useTransition();
  const [error, setError] = useState("");
  const [translating, setTranslating] = useState(false);
  const [expandedLangs, setExpandedLangs] = useState<Set<string>>(
    new Set(["en"])
  );

  const isEditing = !!initialData?.id;

  const [formData, setFormData] = useState<PoiFormData>({
    type: initialData?.type ?? "COMMERCIAL",
    latitude: initialData?.latitude ?? 39.95,
    longitude: initialData?.longitude ?? 3.86,
    imageUrl: initialData?.imageUrl ?? "",
    actionUrl: initialData?.actionUrl ?? "",
    routeId: initialData?.routeId ?? null,
    isAdvertisement: initialData?.isAdvertisement ?? false,
    isActive: initialData?.isActive ?? true,
    source: initialData?.source ?? "DYNAMIC",
    hardcodedPoiId: initialData?.hardcodedPoiId ?? null,
    priority: initialData?.priority ?? 0,
    translations: initialData?.translations ??
      SUPPORTED_LANGUAGES.map((lang) => ({
        language: lang,
        name: "",
        description: "",
        actionButtonText: "",
      })),
  });

  const poiTypeLabels = useMemo(() => {
    const labels: Record<string, string> = {};
    for (const type of POI_TYPES) {
      labels[type] = t(`poi_type.${type}`);
    }
    return labels;
  }, [t]);

  function updateField<K extends keyof PoiFormData>(
    key: K,
    value: PoiFormData[K]
  ) {
    setFormData((prev) => ({ ...prev, [key]: value }));
  }

  function updateTranslation(
    lang: string,
    field: "name" | "description" | "actionButtonText",
    value: string
  ) {
    setFormData((prev) => ({
      ...prev,
      translations: prev.translations.map((tr) =>
        tr.language === lang ? { ...tr, [field]: value } : tr
      ),
    }));
  }

  function toggleLang(lang: string) {
    setExpandedLangs((prev) => {
      const next = new Set(prev);
      if (next.has(lang)) next.delete(lang);
      else next.add(lang);
      return next;
    });
  }

  async function translateToAll() {
    // Find a source language that has content
    const filledTranslation = formData.translations.find(
      (tr) => tr.name.trim() && tr.description.trim()
    );

    if (!filledTranslation) {
      setError(t("poi_form.translate_need_source"));
      return;
    }

    const sourceLang = filledTranslation.language;
    const targetLangs = formData.translations.filter(
      (tr) => tr.language !== sourceLang
    );

    // Check if there's anything to translate
    const hasEmptyNames = targetLangs.some((tr) => !tr.name.trim());
    const hasEmptyButtons = filledTranslation.actionButtonText?.trim() &&
      targetLangs.some((tr) => !tr.actionButtonText?.trim());

    if (!hasEmptyNames && !hasEmptyButtons) return;

    setTranslating(true);
    setError("");

    try {
      const updatedTranslations = [...formData.translations];

      for (const target of targetLangs) {
        const targetIdx = updatedTranslations.findIndex(
          (tr) => tr.language === target.language
        );
        if (targetIdx === -1) continue;

        const current = updatedTranslations[targetIdx];
        let newName = current.name;
        let newDesc = current.description;
        let newBtnText = current.actionButtonText ?? "";

        // Translate name + description if empty
        if (!current.name.trim()) {
          const nameRes = await fetch("/api/translate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              text: filledTranslation.name,
              from: sourceLang,
              to: target.language,
            }),
          });
          const nameData = await nameRes.json();
          newName = nameData.translatedText || "";

          const descRes = await fetch("/api/translate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              text: filledTranslation.description,
              from: sourceLang,
              to: target.language,
            }),
          });
          const descData = await descRes.json();
          newDesc = descData.translatedText || "";
        }

        // Translate action button text if source has it and target is empty
        if (filledTranslation.actionButtonText?.trim() && !current.actionButtonText?.trim()) {
          const btnRes = await fetch("/api/translate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              text: filledTranslation.actionButtonText,
              from: sourceLang,
              to: target.language,
            }),
          });
          const btnData = await btnRes.json();
          newBtnText = btnData.translatedText || "";
        }

        updatedTranslations[targetIdx] = {
          ...current,
          name: newName,
          description: newDesc,
          actionButtonText: newBtnText,
        };
      }

      setFormData((prev) => ({ ...prev, translations: updatedTranslations }));
    } catch {
      setError(t("poi_form.translate_error"));
    } finally {
      setTranslating(false);
    }
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");

    const enTranslation = formData.translations.find(
      (tr) => tr.language === "en"
    );
    if (!enTranslation?.name?.trim()) {
      setError(t("poi_form.en_name_required"));
      return;
    }

    startTransition(async () => {
      try {
        await onSubmit(formData);
      } catch (err) {
        setError(err instanceof Error ? err.message : "An error occurred");
      }
    });
  }

  const saveButtons = (
    <div className="flex gap-3">
      <button
        type="button"
        onClick={() => router.back()}
        className="px-4 py-2 border border-dark-border text-gray-text rounded-lg hover:bg-dark-lighter transition-colors flex items-center gap-2"
      >
        <X size={16} />
        {t("poi_form.cancel")}
      </button>
      <button
        type="submit"
        disabled={isPending}
        className="px-4 py-2 bg-primary text-dark font-semibold rounded-lg hover:bg-primary-hover transition-colors disabled:opacity-50 flex items-center gap-2"
      >
        <Save size={16} />
        {isPending ? t("poi_form.saving") : t("poi_form.save")}
      </button>
    </div>
  );

  return (
    <form onSubmit={handleSubmit} className="max-w-4xl">
      {/* Header with save buttons */}
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-white">
          {isEditing ? t("poi_form.edit_title") : t("poi_form.create_title")}
        </h1>
        {saveButtons}
      </div>

      {error && (
        <div className="mb-4 p-3 bg-error/10 border border-error/30 rounded-lg text-error text-sm">
          {error}
        </div>
      )}

      <div className="space-y-6">
        {/* Basic Info Card */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6">
          <h2 className="text-lg font-semibold text-white mb-4">
            {t("poi_form.basic_info")}
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Type */}
            <div>
              <label className="block text-sm text-gray-text mb-1.5">
                {t("poi_form.type")}
              </label>
              <select
                value={formData.type}
                onChange={(e) => updateField("type", e.target.value as PoiFormData["type"])}
                className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white"
              >
                {POI_TYPES.map((type) => (
                  <option key={type} value={type}>
                    {poiTypeLabels[type]}
                  </option>
                ))}
              </select>
              <div
                className="mt-2 h-1 rounded"
                style={{ backgroundColor: POI_TYPE_COLORS[formData.type] }}
              />
            </div>

            {/* Source */}
            <div>
              <label className="block text-sm text-gray-text mb-1.5">
                {t("poi_form.source")}
              </label>
              <select
                value={formData.source}
                onChange={(e) =>
                  updateField("source", e.target.value as PoiFormData["source"])
                }
                className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white"
              >
                <option value="DYNAMIC">{t("poi_form.source_dynamic")}</option>
                <option value="HARDCODED_OVERRIDE">
                  {t("poi_form.source_override")}
                </option>
              </select>
            </div>
          </div>
        </div>

        {/* Location Card with Map */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6">
          <h2 className="text-lg font-semibold text-white mb-2">
            {t("poi_form.location")}
          </h2>
          <p className="text-gray-text text-sm mb-4">
            {t("poi_form.location_hint")}
          </p>

          {/* Map */}
          <Suspense
            fallback={
              <div className="w-full h-[350px] rounded-lg border border-dark-border bg-dark-lighter flex items-center justify-center">
                <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
              </div>
            }
          >
            <MapPicker
              latitude={formData.latitude}
              longitude={formData.longitude}
              onChange={(lat, lng) => {
                updateField("latitude", lat);
                updateField("longitude", lng);
              }}
            />
          </Suspense>

          {/* Lat/Lng inputs */}
          <div className="grid grid-cols-2 gap-4 mt-4">
            <div>
              <label className="block text-sm text-gray-text mb-1.5">
                {t("poi_form.latitude")}
              </label>
              <input
                type="number"
                step="any"
                value={formData.latitude}
                onChange={(e) =>
                  updateField("latitude", parseFloat(e.target.value) || 0)
                }
                className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white font-mono text-sm"
                required
              />
            </div>
            <div>
              <label className="block text-sm text-gray-text mb-1.5">
                {t("poi_form.longitude")}
              </label>
              <input
                type="number"
                step="any"
                value={formData.longitude}
                onChange={(e) =>
                  updateField("longitude", parseFloat(e.target.value) || 0)
                }
                className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white font-mono text-sm"
                required
              />
            </div>
          </div>
        </div>

        {/* Image Card */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6">
          <ImageUpload
            value={formData.imageUrl ?? ""}
            onChange={(url) => updateField("imageUrl", url)}
          />
        </div>

        {/* Action URL Card */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6">
          <h2 className="text-lg font-semibold text-white mb-2">
            {t("poi_form.action")}
          </h2>
          <p className="text-gray-text text-sm mb-4">
            {t("poi_form.action_hint")}
          </p>
          <div>
            <label className="block text-sm text-gray-text mb-1.5">
              {t("poi_form.action_url")}
            </label>
            <input
              type="url"
              value={formData.actionUrl ?? ""}
              onChange={(e) => updateField("actionUrl", e.target.value)}
              className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white text-sm placeholder-gray-text/50"
              placeholder="https://example.com/booking"
            />
          </div>
        </div>

        {/* Extra Options Card */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Route ID */}
            <div>
              <label className="block text-sm text-gray-text mb-1.5">
                {t("poi_form.route_id")}
              </label>
              <input
                type="number"
                value={formData.routeId ?? ""}
                onChange={(e) =>
                  updateField(
                    "routeId",
                    e.target.value ? parseInt(e.target.value) : null
                  )
                }
                className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white"
                min={1}
                max={20}
              />
            </div>

            {/* Hardcoded POI ID */}
            {formData.source === "HARDCODED_OVERRIDE" && (
              <div>
                <label className="block text-sm text-gray-text mb-1.5">
                  {t("poi_form.hardcoded_id")}
                </label>
                <input
                  type="number"
                  value={formData.hardcodedPoiId ?? ""}
                  onChange={(e) =>
                    updateField(
                      "hardcodedPoiId",
                      e.target.value ? parseInt(e.target.value) : null
                    )
                  }
                  className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white"
                />
              </div>
            )}

            {/* Priority */}
            <div>
              <label className="block text-sm text-gray-text mb-1.5">
                {t("poi_form.priority")}
              </label>
              <input
                type="number"
                value={formData.priority}
                onChange={(e) =>
                  updateField("priority", parseInt(e.target.value) || 0)
                }
                className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white"
              />
            </div>
          </div>

          {/* Toggles */}
          <div className="flex gap-6 mt-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={formData.isActive}
                onChange={(e) => updateField("isActive", e.target.checked)}
                className="w-4 h-4 accent-primary"
              />
              <span className="text-sm text-gray-text">{t("poi_form.active")}</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={formData.isAdvertisement}
                onChange={(e) =>
                  updateField("isAdvertisement", e.target.checked)
                }
                className="w-4 h-4 accent-commercial"
              />
              <span className="text-sm text-gray-text">{t("poi_form.advertisement")}</span>
            </label>
          </div>
        </div>

        {/* Translations Card */}
        <div className="bg-dark-card border border-dark-border rounded-xl p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-lg font-semibold text-white">
                {t("poi_form.translations")}
              </h2>
              <p className="text-gray-text text-sm mt-1">
                {t("poi_form.translations_hint")}
              </p>
            </div>
            <button
              type="button"
              onClick={translateToAll}
              disabled={translating}
              className="px-3 py-2 bg-primary/10 text-primary rounded-lg hover:bg-primary/20 transition-colors disabled:opacity-50 flex items-center gap-2 text-sm font-medium"
            >
              <Languages size={16} />
              {translating ? t("poi_form.translating") : t("poi_form.auto_translate")}
            </button>
          </div>

          <div className="space-y-3">
            {ORDERED_LANGUAGES.map((lang) => {
              const translation = formData.translations.find(
                (tr) => tr.language === lang
              );
              const isExpanded = expandedLangs.has(lang);
              const hasContent =
                translation?.name?.trim() || translation?.description?.trim();
              const hasActionUrl = !!(formData.actionUrl?.trim());

              return (
                <div
                  key={lang}
                  className="border border-dark-border rounded-lg overflow-hidden"
                >
                  <button
                    type="button"
                    onClick={() => toggleLang(lang)}
                    className="w-full flex items-center justify-between px-4 py-3 bg-dark-lighter hover:bg-dark-border/30 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <span className="text-xs font-mono bg-primary/10 text-primary px-2 py-0.5 rounded uppercase">
                        {lang}
                      </span>
                      <span className="text-sm text-white">
                        {LANGUAGE_LABELS[lang as SupportedLanguage]}
                      </span>
                      {lang === "en" && (
                        <span className="text-xs text-primary">{t("poi_form.required")}</span>
                      )}
                      {hasContent && (
                        <span className="text-xs text-success">{t("poi_form.filled")}</span>
                      )}
                    </div>
                    {isExpanded ? (
                      <ChevronUp size={16} className="text-gray-text" />
                    ) : (
                      <ChevronDown size={16} className="text-gray-text" />
                    )}
                  </button>

                  {isExpanded && (
                    <div className="p-4 space-y-3 bg-dark">
                      <div>
                        <label className="block text-xs text-gray-text mb-1">
                          {t("poi_form.name")}
                        </label>
                        <input
                          type="text"
                          value={translation?.name ?? ""}
                          onChange={(e) =>
                            updateTranslation(lang, "name", e.target.value)
                          }
                          className="w-full px-3 py-2 bg-dark-lighter border border-dark-border rounded-lg text-white text-sm"
                          placeholder={`${t("poi_form.name")} (${LANGUAGE_LABELS[lang as SupportedLanguage]})`}
                          required={lang === "en"}
                        />
                      </div>
                      <div>
                        <label className="block text-xs text-gray-text mb-1">
                          {t("poi_form.description")}
                        </label>
                        <textarea
                          value={translation?.description ?? ""}
                          onChange={(e) =>
                            updateTranslation(
                              lang,
                              "description",
                              e.target.value
                            )
                          }
                          className="w-full px-3 py-2 bg-dark-lighter border border-dark-border rounded-lg text-white text-sm resize-y"
                          rows={3}
                          placeholder={`${t("poi_form.description")} (${LANGUAGE_LABELS[lang as SupportedLanguage]})`}
                          required={lang === "en"}
                        />
                      </div>
                      {hasActionUrl && (
                        <div>
                          <label className="block text-xs text-gray-text mb-1">
                            {t("poi_form.action_button_text")}
                          </label>
                          <input
                            type="text"
                            value={translation?.actionButtonText ?? ""}
                            onChange={(e) =>
                              updateTranslation(lang, "actionButtonText", e.target.value)
                            }
                            className="w-full px-3 py-2 bg-dark-lighter border border-dark-border rounded-lg text-white text-sm"
                            placeholder={`${t("poi_form.action_button_text")} (${LANGUAGE_LABELS[lang as SupportedLanguage]})`}
                          />
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        {/* Bottom save buttons */}
        <div className="flex justify-end">
          {saveButtons}
        </div>
      </div>
    </form>
  );
}
