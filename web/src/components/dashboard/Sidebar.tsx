"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { signOut } from "next-auth/react";
import HorseshoeIcon from "@/components/HorseshoeIcon";
import {
  MapPin,
  Users,
  LayoutDashboard,
  ChevronLeft,
  ChevronRight,
  LogOut,
  MessageSquare,
  AlertTriangle,
  Settings,
} from "lucide-react";
import { useLocale } from "@/lib/locale-context";
import { APP_VERSION } from "@/lib/version";
import LocaleSwitcher from "./LocaleSwitcher";

interface SidebarProps {
  userRole: string;
  userName?: string | null;
}

export default function Sidebar({ userRole, userName }: SidebarProps) {
  const [collapsed, setCollapsed] = useState(false);
  const pathname = usePathname();
  const { t } = useLocale();

  const navItems = [
    {
      href: "/dashboard",
      labelKey: "nav.dashboard",
      icon: <LayoutDashboard size={20} />,
      section: "dashboard",
      enabled: true,
    },
    {
      href: "/dashboard/pois",
      labelKey: "nav.pois",
      icon: <MapPin size={20} />,
      section: "pois",
      enabled: true,
    },
    {
      href: "/dashboard/users",
      labelKey: userRole === "ADMIN" ? "nav.users" : "nav.my_account",
      icon: <Users size={20} />,
      section: "users",
      enabled: true,
    },
    {
      href: "#",
      labelKey: "nav.reviews",
      icon: <MessageSquare size={20} />,
      section: "reviews",
      enabled: false,
    },
    {
      href: "#",
      labelKey: "nav.danger_reports",
      icon: <AlertTriangle size={20} />,
      section: "danger_reports",
      enabled: false,
    },
    {
      href: "#",
      labelKey: "nav.settings",
      icon: <Settings size={20} />,
      section: "settings",
      enabled: false,
    },
  ];

  return (
    <aside
      className={`h-screen bg-dark-card border-r border-dark-border flex flex-col transition-all duration-300 ${
        collapsed ? "w-16" : "w-64"
      }`}
    >
      {/* Header */}
      <div className="flex items-center h-16 px-4 border-b border-dark-border">
        <div className="flex items-center gap-3 min-w-0">
          <div className="w-8 h-8 bg-primary/10 rounded-lg flex items-center justify-center shrink-0">
            <HorseshoeIcon size={18} className="text-primary" />
          </div>
          {!collapsed && (
            <span className="font-bold text-white truncate text-sm">
              Cami de Cavalls
            </span>
          )}
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 py-4 px-2 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const label = t(item.labelKey);
          const isActive =
            item.href === "/dashboard"
              ? pathname === "/dashboard"
              : pathname.startsWith(item.href) && item.href !== "#";

          if (!item.enabled && item.href === "#") {
            return (
              <div
                key={item.section}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-gray-text/40 cursor-not-allowed ${
                  collapsed ? "justify-center" : ""
                }`}
                title={collapsed ? `${label} (${t("nav.coming_soon")})` : undefined}
              >
                {item.icon}
                {!collapsed && (
                  <span className="text-sm truncate">
                    {label}
                    <span className="ml-2 text-xs bg-dark-border/50 text-gray-text/50 px-1.5 py-0.5 rounded">
                      {t("nav.coming_soon")}
                    </span>
                  </span>
                )}
              </div>
            );
          }

          if (!item.enabled) return null;

          return (
            <Link
              key={item.section}
              href={item.href}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
                collapsed ? "justify-center" : ""
              } ${
                isActive
                  ? "bg-primary/10 text-primary"
                  : "text-gray-text hover:bg-dark-lighter hover:text-white"
              }`}
              title={collapsed ? label : undefined}
            >
              {item.icon}
              {!collapsed && (
                <span className="text-sm truncate">{label}</span>
              )}
            </Link>
          );
        })}
      </nav>

      {/* Footer */}
      <div className="border-t border-dark-border p-2 space-y-1">
        {/* Language switcher */}
        {!collapsed && (
          <div className="px-2 py-1">
            <LocaleSwitcher />
          </div>
        )}

        {/* User info */}
        {!collapsed && (
          <div className="px-3 py-2">
            <p className="text-xs text-gray-text truncate">{userName}</p>
            <p className="text-xs text-primary/70">{userRole}</p>
            <p className="text-[10px] text-gray-text/40 mt-1">v{APP_VERSION}</p>
          </div>
        )}

        {/* Collapse toggle */}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className={`flex items-center gap-3 w-full px-3 py-2 rounded-lg text-gray-text hover:bg-dark-lighter hover:text-white transition-colors ${
            collapsed ? "justify-center" : ""
          }`}
        >
          {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
          {!collapsed && <span className="text-sm">{t("nav.collapse")}</span>}
        </button>

        {/* Logout */}
        <button
          onClick={() => signOut({ callbackUrl: "/login" })}
          className={`flex items-center gap-3 w-full px-3 py-2 rounded-lg text-gray-text hover:bg-error/10 hover:text-error transition-colors ${
            collapsed ? "justify-center" : ""
          }`}
          title={collapsed ? t("nav.signout") : undefined}
        >
          <LogOut size={18} />
          {!collapsed && <span className="text-sm">{t("nav.signout")}</span>}
        </button>
      </div>
    </aside>
  );
}
