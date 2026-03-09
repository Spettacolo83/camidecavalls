"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { db } from "@/lib/db";
import { auth } from "@/lib/auth";
import { hasPermission } from "@/lib/permissions";
import { PoiSource, PoiType } from "@prisma/client";
import { z } from "zod";

const translationSchema = z.object({
  language: z.string(),
  name: z.string(),
  description: z.string(),
  actionButtonText: z.string().optional().default(""),
});

// Accept both full URLs and relative paths (e.g. /uploads/xxx.png)
const optionalUrlOrPath = z
  .string()
  .refine(
    (val) => !val || val.startsWith("/") || val.startsWith("http://") || val.startsWith("https://"),
    { message: "Must be a URL or a path starting with /" }
  )
  .optional()
  .or(z.literal(""));

const poiSchema = z.object({
  type: z.nativeEnum(PoiType),
  latitude: z.number().min(-90).max(90),
  longitude: z.number().min(-180).max(180),
  imageUrl: optionalUrlOrPath,
  actionUrl: optionalUrlOrPath,
  routeId: z.number().int().positive().optional().nullable(),
  isAdvertisement: z.boolean().default(false),
  isActive: z.boolean().default(true),
  source: z.nativeEnum(PoiSource).default("DYNAMIC"),
  hardcodedPoiId: z.number().int().positive().optional().nullable(),
  priority: z.number().int().default(0),
  translations: z.array(translationSchema),
});

export type PoiFormData = z.infer<typeof poiSchema>;

export async function createPoi(data: PoiFormData) {
  const session = await auth();
  if (!session?.user || !hasPermission(session.user.role, "pois", "create")) {
    throw new Error("Unauthorized");
  }

  const parsed = poiSchema.parse(data);

  // Only require English name
  const enTranslation = parsed.translations.find((t) => t.language === "en");
  if (!enTranslation?.name?.trim()) {
    throw new Error("English name is required");
  }

  await db.poi.create({
    data: {
      type: parsed.type,
      latitude: parsed.latitude,
      longitude: parsed.longitude,
      imageUrl: parsed.imageUrl || null,
      actionUrl: parsed.actionUrl || null,
      routeId: parsed.routeId || null,
      isAdvertisement: parsed.isAdvertisement,
      isActive: parsed.isActive,
      source: parsed.source,
      hardcodedPoiId: parsed.hardcodedPoiId || null,
      priority: parsed.priority,
      createdById: session.user.id,
      updatedById: session.user.id,
      translations: {
        create: parsed.translations
          .filter((t) => t.name.trim() || t.description.trim())
          .map((t) => ({
            language: t.language,
            name: t.name,
            description: t.description,
            actionButtonText: t.actionButtonText || null,
          })),
      },
    },
  });

  revalidatePath("/dashboard/pois");
  redirect("/dashboard/pois");
}

export async function updatePoi(id: string, data: PoiFormData) {
  const session = await auth();
  if (!session?.user || !hasPermission(session.user.role, "pois", "edit")) {
    throw new Error("Unauthorized");
  }

  const parsed = poiSchema.parse(data);

  const enTranslation = parsed.translations.find((t) => t.language === "en");
  if (!enTranslation?.name?.trim()) {
    throw new Error("English name is required");
  }

  await db.$transaction(async (tx) => {
    await tx.poi.update({
      where: { id },
      data: {
        type: parsed.type,
        latitude: parsed.latitude,
        longitude: parsed.longitude,
        imageUrl: parsed.imageUrl || null,
        actionUrl: parsed.actionUrl || null,
        routeId: parsed.routeId || null,
        isAdvertisement: parsed.isAdvertisement,
        isActive: parsed.isActive,
        source: parsed.source,
        hardcodedPoiId: parsed.hardcodedPoiId || null,
        priority: parsed.priority,
        updatedById: session.user.id,
      },
    });

    // Upsert translations
    for (const t of parsed.translations) {
      if (!t.name.trim() && !t.description.trim()) {
        await tx.poiTranslation.deleteMany({
          where: { poiId: id, language: t.language },
        });
        continue;
      }

      await tx.poiTranslation.upsert({
        where: { poiId_language: { poiId: id, language: t.language } },
        create: {
          poiId: id,
          language: t.language,
          name: t.name,
          description: t.description,
          actionButtonText: t.actionButtonText || null,
        },
        update: {
          name: t.name,
          description: t.description,
          actionButtonText: t.actionButtonText || null,
        },
      });
    }
  });

  revalidatePath("/dashboard/pois");
  redirect("/dashboard/pois");
}

export async function deletePoi(id: string) {
  const session = await auth();
  if (!session?.user || !hasPermission(session.user.role, "pois", "delete")) {
    throw new Error("Unauthorized");
  }

  await db.poi.delete({ where: { id } });

  revalidatePath("/dashboard/pois");
}

export async function togglePoiActive(id: string, isActive: boolean) {
  const session = await auth();
  if (!session?.user || !hasPermission(session.user.role, "pois", "edit")) {
    throw new Error("Unauthorized");
  }

  await db.poi.update({
    where: { id },
    data: { isActive, updatedById: session.user.id },
  });

  revalidatePath("/dashboard/pois");
}
