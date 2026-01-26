import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { toast } from 'react-toastify'
import './AuthPage.css'
import './Register.css'

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    fullName: '',
    phoneNumber: '',
    bloodGroup: '',
    city: '',
  })
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    if (formData.password !== formData.confirmPassword) {
      toast.error('Passwords do not match')
      return
    }

    setLoading(true)

    try {
      await register(formData)
      toast.success('Registration successful! Please check your email to verify your account.')
      setTimeout(() => {
        navigate('/login', {
          state: {
            message: 'Registration successful! Please check your email to verify your account before logging in.',
          },
        })
      }, 2000)
    } catch (error) {
      toast.error(error.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page register-page">
      <div className="auth-page__brand">
        <div className="auth-page__brand-inner">
          <div className="auth-page__brand-text">Your kindness,
          <br />measured in impact.</div>
          <img src="/logincr.png" alt="KindCircle – community, donations & kindness" />
        </div>
      </div>
      <div className="auth-page__form-wrap auth-page__form-wrap--scroll">
        <div className="auth-form register-form">
          <h1 className="auth-form__logo">KindCircle</h1>
          <h2 className="auth-form__title">Create an account</h2>
          <form onSubmit={handleSubmit}>
            <input
              type="text"
              name="username"
              placeholder="Username"
              value={formData.username}
              onChange={handleChange}
              required
              className="input"
              autoComplete="username"
            />
            <input
              type="email"
              name="email"
              placeholder="Email address"
              value={formData.email}
              onChange={handleChange}
              required
              className="input"
              autoComplete="email"
            />
            <input
              type="text"
              name="fullName"
              placeholder="Full name"
              value={formData.fullName}
              onChange={handleChange}
              required
              className="input"
              autoComplete="name"
            />
            <input
              type="tel"
              name="phoneNumber"
              placeholder="Phone number"
              value={formData.phoneNumber}
              onChange={handleChange}
              required
              className="input"
              autoComplete="tel"
            />
            <select
              name="bloodGroup"
              value={formData.bloodGroup}
              onChange={handleChange}
              className="input"
              required
            >
              <option value="">Select blood group</option>
              <option value="A+">A+</option>
              <option value="A-">A-</option>
              <option value="B+">B+</option>
              <option value="B-">B-</option>
              <option value="AB+">AB+</option>
              <option value="AB-">AB-</option>
              <option value="O+">O+</option>
              <option value="O-">O-</option>
            </select>
            <input
              type="text"
              name="city"
              placeholder="City"
              value={formData.city}
              onChange={handleChange}
              className="input"
              autoComplete="address-level2"
            />
            <input
              type="password"
              name="password"
              placeholder="Password"
              value={formData.password}
              onChange={handleChange}
              required
              className="input"
              autoComplete="new-password"
            />
            <input
              type="password"
              name="confirmPassword"
              placeholder="Confirm password"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
              className="input"
              autoComplete="new-password"
            />
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating account…' : 'Sign up'}
            </button>
          </form>
          <hr className="auth-form__divider" />
          <div className="auth-form__footer">
            <p>
              Already have an account? <Link to="/login">Log in</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Register
