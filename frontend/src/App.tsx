import { useEffect, useState } from 'react';
import axios from 'axios';

export default function App() {
  const [status, setStatus] = useState('loading...');

  useEffect(() => {
    axios
      .get('http://localhost:8080/api/health')
      .then((res) => setStatus(res.data.status))
      .catch(() => setStatus('backend unreachable'));
  }, []);

  return <h1>Backend status: {status}</h1>;
}