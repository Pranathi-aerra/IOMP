import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { executeTests, getSuites, getSuiteResults } from '../services/api';
import { Play, Activity, Clock, ArrowRight } from 'lucide-react';
import './HomePage.css';

export default function HomePage() {
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [latestSuite, setLatestSuite] = useState(null);
  const [results, setResults] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchLatestSuite();
    const id = setInterval(fetchLatestSuite, 5000);
    return () => clearInterval(id);
  }, []);

  const fetchLatestSuite = async () => {
    try {
      const data = await getSuites();
      if (data && data.length > 0) {
        setLatestSuite(data[0]);
      }
    } catch (e) {
      console.error("Failed to fetch suites for home page", e);
    }
  };

  useEffect(() => {
    if (!latestSuite) return;
    const fetchResults = async () => {
      try {
        const res = await getSuiteResults(latestSuite);
        setResults(res);
      } catch (e) {
        console.error("Failed to fetch results", e);
      }
    };
    fetchResults();
    const id = setInterval(fetchResults, 4000);
    return () => clearInterval(id);
  }, [latestSuite]);

  const handleRun = async (e) => {
    e.preventDefault();
    if (!url) return;
    
    setLoading(true);
    try {
      await executeTests(url);
      navigate('/dashboard', { state: { selectedSuite: url.trim().toLowerCase() } });
    } catch (error) {
      alert('Cannot reach backend. Is it running on :8081?');
      setLoading(false);
    }
  };

  const stats = useMemo(() => ({
    total : results.length,
    pass  : results.filter(r => r.status === 'PASS').length,
    fail  : results.filter(r => r.status === 'FAIL').length,
    warn  : results.filter(r => r.status === 'WARN').length,
  }), [results]);


  // If still running, consider pass rate based on completed vs logic or just total ran
  const passRateDisplay = stats.total > 0 ? Math.round((stats.pass / stats.total) * 100) : 0;

  return (
    <div className="home-container fade-in">
      <div className="home-header">
        <h1 className="page-title">Run <span className="neon-text">New Tests</span></h1>
        <p className="page-subtitle">Execute a suite of comprehensive checks instantly.</p>
      </div>

      <div className="run-card glass-panel">
        <form className="run-form" onSubmit={handleRun}>
          <div className="url-input-container">
            <input 
              type="url" 
              required 
              placeholder="https://yourwebsite.com"
              value={url} 
              onChange={e => setUrl(e.target.value)} 
              className="url-input"
            />
            <div className="url-icon">🌐</div>
          </div>
          
          <button type="submit" disabled={loading} className={`run-btn ${loading ? 'loading' : ''}`}>
             {loading ? <span className="spin"></span> : <Play size={20} fill="currentColor" />}
             {loading ? 'Executing...' : 'Run Tests'}
          </button>
        </form>
      </div>

      <div className="history-section">
        <div className="history-header">
          <h3><Activity size={18} /> Latest Run Overview</h3>
          {latestSuite && (
             <div className="suite-link" onClick={() => navigate('/dashboard', { state: { selectedSuite: latestSuite } })}>
               View Full Dashboard <ArrowRight size={14} />
             </div>
          )}
        </div>

        {!latestSuite ? (
          <div className="empty-history glass-panel">
            <Activity className="empty-icon" size={48} />
            <p>No tests running yet. Enter a URL above to spark your first execution!</p>
          </div>
        ) : (
          <div className="latest-results-panel glass-panel">
            <div className="lr-header">
              <div className="lr-title">{latestSuite.replace(/^https?:\/\//, '')}</div>
              <div className="lr-prog">{stats.total} completed</div>
            </div>

            {stats.total > 0 && (
              <div className="stats-grid">
                {[
                  { k: 'total', label: 'COMPLETED', val: stats.total, bar: 100 },
                  { k: 'pass', label: 'PASSED', val: stats.pass, bar: stats.total ? (stats.pass/stats.total)*100 : 0 },
                  { k: 'fail', label: 'FAILED', val: stats.fail, bar: stats.total ? (stats.fail/stats.total)*100 : 0 },
                  { k: 'warn', label: 'WARNINGS', val: stats.warn, bar: stats.total ? (stats.warn/stats.total)*100 : 0 },
                ].map(c => (
                  <div key={c.k} className={`stat-card stat-${c.k}`}>
                    <div className="sc-val">{c.val}</div>
                    <div className="sc-label">{c.label}</div>
                    <div className="sc-track"><div className={`sc-fill fill-${c.k}`} style={{ width:`${c.bar}%` }}></div></div>
                  </div>
                ))}
              </div>
            )}
            {stats.total === 0 && (
               <div className="loading-state">
                  <div className="ring-spinner"></div>
                  <p>Awaiting metrics from headless browser…</p>
               </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
