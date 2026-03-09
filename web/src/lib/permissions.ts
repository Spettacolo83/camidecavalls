import { Role } from "@prisma/client";

type Section = "pois" | "users" | "reviews" | "danger_reports" | "settings";
type Action = "view" | "create" | "edit" | "delete";

const permissions: Record<Role, Record<Section, Action[]>> = {
  ADMIN: {
    pois: ["view", "create", "edit", "delete"],
    users: ["view", "create", "edit", "delete"],
    reviews: ["view", "create", "edit", "delete"],
    danger_reports: ["view", "create", "edit", "delete"],
    settings: ["view", "edit"],
  },
  EDITOR: {
    pois: ["view", "create", "edit"],
    users: [],
    reviews: ["view", "edit"],
    danger_reports: ["view", "edit"],
    settings: ["view"],
  },
  VIEWER: {
    pois: ["view"],
    users: [],
    reviews: ["view"],
    danger_reports: ["view"],
    settings: ["view"],
  },
};

export function hasPermission(
  role: Role | string,
  section: Section,
  action: Action
): boolean {
  const rolePerms = permissions[role as Role];
  if (!rolePerms) return false;
  return rolePerms[section]?.includes(action) ?? false;
}

export function canAccess(role: Role | string, section: Section): boolean {
  return hasPermission(role, section, "view");
}
