import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import Layout from './components/Layout'
import CreatorsPage from './pages/CreatorsPage'
import CoursesPage from './pages/CoursesPage'
import SaleRecordsPage from './pages/SaleRecordsPage'
import SettlementPage from './pages/SettlementPage'

const queryClient = new QueryClient()

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route index element={<Navigate to="/creators" replace />} />
            <Route path="/creators" element={<CreatorsPage />} />
            <Route path="/courses" element={<CoursesPage />} />
            <Route path="/sale-records" element={<SaleRecordsPage />} />
            <Route path="/settlements" element={<SettlementPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
