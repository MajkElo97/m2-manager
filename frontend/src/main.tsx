import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { AppRoutes } from './App';
import { AuthProvider } from '@/features/auth/AuthProvider';
import { PermissionProvider } from '@/features/permissions/PermissionProvider';
import { ThemeProvider } from '@/hooks/ThemeProvider';
import '@/styles/global.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <PermissionProvider>
          <AuthProvider>
            <AppRoutes />
          </AuthProvider>
        </PermissionProvider>
      </ThemeProvider>
    </BrowserRouter>
  </StrictMode>,
);
