import { NavLink, Outlet } from 'react-router-dom'

const navStyle: React.CSSProperties = {
  display: 'flex',
  gap: '1rem',
  padding: '1rem 2rem',
  background: '#1a1a2e',
  marginBottom: '2rem',
}

const linkStyle = ({ isActive }: { isActive: boolean }): React.CSSProperties => ({
  color: isActive ? '#e94560' : '#fff',
  textDecoration: 'none',
  fontWeight: isActive ? 'bold' : 'normal',
})

export default function Layout() {
  return (
    <>
      <nav style={navStyle}>
        <span style={{ color: '#fff', marginRight: '1rem', fontWeight: 'bold' }}>LiveKlass</span>
        <NavLink to="/creators" style={linkStyle}>크리에이터</NavLink>
        <NavLink to="/courses" style={linkStyle}>강좌</NavLink>
        <NavLink to="/sale-records" style={linkStyle}>판매 내역</NavLink>
        <NavLink to="/settlements" style={linkStyle}>정산</NavLink>
      </nav>
      <main style={{ maxWidth: '960px', margin: '0 auto', padding: '0 1rem' }}>
        <Outlet />
      </main>
    </>
  )
}
