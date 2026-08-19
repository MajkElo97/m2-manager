export type EquipmentCondition = 'GOOD' | 'USED' | 'DAMAGED' | 'OUT_OF_SERVICE';

export interface Equipment {
  id: string;
  code: string;
  name: string;
  category: string;
  manufacturer: string | null;
  model: string | null;
  serialNumber: string | null;
  quantity: number;
  conditionStatus: EquipmentCondition;
  location: string | null;
  employeeId: string | null;
  employeeCode: string | null;
  employeeName: string | null;
  purchaseDate: string | null;
  purchaseValue: number | null;
  active: boolean;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEquipmentPayload {
  code: string;
  name: string;
  category: string;
  manufacturer?: string;
  model?: string;
  serialNumber?: string;
  quantity: number;
  conditionStatus: EquipmentCondition;
  location?: string;
  employeeId?: string | null;
  purchaseDate?: string | null;
  purchaseValue?: number | null;
  active?: boolean;
  notes?: string;
}

export interface UpdateEquipmentPayload extends CreateEquipmentPayload {
  active: boolean;
}

export interface EquipmentListParams {
  search?: string;
  category?: string;
  employeeId?: string | null;
  condition?: EquipmentCondition | null;
  active?: boolean | null;
}
