import { EmptyState } from '@/components/ui/EmptyState';
import { PageHeader } from '@/components/ui/PageHeader';
import { AppLayoutContainer } from '@/components/layout/AppLayout';

interface PlaceholderPageProps {
  moduleName: string;
}

export function PlaceholderPage({ moduleName }: PlaceholderPageProps) {
  return (
    <AppLayoutContainer>
      <PageHeader title={moduleName} />
      <EmptyState
        title="Moduł w przygotowaniu"
        description={`Moduł „${moduleName}” będzie dostępny w kolejnych fazach projektu.`}
      />
    </AppLayoutContainer>
  );
}
