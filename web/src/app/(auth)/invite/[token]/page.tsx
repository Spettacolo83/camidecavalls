"use client";

import { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import HorseshoeIcon from "@/components/HorseshoeIcon";
import { CheckCircle, XCircle, Loader2 } from "lucide-react";
import { getTranslations, type Locale, LOCALE_COOKIE } from "@/lib/i18n";

function getLocaleClient(): Locale {
  if (typeof document === "undefined") return "en";
  const match = document.cookie.match(new RegExp(`${LOCALE_COOKIE}=([^;]+)`));
  return (match?.[1] as Locale) || "en";
}

export default function AcceptInvitePage() {
  const { token } = useParams<{ token: string }>();
  const router = useRouter();
  const [status, setStatus] = useState<"idle" | "loading" | "success" | "error">("idle");
  const [errorMsg, setErrorMsg] = useState("");

  const [dict, setDict] = useState(getTranslations("en"));
  useEffect(() => {
    setDict(getTranslations(getLocaleClient()));
  }, []);

  async function handleAccept() {
    setStatus("loading");
    try {
      const res = await fetch("/api/invite/accept", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token }),
      });
      const data = await res.json();
      if (!res.ok) {
        setStatus("error");
        setErrorMsg(data.error || "Something went wrong");
        return;
      }
      setStatus("success");
      setTimeout(() => router.push("/login"), 3000);
    } catch {
      setStatus("error");
      setErrorMsg("Network error. Please try again.");
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
              {dict["invite.title"]}
            </h1>
            <p className="text-gray-text text-sm mt-1">{dict["invite.subtitle"]}</p>
          </div>

          {status === "idle" && (
            <div className="text-center space-y-4">
              <p className="text-gray-text text-sm">{dict["invite.description"]}</p>
              <button
                onClick={handleAccept}
                className="w-full py-2.5 bg-primary hover:bg-primary-hover text-dark font-semibold rounded-lg transition-colors"
              >
                {dict["invite.accept"]}
              </button>
            </div>
          )}

          {status === "loading" && (
            <div className="flex flex-col items-center gap-3 py-4">
              <Loader2 className="w-8 h-8 text-primary animate-spin" />
              <p className="text-gray-text text-sm">{dict["invite.loading"]}</p>
            </div>
          )}

          {status === "success" && (
            <div className="flex flex-col items-center gap-3 py-4">
              <CheckCircle className="w-10 h-10 text-success" />
              <p className="text-white font-medium">{dict["invite.success"]}</p>
              <p className="text-gray-text text-sm">{dict["invite.redirect"]}</p>
            </div>
          )}

          {status === "error" && (
            <div className="flex flex-col items-center gap-3 py-4">
              <XCircle className="w-10 h-10 text-error" />
              <p className="text-white font-medium">{dict["invite.error"]}</p>
              <p className="text-error text-sm">{errorMsg}</p>
              <button
                onClick={() => setStatus("idle")}
                className="mt-2 px-4 py-2 text-sm text-primary hover:text-primary-hover transition-colors"
              >
                {dict["invite.retry"]}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
