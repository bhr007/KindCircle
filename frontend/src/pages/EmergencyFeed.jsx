import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'
import { toast } from 'react-toastify'
import './EmergencyFeed.css'

const EmergencyFeed = () => {
  const { user } = useAuth()
  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [selectedRequest, setSelectedRequest] = useState(null)
  const [showChat, setShowChat] = useState(false)
  const [chatMessages, setChatMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [showLocation, setShowLocation] = useState(false)
  const [locationData, setLocationData] = useState(null)
  const [deletingRequestId, setDeletingRequestId] = useState(null)

  // Form state
  const [formData, setFormData] = useState({
    bloodGroup: '',
    bagsRequired: 1,
    hospitalName: '',
    hospitalAddress: '',
    city: '',
    urgencyLevel: 'MEDIUM',
    description: ''
  })

  useEffect(() => {
    fetchRequests()
  }, [])

  useEffect(() => {
    if (selectedRequest && showChat) {
      fetchChatMessages()
      const interval = setInterval(() => {
        fetchChatMessages()
      }, 3000) // Poll every 3 seconds
      return () => clearInterval(interval)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedRequest, showChat])

  useEffect(() => {
    if (selectedRequest && showLocation && locationData) {
      const interval = setInterval(() => {
        updateMyLocation()
      }, 5000) // Update location every 5 seconds
      return () => clearInterval(interval)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedRequest, showLocation, locationData])

  const fetchRequests = async () => {
    try {
      setLoading(true)
      const response = await api.get('/emergency')
      console.log('Fetched emergency requests response:', response)
      console.log('Fetched emergency requests data:', response.data)
      if (response.data && Array.isArray(response.data)) {
      setRequests(response.data)
      } else {
        console.warn('Unexpected response format:', response.data)
        setRequests([])
      }
    } catch (error) {
      console.error('Failed to load emergency requests:', error)
      console.error('Error response:', error.response)
      toast.error(error.response?.data?.message || 'Failed to load emergency requests')
      setRequests([])
    } finally {
      setLoading(false)
    }
  }

  const fetchChatMessages = async () => {
    if (!selectedRequest) return
    try {
      const response = await api.get(`/emergency/${selectedRequest.id}/chat`)
      setChatMessages(response.data)
    } catch (error) {
      console.error('Failed to load chat messages', error)
    }
  }

  const handleCreateRequest = async (e) => {
    e.preventDefault()
    try {
      // Validate impact score
      const requiredScore = formData.bagsRequired * 100
      if (user.currentImpactScore < requiredScore) {
        toast.error(`Insufficient Impact Score. Required: ${requiredScore}, Current: ${user.currentImpactScore}`)
        return
      }

      await api.post('/emergency', formData)
      toast.success('Emergency request created successfully!')
      setShowCreateForm(false)
      setFormData({
        bloodGroup: '',
        bagsRequired: 1,
        hospitalName: '',
        hospitalAddress: '',
        city: '',
        urgencyLevel: 'MEDIUM',
        description: ''
      })
      fetchRequests()
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create request')
    }
  }

  const handleRespond = async (requestId) => {
    try {
      await api.post(`/emergency/${requestId}/respond`, { message: 'I can help with this blood request.' })
      toast.success('Response sent! Opening chat...')
      const request = requests.find(r => r.id === requestId)
      setSelectedRequest(request)
      setShowChat(true)
      fetchChatMessages()
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to respond')
    }
  }

  const handleSendMessage = async (e) => {
    e.preventDefault()
    if (!newMessage.trim()) return

    try {
      await api.post(`/emergency/${selectedRequest.id}/chat`, { message: newMessage })
      setNewMessage('')
      fetchChatMessages()
    } catch (error) {
      toast.error('Failed to send message')
    }
  }

  const handleConfirmMatch = async (donorId) => {
    if (!window.confirm('Confirm match with this donor?')) return

    try {
      await api.post(`/emergency/${selectedRequest.id}/confirm-match/${donorId}`)
      toast.success('Match confirmed! Both parties need to confirm donation.')
      fetchRequests()
      fetchChatMessages()
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to confirm match')
    }
  }

  const handleConfirmDonation = async (donationId, isDonor) => {
    try {
      await api.post(`/emergency/donations/${donationId}/confirm`, { isDonor })
      toast.success('Donation confirmed!')
      await fetchRequests()
      
      // Refresh selected request
      const updatedRequests = await api.get('/emergency')
      const updatedRequest = updatedRequests.data.find(r => r.id === selectedRequest?.id)
      if (updatedRequest) {
        setSelectedRequest(updatedRequest)
        // If both confirmed, enable location sharing
        if (updatedRequest.locationSharingEnabled) {
          setShowLocation(true)
          fetchLocationSession()
        }
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to confirm donation')
    }
  }

  const handleDeleteRequest = async (requestId) => {
    if (!window.confirm('Are you sure you want to delete this blood request? This action cannot be undone.')) {
      return
    }
    
    setDeletingRequestId(requestId)
    try {
      await api.delete(`/emergency/${requestId}`)
      toast.success('Blood request deleted successfully')
      fetchRequests()
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete request')
    } finally {
      setDeletingRequestId(null)
    }
  }

  const handleCompleteDonation = async (donationId) => {
    if (!window.confirm('Confirm that you have received the blood donation?')) return

    try {
      await api.post(`/emergency/donations/${donationId}/complete`)
      toast.success('Donation completed! Impact scores updated.')
      setShowLocation(false)
      fetchRequests()
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to complete donation')
    }
  }

  const fetchLocationSession = async () => {
    if (!selectedRequest) return
    try {
      const response = await api.get(`/emergency/${selectedRequest.id}/location`)
      setLocationData(response.data)
    } catch (error) {
      console.error('Failed to fetch location session', error)
    }
  }

  const updateMyLocation = async () => {
    if (!selectedRequest || !navigator.geolocation) return

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const isDonor = selectedRequest.matchedDonor?.id === user.id
        try {
          await api.post(`/emergency/${selectedRequest.id}/location`, {
            lat: position.coords.latitude,
            lng: position.coords.longitude,
            isDonor
          })
          fetchLocationSession()
        } catch (error) {
          console.error('Failed to update location', error)
        }
      },
      (error) => {
        console.error('Geolocation error', error)
      }
    )
  }

  const getUrgencyColor = (urgency) => {
    switch (urgency?.toUpperCase()) {
      case 'CRITICAL': return '#dc3545'
      case 'HIGH': return '#ff6b6b'
      case 'MEDIUM': return '#ffa500'
      case 'LOW': return '#28a745'
      default: return '#666'
    }
  }

  if (loading) {
    return <div className="loading">Loading emergency requests...</div>
  }

  return (
    <div className="emergency-feed">
      <div className="emergency-header">
        <h1>Emergency Blood Requests</h1>
        <button className="btn btn-danger" onClick={() => setShowCreateForm(true)}>
          + Create Emergency Request
        </button>
      </div>

      {showCreateForm && (
        <div className="create-request-modal">
          <div className="modal-content">
            <h2>Create Emergency Blood Request</h2>
            <form onSubmit={handleCreateRequest}>
              <div className="form-group">
                <label>Blood Group *</label>
                <select
                  value={formData.bloodGroup}
                  onChange={(e) => setFormData({ ...formData, bloodGroup: e.target.value })}
                  required
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
                <label>Number of Bags Required *</label>
                <input
                  type="number"
                  min="1"
                  value={formData.bagsRequired}
                  onChange={(e) => setFormData({ ...formData, bagsRequired: parseInt(e.target.value) })}
                  required
                />
                <small>Required Impact Score: {formData.bagsRequired * 100} (You have: {user?.currentImpactScore || 0})</small>
              </div>

              <div className="form-group">
                <label>Hospital Name *</label>
                <input
                  type="text"
                  value={formData.hospitalName}
                  onChange={(e) => setFormData({ ...formData, hospitalName: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label>Hospital Address *</label>
                <input
                  type="text"
                  value={formData.hospitalAddress}
                  onChange={(e) => setFormData({ ...formData, hospitalAddress: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label>City *</label>
                <input
                  type="text"
                  value={formData.city}
                  onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label>Urgency Level *</label>
                <select
                  value={formData.urgencyLevel}
                  onChange={(e) => setFormData({ ...formData, urgencyLevel: e.target.value })}
                  required
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows="3"
                />
              </div>

              <div className="form-actions">
                <button type="submit" className="btn btn-danger">Create Request</button>
                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateForm(false)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showChat && selectedRequest && (
        <div className="chat-modal">
          <div className="chat-content">
            <div className="chat-header">
              <h3>Chat with {selectedRequest.matchedDonor ? selectedRequest.matchedDonor.fullName : 'Donor'}</h3>
              <button onClick={() => {
                setShowChat(false)
                setSelectedRequest(null)
                setChatMessages([])
              }}>✕</button>
            </div>
            <div className="chat-messages">
              {chatMessages.map((msg) => (
                <div key={msg.id} className={`chat-message ${msg.sender.id === user.id ? 'sent' : 'received'}`}>
                  <strong>{msg.sender.fullName}:</strong> {msg.message}
                  <span className="message-time">
                    {new Date(msg.createdAt).toLocaleTimeString()}
                  </span>
                </div>
              ))}
            </div>
            <form onSubmit={handleSendMessage} className="chat-input-form">
              <input
                type="text"
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                placeholder="Type a message..."
              />
              <button type="submit">Send</button>
            </form>
          </div>
        </div>
      )}

      {showLocation && selectedRequest && locationData && (
        <div className="location-modal">
          <div className="location-content">
            <div className="location-header">
              <h3>Real-time Location Sharing</h3>
              <button onClick={() => setShowLocation(false)}>✕</button>
            </div>
            <div className="location-map">
              {/* Location sharing UI - can be enhanced with actual map integration */}
              <p>Location sharing is active</p>
              {locationData.donorLocationLat && (
                <p>Donor Location: {locationData.donorLocationLat}, {locationData.donorLocationLng}</p>
              )}
              {locationData.requesterLocationLat && (
                <p>Requester Location: {locationData.requesterLocationLat}, {locationData.requesterLocationLng}</p>
              )}
            </div>
          </div>
        </div>
      )}

      <div className="requests-container">
        {requests.length === 0 ? (
          <div className="no-requests">No emergency requests at the moment.</div>
        ) : (
          requests.map((request) => (
            <div key={request.id} className={`request-card ${request.urgencyLevel?.toLowerCase()}`}>
              <div className="request-header">
                <div className="urgency-badge" style={{ backgroundColor: getUrgencyColor(request.urgencyLevel) }}>
                  {request.urgencyLevel}
                </div>
                <div className="blood-group-badge">{request.bloodGroup}</div>
                <div className="status-badge">{request.status}</div>
              </div>
              
              <div className="request-details">
                <h3>{request.hospitalName}</h3>
                <p><strong>Location:</strong> {request.hospitalAddress}, {request.city}</p>
                <p><strong>Bags Required:</strong> {request.bagsRequired}</p>
                <p><strong>Requester Impact Score:</strong> {request.requester?.currentImpactScore || 0}</p>
                <p><strong>Required Impact Score:</strong> {request.impactScoreRequired}</p>
                {request.description && <p><strong>Description:</strong> {request.description}</p>}
                {request.matchedDonor && (
                  <p><strong>Matched Donor:</strong> {request.matchedDonor.fullName}</p>
                )}
              </div>

              <div className="request-footer">
                {request.status === 'OPEN' && request.requester.id === user.id && (
                  <button
                    onClick={() => handleDeleteRequest(request.id)}
                    disabled={deletingRequestId === request.id}
                    className="btn btn-danger"
                  >
                    {deletingRequestId === request.id ? 'Deleting...' : '🗑️ Delete Request'}
                  </button>
                )}
                {request.status === 'OPEN' && request.requester.id !== user.id && (
                <button
                  className="btn btn-primary"
                  onClick={() => handleRespond(request.id)}
                  >
                    Respond
                  </button>
                )}
                {request.status === 'MATCHED' && (
                  <>
                    <button
                      className="btn btn-success"
                      onClick={() => {
                        setSelectedRequest(request)
                        setShowChat(true)
                      }}
                    >
                      Open Chat
                    </button>
                    {request.requester.id === user.id && chatMessages.length > 0 && (
                      <button
                        className="btn btn-info"
                        onClick={async () => {
                          try {
                            const donationResponse = await api.get(`/emergency/${request.id}/donation`)
                            const donationId = donationResponse.data.id
                            handleConfirmDonation(donationId, false) // requester confirms
                          } catch (error) {
                            toast.error('Failed to confirm donation')
                          }
                        }}
                      >
                        Confirm Receipt
                      </button>
                    )}
                    {request.matchedDonor?.id === user.id && (
                      <button
                        className="btn btn-info"
                        onClick={async () => {
                          try {
                            const donationResponse = await api.get(`/emergency/${request.id}/donation`)
                            const donationId = donationResponse.data.id
                            handleConfirmDonation(donationId, true) // donor confirms
                          } catch (error) {
                            toast.error('Failed to confirm donation')
                          }
                        }}
                >
                        Confirm Donation
                      </button>
                    )}
                  </>
                )}
                {request.status === 'IN_PROGRESS' && (
                  <>
                    <button
                      className="btn btn-info"
                      onClick={() => {
                        setSelectedRequest(request)
                        setShowLocation(true)
                        fetchLocationSession()
                      }}
                    >
                      View Location
                    </button>
                    {request.requester.id === user.id && (
                      <button
                        className="btn btn-success"
                        onClick={async () => {
                          try {
                            const donationResponse = await api.get(`/emergency/${request.id}/donation`)
                            const donationId = donationResponse.data.id
                            handleCompleteDonation(donationId)
                          } catch (error) {
                            toast.error('Failed to fetch donation information')
                          }
                        }}
                      >
                        Complete Donation
                </button>
                    )}
                  </>
                )}
                <span className="request-date">
                  {new Date(request.createdAt).toLocaleString()}
                </span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default EmergencyFeed
