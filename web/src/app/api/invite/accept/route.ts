import { NextRequest, NextResponse } from "next/server";
import { db } from "@/lib/db";

export async function POST(request: NextRequest) {
  const body = await request.json();
  const { token } = body;

  if (!token || typeof token !== "string") {
    return NextResponse.json(
      { error: "Invalid invite token" },
      { status: 400 }
    );
  }

  const user = await db.user.findUnique({
    where: { inviteToken: token },
  });

  if (!user) {
    return NextResponse.json(
      { error: "Invite not found or already used" },
      { status: 404 }
    );
  }

  if (user.inviteAccepted) {
    return NextResponse.json(
      { error: "This invite has already been accepted" },
      { status: 400 }
    );
  }

  await db.user.update({
    where: { id: user.id },
    data: {
      inviteAccepted: true,
      inviteToken: null,
    },
  });

  return NextResponse.json({ success: true });
}
