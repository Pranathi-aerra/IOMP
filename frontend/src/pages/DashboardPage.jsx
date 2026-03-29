import React, { useState, useEffect, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getSuiteResults, analyzeFailure } from '../services/api';
import { 
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid 
} from 'recharts';
import { ArrowLeft, ExternalLink, Activity, AlertCircle } from 'lucide-react';
import './DashboardPage.css';

const BACKEND = 'http://localhost:8081';
const COLORS  = { PASS: '#39ff14', FAIL: '#ff006e', WARN: '#ffcc00', UNKNOWN: '#7b2fff' };

function ImageModal({ src, onClose }) {
  useEffect(() => {
    const onKey = e => { if (e.key === 'Escape') onClose(); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={e => e.stopPropagation()}>
        <button className="modal-close" onClick={onClose}>✕</button>
        <img src={src} alt="Screenshot" className="modal-img" />
        <p className="modal-hint">Press Esc or click outside to close</p>
      </div>
    </div>
  );
}

function NeonTooltip({ active, payload }) {
  if (!active || !payload?.length) return null;
  const { name, value, payload: p } = payload[0];
  return (
    <div className="chart-tip">
      <span>{p?.full || name}</span>
      <strong style={{ color: COLORS[p?.status] || '#00d4ff' }}>{p?.status || value}</strong>
    </div>
  );
}

export default function DashboardPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();
  const selectedSuite = location.state?.selectedSuite;

  const [results, setResults] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [aiMap, setAiMap] = useState({});
  const [analyzingId, setAnalyzingId] = useState(null);
  const [modalSrc, setModalSrc] = useState(null);

  useEffect(() => {
    if (!selectedSuite) {
      navigate('/');
      return;
    }
    
    const load = async () => {
      try { 
        if (!user?.email) return;
        setResults(await getSuiteResults(selectedSuite, user.email)); 
      } catch (e) {
        console.error("Error loading suite results", e);
      }
    };
    load();
    const id = setInterval(load, 4000);
    return () => clearInterval(id);
  }, [selectedSuite, navigate]);

  const handleAnalyze = async (msg, id) => {
    setAnalyzingId(id);
    try {
      const r = await analyzeFailure(msg);
      setAiMap(prev => ({ ...prev, [id]: r }));
    } catch (_) {}
    setAnalyzingId(null);
  };

  const stats = useMemo(() => ({
    total : results.length,
    pass  : results.filter(r => r.status === 'PASS').length,
    fail  : results.filter(r => r.status === 'FAIL').length,
    warn  : results.filter(r => r.status === 'WARN').length,
  }), [results]);

  const passRate = stats.total ? Math.round((stats.pass / stats.total) * 100) : 0;
  const healthCls = passRate >= 80 ? 'healthy' : passRate >= 50 ? 'degraded' : stats.total > 0 ? 'critical' : '';

  const pieData = useMemo(() => [
    { name: 'Passed', value: stats.pass },
    { name: 'Failed', value: stats.fail },
    { name: 'Warnings', value: stats.warn },
  ].filter(d => d.value > 0), [stats]);

  const barData = useMemo(() => results.map((r, i) => ({
    name: (r.testName || `T${i+1}`).replace(' Test','').substring(0,12),
    full: r.testName,
    status: r.status,
    v: 1
  })), [results]);

  const filteredResults = useMemo(() => {
    if (filter === 'ALL') return results;
    return results.filter(r => r.status === filter);
  }, [results, filter]);

  if (!selectedSuite) return null;

  return (
    <div className="dashboard-container fade-in">
      {modalSrc && <ImageModal src={modalSrc} onClose={() => setModalSrc(null)} />}

      <div className="dash-header">
        <button className="back-btn" onClick={() => navigate('/')}>
          <ArrowLeft size={16} /> Back
        </button>
        <div className="titles">
          <h1 className="dash-title">{selectedSuite.replace(/^https?:\/\//,'')}</h1>
          <p className="dash-sub">{stats.total} tests completed</p>
        </div>
        {stats.total > 0 && (
          <div className={`health-badge ${healthCls}`}>
            {healthCls === 'healthy' ? '✅ Great' : healthCls === 'degraded' ? '⚠ Degraded' : '🚨 Critical'} · {passRate}% 
          </div>
        )}
      </div>

      {stats.total > 0 && (
        <div className="stats-grid">
          {[
            { k: 'total', label: 'TOTAL', val: stats.total, bar: 100 },
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

      {stats.total > 0 && (
        <div className="charts-area">
          <div className="chart-panel">
            <h4 className="panel-ttl">Result Distribution</h4>
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie 
                  data={pieData} cx="50%" cy="50%" 
                  innerRadius={55} outerRadius={85} paddingAngle={4} dataKey="value"
                >
                  {pieData.map((e,i) => (
                    <Cell 
                      key={i} 
                      fill={COLORS[e.name.toUpperCase().replace('D','')] || (i===0?COLORS.PASS:i===1?COLORS.FAIL:COLORS.WARN)} 
                      strokeWidth={0} 
                    />
                  ))}
                </Pie>
                <Tooltip content={<NeonTooltip />} />
                <Legend iconType="circle" iconSize={10} wrapperStyle={{ fontSize:'0.8rem', color:'#94a3b8' }} />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="chart-panel">
            <h4 className="panel-ttl">Test Execution Overview</h4>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={barData} margin={{ top:10, right:10, left:-20, bottom:0 }}>
                <CartesianGrid strokeDasharray="2 4" stroke="rgba(255,255,255,.05)" />
                <XAxis dataKey="name" tick={{ fontSize:10, fill:'#64748b' }} axisLine={false} tickLine={false} />
                <YAxis hide />
                <Tooltip content={<NeonTooltip />} />
                <Bar dataKey="v" radius={[4,4,0,0]}>
                  {barData.map((e,i) => <Cell key={i} fill={COLORS[e.status] || '#334155'} />)}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      <div className="results-area">
        <div className="results-header">
          <h4 className="section-ttl">⚡ TEST EXECUTION LOGS</h4>
          <div className="filters">
            {['ALL', 'PASS', 'FAIL'].map(f => (
              <button 
                key={f} 
                className={`filter-btn ${filter === f ? 'active' : ''}`}
                onClick={() => setFilter(f)}
              >
                {f}
              </button>
            ))}
          </div>
        </div>

        {results.length === 0 ? (
          <div className="loading-state">
            <div className="ring-spinner"></div>
            <p>Gathering analytics from headless browser…</p>
          </div>
        ) : (
          <div className="list-wrapper">
            {filteredResults.map((r, idx) => (
              <div key={idx} className={`test-item status-${r.status?.toLowerCase()}`}>
                <div className="ti-header">
                  <div className="ti-left">
                    <span className={`led-indicator led-${r.status?.toLowerCase()}`}></span>
                    <span className="ti-name">{r.testName}</span>
                  </div>
                  <span className={`badge badge-${r.status?.toLowerCase()}`}>{r.status}</span>
                </div>

                {r.errorMessage && (
                  <div className="ti-error-panel">
                    <div className="error-text">
                      <AlertCircle size={14} className="err-icon" />
                      {r.errorMessage}
                    </div>

                    {r.screenshotPath && (
                      <div className="screenshot-wrap" onClick={() => setModalSrc(`${BACKEND}/${r.screenshotPath}`)}>
                        <img 
                          src={`${BACKEND}/${r.screenshotPath}`} 
                          alt="Failure Screenshot" 
                          onError={e => { e.currentTarget.style.display = 'none'; }}
                        />
                        <div className="expand-overlay">
                          <ExternalLink size={20} />
                          <span>View Full</span>
                        </div>
                      </div>
                    )}

                    <button 
                      className={`btn-ai ${analyzingId === idx ? 'running' : ''}`}
                      onClick={() => handleAnalyze(r.errorMessage, idx)}
                      disabled={analyzingId === idx || aiMap[idx]}
                    >
                      {analyzingId === idx ? 'Analyzing…' : aiMap[idx] ? 'AI Insight Generated' : '✨ Generate AI Fix Request'}
                    </button>

                    {aiMap[idx] && (
                      <div className="ai-report fade-in">
                        <div className="ai-head">🤖 Llama3.2 Diagnostics</div>
                        {aiMap[idx].success === false ? (
                          <div className="c-err">LLM processing failed: {aiMap[idx].message}</div>
                        ) : (
                          <div className="report-content">
                            <div className="r-item">
                              <span className="r-label">Issue Category</span>
                              <span className="chip-cat">{aiMap[idx].category}</span>
                            </div>
                            <div className="r-item">
                              <span className="r-label">Resolution Path</span>
                              <span className="r-ans">{aiMap[idx].suggestion}</span>
                            </div>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
