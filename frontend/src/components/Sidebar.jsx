import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getSuites } from '../services/api';
import { LayoutDashboard, Zap, LogOut, Clock } from 'lucide-react';
import './Sidebar.css';

export default function Sidebar() {
  const { logout, user } = useAuth();
  const navigate = useNavigate();
  const [suites, setSuites] = useState([]);

  useEffect(() => {
    fetchSuites();
    const id = setInterval(fetchSuites, 5000);
    return () => clearInterval(id);
  }, []);

  const fetchSuites = async () => {
    try {
      const data = await getSuites();
      setSuites(data);
    } catch (e) {
      console.error("Failed to fetch suites for sidebar", e);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/auth');
  };

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-glyph">🔮</div>
        <div className="brand-words">
          <span className="brand-top">SCRYBIT</span>
          <span className="brand-bot">AI ANALYTICS</span>
        </div>
      </div>

      <div className="sidebar-nav">
        <div className="sidebar-label">NAVIGATION</div>
        <NavLink 
          to="/" 
          className={({ isActive }) => `nav-item ${isActive && window.location.pathname === '/' ? 'active' : ''}`}
        >
          <Zap className="nav-icon" size={18} />
          <span>New Test Run</span>
        </NavLink>
        
        <NavLink 
          to="/dashboard" 
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <LayoutDashboard className="nav-icon" size={18} />
          <span>Test Dashboard</span>
        </NavLink>

        <div className="sidebar-label" style={{ marginTop: '1.5rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <Clock size={14} /> RECENT FIXTURES
        </div>
        
        <div className="history-list">
          {suites.length === 0 ? (
            <div className="history-empty">No tests run yet.</div>
          ) : (
            suites.map((s, i) => (
              <div 
                key={i} 
                className="history-nav-item"
                onClick={() => navigate('/dashboard', { state: { selectedSuite: s } })}
              >
                <div className="status-dot"></div>
                <span className="h-url" title={s}>{s.replace(/^https?:\/\//, '')}</span>
              </div>
            ))
          )}
        </div>
      </div>

      <div className="sidebar-footer">
        {user && <div className="user-email">{user.email}</div>}
        <button onClick={handleLogout} className="logout-btn">
          <LogOut size={16} />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
}
