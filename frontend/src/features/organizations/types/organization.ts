export interface OrganizationListItem {
  id: string;
  name: string;
  slug: string;
  adminName: string;
  adminEmail: string;
  active: boolean;
  createdAt: string;
}

export interface OrganizationDetail extends OrganizationListItem {
  adminUserId: string | null;
  updatedAt: string;
}

export interface CreateOrganizationPayload {
  name: string;
  slug: string;
  adminEmail: string;
}

export interface UpdateOrganizationPayload {
  name: string;
  slug: string;
}

export interface CreateOrganizationResult {
  id: string;
  name: string;
  slug: string;
  adminEmail: string;
  temporaryPassword: string;
}

export interface ResetAdminPasswordResult {
  adminEmail: string;
  temporaryPassword: string;
}

export interface OrganizationListParams {
  search?: string;
  active?: boolean | null;
}
