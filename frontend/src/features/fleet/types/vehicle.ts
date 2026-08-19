export type VehicleStatus = 'ACTIVE' | 'IN_SERVICE' | 'INACTIVE' | 'SOLD';

export type VehicleType = 'PASSENGER' | 'DELIVERY' | 'VAN' | 'OTHER';

export interface Vehicle {
  id: string;
  code: string;
  registrationNumber: string;
  make: string;
  model: string;
  productionYear: number | null;
  vin: string | null;
  vehicleType: VehicleType;
  employeeId: string | null;
  employeeCode: string | null;
  employeeName: string | null;
  status: VehicleStatus;
  insuranceStartDate: string | null;
  insuranceEndDate: string | null;
  insurer: string | null;
  insurancePolicyNumber: string | null;
  lastInspectionDate: string | null;
  nextInspectionDate: string | null;
  lastInspectionMileage: number | null;
  purchaseDate: string | null;
  currentMileage: number | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateVehiclePayload {
  code: string;
  registrationNumber: string;
  make: string;
  model: string;
  productionYear?: number | null;
  vin?: string;
  vehicleType: VehicleType;
  employeeId?: string | null;
  status: VehicleStatus;
  insuranceStartDate?: string | null;
  insuranceEndDate?: string | null;
  insurer?: string;
  insurancePolicyNumber?: string;
  lastInspectionDate?: string | null;
  nextInspectionDate?: string | null;
  lastInspectionMileage?: number | null;
  purchaseDate?: string | null;
  currentMileage?: number | null;
  notes?: string;
}

export type UpdateVehiclePayload = CreateVehiclePayload;

export interface VehicleListParams {
  search?: string;
  status?: VehicleStatus | null;
  employeeId?: string | null;
  vehicleType?: VehicleType | null;
}
