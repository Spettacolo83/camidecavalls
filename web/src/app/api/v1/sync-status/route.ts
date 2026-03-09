import { NextResponse } from "next/server";
import { db } from "@/lib/db";

// GET /api/v1/sync-status
// Returns the latest update timestamp and POI count
// Used by the mobile app to check if a full sync is needed
export async function GET() {
  const [latestPoi, count, deletedCount] = await Promise.all([
    db.poi.findFirst({
      where: { isActive: true },
      orderBy: { updatedAt: "desc" },
      select: { updatedAt: true },
    }),
    db.poi.count({ where: { isActive: true } }),
    db.poi.count({ where: { isActive: false } }),
  ]);

  return NextResponse.json({
    lastUpdated: latestPoi?.updatedAt?.toISOString() ?? null,
    activeCount: count,
    inactiveCount: deletedCount,
    serverTime: new Date().toISOString(),
  });
}
