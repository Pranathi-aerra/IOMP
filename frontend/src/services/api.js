import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

export const executeTests = async (url) => {
  const response = await apiClient.post('/test/execute', { url });
  return response.data;
};

export const getSuites = async () => {
  const response = await apiClient.get('/test/suites');
  return response.data;
};

export const getSuiteResults = async (suite) => {
  const response = await apiClient.get('/test/suite', { params: { name: suite } });
  return response.data;
};

export const analyzeFailure = async (errorMessage) => {
  const response = await apiClient.post('/analysis/analyze', { errorMessage });
  return response.data;
};
