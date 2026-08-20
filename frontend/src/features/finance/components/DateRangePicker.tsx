import { Input } from '@/components/ui/Input';
import {
  DATE_RANGE_PRESETS,
  getPresetLabel,
  type DateRange,
  type DateRangePreset,
} from '@/features/finance/utils/dateRangePresets';
import { parseIsoDate } from '@/utils/dateFormat';
import './DateRangePicker.css';

interface DateRangePickerProps {
  preset: DateRangePreset;
  dateRange: DateRange;
  onPresetChange: (preset: DateRangePreset) => void;
  onDateRangeChange: (range: DateRange) => void;
}

export function DateRangePicker({
  preset,
  dateRange,
  onPresetChange,
  onDateRangeChange,
}: DateRangePickerProps) {
  return (
    <div className="date-range-picker">
      <div className="date-range-picker__presets" role="group" aria-label="Zakres dat">
        {DATE_RANGE_PRESETS.map((item) => (
          <button
            key={item}
            type="button"
            className={`date-range-picker__preset${preset === item ? ' date-range-picker__preset--active' : ''}`}
            onClick={() => onPresetChange(item)}
          >
            {getPresetLabel(item)}
          </button>
        ))}
      </div>

      {preset === 'CUSTOM' ? (
        <div className="date-range-picker__custom">
          <Input
            label="Od"
            name="dateFrom"
            type="date"
            value={parseIsoDate(dateRange.dateFrom)}
            onChange={(event) =>
              onDateRangeChange({ ...dateRange, dateFrom: event.target.value })
            }
          />
          <Input
            label="Do"
            name="dateTo"
            type="date"
            value={parseIsoDate(dateRange.dateTo)}
            onChange={(event) => onDateRangeChange({ ...dateRange, dateTo: event.target.value })}
          />
        </div>
      ) : null}
    </div>
  );
}
