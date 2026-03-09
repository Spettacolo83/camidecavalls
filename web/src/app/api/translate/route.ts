"use server";

import { NextRequest, NextResponse } from "next/server";
import { auth } from "@/lib/auth";

// POST /api/translate
// Body: { text: string, from: string, to: string }
// Uses MyMemory free translation API
export async function POST(request: NextRequest) {
  const session = await auth();
  if (!session?.user) {
    return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
  }

  const { text, from, to } = await request.json();

  if (!text || !from || !to) {
    return NextResponse.json(
      { error: "Missing required fields: text, from, to" },
      { status: 400 }
    );
  }

  if (!text.trim()) {
    return NextResponse.json({ translatedText: "" });
  }

  try {
    const url = `https://api.mymemory.translated.net/get?q=${encodeURIComponent(text)}&langpair=${from}|${to}&de=automation@followtheflowai.com`;
    const res = await fetch(url);
    const data = await res.json();

    if (data.responseStatus === 200 && data.responseData?.translatedText) {
      return NextResponse.json({
        translatedText: data.responseData.translatedText,
      });
    }

    return NextResponse.json(
      { error: "Translation failed", details: data },
      { status: 502 }
    );
  } catch (err) {
    return NextResponse.json(
      { error: "Translation service unavailable" },
      { status: 502 }
    );
  }
}
