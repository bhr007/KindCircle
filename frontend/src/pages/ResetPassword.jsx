import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import api from '../services/api'
import { toast } from 'react-toastify'
import './ResetPassword.css'

const ResetPassword = () => {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: ''
  })
  const [loading, setLoading] = useState(false)
  const [status, setStatus] = useState('form') // form, success, error
  const [error, setError] = useState('')

  useEffect(() => {
    if (!token) {
      setStatus('error')
      setError('Invalid reset link')
    }
  }, [token])

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (formData.newPassword.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }

    if (formData.newPassword !== formData.confirmPassword) {
      setError('Passwords do not match')
      return
    }

    setLoading(true)

    try {
      await api.post('/auth/reset-password', {
        token: token,
        newPassword: formData.newPassword
      })
      setStatus('success')
      toast.success('Password reset successfully!')
      setTimeout(() => {
        navigate('/login')
      }, 3000)
    } catch (error) {
      setStatus('error')
      const errorMessage = error.response?.data?.message || 
                         error.response?.data?.error || 
                         'Failed to reset password'
      setError(errorMessage)
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  if (status === 'success') {
    return (
      <div className="reset-password-container">
        <div className="reset-password-card">
          <div className="success-icon">✓</div>
          <h2>Password Reset Successful!</h2>
          <p>Your password has been reset successfully.</p>
          <p>Redirecting to login page...</p>
          <Link to="/login" className="btn btn-primary">
            Go to Login
          </Link>
        </div>
      </div>
    )
  }

  if (status === 'error' && !token) {
    return (
      <div className="reset-password-container">
        <div className="reset-password-card">
          <div className="error-icon">✗</div>
          <h2>Invalid Reset Link</h2>
          <p>{error || 'This password reset link is invalid or has expired.'}</p>
          <Link to="/forgot-password" className="btn btn-secondary">
            Request New Reset Link
          </Link>
          <Link to="/login" className="btn btn-primary">
            Back to Login
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="reset-password-container">
      <div className="reset-password-card">
        <h1>Reset Password</h1>
        <p>Enter your new password below.</p>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <input
            type="password"
            name="newPassword"
            placeholder="New Password"
            value={formData.newPassword}
            onChange={handleChange}
            required
            minLength={6}
            className="input"
          />
          <input
            type="password"
            name="confirmPassword"
            placeholder="Confirm New Password"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
            minLength={6}
            className="input"
          />
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Resetting...' : 'Reset Password'}
          </button>
        </form>
        <p className="back-link">
          <Link to="/login">Back to Login</Link>
        </p>
      </div>
    </div>
  )
}

export default ResetPassword
