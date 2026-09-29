import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'VIC Cloud Control Center',
  description: 'Velmora Intelligence Commander — Multi-surface AI Assistant Management Portal',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="bg-slate-950 text-slate-100 antialiased overflow-hidden">
        {children}
      </body>
    </html>
  );
}
