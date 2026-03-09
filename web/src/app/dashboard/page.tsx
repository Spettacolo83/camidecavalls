import { cookies } from "next/headers";
import { db } from "@/lib/db";
import { auth } from "@/lib/auth";
import { getLocaleFromCookie, t } from "@/lib/i18n";
import { MapPin, Eye, AlertTriangle, TrendingUp } from "lucide-react";

async function getStats() {
  const [totalPois, activePois, dangerPois, commercialPois] = await Promise.all(
    [
      db.poi.count(),
      db.poi.count({ where: { isActive: true } }),
      db.poi.count({ where: { type: "DANGER", isActive: true } }),
      db.poi.count({ where: { type: "COMMERCIAL", isActive: true } }),
    ]
  );

  return { totalPois, activePois, dangerPois, commercialPois };
}

export default async function DashboardPage() {
  const session = await auth();
  const stats = await getStats();
  const cookieStore = await cookies();
  const locale = getLocaleFromCookie(cookieStore.toString());

  const cards = [
    {
      label: t(locale, "dashboard.total_pois"),
      value: stats.totalPois,
      icon: <MapPin size={24} />,
      color: "text-primary",
      bgColor: "bg-primary/10",
    },
    {
      label: t(locale, "dashboard.active_pois"),
      value: stats.activePois,
      icon: <Eye size={24} />,
      color: "text-success",
      bgColor: "bg-success/10",
    },
    {
      label: t(locale, "dashboard.danger_alerts"),
      value: stats.dangerPois,
      icon: <AlertTriangle size={24} />,
      color: "text-danger",
      bgColor: "bg-danger/10",
    },
    {
      label: t(locale, "dashboard.commercial"),
      value: stats.commercialPois,
      icon: <TrendingUp size={24} />,
      color: "text-commercial",
      bgColor: "bg-commercial/10",
    },
  ];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-white">{t(locale, "dashboard.title")}</h1>
        <p className="text-gray-text mt-1">
          {t(locale, "dashboard.welcome")} {session?.user?.name || session?.user?.email}
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map((card) => (
          <div
            key={card.label}
            className="bg-dark-card border border-dark-border rounded-xl p-5"
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-gray-text text-sm">{card.label}</p>
                <p className="text-3xl font-bold text-white mt-1">
                  {card.value}
                </p>
              </div>
              <div
                className={`w-12 h-12 ${card.bgColor} rounded-xl flex items-center justify-center ${card.color}`}
              >
                {card.icon}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-8 bg-dark-card border border-dark-border rounded-xl p-6">
        <h2 className="text-lg font-semibold text-white mb-4">{t(locale, "dashboard.quickstart")}</h2>
        <div className="space-y-3 text-gray-text text-sm">
          <p dangerouslySetInnerHTML={{ __html: t(locale, "dashboard.quickstart_1") }} />
          <p dangerouslySetInnerHTML={{ __html: t(locale, "dashboard.quickstart_2") }} />
          <p dangerouslySetInnerHTML={{ __html: t(locale, "dashboard.quickstart_3") }} />
        </div>
      </div>
    </div>
  );
}
