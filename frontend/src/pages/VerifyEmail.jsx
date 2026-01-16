import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import api from '../services/api'
import { toast } from 'react-toastify'
import './VerifyEmail.css'

const VerifyEmail = () => {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const navigate = useNavigate()
  const [status, setStatus] = useState('verifying') // verifying, success, error
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (token) {
      verifyEmail(token)
    } else {
      setStatus('error')
      setMessage('Invalid verification link')
    }
  }, [token])

  const verifyEmail = async (verificationToken) => {
    try {
      const response = await api.post('/auth/verify-email', { token: verificationToken })
      setStatus('success')
      setMessage(response.data.message || 'Email verified successfully!')
      toast.success('Email verified successfully!')
      setTimeout(() => {
        navigate('/login')
      }, 3000)
    } catch (error) {
      setStatus('error')
      const errorMessage = error.response?.data?.message || 
                         error.response?.data?.error || 
                         'Failed to verify email'
      setMessage(errorMessage)
      toast.error(errorMessage)
    }
  }

  const resendVerification = async () => {
    const email = prompt('Please enter your email address:')
    if (!email) return

    try {
      await api.post('/auth/resend-verification', { email })
      toast.success('Verification email sent! Please check your inbox.')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to resend verification email')
    }
  }

  return (
    <div className="verify-email-container">
      <div className="verify-email-card">
        {status === 'verifying' && (
          <>
            <div className="loading-spinner"></div>
            <h2>Verifying your email...</h2>
            <p>Please wait while we verify your email address.</p>
          </>
        )}

        {status === 'success' && (
          <>
            <div className="success-icon">✓</div>
            <h2>Email Verified!</h2>
            <p>{message}</p>
            <p>Redirecting to login page...</p>
            <Link to="/login" className="btn btn-primary">
              Go to Login
            </Link>
          </>
        )}

        {status === 'error' && (
          <>
            <div className="error-icon">✗</div>
            <h2>Verification Failed</h2>
            <p>{message}</p>
            <div className="verify-actions">
              <button onClick={resendVerification} className="btn btn-secondary">
                Resend Verification Email
              </button>
              <Link to="/login" className="btn btn-primary">
                Go to Login
              </Link>
            </div>
          </>
        )}
      </div>
    </div>
  )
}

export default VerifyEmail
