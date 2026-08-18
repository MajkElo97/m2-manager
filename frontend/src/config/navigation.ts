import type { LucideIcon } from 'lucide-react';
import {
  Activity,
  Building2,
  CalendarDays,
  Car,
  ClipboardList,
  Contact,
  FileText,
  LayoutDashboard,
  Settings,
  Shield,
  Layers,
  Target,
  UserCog,
  Users,
  Wallet,
  Warehouse,
} from 'lucide-react';

export interface NavigationItem {
  label: string;
  path: string;
  icon: LucideIcon;
  requiredPermission: string;
}

export const mainNavigation: NavigationItem[] = [
  { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, requiredPermission: 'DASHBOARD_VIEW' },
  { label: 'Budynki', path: '/buildings', icon: Building2, requiredPermission: 'BUILDINGS_VIEW' },
  { label: 'Klatki schodowe', path: '/staircases', icon: Layers, requiredPermission: 'STAIRCASES_VIEW' },
  { label: 'Zakresy', path: '/scopes', icon: Target, requiredPermission: 'SCOPES_VIEW' },
  { label: 'Czynności', path: '/activities', icon: Activity, requiredPermission: 'ACTIVITIES_VIEW' },
  { label: 'Pracownicy', path: '/employees', icon: Users, requiredPermission: 'EMPLOYEES_VIEW' },
  { label: 'Zarządcy', path: '/managers', icon: UserCog, requiredPermission: 'MANAGERS_VIEW' },
  { label: 'Opiekunowie', path: '/supervisors', icon: Shield, requiredPermission: 'SUPERVISORS_VIEW' },
  { label: 'Kontakty', path: '/contacts', icon: Contact, requiredPermission: 'CONTACTS_VIEW' },
  { label: 'Harmonogram', path: '/schedule', icon: CalendarDays, requiredPermission: 'SCHEDULE_VIEW' },
  { label: 'Finanse', path: '/finances', icon: Wallet, requiredPermission: 'FINANCES_VIEW' },
  { label: 'Magazyn', path: '/warehouse', icon: Warehouse, requiredPermission: 'WAREHOUSE_VIEW' },
  { label: 'Flota', path: '/fleet', icon: Car, requiredPermission: 'FLEET_VIEW' },
  { label: 'Raporty', path: '/reports', icon: FileText, requiredPermission: 'REPORTS_VIEW' },
];

export const adminNavigation: NavigationItem[] = [
  { label: 'Użytkownicy', path: '/users', icon: Users, requiredPermission: 'USERS_VIEW' },
  { label: 'Role', path: '/roles', icon: Shield, requiredPermission: 'ROLES_VIEW' },
  { label: 'Ustawienia', path: '/settings', icon: Settings, requiredPermission: 'SETTINGS_VIEW' },
];

export interface AppRoute {
  path: string;
  moduleName: string;
  placeholder?: boolean;
}

export const appRoutes: AppRoute[] = [
  { path: '/dashboard', moduleName: 'Dashboard' },
  { path: '/buildings', moduleName: 'Budynki' },
  { path: '/staircases', moduleName: 'Klatki schodowe', placeholder: true },
  { path: '/scopes', moduleName: 'Zakresy', placeholder: true },
  { path: '/activities', moduleName: 'Czynności', placeholder: true },
  { path: '/employees', moduleName: 'Pracownicy', placeholder: true },
  { path: '/managers', moduleName: 'Zarządcy', placeholder: true },
  { path: '/supervisors', moduleName: 'Opiekunowie', placeholder: true },
  { path: '/contacts', moduleName: 'Kontakty', placeholder: true },
  { path: '/schedule', moduleName: 'Harmonogram', placeholder: true },
  { path: '/finances', moduleName: 'Finanse', placeholder: true },
  { path: '/warehouse', moduleName: 'Magazyn', placeholder: true },
  { path: '/fleet', moduleName: 'Flota', placeholder: true },
  { path: '/reports', moduleName: 'Raporty', placeholder: true },
  { path: '/users', moduleName: 'Użytkownicy', placeholder: true },
  { path: '/roles', moduleName: 'Role', placeholder: true },
  { path: '/settings', moduleName: 'Ustawienia', placeholder: true },
];

export const PLACEHOLDER_ICON = ClipboardList;
