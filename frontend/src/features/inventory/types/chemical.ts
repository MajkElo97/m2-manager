export type ChemicalUnit = 'LITER' | 'KILOGRAM' | 'PIECE' | 'PACK' | 'OTHER';

export interface Chemical {
  id: string;
  code: string;
  name: string;
  category: string;
  quantity: number;
  unit: ChemicalUnit;
  minimumStock: number | null;
  lowStock: boolean;
  location: string | null;
  active: boolean;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateChemicalPayload {
  code: string;
  name: string;
  category: string;
  quantity: number;
  unit: ChemicalUnit;
  minimumStock?: number | null;
  location?: string;
  active?: boolean;
  notes?: string;
}

export interface UpdateChemicalPayload extends CreateChemicalPayload {
  active: boolean;
}

export interface ChemicalListParams {
  search?: string;
  category?: string;
  active?: boolean | null;
  lowStock?: boolean | null;
}
