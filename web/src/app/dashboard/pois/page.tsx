import Link from "next/link";
import { cookies } from "next/headers";
import { db } from "@/lib/db";
import { auth } from "@/lib/auth";
import { hasPermission } from "@/lib/permissions";
import { getLocaleFromCookie, t } from "@/lib/i18n";
import { Plus, MapPin } from "lucide-react";
import { POI_TYPE_COLORS } from "@/lib/constants";
import PoiListActions from "@/components/dashboard/PoiListActions";

async function getPois() {
  return db.poi.findMany({
    include: {
      translations: true,
      createdBy: { select: { name: true, email: true } },
    },
    orderBy: [{ priority: "desc" }, { updatedAt: "desc" }],
  });
}

export default async function PoisPage() {
  const session = await auth();
  const pois = await getPois();
  const canCreate = hasPermission(session!.user.role, "pois", "create");
  const canEdit = hasPermission(session!.user.role, "pois", "edit");
  const canDelete = hasPermission(session!.user.role, "pois", "delete");
  const cookieStore = await cookies();
  const locale = getLocaleFromCookie(cookieStore.toString());

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white">{t(locale, "pois.title")}</h1>
          <p className="text-gray-text mt-1">{t(locale, "pois.subtitle")}</p>
        </div>
        {canCreate && (
          <Link
            href="/dashboard/pois/new"
            className="px-4 py-2 bg-primary text-dark font-semibold rounded-lg hover:bg-primary-hover transition-colors flex items-center gap-2"
          >
            <Plus size={18} />
            {t(locale, "pois.new")}
          </Link>
        )}
      </div>

      {pois.length === 0 ? (
        <div className="bg-dark-card border border-dark-border rounded-xl p-12 text-center">
          <MapPin size={48} className="mx-auto text-gray-text/30 mb-4" />
          <p className="text-gray-text">{t(locale, "pois.empty")}</p>
          {canCreate && (
            <Link
              href="/dashboard/pois/new"
              className="inline-block mt-4 px-4 py-2 bg-primary text-dark font-semibold rounded-lg hover:bg-primary-hover transition-colors"
            >
              {t(locale, "pois.create_first")}
            </Link>
          )}
        </div>
      ) : (
        <div className="bg-dark-card border border-dark-border rounded-xl overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-dark-border text-left">
                <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase tracking-wider">
                  {t(locale, "pois.col_type")}
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase tracking-wider">
                  {t(locale, "pois.col_name")}
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase tracking-wider hidden md:table-cell">
                  {t(locale, "pois.col_coords")}
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase tracking-wider hidden lg:table-cell">
                  {t(locale, "pois.col_source")}
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase tracking-wider">
                  {t(locale, "pois.col_status")}
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase tracking-wider hidden lg:table-cell">
                  {t(locale, "pois.col_langs")}
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase tracking-wider text-right">
                  {t(locale, "pois.col_actions")}
                </th>
              </tr>
            </thead>
            <tbody>
              {pois.map((poi) => {
                const enName =
                  poi.translations.find((tr) => tr.language === "en")?.name ??
                  poi.translations[0]?.name ??
                  "Untitled";
                const langCount = poi.translations.length;

                return (
                  <tr
                    key={poi.id}
                    className="border-b border-dark-border/50 hover:bg-dark-lighter/50 transition-colors"
                  >
                    <td className="px-4 py-3">
                      <span
                        className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium text-white"
                        style={{
                          backgroundColor: `${POI_TYPE_COLORS[poi.type]}25`,
                          color: POI_TYPE_COLORS[poi.type],
                        }}
                      >
                        <span
                          className="w-2 h-2 rounded-full"
                          style={{ backgroundColor: POI_TYPE_COLORS[poi.type] }}
                        />
                        {t(locale, `poi_type.${poi.type}`)}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-sm text-white font-medium">
                        {enName}
                      </span>
                    </td>
                    <td className="px-4 py-3 hidden md:table-cell">
                      <span className="text-xs text-gray-text font-mono">
                        {poi.latitude.toFixed(4)}, {poi.longitude.toFixed(4)}
                      </span>
                    </td>
                    <td className="px-4 py-3 hidden lg:table-cell">
                      <span
                        className={`text-xs ${
                          poi.source === "HARDCODED_OVERRIDE"
                            ? "text-warning"
                            : "text-gray-text"
                        }`}
                      >
                        {poi.source === "HARDCODED_OVERRIDE"
                          ? `${t(locale, "pois.source_override")} #${poi.hardcodedPoiId}`
                          : t(locale, "pois.source_dynamic")}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-block w-2 h-2 rounded-full ${
                          poi.isActive ? "bg-success" : "bg-gray-text/30"
                        }`}
                      />
                    </td>
                    <td className="px-4 py-3 hidden lg:table-cell">
                      <span className="text-xs text-gray-text">
                        {langCount}/6
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <PoiListActions
                        poiId={poi.id}
                        isActive={poi.isActive}
                        canEdit={canEdit}
                        canDelete={canDelete}
                      />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
