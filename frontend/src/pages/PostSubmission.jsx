import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import { toast } from 'react-toastify'
import './PostSubmission.css'

const PostSubmission = () => {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    activityType: '',
    title: '',
    description: '',
    location: '',
    activityDate: new Date().toISOString().split('T')[0],
    mediaFiles: []
  })
  const [previewUrls, setPreviewUrls] = useState([])
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleFileChange = (e) => {
    const files = Array.from(e.target.files)
    setFormData({
      ...formData,
      mediaFiles: files
    })

    // Create preview URLs
    const urls = files.map(file => URL.createObjectURL(file))
    setPreviewUrls(urls)
  }

  const removeImage = (index) => {
    const newFiles = formData.mediaFiles.filter((_, i) => i !== index)
    const newUrls = previewUrls.filter((_, i) => i !== index)
    setFormData({ ...formData, mediaFiles: newFiles })
    setPreviewUrls(newUrls)
  }


  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!formData.activityType) {
      toast.error('Please select an activity type')
      return
    }
    
    if (!formData.description.trim()) {
      toast.error('Please provide a description')
      return
    }

    setLoading(true)

    try {
      // Create FormData to send files directly
      const formDataToSend = new FormData()
      
      // Add post data as JSON string
      const postData = {
        activityType: formData.activityType,
        title: formData.title || null,
        description: formData.description.trim(),
        location: formData.location || null,
        activityDate: formData.activityDate
      }
      formDataToSend.append('post', new Blob([JSON.stringify(postData)], {
        type: 'application/json'
      }))
      
      // Add media files directly (no base64 conversion)
      if (formData.mediaFiles && formData.mediaFiles.length > 0) {
        formData.mediaFiles.forEach((file) => {
          formDataToSend.append('mediaFiles', file)
        })
      }

      const response = await api.post('/feed', formDataToSend)
      toast.success('Post submitted successfully! It will be reviewed by an admin before appearing in the feed.')
      navigate('/')
    } catch (error) {
      console.error('Post submission error:', error)
      let errorMessage = 'Failed to submit post'
      
      if (error.response?.data) {
        if (error.response.data.message) {
          errorMessage = error.response.data.message
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error
        } else if (typeof error.response.data === 'string') {
          errorMessage = error.response.data
        } else if (error.response.data.activityType) {
          errorMessage = error.response.data.activityType
        } else if (error.response.data.description) {
          errorMessage = error.response.data.description
        } else if (error.response.data.activityDate) {
          errorMessage = error.response.data.activityDate
        }
      } else if (error.message) {
        errorMessage = error.message
      }
      
      toast.error(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  const activityTypes = [
    { value: 'BLOOD_DONATION', label: 'Blood Donation', score: '+100 Impact Score' },
    { value: 'FEEDING', label: 'Feeding Helpless People', score: '+40 Impact Score' },
    { value: 'WINTER_CLOTHES', label: 'Giving Winter Clothes', score: '+30 Impact Score' },
    { value: 'TREE_PLANTATION', label: 'Tree Plantation', score: '+20 Impact Score' }
  ]

  return (
    <div className="post-submission">
      <div className="submission-container">
        <h1>Share Your Good Work</h1>
        <p className="subtitle">Post your humanitarian work and earn Impact Score instantly</p>

        <form onSubmit={handleSubmit} className="submission-form">
          <div className="form-group">
            <label htmlFor="activityType">Activity Type *</label>
            <select
              id="activityType"
              name="activityType"
              value={formData.activityType}
              onChange={handleChange}
              required
              className="input"
            >
              <option value="">Select Activity Type</option>
              {activityTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label} ({type.score})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="title">Title (Optional)</label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="Give your post a title"
              className="input"
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description *</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Describe your humanitarian work..."
              rows="5"
              required
              className="input textarea"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="location">Location (Optional)</label>
              <input
                type="text"
                id="location"
                name="location"
                value={formData.location}
                onChange={handleChange}
                placeholder="City, Country"
                className="input"
              />
            </div>

            <div className="form-group">
              <label htmlFor="activityDate">Activity Date *</label>
              <input
                type="date"
                id="activityDate"
                name="activityDate"
                value={formData.activityDate}
                onChange={handleChange}
                max={new Date().toISOString().split('T')[0]}
                required
                className="input"
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="mediaFiles">Photo/Video Proof (Optional)</label>
            <p className="help-text">Upload photos or videos as proof of your work</p>
            <input
              type="file"
              id="mediaFiles"
              name="mediaFiles"
              onChange={handleFileChange}
              accept="image/*,video/*"
              multiple
              className="input file-input"
            />
            
            {previewUrls.length > 0 && (
              <div className="preview-grid">
                {previewUrls.map((url, index) => (
                  <div key={index} className="preview-item">
                    <img src={url} alt={`Preview ${index + 1}`} />
                    <button
                      type="button"
                      onClick={() => removeImage(index)}
                      className="remove-btn"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate('/')}
              className="btn btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Posting...' : 'Post'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default PostSubmission
