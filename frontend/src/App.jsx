import React, { useState, useEffect, useMemo, useCallback } from 'react';
import {
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid
} from 'recharts';
import { executeTests, getSuites, getSuiteResults, analyzeFailure } from './services/api';
import './App.css';

const BACKEND = 'http://localhost:8081';
const COLORS  = { PASS: '#39ff14', FAIL: '#ff006e', WARN: '#ffcc00', UNKNOWN: '#7b2fff' };
const TOTAL_TESTS = 25;

// ── Modal ─────────────────────────────────────────────────────────────────────
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

// ── Custom chart tooltip ──────────────────────────────────────────────────────
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

// ── App ───────────────────────────────────────────────────────────────────────
export default function App() {
  const [url, setUrl]               = useState('');
  const [loading, setLoading]       = useState(false);
  const [toast, setToast]           = useState(null);      // { msg, type }
  const [suites, setSuites]         = useState([]);
  const [selected, setSelected]     = useState(null);
  const [results, setResults]       = useState([]);
  const [aiMap, setAiMap]           = useState({});
  const [analyzingId, setAnalyzingId] = useState(null);
  const [progress, setProgress]     = useState(0);
  const [isRunning, setIsRunning]   = useState(false);
  const [modalSrc, setModalSrc]     = useState(null);

  // ── Fetching ────────────────────────────────────────────────────────────────
  const fetchSuites = useCallback(async () => {
    try { setSuites(await getSuites()); } catch (_) {}
  }, []);

  useEffect(() => {
    fetchSuites();
    const id = setInterval(fetchSuites, 4000);
    return () => clearInterval(id);
  }, [fetchSuites]);

  useEffect(() => {
    if (!selected) return;
    const load = async () => {
      try { setResults(await getSuiteResults(selected)); } catch (_) {}
    };
    load();
    const id = setInterval(load, 4000);
    return () => clearInterval(id);
  }, [selected]);

  // ── Progress bar ─────────────────────────────────────────────────────────────
  useEffect(() => {
    if (!isRunning) { setProgress(0); return; }
    setProgress(3);
    const id = setInterval(() => setProgress(p => Math.min(p + 1.2 + Math.random(), 88)), 2500);
    return () => clearInterval(id);
  }, [isRunning]);

  const showToast = (msg, type = 'info') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 6000);
  };

  // ── Handlers ─────────────────────────────────────────────────────────────────
  const handleRun = async e => {
    e.preventDefault();
    if (!url) return;
    setLoading(true); setIsRunning(true);
    showToast(`Headless Chrome running ${TOTAL_TESTS} tests on ${url}…`, 'info');
    try {
      await executeTests(url);
      setProgress(100);
      showToast('Tests initiated — results appear as they complete!', 'success');
      setSelected(url.trim().toLowerCase());
      setTimeout(() => setIsRunning(false), 6000);
    } catch {
      showToast('Cannot reach backend. Is it running on :8081?', 'error');
      setIsRunning(false);
    }
    setLoading(false);
  };

  const handleAnalyze = async (msg, id) => {
    setAnalyzingId(id);
    try {
      const r = await analyzeFailure(msg);
      setAiMap(prev => ({ ...prev, [id]: r }));
    } catch (_) {}
    setAnalyzingId(null);
  };

  const pickSuite = suite => { setSelected(suite); setAiMap({}); };

  // ── Derived stats ─────────────────────────────────────────────────────────────
  const stats = useMemo(() => ({
    total : results.length,
    pass  : results.filter(r => r.status === 'PASS').length,
    fail  : results.filter(r => r.status === 'FAIL').length,
    warn  : results.filter(r => r.status === 'WARN').length,
  }), [results]);

  const passRate = stats.total ? Math.round((stats.pass / stats.total) * 100) : 0;

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

  const healthCls = passRate >= 80 ? 'healthy' : passRate >= 50 ? 'degraded' : stats.total > 0 ? 'critical' : '';

  // ── Render ────────────────────────────────────────────────────────────────────
  return (
    <div className="shell">
      {/* Modal */}
      {modalSrc && <ImageModal src={modalSrc} onClose={() => setModalSrc(null)} />}

      {/* Sidebar */}
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-glyph">⚡</div>
          <div className="brand-words">
            <span className="brand-top">AI TEST</span>
            <span className="brand-bot">ANALYTICS</span>
          </div>
        </div>

        <div className="sidebar-label">TEST SUITES <span className="count-pill">{suites.length}</span></div>

        {suites.length === 0
          ? <p className="sidebar-empty">No suites yet. Run a test →</p>
          : <ul className="suite-list">
              {suites.map((s, i) => (
                <li key={i} className={selected === s ? 'active' : ''} onClick={() => pickSuite(s)}>
                  <span className="s-dot"></span>
                  <span className="s-url">{s.replace(/^https?:\/\//, '')}</span>
                </li>
              ))}
            </ul>
        }

        <div className="sidebar-pills">
          <div className="spill green">⬤ Headless Mode</div>
          <div className="spill blue">{TOTAL_TESTS} tests / suite</div>
          <div className="spill pink">⚡ AI Analysis</div>
        </div>
      </aside>

      {/* Main */}
      <main className="main">
        {/* Topbar */}
        <div className="topbar">
          <div className="topbar-left">
            <h2 className="topbar-title">
              <span className="neon-pink">NYC</span><span className="neon-blue"> Test Lab</span>
            </h2>
          </div>
          <form className="run-form" onSubmit={handleRun}>
            <div className="url-row">
              <span className="url-globe">🌐</span>
              <input type="url" required placeholder="https://yoursite.com"
                value={url} onChange={e => setUrl(e.target.value)} className="url-input" />
            </div>
            <button type="submit" disabled={loading} className={`run-btn${loading ? ' busy' : ''}`}>
              {loading
                ? <><span className="spin"></span> Running…</>
                : <><span className="btn-icon">▶</span> Run Tests</>}
            </button>
          </form>
        </div>

        {/* Progress */}
        {isRunning && (
          <div className="prog-wrap">
            <div className="prog-track">
              <div className="prog-fill" style={{ width: `${progress}%` }}></div>
            </div>
            <span className="prog-pct">{Math.round(progress)}%</span>
          </div>
        )}

        {/* Toast */}
        {toast && <div className={`toast toast-${toast.type}`}>{toast.msg}</div>}

        {/* Content */}
        <div className="content">
          {!selected ? (
            <div className="welcome">
              <div className="welcome-orb">🗽</div>
              <h2 className="welcome-title">Welcome to <span className="neon-blue">NYC Test Lab</span></h2>
              <p className="welcome-sub">Select a suite from the sidebar or paste a URL above to run {TOTAL_TESTS} automated headless tests.</p>
            </div>
          ) : (<>
            {/* Suite header */}
            <div className="suite-header">
              <div>
                <h2 className="suite-title">{selected.replace(/^https?:\/\//,'')}</h2>
                <p className="suite-sub">{results.length} / {TOTAL_TESTS} tests complete</p>
              </div>
              {stats.total > 0 && (
                <div className={`health-tag ${healthCls}`}>
                  {healthCls === 'healthy' ? '✅ Healthy' : healthCls === 'degraded' ? '⚠ Degraded' : '🚨 Critical'} · {passRate}%
                </div>
              )}
            </div>

            {/* Stat Cards */}
            {stats.total > 0 && (
              <div className="stat-grid">
                {[
                  { k: 'total',  label: 'TOTAL',     val: stats.total, bar: 100 },
                  { k: 'pass',   label: 'PASSED',    val: stats.pass,  bar: stats.total ? (stats.pass/stats.total)*100 : 0 },
                  { k: 'fail',   label: 'FAILED',    val: stats.fail,  bar: stats.total ? (stats.fail/stats.total)*100 : 0 },
                  { k: 'warn',   label: 'WARNINGS',  val: stats.warn,  bar: stats.total ? (stats.warn/stats.total)*100 : 0 },
                  { k: 'rate',   label: 'PASS RATE', val: `${passRate}%`, bar: passRate },
                ].map(c => (
                  <div key={c.k} className={`stat stat-${c.k}`}>
                    <div className="stat-val">{c.val}</div>
                    <div className="stat-label">{c.label}</div>
                    <div className="stat-track"><div className={`stat-fill fill-${c.k}`} style={{ width:`${c.bar}%` }}></div></div>
                  </div>
                ))}
              </div>
            )}

            {/* Charts */}
            {stats.total > 0 && (
              <div className="charts">
                <div className="chart-box">
                  <h4 className="chart-ttl">⬤ Distribution</h4>
                  <ResponsiveContainer width="100%" height={200}>
                    <PieChart>
                      <Pie data={pieData} cx="50%" cy="50%"
                           innerRadius={52} outerRadius={80}
                           paddingAngle={4} dataKey="value">
                        {pieData.map((e,i) => (
                          <Cell key={i}
                            fill={COLORS[e.name.toUpperCase().replace('D','')] || (i===0?COLORS.PASS:i===1?COLORS.FAIL:COLORS.WARN)}
                            strokeWidth={0} />
                        ))}
                      </Pie>
                      <Tooltip content={<NeonTooltip />} />
                      <Legend iconType="circle" iconSize={8}
                        wrapperStyle={{ fontSize:'0.75rem', color:'#94a3b8' }} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                <div className="chart-box">
                  <h4 className="chart-ttl">📊 Breakdown</h4>
                  <ResponsiveContainer width="100%" height={200}>
                    <BarChart data={barData} margin={{ top:8, right:8, left:-20, bottom:0 }}>
                      <CartesianGrid strokeDasharray="2 4" stroke="rgba(255,255,255,.04)" />
                      <XAxis dataKey="name" tick={{ fontSize:9, fill:'#475569' }} axisLine={false} tickLine={false} />
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

            {/* Results */}
            <div className="results-section">
              <h4 className="section-ttl">⚡ TEST RESULTS</h4>
              {results.length === 0 ? (
                <div className="loading-card">
                  <div className="ring-spinner"></div>
                  <p>Tests running in headless browser…</p>
                  <p className="loading-sub">Results appear as each test completes.</p>
                </div>
              ) : (
                <div className="result-list">
                  {results.map((r, idx) => (
                    <div key={idx} className={`result-item ri-${r.status?.toLowerCase()}`}>
                      <div className="ri-header">
                        <div className="ri-left">
                          <span className={`ri-led led-${r.status?.toLowerCase()}`}></span>
                          <span className="ri-name">{r.testName}</span>
                        </div>
                        <span className={`ri-badge badge-${r.status?.toLowerCase()}`}>{r.status}</span>
                      </div>

                      {r.errorMessage && (
                        <div className="ri-error-block">
                          <div className="ri-error-msg">{r.errorMessage}</div>

                          {/* Screenshot thumbnail */}
                          {r.screenshotPath && (
                            <div className="ss-thumb-wrap">
                              <img
                                src={`${BACKEND}/${r.screenshotPath}`}
                                alt="Screenshot"
                                className="ss-thumb"
                                onClick={() => setModalSrc(`${BACKEND}/${r.screenshotPath}`)}
                                title="Click to expand"
                                onError={e => { e.currentTarget.style.display = 'none'; }}
                              />
                              <span className="ss-hint">🔍 Click to expand</span>
                            </div>
                          )}

                          <button
                            className={`ai-btn${analyzingId === idx ? ' busy' : ''}`}
                            onClick={() => handleAnalyze(r.errorMessage, idx)}
                            disabled={analyzingId === idx}
                          >
                            {analyzingId === idx
                              ? <><span className="spin-sm"></span> Analyzing…</>
                              : '✨ AI Analyze'}
                          </button>

                          {aiMap[idx] && (
                            <div className="ai-box">
                              <div className="ai-box-head">🤖 AI INSIGHTS</div>
                              {aiMap[idx].success === false
                                ? <p className="ai-err">Analysis failed: {aiMap[idx].message}</p>
                                : <div className="ai-fields">
                                    <div><span className="ai-key">Category</span><span className="ai-chip">{aiMap[idx].category}</span></div>
                                    <div><span className="ai-key">Suggestion</span><span className="ai-val">{aiMap[idx].suggestion}</span></div>
                                  </div>
                              }
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </>)}
        </div>
      </main>
    </div>
  );
}
