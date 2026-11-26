import axios from 'axios';

// Create an axios instance with the base URL pointing to the proxy ("/api")
const api = axios.create({
  baseURL: '/api',
});

// Add a request interceptor to attach the JWT token when it exists
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
