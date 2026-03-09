import { NextResponse } from "next/server";
import { db } from "@/lib/db";

export async function GET(
  _request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;

  const poi = await db.poi.findUnique({
    where: { id, isActive: true },
    include: { translations: true },
  });

  if (!poi) {
    return NextResponse.json({ error: "POI not found" }, { status: 404 });
  }

  return NextResponse.json({
    id: poi.id,
    type: poi.type,
    latitude: poi.latitude,
    longitude: poi.longitude,
    imageUrl: poi.imageUrl,
    routeId: poi.routeId,
    isAdvertisement: poi.isAdvertisement,
    source: poi.source,
    hardcodedPoiId: poi.hardcodedPoiId,
    priority: poi.priority,
    updatedAt: poi.updatedAt.toISOString(),
    translations: Object.fromEntries(
      poi.translations.map((t) => [
        t.language,
        { name: t.name, description: t.description },
      ])
    ),
  });
}
