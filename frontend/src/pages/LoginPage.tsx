import { useState } from 'react';
import type { FormEvent } from 'react';
import { isAxiosError } from 'axios';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

function message(error: unknown): string {
  if (isAxiosError(error)) {
    switch (error.response?.status) {
      case 401:
        return 'Invalid email or password.';
      case 409:
        return 'An account with that email already exists.';
      case 400:
        return 'Enter a valid email and a password of at least 8 characters.';
      default:
        break;
    }
  }
  return 'Something went wrong. Is the backend running?';
}

export default function LoginPage() {
  const { user, login, register } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      if (mode === 'login') {
        await login(email, password);
      } else {
        await register(email, password);
      }
      navigate('/dashboard');
    } catch (err) {
      setError(message(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="panel auth-box">
      <h1>{mode === 'login' ? 'Log in' : 'Create account'}</h1>
      <form onSubmit={submit}>
        <label>
          Email
          <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        </label>
        <label>
          Password
          <input
            type="password"
            required
            minLength={mode === 'register' ? 8 : undefined}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
        {error && <p className="warn">{error}</p>}
        <button type="submit" disabled={busy}>
          {mode === 'login' ? 'Log in' : 'Create account'}
        </button>
      </form>
      <p>
        {mode === 'login' ? 'No account yet? ' : 'Already registered? '}
        <a
          href="#"
          onClick={(e) => {
            e.preventDefault();
            setError('');
            setMode(mode === 'login' ? 'register' : 'login');
          }}
        >
          {mode === 'login' ? 'Create one' : 'Log in'}
        </a>
      </p>
    </div>
  );
}