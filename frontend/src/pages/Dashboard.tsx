import React, { useEffect, useState } from 'react';
import api from '../api';

function Dashboard() {
  const [message, setMessage] = useState('Loading...');

  useEffect(() => {
    // Example request to verify token works; replace with real endpoint as needed.
    api.get('/members')
      .then(() => setMessage('Dashboard: API reachable'))
      .catch(() => setMessage('Dashboard: Unable to fetch data (maybe no endpoint)'));
  }, []);

  return (
    <div style={{ padding: '1rem' }}>
      <h2>{message}</h2>
    </div>
  );
}

export default Dashboard;
