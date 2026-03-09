-- CreateEnum
CREATE TYPE "Role" AS ENUM ('ADMIN', 'EDITOR', 'VIEWER');

-- CreateEnum
CREATE TYPE "PoiType" AS ENUM ('BEACH', 'NATURAL', 'HISTORIC', 'COMMERCIAL', 'DANGER');

-- CreateEnum
CREATE TYPE "PoiSource" AS ENUM ('DYNAMIC', 'HARDCODED_OVERRIDE');

-- CreateTable
CREATE TABLE "users" (
    "id" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "name" TEXT,
    "passwordHash" TEXT NOT NULL,
    "role" "Role" NOT NULL DEFAULT 'VIEWER',
    "inviteToken" TEXT,
    "inviteAccepted" BOOLEAN NOT NULL DEFAULT false,
    "isActive" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "users_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "sessions" (
    "id" TEXT NOT NULL,
    "sessionToken" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "expires" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "sessions_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "pois" (
    "id" TEXT NOT NULL,
    "type" "PoiType" NOT NULL,
    "latitude" DOUBLE PRECISION NOT NULL,
    "longitude" DOUBLE PRECISION NOT NULL,
    "imageUrl" TEXT,
    "routeId" INTEGER,
    "isAdvertisement" BOOLEAN NOT NULL DEFAULT false,
    "isActive" BOOLEAN NOT NULL DEFAULT true,
    "source" "PoiSource" NOT NULL DEFAULT 'DYNAMIC',
    "hardcodedPoiId" INTEGER,
    "priority" INTEGER NOT NULL DEFAULT 0,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "createdById" TEXT,
    "updatedById" TEXT,

    CONSTRAINT "pois_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "poi_translations" (
    "id" TEXT NOT NULL,
    "poiId" TEXT NOT NULL,
    "language" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "description" TEXT NOT NULL,

    CONSTRAINT "poi_translations_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "users_email_key" ON "users"("email");

-- CreateIndex
CREATE UNIQUE INDEX "users_inviteToken_key" ON "users"("inviteToken");

-- CreateIndex
CREATE UNIQUE INDEX "sessions_sessionToken_key" ON "sessions"("sessionToken");

-- CreateIndex
CREATE UNIQUE INDEX "pois_hardcodedPoiId_key" ON "pois"("hardcodedPoiId");

-- CreateIndex
CREATE INDEX "pois_type_idx" ON "pois"("type");

-- CreateIndex
CREATE INDEX "pois_latitude_longitude_idx" ON "pois"("latitude", "longitude");

-- CreateIndex
CREATE INDEX "pois_updatedAt_idx" ON "pois"("updatedAt");

-- CreateIndex
CREATE INDEX "pois_isActive_idx" ON "pois"("isActive");

-- CreateIndex
CREATE UNIQUE INDEX "poi_translations_poiId_language_key" ON "poi_translations"("poiId", "language");

-- AddForeignKey
ALTER TABLE "sessions" ADD CONSTRAINT "sessions_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "pois" ADD CONSTRAINT "pois_createdById_fkey" FOREIGN KEY ("createdById") REFERENCES "users"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "pois" ADD CONSTRAINT "pois_updatedById_fkey" FOREIGN KEY ("updatedById") REFERENCES "users"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "poi_translations" ADD CONSTRAINT "poi_translations_poiId_fkey" FOREIGN KEY ("poiId") REFERENCES "pois"("id") ON DELETE CASCADE ON UPDATE CASCADE;
