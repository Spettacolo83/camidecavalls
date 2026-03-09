"use client";

import { useState, useTransition } from "react";
import { Role } from "@prisma/client";
import {
  UserPlus,
  Trash2,
  Shield,
  ShieldCheck,
  Eye,
  EyeOff,
  Copy,
  Check,
  Mail,
  MailX,
  RefreshCw,
} from "lucide-react";
import {
  createInvite,
  deleteUser,
  updateUserRole,
  toggleUserActive,
  resendInvite,
} from "@/lib/actions/user-actions";
import { useLocale } from "@/lib/locale-context";

interface User {
  id: string;
  email: string;
  name: string | null;
  role: Role;
  isActive: boolean;
  inviteAccepted: boolean;
  createdAt: Date;
}

interface Props {
  users: User[];
  currentUserId: string;
}

export default function UserManagement({ users, currentUserId }: Props) {
  const { t } = useLocale();
  const [showInviteForm, setShowInviteForm] = useState(false);
  const [inviteResult, setInviteResult] = useState<{
    inviteToken: string;
    tempPassword: string;
    emailSent: boolean;
  } | null>(null);
  const [copied, setCopied] = useState(false);
  const [isPending, startTransition] = useTransition();
  const [error, setError] = useState("");

  const [email, setEmail] = useState("");
  const [name, setName] = useState("");
  const [role, setRole] = useState<Role>("EDITOR");

  function handleInvite(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    startTransition(async () => {
      try {
        const result = await createInvite(email, name, role);
        setInviteResult(result);
        setEmail("");
        setName("");
      } catch (err) {
        setError(err instanceof Error ? err.message : "Error creating invite");
      }
    });
  }

  function copyCredentials() {
    if (!inviteResult) return;
    navigator.clipboard.writeText(
      `Email: ${email || "see form"}\n${t("users.temp_password")}: ${inviteResult.tempPassword}`
    );
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  const roleIcon = (r: Role) => {
    switch (r) {
      case "ADMIN":
        return <ShieldCheck size={14} className="text-primary" />;
      case "EDITOR":
        return <Shield size={14} className="text-commercial" />;
      default:
        return <Eye size={14} className="text-gray-text" />;
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white">{t("users.title")}</h1>
          <p className="text-gray-text mt-1">{t("users.subtitle")}</p>
        </div>
        <button
          onClick={() => {
            setShowInviteForm(!showInviteForm);
            setInviteResult(null);
          }}
          className="px-4 py-2 bg-primary text-dark font-semibold rounded-lg hover:bg-primary-hover transition-colors flex items-center gap-2"
        >
          <UserPlus size={18} />
          {t("users.invite")}
        </button>
      </div>

      {/* Invite Form */}
      {showInviteForm && (
        <div className="bg-dark-card border border-dark-border rounded-xl p-6 mb-6">
          <h2 className="text-lg font-semibold text-white mb-4">
            {t("users.invite_title")}
          </h2>

          {inviteResult ? (
            <div className="space-y-3">
              <div className="flex items-center gap-2">
                {inviteResult.emailSent ? (
                  <>
                    <Mail size={16} className="text-success" />
                    <p className="text-success text-sm">{t("users.created_email_sent")}</p>
                  </>
                ) : (
                  <>
                    <MailX size={16} className="text-warning" />
                    <p className="text-warning text-sm">{t("users.created_email_failed")}</p>
                  </>
                )}
              </div>
              <div className="bg-dark-lighter p-4 rounded-lg space-y-2">
                <p className="text-sm text-gray-text">
                  {t("users.temp_password")}:{" "}
                  <code className="text-white bg-dark px-2 py-0.5 rounded">
                    {inviteResult.tempPassword}
                  </code>
                </p>
                <p className="text-xs text-gray-text">
                  {inviteResult.emailSent ? t("users.email_sent_hint") : t("users.email_failed_hint")}
                </p>
              </div>
              <button
                onClick={copyCredentials}
                className="flex items-center gap-2 text-sm text-primary hover:text-primary-hover"
              >
                {copied ? <Check size={14} /> : <Copy size={14} />}
                {copied ? t("users.copied") : t("users.copy_credentials")}
              </button>
            </div>
          ) : (
            <form onSubmit={handleInvite} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm text-gray-text mb-1.5">
                    {t("users.email")}
                  </label>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-text mb-1.5">
                    {t("users.name")}
                  </label>
                  <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-text mb-1.5">
                    {t("users.role")}
                  </label>
                  <select
                    value={role}
                    onChange={(e) => setRole(e.target.value as Role)}
                    className="w-full px-4 py-2.5 bg-dark-lighter border border-dark-border rounded-lg text-white"
                  >
                    <option value="VIEWER">{t("users.role_viewer")}</option>
                    <option value="EDITOR">{t("users.role_editor")}</option>
                    <option value="ADMIN">{t("users.role_admin")}</option>
                  </select>
                </div>
              </div>
              {error && <p className="text-error text-sm">{error}</p>}
              <button
                type="submit"
                disabled={isPending}
                className="px-4 py-2 bg-primary text-dark font-semibold rounded-lg hover:bg-primary-hover transition-colors disabled:opacity-50"
              >
                {isPending ? t("users.creating") : t("users.create_btn")}
              </button>
            </form>
          )}
        </div>
      )}

      {/* Users Table */}
      <div className="bg-dark-card border border-dark-border rounded-xl overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-dark-border text-left">
              <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase">
                {t("users.name")}
              </th>
              <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase">
                {t("users.role")}
              </th>
              <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase">
                {t("users.status")}
              </th>
              <th className="px-4 py-3 text-xs font-medium text-gray-text uppercase text-right">
                {t("users.actions")}
              </th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr
                key={user.id}
                className="border-b border-dark-border/50 hover:bg-dark-lighter/50"
              >
                <td className="px-4 py-3">
                  <p className="text-sm text-white font-medium">
                    {user.name || t("users.no_name")}
                  </p>
                  <p className="text-xs text-gray-text">{user.email}</p>
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-1.5">
                    {roleIcon(user.role)}
                    {user.id !== currentUserId ? (
                      <select
                        value={user.role}
                        onChange={(e) =>
                          startTransition(() =>
                            updateUserRole(user.id, e.target.value as Role)
                          )
                        }
                        className="bg-transparent text-sm text-white border-none cursor-pointer"
                      >
                        <option value="VIEWER">{t("users.role_viewer")}</option>
                        <option value="EDITOR">{t("users.role_editor")}</option>
                        <option value="ADMIN">{t("users.role_admin")}</option>
                      </select>
                    ) : (
                      <span className="text-sm text-white">{user.role}</span>
                    )}
                  </div>
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <span
                      className={`w-2 h-2 rounded-full ${
                        user.isActive ? "bg-success" : "bg-gray-text/30"
                      }`}
                    />
                    <span className="text-xs text-gray-text">
                      {!user.inviteAccepted
                        ? t("users.pending_invite")
                        : user.isActive
                          ? t("users.active")
                          : t("users.disabled")}
                    </span>
                  </div>
                </td>
                <td className="px-4 py-3 text-right">
                  {user.id !== currentUserId && (
                    <div className="flex items-center gap-1 justify-end">
                      {!user.inviteAccepted && (
                        <button
                          onClick={() =>
                            startTransition(async () => {
                              try {
                                await resendInvite(user.id);
                              } catch {
                                alert(t("users.resend_failed"));
                              }
                            })
                          }
                          disabled={isPending}
                          className="p-2 rounded-lg text-gray-text hover:bg-primary/10 hover:text-primary transition-colors"
                          title={t("users.resend_invite")}
                        >
                          <RefreshCw size={16} />
                        </button>
                      )}
                      <button
                        onClick={() =>
                          startTransition(() =>
                            toggleUserActive(user.id, !user.isActive)
                          )
                        }
                        disabled={isPending}
                        className="p-2 rounded-lg text-gray-text hover:bg-dark-lighter hover:text-white transition-colors"
                        title={user.isActive ? t("users.disable") : t("users.enable")}
                      >
                        {user.isActive ? (
                          <EyeOff size={16} />
                        ) : (
                          <Eye size={16} />
                        )}
                      </button>
                      <button
                        onClick={() => {
                          if (
                            confirm(t("users.confirm_delete", { email: user.email }))
                          ) {
                            startTransition(() => deleteUser(user.id));
                          }
                        }}
                        disabled={isPending}
                        className="p-2 rounded-lg text-gray-text hover:bg-error/10 hover:text-error transition-colors"
                        title={t("pois.delete")}
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
