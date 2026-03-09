import { PrismaClient } from "@prisma/client";
import { hash } from "bcryptjs";

const prisma = new PrismaClient();

async function main() {
  const email = process.env.ADMIN_EMAIL || "admin@camidecavalls.com";
  const password = process.env.ADMIN_PASSWORD || "changeme";

  const existing = await prisma.user.findUnique({ where: { email } });
  if (existing) {
    console.log(`Admin user already exists: ${email}`);
    return;
  }

  const passwordHash = await hash(password, 12);

  await prisma.user.create({
    data: {
      email,
      name: "Admin",
      passwordHash,
      role: "ADMIN",
      inviteAccepted: true,
      isActive: true,
    },
  });

  console.log(`Admin user created: ${email}`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
