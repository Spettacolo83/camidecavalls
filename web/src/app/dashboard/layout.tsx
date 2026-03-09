import { redirect } from "next/navigation";
import { cookies } from "next/headers";
import { auth } from "@/lib/auth";
import { getLocaleFromCookie, type Locale } from "@/lib/i18n";
import { LocaleProvider } from "@/lib/locale-context";
import { SessionProvider } from "next-auth/react";
import Sidebar from "@/components/dashboard/Sidebar";

export default async function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const session = await auth();

  if (!session?.user) {
    redirect("/login");
  }

  const cookieStore = await cookies();
  const locale = getLocaleFromCookie(cookieStore.toString()) as Locale;

  return (
    <SessionProvider session={session}>
      <LocaleProvider initialLocale={locale}>
        <div className="flex h-screen overflow-hidden">
          <Sidebar
            userRole={session.user.role}
            userName={session.user.name || session.user.email}
          />
          <main className="flex-1 overflow-y-auto bg-dark p-6">
            {children}
          </main>
        </div>
      </LocaleProvider>
    </SessionProvider>
  );
}
