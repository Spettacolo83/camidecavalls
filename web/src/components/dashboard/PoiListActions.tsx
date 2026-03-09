"use client";

import { useTransition } from "react";
import Link from "next/link";
import { Pencil, Trash2, Eye, EyeOff } from "lucide-react";
import { deletePoi, togglePoiActive } from "@/lib/actions/poi-actions";
import { useLocale } from "@/lib/locale-context";

interface PoiListActionsProps {
  poiId: string;
  isActive: boolean;
  canEdit: boolean;
  canDelete: boolean;
}

export default function PoiListActions({
  poiId,
  isActive,
  canEdit,
  canDelete,
}: PoiListActionsProps) {
  const [isPending, startTransition] = useTransition();
  const { t } = useLocale();

  return (
    <div className="flex items-center gap-1 justify-end">
      {canEdit && (
        <>
          <button
            onClick={() =>
              startTransition(() => togglePoiActive(poiId, !isActive))
            }
            disabled={isPending}
            className="p-2 rounded-lg text-gray-text hover:bg-dark-lighter hover:text-white transition-colors disabled:opacity-50"
            title={isActive ? t("pois.deactivate") : t("pois.activate")}
          >
            {isActive ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
          <Link
            href={`/dashboard/pois/${poiId}`}
            className="p-2 rounded-lg text-gray-text hover:bg-dark-lighter hover:text-primary transition-colors"
            title={t("pois.edit")}
          >
            <Pencil size={16} />
          </Link>
        </>
      )}
      {canDelete && (
        <button
          onClick={() => {
            if (confirm(t("pois.confirm_delete"))) {
              startTransition(() => deletePoi(poiId));
            }
          }}
          disabled={isPending}
          className="p-2 rounded-lg text-gray-text hover:bg-error/10 hover:text-error transition-colors disabled:opacity-50"
          title={t("pois.delete")}
        >
          <Trash2 size={16} />
        </button>
      )}
    </div>
  );
}
