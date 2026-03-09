"use client";

import { useState } from "react";
import { changePassword } from "@/lib/actions/user-actions";
import { useLocale } from "@/lib/locale-context";
import { KeyRound } from "lucide-react";

export default function PasswordChangeCard() {
  const { t } = useLocale();
  const [current, setCurrent] = useState("");
  const [newPass, setNewPass] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setSuccess(false);

    if (newPass !== confirm) {
      setError(t("password.mismatch"));
      return;
    }

    if (newPass.length < 8) {
      setError(t("password.min_length"));
      return;
    }

    setLoading(true);
    try {
      await changePassword(current, newPass);
      setSuccess(true);
      setCurrent("");
      setNewPass("");
      setConfirm("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="bg-dark-card border border-dark-border rounded-xl p-6">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-9 h-9 bg-primary/10 rounded-lg flex items-center justify-center">
          <KeyRound size={18} className="text-primary" />
        </div>
        <h2 className="text-lg font-semibold text-white">{t("password.title")}</h2>
      </div>

      <form onSubmit={handleSubmit} className="max-w-md space-y-4">
        <div>
          <label className="block text-sm text-gray-text mb-1.5">{t("password.current")}</label>
          <input
            type="password"
            value={current}
            onChange={(e) => setCurrent(e.target.value)}
            required
            className="w-full bg-dark-lighter border border-dark-border rounded-lg px-3 py-2 text-white text-sm"
          />
        </div>

        <div>
          <label className="block text-sm text-gray-text mb-1.5">{t("password.new")}</label>
          <input
            type="password"
            value={newPass}
            onChange={(e) => setNewPass(e.target.value)}
            required
            minLength={8}
            className="w-full bg-dark-lighter border border-dark-border rounded-lg px-3 py-2 text-white text-sm"
          />
        </div>

        <div>
          <label className="block text-sm text-gray-text mb-1.5">{t("password.confirm")}</label>
          <input
            type="password"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
            required
            minLength={8}
            className="w-full bg-dark-lighter border border-dark-border rounded-lg px-3 py-2 text-white text-sm"
          />
        </div>

        {error && (
          <div className="bg-error/10 border border-error/30 rounded-lg px-4 py-2.5 text-error text-sm">
            {error}
          </div>
        )}

        {success && (
          <div className="bg-success/10 border border-success/30 rounded-lg px-4 py-2.5 text-success text-sm">
            {t("password.success")}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="px-4 py-2.5 bg-primary hover:bg-primary-hover disabled:opacity-50 text-dark font-medium rounded-lg text-sm transition-colors"
        >
          {loading ? t("password.saving") : t("password.submit")}
        </button>
      </form>
    </div>
  );
}
