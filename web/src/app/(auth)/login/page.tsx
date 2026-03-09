"use client";

import { useState, useEffect } from "react";
import { signIn } from "next-auth/react";
import { useRouter } from "next/navigation";
import HorseshoeIcon from "@/components/HorseshoeIcon";
import { getTranslations, type Locale, LOCALE_COOKIE } from "@/lib/i18n";

function getLocaleClient(): Locale {
  if (typeof document === "undefined") return "en";
  const match = document.cookie.match(new RegExp(`${LOCALE_COOKIE}=([^;]+)`));
  return (match?.[1] as Locale) || "en";
}

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const [dict, setDict] = useState(getTranslations("en"));
  useEffect(() => {
    setDict(getTranslations(getLocaleClient()));
  }, []);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);

    const result = await signIn("credentials", {
      email,
      password,
      redirect: false,
    });

    setLoading(false);

    if (result?.error) {
      setError(dict["login.error"]);
    } else {
      router.push("/dashboard");
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-dark">
      <div className="w-full max-w-md p-8">
        <div className="bg-dark-card rounded-2xl border border-dark-border p-8 shadow-xl">
          <div className="flex flex-col items-center mb-8">
            <div className="w-16 h-16 bg-primary/10 rounded-2xl flex items-center justify-center mb-4">
              <HorseshoeIcon size={32} className="text-primary" />
            </div>
            <h1 className="text-2xl font-bold text-white">
              {dict["login.title"]}
            </h1>
            <p className="text-gray-text text-sm mt-1">{dict["login.subtitle"]}</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm text-gray-text mb-1.5">
                {dict["login.email"]}
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white placeholder-gray-text/50"
                placeholder={dict["login.email_placeholder"]}
                required
              />
            </div>

            <div>
              <label className="block text-sm text-gray-text mb-1.5">
                {dict["login.password"]}
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white placeholder-gray-text/50"
                placeholder={dict["login.password_placeholder"]}
                required
              />
            </div>

            {error && (
              <p className="text-error text-sm text-center">{error}</p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 bg-primary hover:bg-primary-hover text-dark font-semibold rounded-lg transition-colors disabled:opacity-50"
            >
              {loading ? dict["login.loading"] : dict["login.submit"]}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
