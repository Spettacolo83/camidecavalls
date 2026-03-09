import { notFound, redirect } from "next/navigation";
import { db } from "@/lib/db";
import { auth } from "@/lib/auth";
import { hasPermission } from "@/lib/permissions";
import PoiForm from "@/components/dashboard/PoiForm";
import { updatePoi } from "@/lib/actions/poi-actions";
import { SUPPORTED_LANGUAGES } from "@/lib/constants";

async function getPoi(id: string) {
  return db.poi.findUnique({
    where: { id },
    include: { translations: true },
  });
}

export default async function EditPoiPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const session = await auth();
  if (!session?.user || !hasPermission(session.user.role, "pois", "edit")) {
    redirect("/dashboard/pois");
  }

  const { id } = await params;
  const poi = await getPoi(id);
  if (!poi) notFound();

  const translations = SUPPORTED_LANGUAGES.map((lang) => {
    const existing = poi.translations.find((tr) => tr.language === lang);
    return {
      language: lang,
      name: existing?.name ?? "",
      description: existing?.description ?? "",
      actionButtonText: existing?.actionButtonText ?? "",
    };
  });

  const initialData = {
    id: poi.id,
    type: poi.type,
    latitude: poi.latitude,
    longitude: poi.longitude,
    imageUrl: poi.imageUrl ?? "",
    actionUrl: poi.actionUrl ?? "",
    routeId: poi.routeId,
    isAdvertisement: poi.isAdvertisement,
    isActive: poi.isActive,
    source: poi.source,
    hardcodedPoiId: poi.hardcodedPoiId,
    priority: poi.priority,
    translations,
  };

  async function handleUpdate(data: Parameters<typeof updatePoi>[1]) {
    "use server";
    return updatePoi(id, data);
  }

  return <PoiForm initialData={initialData} onSubmit={handleUpdate} />;
}
