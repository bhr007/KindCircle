import { useState, useEffect } from 'react'
import { useNavigate, Link, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { toast } from 'react-toastify'
import './AuthPage.css'
import './Login.css'

const Login = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    if (location.state?.message) {
      toast.info(location.state.message)
    }
  }, [location])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await login(email, password)
      toast.success('Login successful!')
      navigate('/')
    } catch (error) {
      const errorMessage = error.response?.data?.message ||
        error.response?.data?.error ||
        'Login failed'
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page login-page">
      <div className="auth-page__brand">
        <div className="auth-page__brand-inner">
          <div className="auth-page__brand-text">Your kindness,
            <br />measured in impact.</div>
          <img src="/logincr.png" alt="KindCircle – community, donations & kindness" />
        </div>
      </div>
      <div className="auth-page__form-wrap">
        <div className="auth-form">
          <h1 className="auth-form__logo">KindCircle</h1>
          <h2 className="auth-form__title">Log in</h2>
          <form onSubmit={handleSubmit}>
            <input
              type="email"
              placeholder="Email address"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="input"
              autoComplete="email"
            />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="input"
              autoComplete="current-password"
            />
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Logging in…' : 'Log in'}
            </button>
          </form>
          <hr className="auth-form__divider" />
          <div className="auth-form__footer">
            <p>
              Don&apos;t have an account? <Link to="/register">Sign up</Link>
            </p>
            <p>
              <Link to="/forgot-password">Forgot password?</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login
