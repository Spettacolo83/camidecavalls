import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Cami de Cavalls - Admin",
  description: "Backend administration panel for Cami de Cavalls app",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="antialiased">{children}</body>
    </html>
  );
}
