import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../services/api'
import { toast } from 'react-toastify'
import './ForgotPassword.css'

const ForgotPassword = () => {
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [sent, setSent] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)

    try {
      await api.post('/auth/forgot-password', { email })
      setSent(true)
      toast.success('Password reset email sent! Please check your inbox.')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to send password reset email')
    } finally {
      setLoading(false)
    }
  }

  if (sent) {
    return (
      <div className="forgot-password-container">
        <div className="forgot-password-card">
          <div className="success-icon">✓</div>
          <h2>Check Your Email</h2>
          <p>We've sent a password reset link to <strong>{email}</strong></p>
          <p>Please check your inbox and click the link to reset your password.</p>
          <p className="note">The link will expire in 1 hour.</p>
          <Link to="/login" className="btn btn-primary">
            Back to Login
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="forgot-password-container">
      <div className="forgot-password-card">
        <h1>Forgot Password?</h1>
        <p>Enter your email address and we'll send you a link to reset your password.</p>
        <form onSubmit={handleSubmit}>
          <input
            type="email"
            placeholder="Email address"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="input"
          />
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Sending...' : 'Send Reset Link'}
          </button>
        </form>
        <p className="back-link">
          <Link to="/login">Back to Login</Link>
        </p>
      </div>
    </div>
  )
}

export default ForgotPassword
