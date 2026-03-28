import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check local storage for existing session
    const storedUser = localStorage.getItem('nycTestLabUser');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = (email, password) => {
    // Simulated authentication (accepts anything for demo)
    if (email && password) {
      const userData = { email, token: 'fake-jwt-token-123' };
      setUser(userData);
      localStorage.setItem('nycTestLabUser', JSON.stringify(userData));
      return true;
    }
    return false;
  };

  const register = (email, password) => {
    // Simulated registration
    if (email && password) {
      const userData = { email, token: 'fake-jwt-token-123' };
      setUser(userData);
      localStorage.setItem('nycTestLabUser', JSON.stringify(userData));
      return true;
    }
    return false;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('nycTestLabUser');
  };

  const value = {
    user,
    login,
    register,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
