import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { EmptyState } from '@/components/ui/EmptyState';

export function NoOrganizationSelectedPage() {
  return (
    <AppLayoutContainer>
      <EmptyState
        title="Brak wybranej organizacji"
        description="Z menu u góry wybierz organizację, aby rozpocząć pracę w jej kontekście."
      />
    </AppLayoutContainer>
  );
}
