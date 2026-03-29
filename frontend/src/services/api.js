import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

export const executeTests = async (url, userEmail) => {
  const response = await apiClient.post('/test/execute', { url, userEmail });
  return response.data;
};

export const getSuites = async (userEmail) => {
  const response = await apiClient.get('/test/suites', { params: { userEmail } });
  return response.data;
};

export const getSuiteResults = async (suite, userEmail) => {
  const response = await apiClient.get('/test/suite', { params: { name: suite, userEmail } });
  return response.data;
};

export const analyzeFailure = async (errorMessage) => {
  const response = await apiClient.post('/analysis/analyze', { errorMessage });
  return response.data;
};
