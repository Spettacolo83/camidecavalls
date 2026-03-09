import { auth } from "@/lib/auth";
import { hasPermission } from "@/lib/permissions";
import { redirect } from "next/navigation";
import PoiForm from "@/components/dashboard/PoiForm";
import { createPoi } from "@/lib/actions/poi-actions";

export default async function NewPoiPage() {
  const session = await auth();
  if (!session?.user || !hasPermission(session.user.role, "pois", "create")) {
    redirect("/dashboard/pois");
  }

  return <PoiForm onSubmit={createPoi} />;
}
