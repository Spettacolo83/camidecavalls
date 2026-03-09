"use server";

import { revalidatePath } from "next/cache";
import { db } from "@/lib/db";
import { auth } from "@/lib/auth";
import { hash, compare } from "bcryptjs";
import { Role } from "@prisma/client";
import { randomBytes } from "crypto";
import { sendInviteEmail } from "@/lib/email";

export async function createInvite(email: string, name: string, role: Role) {
  const session = await auth();
  if (!session?.user || session.user.role !== "ADMIN") {
    throw new Error("Unauthorized");
  }

  const existing = await db.user.findUnique({ where: { email } });
  if (existing) throw new Error("User with this email already exists");

  const inviteToken = randomBytes(32).toString("hex");
  const tempPassword = randomBytes(16).toString("hex");
  const passwordHash = await hash(tempPassword, 12);

  await db.user.create({
    data: {
      email,
      name,
      role,
      passwordHash,
      inviteToken,
      inviteAccepted: false,
      isActive: true,
    },
  });

  // Send invite email with activation link and temp credentials
  let emailSent = false;
  try {
    await sendInviteEmail(email, name, inviteToken, tempPassword);
    emailSent = true;
  } catch (err) {
    console.error("Failed to send invite email:", err);
  }

  revalidatePath("/dashboard/users");

  return { inviteToken, tempPassword, emailSent };
}

export async function resendInvite(userId: string) {
  const session = await auth();
  if (!session?.user || session.user.role !== "ADMIN") {
    throw new Error("Unauthorized");
  }

  const user = await db.user.findUnique({ where: { id: userId } });
  if (!user || user.inviteAccepted) {
    throw new Error("User not found or invite already accepted");
  }

  // Generate new token and password
  const inviteToken = randomBytes(32).toString("hex");
  const tempPassword = randomBytes(16).toString("hex");
  const passwordHash = await hash(tempPassword, 12);

  await db.user.update({
    where: { id: userId },
    data: { inviteToken, passwordHash },
  });

  try {
    await sendInviteEmail(user.email, user.name || user.email, inviteToken, tempPassword);
  } catch (err) {
    console.error("Failed to resend invite email:", err);
    throw new Error("Failed to send email");
  }

  revalidatePath("/dashboard/users");
}

export async function changePassword(currentPassword: string, newPassword: string) {
  const session = await auth();
  if (!session?.user) {
    throw new Error("Unauthorized");
  }

  if (newPassword.length < 8) {
    throw new Error("Password must be at least 8 characters");
  }

  const user = await db.user.findUnique({ where: { id: session.user.id } });
  if (!user) throw new Error("User not found");

  const isValid = await compare(currentPassword, user.passwordHash);
  if (!isValid) {
    throw new Error("Current password is incorrect");
  }

  const passwordHash = await hash(newPassword, 12);
  await db.user.update({
    where: { id: session.user.id },
    data: { passwordHash },
  });
}

export async function deleteUser(id: string) {
  const session = await auth();
  if (!session?.user || session.user.role !== "ADMIN") {
    throw new Error("Unauthorized");
  }

  if (id === session.user.id) {
    throw new Error("Cannot delete yourself");
  }

  await db.user.delete({ where: { id } });
  revalidatePath("/dashboard/users");
}

export async function updateUserRole(id: string, role: Role) {
  const session = await auth();
  if (!session?.user || session.user.role !== "ADMIN") {
    throw new Error("Unauthorized");
  }

  await db.user.update({ where: { id }, data: { role } });
  revalidatePath("/dashboard/users");
}

export async function toggleUserActive(id: string, isActive: boolean) {
  const session = await auth();
  if (!session?.user || session.user.role !== "ADMIN") {
    throw new Error("Unauthorized");
  }

  if (id === session.user.id) {
    throw new Error("Cannot deactivate yourself");
  }

  await db.user.update({ where: { id }, data: { isActive } });
  revalidatePath("/dashboard/users");
}
