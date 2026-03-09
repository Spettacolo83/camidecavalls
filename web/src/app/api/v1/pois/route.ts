import { NextRequest, NextResponse } from "next/server";
import { db } from "@/lib/db";

// GET /api/v1/pois?since=ISO_TIMESTAMP
// Returns active POIs, optionally filtered by last update time
export async function GET(request: NextRequest) {
  const since = request.nextUrl.searchParams.get("since");

  const where: Record<string, unknown> = { isActive: true };

  if (since) {
    const sinceDate = new Date(since);
    if (isNaN(sinceDate.getTime())) {
      return NextResponse.json(
        { error: "Invalid 'since' parameter. Use ISO 8601 format." },
        { status: 400 }
      );
    }
    where.updatedAt = { gte: sinceDate };
  }

  const pois = await db.poi.findMany({
    where,
    include: { translations: true },
    orderBy: [{ priority: "desc" }, { updatedAt: "desc" }],
  });

  const response = pois.map((poi) => ({
    id: poi.id,
    type: poi.type,
    latitude: poi.latitude,
    longitude: poi.longitude,
    imageUrl: poi.imageUrl,
    actionUrl: poi.actionUrl,
    routeId: poi.routeId,
    isAdvertisement: poi.isAdvertisement,
    source: poi.source,
    hardcodedPoiId: poi.hardcodedPoiId,
    priority: poi.priority,
    updatedAt: poi.updatedAt.toISOString(),
    translations: Object.fromEntries(
      poi.translations.map((t) => [
        t.language,
        { name: t.name, description: t.description, actionButtonText: t.actionButtonText },
      ])
    ),
  }));

  return NextResponse.json({
    pois: response,
    count: response.length,
    timestamp: new Date().toISOString(),
  });
}
