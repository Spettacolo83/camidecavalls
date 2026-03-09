"use client";

import { useState, useRef } from "react";
import { Upload, X, Image as ImageIcon } from "lucide-react";
import { useLocale } from "@/lib/locale-context";

interface ImageUploadProps {
  value: string;
  onChange: (url: string) => void;
}

export default function ImageUpload({ value, onChange }: ImageUploadProps) {
  const { t } = useLocale();
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);
  const [error, setError] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  async function handleFile(file: File) {
    if (!file.type.startsWith("image/")) {
      setError("Only image files are allowed");
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      setError("Max file size is 5MB");
      return;
    }

    setError("");
    setUploading(true);

    try {
      const formData = new FormData();
      formData.append("file", file);

      const res = await fetch("/api/upload", {
        method: "POST",
        body: formData,
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || "Upload failed");
      }

      const data = await res.json();
      onChange(data.url);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Upload failed");
    } finally {
      setUploading(false);
    }
  }

  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    setDragOver(false);
    const file = e.dataTransfer.files[0];
    if (file) handleFile(file);
  }

  return (
    <div className="space-y-3">
      <label className="block text-sm text-gray-text mb-1.5">
        {t("poi_form.image")}
      </label>

      {/* Preview */}
      {value && (
        <div className="relative inline-block">
          <img
            src={value}
            alt="POI"
            className="w-32 h-32 object-cover rounded-lg border border-dark-border"
          />
          <button
            type="button"
            onClick={() => onChange("")}
            className="absolute -top-2 -right-2 w-6 h-6 bg-error rounded-full flex items-center justify-center text-white hover:bg-error/80"
          >
            <X size={14} />
          </button>
        </div>
      )}

      {/* Drop zone */}
      {!value && (
        <div
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleDrop}
          onClick={() => inputRef.current?.click()}
          className={`border-2 border-dashed rounded-lg p-6 text-center cursor-pointer transition-colors ${
            dragOver
              ? "border-primary bg-primary/5"
              : "border-dark-border hover:border-gray-text"
          }`}
        >
          {uploading ? (
            <div className="flex flex-col items-center gap-2">
              <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
              <p className="text-sm text-gray-text">{t("poi_form.image_uploading")}</p>
            </div>
          ) : (
            <div className="flex flex-col items-center gap-2">
              <Upload size={24} className="text-gray-text" />
              <p className="text-sm text-gray-text">{t("poi_form.image_upload")}</p>
              <p className="text-xs text-gray-text/60">Drag & drop or click (max 5MB)</p>
            </div>
          )}
        </div>
      )}

      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) handleFile(file);
          e.target.value = "";
        }}
      />

      {error && <p className="text-error text-xs">{error}</p>}

      {/* URL fallback */}
      <div>
        <label className="block text-xs text-gray-text mb-1">
          {t("poi_form.image_or_url")}
        </label>
        <input
          type="text"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className="w-full px-3 py-2 bg-dark-lighter border border-dark-border rounded-lg text-white text-sm placeholder-gray-text/50"
          placeholder="https://example.com/image.jpg"
        />
      </div>
    </div>
  );
}
