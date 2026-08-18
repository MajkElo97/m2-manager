export interface Staircase {
  id: string;
  buildingId: string;
  code: string;
  designation: string;
  intercomCode: string | null;
  keyRequired: boolean;
  elevator: boolean;
  floors: number;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateStaircasePayload {
  buildingId: string;
  code: string;
  designation: string;
  intercomCode?: string | null;
  keyRequired: boolean;
  elevator: boolean;
  floors: number;
  notes?: string | null;
}

export interface UpdateStaircasePayload {
  code: string;
  designation: string;
  intercomCode?: string | null;
  keyRequired: boolean;
  elevator: boolean;
  floors: number;
  notes?: string | null;
}
