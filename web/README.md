# Camí de Cavalls - CRM Backend

Content Management System for managing Points of Interest (POIs) served to the Camí de Cavalls mobile apps.

## Tech Stack

- **Framework**: Next.js 15 (App Router) + TypeScript
- **Database**: PostgreSQL + Prisma 6 ORM
- **Auth**: NextAuth.js v5 (credentials provider, JWT strategy)
- **UI**: Tailwind CSS v4, dark theme (#1C1C2E, #4FC3F7)
- **Deploy**: Docker (standalone output) → EasyPanel on Contabo

## Features

- **POI Management**: Create, edit, delete POIs with multilingual translations (6 languages)
- **Image Upload**: Upload POI images with Sharp-based optimization
- **User Management**: Admin-only user CRUD with role-based access (ADMIN, EDITOR, VIEWER)
- **Mobile API**: REST endpoints for app sync (`/api/v1/pois`, `/api/v1/sync-status`)
- **Incremental Sync**: Apps fetch only POIs updated since last sync via `?since=` parameter
- **Dashboard**: Overview with POI/user counts and recent activity

## Local Development

### Prerequisites

- Node.js 20+
- PostgreSQL running locally (or Docker)

### Setup

```bash
cd web

# Install dependencies
npm install

# Create .env.local from example
cp .env.example .env.local
# Edit .env.local with your database URL and secrets

# Run database migrations
npx prisma migrate deploy

# Seed the database (creates admin user)
npx prisma db seed

# Start development server
npm run dev -- -H 0.0.0.0 -p 3002
```

The CRM will be available at `http://localhost:3002`.

Default admin credentials (from seed):
- Email: `admin@camidecavalls.com`
- Password: check `prisma/seed.ts`

### Environment Variables

```env
DATABASE_URL="postgresql://user:password@localhost:5432/cami_de_cavalls"
NEXTAUTH_SECRET="your-secret-here"
NEXTAUTH_URL="http://localhost:3002"
```

## Mobile App Configuration

The mobile apps (Android/iOS) connect to the CRM backend to sync POIs. The endpoint is configured per environment:

### Android (Build Variants)

Configured in `composeApp/build.gradle.kts` via product flavors:

| Variant | API URL | App ID |
|---------|---------|--------|
| `devDebug` | `http://192.168.8.106:3002` | `com.followmemobile.camidecavalls.dev` |
| `productionDebug` | `https://camidecavalls.followtheflowai.com` | `com.followmemobile.camidecavalls` |
| `productionRelease` | `https://camidecavalls.followtheflowai.com` | `com.followmemobile.camidecavalls` |

To change the dev API URL (e.g., your LAN IP), edit the `dev` flavor in `composeApp/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://YOUR_IP:3002\"")
```

### iOS (Xcode Schemes)

Configured via User-Defined Build Settings in `project.pbxproj`:

| Scheme | Configuration | API URL | Bundle ID |
|--------|--------------|---------|-----------|
| iosApp Dev | Debug | `http://192.168.8.106:3002` | `com.followmemobile.camidecavalls.dev` |
| iosApp Production | ProductionDebug | `https://camidecavalls.followtheflowai.com` | `com.followmemobile.camidecavalls` |

To change the dev API URL, edit `API_BASE_URL` in the Debug build configuration of the Xcode project.

### Switching Environments

When switching between dev and production, the app automatically detects the endpoint change and clears the POI cache to re-sync from the new server. A sync version mechanism ensures stale data is always cleared on app update.

## Production Deployment (Docker)

### Build & Deploy

```bash
# Build Docker image
docker build -t camidecavalls-crm .

# Run with docker-compose
docker-compose up -d
```

### EasyPanel (Contabo)

1. Create a PostgreSQL service (`camidecavalls-db`)
2. Create an app service pointing to the Git repo
3. Set environment variables:
   - `DATABASE_URL`: PostgreSQL connection string
   - `NEXTAUTH_SECRET`: random secret
   - `NEXTAUTH_URL`: `https://camidecavalls.followtheflowai.com`
4. Add a volume mount: `/app/public/uploads` for persistent image storage
5. Configure domain and DNS

### Image Storage

In production (Next.js standalone mode), uploaded images are served via an API route (`/uploads/[...path]/route.ts`) since standalone mode doesn't serve runtime-added static files. Mount `/app/public/uploads` as a persistent volume.

## API Endpoints

### `GET /api/v1/sync-status`

Returns sync metadata:
```json
{
  "lastUpdated": "2026-03-09T14:09:01.481Z",
  "activeCount": 5,
  "inactiveCount": 0,
  "serverTime": "2026-03-11T10:00:00.000Z"
}
```

### `GET /api/v1/pois`

Returns all active POIs. Supports incremental sync:
```
GET /api/v1/pois?since=2026-03-09T00:00:00.000Z
```

Response:
```json
{
  "pois": [
    {
      "id": "...",
      "type": "COMMERCIAL",
      "latitude": 39.89,
      "longitude": 4.26,
      "translations": {
        "en": { "name": "...", "description": "..." },
        "es": { "name": "...", "description": "..." }
      },
      "imageUrl": "/uploads/image.png",
      "actionUrl": "https://example.com",
      "updatedAt": "2026-03-09T14:09:01.481Z"
    }
  ],
  "count": 1,
  "timestamp": "2026-03-11T10:00:00.000Z"
}
```

## Version

Current CRM version is displayed in the sidebar. Update in `src/lib/version.ts`.
