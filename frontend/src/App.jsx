import React, { useState, useEffect } from 'react';
import { executeTests, getSuites, getSuiteResults, analyzeFailure } from './services/api';
import './App.css';

function App() {
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [statusMsg, setStatusMsg] = useState('');
  const [suites, setSuites] = useState([]);
  const [selectedSuite, setSelectedSuite] = useState(null);
  const [results, setResults] = useState([]);
  const [aiAnalysis, setAiAnalysis] = useState({});

  const fetchSuites = async () => {
    try {
      const data = await getSuites();
      setSuites(data);
    } catch (err) {
      console.error('Error fetching suites:', err);
    }
  };

  const fetchResults = (suite) => {
    setSelectedSuite(suite);
  };

  useEffect(() => {
    fetchSuites();
    const suiteInterval = setInterval(fetchSuites, 3000);
    return () => clearInterval(suiteInterval);
  }, []);

  useEffect(() => {
    if (selectedSuite) {
      const fetchCurrentResults = async () => {
        try {
          const data = await getSuiteResults(selectedSuite);
          setResults(data);
        } catch (err) {
          console.error('Error fetching results:', err);
        }
      };
      
      fetchCurrentResults();
      const resultInterval = setInterval(fetchCurrentResults, 3000);
      return () => clearInterval(resultInterval);
    }
  }, [selectedSuite]);

  const handleRunTests = async (e) => {
    e.preventDefault();
    if (!url) return;
    setLoading(true);
    setStatusMsg('Running tests on ' + url + '...');
    try {
      await executeTests(url);
      setStatusMsg('Tests initiated successfully!');
      
      const normalizedUrl = url.trim().toLowerCase();
      setSelectedSuite(normalizedUrl);
      
      setTimeout(() => {
        setStatusMsg('');
      }, 3000);
    } catch (err) {
      setStatusMsg('Error running tests.');
    }
    setLoading(false);
  };

  const handleAnalyze = async (errorMessage, resultId) => {
    try {
      const response = await analyzeFailure(errorMessage);
      setAiAnalysis(prev => ({ ...prev, [resultId]: response }));
    } catch (err) {
      console.error('AI Analysis failed:', err);
    }
  };

  return (
    <div className="app-container">
      <header className="header">
        <div className="logo">
          <div className="logo-icon"></div>
          <h1>AI Test Analytics</h1>
        </div>
      </header>
      
      <main className="main-content">
        <section className="glass-card launch-section">
          <h2>Launch New Test Suite</h2>
          <form onSubmit={handleRunTests} className="test-form">
            <input 
              type="url" 
              placeholder="https://example.com"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              required
              className="url-input"
            />
            <button type="submit" disabled={loading} className={`run-btn ${loading ? 'loading' : ''}`}>
              {loading ? 'Initiating...' : 'Run Tests'}
            </button>
          </form>
          {statusMsg && <p className="status-msg animate-fade-in">{statusMsg}</p>}
        </section>

        <div className="dashboard-grid">
          <aside className="suites-sidebar glass-card">
            <h3>Test Suites</h3>
            {suites.length === 0 ? (
              <p className="empty-text">No test suites run yet.</p>
            ) : (
              <ul className="suite-list">
                {suites.map((suite, idx) => (
                  <li 
                    key={idx} 
                    className={selectedSuite === suite ? 'active' : ''}
                    onClick={() => fetchResults(suite)}
                  >
                    <div className="suite-indicator"></div>
                    <span>{suite}</span>
                  </li>
                ))}
              </ul>
            )}
          </aside>

          <section className="results-panel glass-card">
            <h3>
              {selectedSuite ? `Results for ${selectedSuite}` : 'Select a test suite to view results'}
            </h3>
            
            <div className="results-list">
              {results.length > 0 ? results.map((result, idx) => (
                <div 
                  key={idx} 
                  className={`result-card ${result.status === 'PASS' ? 'pass' : result.status === 'FAIL' ? 'fail' : 'warn'}`}
                >
                  <div className="result-header">
                    <h4>{result.testName}</h4>
                    <span className="status-badge">{result.status}</span>
                  </div>
                  
                  {result.errorMessage && (
                    <div className="error-section">
                      <p className="error-message">{result.errorMessage}</p>
                      <button 
                        onClick={() => handleAnalyze(result.errorMessage, idx)}
                        className="ai-btn"
                      >
                        ✨ AI Analyze
                      </button>
                      
                      {aiAnalysis[idx] && (
                        <div className="ai-analysis-result animate-fade-in">
                          <h5>AI Insights</h5>
                          <p>{aiAnalysis[idx]}</p>
                        </div>
                      )}
                    </div>
                  )}
                  
                  {result.screenshotPath && (
                    <img 
                      src={`http://localhost:8081/${result.screenshotPath}`} 
                      alt="Test Screenshot" 
                      className="test-screenshot" 
                    />
                  )}
                </div>
              )) : (
                selectedSuite && <p className="empty-text">No results found for this suite.</p>
              )}
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}

export default App;
