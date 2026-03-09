import { db } from "@/lib/db";
import { auth } from "@/lib/auth";
import UserManagement from "@/components/dashboard/UserManagement";
import PasswordChangeCard from "@/components/dashboard/PasswordChangeCard";

async function getUsers() {
  return db.user.findMany({
    orderBy: { createdAt: "desc" },
    select: {
      id: true,
      email: true,
      name: true,
      role: true,
      isActive: true,
      inviteAccepted: true,
      createdAt: true,
    },
  });
}

export default async function UsersPage() {
  const session = await auth();
  if (!session?.user) return null;

  const isAdmin = session.user.role === "ADMIN";

  if (isAdmin) {
    const users = await getUsers();
    return (
      <div className="space-y-8">
        <UserManagement users={users} currentUserId={session.user.id} />
        <PasswordChangeCard />
      </div>
    );
  }

  // Non-admin: show only password change
  return <PasswordChangeCard />;
}
