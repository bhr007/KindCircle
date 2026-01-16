import { useState, useEffect } from 'react'
import api from '../services/api'
import { toast } from 'react-toastify'
import './AdminPanel.css'

const AdminPanel = () => {
  const [pendingVerifications, setPendingVerifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [rejectionReason, setRejectionReason] = useState('')
  const [showRejectModal, setShowRejectModal] = useState(false)
  const [selectedPostId, setSelectedPostId] = useState(null)

  useEffect(() => {
    fetchPendingVerifications()
  }, [])

  const fetchPendingVerifications = async () => {
    try {
      const response = await api.get('/admin/pending')
      setPendingVerifications(response.data)
    } catch (error) {
      toast.error('Failed to load pending verifications')
    } finally {
      setLoading(false)
    }
  }

  const handleVerify = async (id, approved) => {
    if (!approved && !rejectionReason.trim()) {
      toast.error('Please provide a rejection reason')
      return
    }

    try {
      // Build request body - only include rejectionReason if rejecting
      const requestBody = { 
        approved: approved
      }
      
      if (!approved) {
        requestBody.rejectionReason = rejectionReason.trim()
      }
      
      console.log('Sending verify request:', { id, requestBody })
      
      const response = await api.post(`/admin/verify/${id}`, requestBody)
      console.log('Verify response:', response.data)
      
      toast.success(approved ? 'Post approved successfully!' : 'Post rejected successfully')
      setShowRejectModal(false)
      setRejectionReason('')
      setSelectedPostId(null)
      fetchPendingVerifications()
    } catch (error) {
      console.error('Verify error:', error)
      console.error('Error response:', error.response?.data)
      
      let errorMessage = 'Failed to verify post'
      if (error.response?.data) {
        if (error.response.data.message) {
          errorMessage = error.response.data.message
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error
        } else if (typeof error.response.data === 'string') {
          errorMessage = error.response.data
        }
      } else if (error.message) {
        errorMessage = error.message
      }
      
      toast.error(errorMessage)
    }
  }

  const openRejectModal = (postId) => {
    setSelectedPostId(postId)
    setShowRejectModal(true)
  }

  const formatActivityType = (type) => {
    return type
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ')
  }

  const getImageUrl = (url) => {
    if (!url) return null
    if (url.startsWith('uploads/')) {
      return `http://localhost:8080/${url}`
    }
    if (url.startsWith('http')) return url
    return url
  }

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h1>Admin Panel</h1>
        <h2>Manage platform content and verifications</h2>
      </div>
      <div className="admin-section">
        <h2>Pending Verifications ({pendingVerifications.length})</h2>
        {pendingVerifications.length === 0 ? (
          <div className="no-pending">
            <p>No pending verifications</p>
          </div>
        ) : (
          <div className="verifications-list">
            {pendingVerifications.map((item) => (
              <div key={item.id} className="verification-card">
                <div className="verification-header">
                  <div className="user-info">
                    <img
                      src={getImageUrl(item.user?.profilePhotoUrl || item.user?.profile_photo_url) || '/default-avatar.png'}
                      alt={item.user?.fullName || item.user?.full_name}
                      className="user-avatar"
                    />
                    <div>
                      <h3>{item.user?.fullName || item.user?.full_name}</h3>
                      <p className="username">@{item.user?.username}</p>
                    </div>
                  </div>
                  <span className="activity-type">{formatActivityType(item.activityType)}</span>
                </div>
                
                {item.title && <h4 className="post-title">{item.title}</h4>}
                <p className="verification-description">{item.description}</p>
                {item.location && (
                  <p className="location">📍 {item.location}</p>
                )}
                <p className="date">
                  📅 Activity Date: {new Date(item.activityDate).toLocaleDateString()}
                </p>
                
                {item.mediaProofs && item.mediaProofs.length > 0 && (
                  <div className="verification-media">
                    {item.mediaProofs.map((media) => {
                      const mediaUrl = getImageUrl(media.mediaUrl || media.media_url)
                      const isVideo = media.mediaType === 'VIDEO' || 
                                     (media.mimeType && media.mimeType.startsWith('video/'))
                      
                      if (!mediaUrl) return null
                      
                      return (
                        <div key={media.id} className="media-item">
                          {isVideo ? (
                            <video src={mediaUrl} controls className="media-preview" />
                          ) : (
                            <img src={mediaUrl} alt="Proof" className="media-preview" />
                          )}
                        </div>
                      )
                    })}
                  </div>
                )}
                
                <div className="verification-actions">
                  <button
                    className="btn btn-success"
                    onClick={() => handleVerify(item.id, true)}
                  >
                    ✓ Approve
                  </button>
                  <button
                    className="btn btn-danger"
                    onClick={() => openRejectModal(item.id)}
                  >
                    ✗ Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {showRejectModal && (
        <div className="modal-overlay" onClick={() => setShowRejectModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Reject Post</h3>
            <p>Please provide a reason for rejection:</p>
            <textarea
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              placeholder="Enter rejection reason..."
              rows="4"
              className="rejection-textarea"
            />
            <div className="modal-actions">
              <button
                className="btn-cancel"
                onClick={() => {
                  setShowRejectModal(false)
                  setRejectionReason('')
                }}
              >
                Cancel
              </button>
              <button
                className="btn-confirm-reject"
                onClick={() => handleVerify(selectedPostId, false)}
                disabled={!rejectionReason.trim()}
              >
                Confirm Rejection
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default AdminPanel
