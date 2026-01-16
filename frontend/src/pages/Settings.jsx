import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'
import { toast } from 'react-toastify'
import { useNavigate } from 'react-router-dom'
import './Settings.css'

const Settings = () => {
  const { user, updateUser } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [uploadingPhoto, setUploadingPhoto] = useState(false)
  const [formData, setFormData] = useState({
    fullName: user?.fullName || user?.full_name || '',
    bloodGroup: user?.bloodGroup || user?.blood_group || '',
    city: user?.city || '',
    location: user?.location || ''
  })
  const [profilePhotoPreview, setProfilePhotoPreview] = useState(() => {
    const photoUrl = user?.profilePhotoUrl || user?.profile_photo_url
    if (!photoUrl) return '/default-avatar.png'
    if (photoUrl.startsWith('uploads/')) {
      return `http://localhost:8080/${photoUrl}`
    }
    if (photoUrl.startsWith('http')) return photoUrl
    return photoUrl
  })

  useEffect(() => {
    if (user) {
      setFormData({
        fullName: user.fullName || user.full_name || '',
        bloodGroup: user.bloodGroup || user.blood_group || '',
        city: user.city || '',
        location: user.location || ''
      })
      const photoUrl = user.profilePhotoUrl || user.profile_photo_url
      if (photoUrl) {
        if (photoUrl.startsWith('uploads/')) {
          setProfilePhotoPreview(`http://localhost:8080/${photoUrl}`)
        } else if (photoUrl.startsWith('http')) {
          setProfilePhotoPreview(photoUrl)
        } else {
          setProfilePhotoPreview(photoUrl)
        }
      } else {
        setProfilePhotoPreview('/default-avatar.png')
      }
    }
  }, [user])

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleProfilePhotoChange = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    // Validate file type
    if (!file.type.startsWith('image/')) {
      toast.error('Please select an image file')
      return
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error('Image size should be less than 5MB')
      return
    }

    setUploadingPhoto(true)

    try {
      // Create preview
      const previewUrl = URL.createObjectURL(file)
      setProfilePhotoPreview(previewUrl)

      // Create FormData and send file directly
      const formData = new FormData()
      formData.append('file', file)

      // Update profile picture
      const response = await api.put('/users/me/profile-picture', formData)

      // Update preview with the returned file path
      if (response.data?.profilePhotoUrl || response.data?.profile_photo_url) {
        const fileUrl = response.data.profilePhotoUrl || response.data.profile_photo_url
        // If it's a relative path, prepend the API URL
        const fullUrl = fileUrl.startsWith('http') ? fileUrl : `http://localhost:8080${fileUrl}`
        setProfilePhotoPreview(fullUrl)
      }
      
      toast.success('Profile picture updated successfully!')
      
      // Update auth context with new user data
      if (updateUser && response.data) {
        updateUser(response.data)
      }
    } catch (error) {
      console.error('Profile photo upload error:', error)
      // Revert preview on error
      const photoUrl = user?.profilePhotoUrl || user?.profile_photo_url
      if (photoUrl) {
        if (photoUrl.startsWith('uploads/')) {
          setProfilePhotoPreview(`http://localhost:8080/${photoUrl}`)
        } else {
          setProfilePhotoPreview(photoUrl)
        }
      } else {
        setProfilePhotoPreview('/default-avatar.png')
      }
      toast.error(error.response?.data?.message || 'Failed to upload profile picture')
    } finally {
      setUploadingPhoto(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)

    try {
      const response = await api.put('/users/me/profile', formData)
      toast.success('Profile updated successfully!')
      
      // Update auth context with new user data
      if (updateUser && response.data) {
        updateUser(response.data)
      }
      
      // Optionally navigate to profile page
      navigate('/profile')
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update profile')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="settings">
      <h1>Settings</h1>
      
      <div className="settings-container">
        <div className="settings-section">
          <h2>Profile Picture</h2>
          <div className="profile-picture-section">
            <div className="profile-picture-preview">
              <img
                src={profilePhotoPreview}
                alt="Profile"
                className="profile-picture-img"
              />
              {uploadingPhoto && (
                <div className="upload-overlay">
                  <div className="upload-spinner"></div>
                </div>
              )}
            </div>
            <label htmlFor="profile-photo-upload" className="btn-upload-photo">
              {uploadingPhoto ? 'Uploading...' : 'Change Profile Picture'}
            </label>
            <input
              type="file"
              id="profile-photo-upload"
              accept="image/*"
              onChange={handleProfilePhotoChange}
              style={{ display: 'none' }}
              disabled={uploadingPhoto}
            />
            <p className="help-text">Recommended: Square image, max 5MB</p>
          </div>
        </div>

        <div className="settings-section">
          <h2>Profile Information</h2>
          <form onSubmit={handleSubmit} className="settings-form">
            <div className="form-group">
              <label htmlFor="fullName">Full Name</label>
              <input
                type="text"
                id="fullName"
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                className="input"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="bloodGroup">Blood Group</label>
              <select
                id="bloodGroup"
                name="bloodGroup"
                value={formData.bloodGroup}
                onChange={handleChange}
                className="input"
              >
                <option value="">Select Blood Group</option>
                <option value="A+">A+</option>
                <option value="A-">A-</option>
                <option value="B+">B+</option>
                <option value="B-">B-</option>
                <option value="AB+">AB+</option>
                <option value="AB-">AB-</option>
                <option value="O+">O+</option>
                <option value="O-">O-</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="city">City</label>
              <input
                type="text"
                id="city"
                name="city"
                value={formData.city}
                onChange={handleChange}
                className="input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="location">Location</label>
              <input
                type="text"
                id="location"
                name="location"
                value={formData.location}
                onChange={handleChange}
                className="input"
                placeholder="Street address or detailed location"
              />
            </div>

            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}

export default Settings
