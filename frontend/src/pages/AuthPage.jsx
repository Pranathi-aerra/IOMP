import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mail, Lock, ArrowRight } from 'lucide-react';
import logo from '../assets/logo.png';
import './AuthPage.css';

export default function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (!email.includes('@') || password.length < 6) {
      setError('Please enter a valid email and a password of at least 6 characters.');
      return;
    }

    let success = false;
    if (isLogin) {
      success = login(email, password);
    } else {
      success = register(email, password);
    }

    if (success) {
      navigate('/');
    } else {
      setError('Authentication failed. Please try again.');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <img src={logo} alt="Scrybit Logo" className="brand-logo" />
          <h2>{isLogin ? 'Welcome Back' : 'Create Account'}</h2>
          <p>Scrybit AI Analytics</p>
        </div>

        {error && <div className="auth-error">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="input-group">
            <Mail className="input-icon" size={18} />
            <input 
              type="email" 
              placeholder="Email address" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required 
            />
          </div>

          <div className="input-group">
            <Lock className="input-icon" size={18} />
            <input 
              type="password" 
              placeholder="Password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required 
            />
          </div>

          <button type="submit" className="auth-btn">
            {isLogin ? 'Sign In' : 'Sign Up'}
            <ArrowRight size={16} />
          </button>
        </form>

        <div className="auth-footer">
          <p>{isLogin ? "Don't have an account?" : "Already have an account?"}</p>
          <button 
            type="button" 
            className="toggle-btn"
            onClick={() => {
              setIsLogin(!isLogin);
              setError('');
            }}
          >
            {isLogin ? 'Sign up here' : 'Sign in instead'}
          </button>
        </div>
      </div>
    </div>
  );
}
