import { useState, useEffect, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import { Link } from 'react-router-dom'
import api from '../services/api'
import { toast } from 'react-toastify'
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet'
import { FaTrash } from 'react-icons/fa'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import './Inbox.css'

// Fix for default marker icons in Leaflet
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})

const Inbox = () => {
  const { user } = useAuth()
  const [conversations, setConversations] = useState([])
  const [archivedConversations, setArchivedConversations] = useState([])
  const [activeTab, setActiveTab] = useState('active') // 'active' or 'archived'
  const [selectedConversation, setSelectedConversation] = useState(null)
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [locationData, setLocationData] = useState(null)
  const [responders, setResponders] = useState([])
  const [loadingResponders, setLoadingResponders] = useState(false)
  const [confirmingMatch, setConfirmingMatch] = useState(false)
  const [confirmingDonation, setConfirmingDonation] = useState(false)
  const [completingDonation, setCompletingDonation] = useState(false)
  const [archivingConversation, setArchivingConversation] = useState(false)
  const [deletingConversation, setDeletingConversation] = useState(false)
  const [mapHeight, setMapHeight] = useState(250)
  const [isResizing, setIsResizing] = useState(false)
  const messagesEndRef = useRef(null)
  const locationIntervalRef = useRef(null)
  const mapContainerRef = useRef(null)

  useEffect(() => {
    fetchConversations()
    fetchArchivedConversations()
    
    // Poll for new conversations every 5 seconds
    const interval = setInterval(() => {
      fetchConversations()
      if (activeTab === 'archived') {
        fetchArchivedConversations()
      }
    }, 5000)
    return () => clearInterval(interval)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab])

  useEffect(() => {
    if (selectedConversation) {
      fetchMessages()
      markAsRead()
      
      // Poll for new messages every 2 seconds
      const messageInterval = setInterval(() => {
        fetchMessages()
        // Only refresh the appropriate list based on active tab
        if (activeTab === 'active') {
          fetchConversations() // Also refresh conversations to update unread count
        } else if (activeTab === 'archived') {
          fetchArchivedConversations() // Refresh archived conversations
        }
      }, 2000)
      
      // Poll for location updates every 5 seconds if location sharing is enabled
      let locationInterval = null
      if (selectedConversation.locationSharingEnabled) {
        fetchLocationData()
        locationInterval = setInterval(() => {
          fetchLocationData()
        }, 5000)
        locationIntervalRef.current = locationInterval
      }
      
      // Cleanup intervals on unmount or conversation change
      return () => {
        clearInterval(messageInterval)
        if (locationInterval) {
          clearInterval(locationInterval)
        }
        if (locationIntervalRef.current) {
          clearInterval(locationIntervalRef.current)
          locationIntervalRef.current = null
        }
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedConversation, activeTab])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const fetchConversations = async () => {
    try {
      const response = await api.get('/emergency/inbox/conversations')
      setConversations(response.data)
      
      // Update selected conversation if it's still open and we're on active tab
      if (selectedConversation && activeTab === 'active') {
        const updated = response.data.find(c =>
          c.requestId === selectedConversation.requestId &&
          c.otherUser?.id === selectedConversation.otherUser?.id
        )
        if (updated) {
          setSelectedConversation(updated)
        } else {
          setSelectedConversation(null)
        }
      }
    } catch (error) {
      console.error('Failed to fetch conversations', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchArchivedConversations = async () => {
    try {
      const response = await api.get('/emergency/inbox/archived')
      setArchivedConversations(response.data)
      
      // Update selected conversation if it's still open and we're on archived tab
      if (selectedConversation && activeTab === 'archived') {
        const updated = response.data.find(c =>
          c.requestId === selectedConversation.requestId &&
          c.otherUser?.id === selectedConversation.otherUser?.id
        )
        if (updated) {
          setSelectedConversation(updated)
        } else {
          setSelectedConversation(null)
        }
      }
    } catch (error) {
      console.error('Failed to fetch archived conversations', error)
    }
  }

  const fetchMessages = async () => {
    if (!selectedConversation) return
    try {
      const rid = selectedConversation.requestId
      const oid = selectedConversation.otherUser?.id
      const url = oid != null
        ? `/emergency/inbox/${rid}/messages?otherUserId=${oid}`
        : `/emergency/inbox/${rid}/messages`
      const response = await api.get(url)
      setMessages(response.data)
    } catch (error) {
      console.error('Failed to fetch messages', error)
    }
  }

  const fetchLocationData = async () => {
    if (!selectedConversation) return
    
    try {
      const response = await api.get(`/emergency/${selectedConversation.requestId}/location`)
      setLocationData(response.data)
      
      // Update location if we have permission
      if (navigator.geolocation) {
        const isDonor = selectedConversation.isDonor
        navigator.geolocation.getCurrentPosition(
          async (position) => {
            try {
              await api.post(`/emergency/${selectedConversation.requestId}/location`, {
                lat: position.coords.latitude,
                lng: position.coords.longitude,
                isDonor
              })
            } catch (error) {
              console.error('Failed to update location', error)
            }
          },
          (error) => console.error('Geolocation error', error)
        )
      }
    } catch (error) {
      // Location session might not exist yet
      console.error('Failed to fetch location', error)
    }
  }

  const markAsRead = async () => {
    if (!selectedConversation) return
    try {
      const rid = selectedConversation.requestId
      const oid = selectedConversation.otherUser?.id
      const url = oid != null
        ? `/emergency/inbox/${rid}/mark-read?otherUserId=${oid}`
        : `/emergency/inbox/${rid}/mark-read`
      await api.post(url)
      fetchConversations()
    } catch (error) {
      console.error('Failed to mark as read', error)
    }
  }

  const handleSendMessage = async (e) => {
    e.preventDefault()
    if (!newMessage.trim() || !selectedConversation) return

    const messageToSend = newMessage.trim()
    setNewMessage('') // Clear input immediately for better UX

    try {
      await api.post(`/emergency/${selectedConversation.requestId}/chat`, {
        message: messageToSend,
        receiverId: selectedConversation.otherUser?.id
      })
      await fetchMessages()
      await fetchConversations()
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to send message')
      setNewMessage(messageToSend) // Restore message if failed
    }
  }

  const handleSelectConversation = (conversation) => {
    setSelectedConversation(conversation)
    setMessages([])
    setLocationData(null)
    setResponders([])
    
    // If requester and no match confirmed, fetch responders
    if (conversation && !conversation.isDonor && 
        conversation.requestInfo?.status === 'OPEN' && 
        !conversation.requestInfo?.matchedDonor) {
      fetchResponders(conversation.requestId)
    }
  }
  
  const fetchResponders = async (requestId) => {
    if (!requestId) return
    setLoadingResponders(true)
    try {
      const response = await api.get(`/emergency/${requestId}/responders`)
      setResponders(response.data)
    } catch (error) {
      console.error('Failed to fetch responders', error)
    } finally {
      setLoadingResponders(false)
    }
  }
  
  const handleConfirmMatch = async (donorId) => {
    if (!selectedConversation) return
    setConfirmingMatch(true)
    try {
      await api.post(`/emergency/${selectedConversation.requestId}/confirm-match/${donorId}`)
      toast.success('Match confirmed! Both parties can now confirm the donation.')
      // Refresh conversations to get updated data
      const conversationsResponse = await api.get('/emergency/inbox/conversations')
      setConversations(conversationsResponse.data)
      // Refresh selected conversation
      const updated = conversationsResponse.data.find(c =>
        c.requestId === selectedConversation.requestId &&
        c.otherUser?.id === selectedConversation.otherUser?.id
      )
      if (updated) {
        setSelectedConversation(updated)
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to confirm match')
    } finally {
      setConfirmingMatch(false)
    }
  }
  
  const handleConfirmDonation = async () => {
    if (!selectedConversation?.donationInfo) return
    setConfirmingDonation(true)
    try {
      const isDonor = selectedConversation.isDonor
      await api.post(`/emergency/donations/${selectedConversation.donationInfo.donationId}/confirm`, {
        isDonor
      })
      // Refresh conversations to get updated data
      const conversationsResponse = await api.get('/emergency/inbox/conversations')
      setConversations(conversationsResponse.data)
      // Refresh selected conversation
      const updated = conversationsResponse.data.find(c =>
        c.requestId === selectedConversation.requestId &&
        c.otherUser?.id === selectedConversation.otherUser?.id
      )
      if (updated) {
        setSelectedConversation(updated)
        if (updated.locationSharingEnabled) {
          fetchLocationData()
          // Start location polling if enabled
          if (locationIntervalRef.current) {
            clearInterval(locationIntervalRef.current)
          }
          const locationInterval = setInterval(() => {
            fetchLocationData()
          }, 5000)
          locationIntervalRef.current = locationInterval
        }
      }
      const wasBothConfirmed = selectedConversation.donationInfo.donorConfirmed && selectedConversation.donationInfo.requesterConfirmed
      toast.success('Donation confirmed! ' + 
        (wasBothConfirmed
          ? 'Location sharing is now enabled.'
          : 'Waiting for the other party to confirm.'))
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to confirm donation')
    } finally {
      setConfirmingDonation(false)
    }
  }
  
  const handleCompleteDonation = async () => {
    if (!selectedConversation?.donationInfo) return
    setCompletingDonation(true)
    try {
      await api.post(`/emergency/donations/${selectedConversation.donationInfo.donationId}/complete`)
      toast.success('Donation completed! Impact scores have been updated.')
      // Refresh conversations to get updated data
      const conversationsResponse = await api.get('/emergency/inbox/conversations')
      setConversations(conversationsResponse.data)
      // Refresh selected conversation
      const updated = conversationsResponse.data.find(c =>
        c.requestId === selectedConversation.requestId &&
        c.otherUser?.id === selectedConversation.otherUser?.id
      )
      if (updated) {
        setSelectedConversation(updated)
      }
      // Stop location sharing
      if (locationIntervalRef.current) {
        clearInterval(locationIntervalRef.current)
        locationIntervalRef.current = null
      }
      setLocationData(null)
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to complete donation')
    } finally {
      setCompletingDonation(false)
    }
  }

  const handleArchiveConversation = async () => {
    if (!selectedConversation) return
    if (!window.confirm('Are you sure you want to archive this conversation?')) return
    
    setArchivingConversation(true)
    try {
      const rid = selectedConversation.requestId
      const oid = selectedConversation.otherUser?.id
      const url = oid != null
        ? `/emergency/inbox/${rid}/archive?otherUserId=${oid}`
        : `/emergency/inbox/${rid}/archive`
      await api.post(url)
      toast.success('Conversation archived successfully')
      await fetchConversations()
      await fetchArchivedConversations()
      setSelectedConversation(null)
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to archive conversation')
    } finally {
      setArchivingConversation(false)
    }
  }

  const handleDeleteConversation = async () => {
    if (!selectedConversation) return
    if (!window.confirm('Are you sure you want to delete this conversation? This action cannot be undone.')) return
    
    setDeletingConversation(true)
    try {
      const rid = selectedConversation.requestId
      const oid = selectedConversation.otherUser?.id
      const url = oid != null
        ? `/emergency/inbox/${rid}/conversation?otherUserId=${oid}`
        : `/emergency/inbox/${rid}/conversation`
      await api.delete(url)
      toast.success('Conversation deleted successfully')
      await fetchConversations()
      await fetchArchivedConversations()
      setSelectedConversation(null)
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete conversation')
    } finally {
      setDeletingConversation(false)
    }
  }

  // Check if donation is completed
  const isDonationCompleted = selectedConversation?.donationInfo?.status === 'COMPLETED'
  
  // Check if conversation is archived (selected from archived tab)
  const isArchivedConversation = activeTab === 'archived' && selectedConversation

  const handleMapResizeStart = (e) => {
    e.preventDefault()
    setIsResizing(true)
    const startY = e.clientY
    const startHeight = mapHeight

    const handleMouseMove = (e) => {
      const diff = startY - e.clientY
      const newHeight = Math.max(200, Math.min(600, startHeight + diff))
      setMapHeight(newHeight)
    }

    const handleMouseUp = () => {
      setIsResizing(false)
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }

    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', handleMouseUp)
  }

  const formatTime = (dateString) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now - date
    const diffMins = Math.floor(diffMs / 60000)
    
    if (diffMins < 1) return 'Just now'
    if (diffMins < 60) return `${diffMins}m ago`
    if (diffMins < 1440) return `${Math.floor(diffMins / 60)}h ago`
    return date.toLocaleDateString()
  }

  if (loading) {
    return <div className="loading">Loading inbox...</div>
  }

  return (
    <div className="inbox-container">
      <div className="inbox-sidebar">
        <div className="inbox-header">
          <h2>Emergency Inbox</h2>
        </div>
        <div className="inbox-tabs">
          <button
            className={`inbox-tab ${activeTab === 'active' ? 'active' : ''}`}
            onClick={() => {
              setActiveTab('active')
              setSelectedConversation(null)
            }}
          >
            Active
          </button>
          <button
            className={`inbox-tab ${activeTab === 'archived' ? 'active' : ''}`}
            onClick={() => {
              setActiveTab('archived')
              setSelectedConversation(null)
              fetchArchivedConversations()
            }}
          >
            Archived
          </button>
        </div>
        <div className="conversations-list">
          {activeTab === 'active' ? (
            conversations.length === 0 ? (
              <div className="no-conversations">No active conversations yet</div>
            ) : (
              conversations.map((conversation) => (
                <div
                  key={`${conversation.requestId}-${conversation.otherUser?.id ?? 'x'}`}
                  className={`conversation-item ${
                    selectedConversation?.requestId === conversation.requestId &&
                    selectedConversation?.otherUser?.id === conversation.otherUser?.id ? 'active' : ''
                  } ${conversation.unreadCount > 0 ? 'unread' : ''}`}
                  onClick={() => handleSelectConversation(conversation)}
                >
                  <div className="conversation-header-row">
                    <Link to={`/profile/${conversation.otherUser?.id}`} className="conversation-user-info" onClick={(e) => e.stopPropagation()}>
                      {conversation.otherUser?.profilePhotoUrl ? (
                        <img
                          src={conversation.otherUser.profilePhotoUrl.startsWith('http')
                            ? conversation.otherUser.profilePhotoUrl
                            : `http://localhost:8080/${conversation.otherUser.profilePhotoUrl}`}
                          alt={conversation.otherUser.fullName}
                          className="conversation-avatar"
                        />
                      ) : (
                        <div className="conversation-avatar-placeholder">
                          {conversation.otherUser?.fullName?.charAt(0) || 'U'}
                        </div>
                      )}
                      <div className="conversation-user-details">
                        <h4>{conversation.otherUser?.fullName || 'Unknown User'}</h4>
                        <p className="conversation-request-info">
                          {conversation.requestInfo?.bloodGroup} • {conversation.requestInfo?.urgencyLevel}
                        </p>
                      </div>
                    </Link>
                    {conversation.unreadCount > 0 && (
                      <span className="unread-badge">{conversation.unreadCount}</span>
                    )}
                  </div>
                  {conversation.lastMessage && (
                    <p className="conversation-preview">
                      {conversation.lastMessage.message.substring(0, 50)}
                      {conversation.lastMessage.message.length > 50 ? '...' : ''}
                    </p>
                  )}
                  <span className="conversation-time">
                    {conversation.lastActivityAt ? formatTime(conversation.lastActivityAt) : ''}
                  </span>
                </div>
              ))
            )
          ) : (
            archivedConversations.length === 0 ? (
              <div className="no-conversations">No archived conversations yet</div>
            ) : (
              archivedConversations.map((conversation) => (
                <div
                  key={`${conversation.requestId}-${conversation.otherUser?.id ?? 'x'}`}
                  className={`conversation-item archived ${
                    selectedConversation?.requestId === conversation.requestId &&
                    selectedConversation?.otherUser?.id === conversation.otherUser?.id ? 'active' : ''
                  }`}
                  onClick={() => handleSelectConversation(conversation)}
                >
                  <div className="conversation-header-row">
                    <Link to={`/profile/${conversation.otherUser?.id}`} className="conversation-user-info" onClick={(e) => e.stopPropagation()}>
                      {conversation.otherUser?.profilePhotoUrl ? (
                        <img
                          src={conversation.otherUser.profilePhotoUrl.startsWith('http')
                            ? conversation.otherUser.profilePhotoUrl
                            : `http://localhost:8080/${conversation.otherUser.profilePhotoUrl}`}
                          alt={conversation.otherUser.fullName}
                          className="conversation-avatar"
                        />
                      ) : (
                        <div className="conversation-avatar-placeholder">
                          {conversation.otherUser?.fullName?.charAt(0) || 'U'}
                        </div>
                      )}
                      <div className="conversation-user-details">
                        <h4>{conversation.otherUser?.fullName || 'Unknown User'}</h4>
                        <p className="conversation-request-info">
                          {conversation.requestInfo?.bloodGroup} • {conversation.requestInfo?.urgencyLevel}
                        </p>
                      </div>
                    </Link>
                    <span className="archived-badge">📦 Archived</span>
                  </div>
                  {conversation.lastMessage && (
                    <p className="conversation-preview">
                      {conversation.lastMessage.message.substring(0, 50)}
                      {conversation.lastMessage.message.length > 50 ? '...' : ''}
                    </p>
                  )}
                  <span className="conversation-time">
                    {conversation.lastActivityAt ? formatTime(conversation.lastActivityAt) : ''}
                  </span>
                </div>
              ))
            )
          )}
        </div>
      </div>

      <div className="inbox-main">
        {selectedConversation ? (
          <>
            <div className="chat-header">
              <Link to={`/profile/${selectedConversation.otherUser?.id}`} className="chat-header-user">
                {selectedConversation.otherUser?.profilePhotoUrl ? (
                  <img
                    src={selectedConversation.otherUser.profilePhotoUrl.startsWith('http')
                      ? selectedConversation.otherUser.profilePhotoUrl
                      : `http://localhost:8080/${selectedConversation.otherUser.profilePhotoUrl}`}
                    alt={selectedConversation.otherUser.fullName}
                    className="chat-header-avatar"
                  />
                ) : (
                  <div className="chat-header-avatar-placeholder">
                    {selectedConversation.otherUser?.fullName?.charAt(0) || 'U'}
                  </div>
                )}
                <div>
                  <h3>{selectedConversation.otherUser?.fullName || 'Unknown User'}</h3>
                  <p className="chat-header-info">
                    Blood Group: {selectedConversation.requestInfo?.bloodGroup} • 
                    Urgency: {selectedConversation.requestInfo?.urgencyLevel}
                  </p>
                </div>
              </Link>
              <div className="chat-header-actions">
                {selectedConversation.locationSharingEnabled && (
                  <div className="location-sharing-badge">📍 Location Sharing Active</div>
                )}
                <button
                  onClick={handleDeleteConversation}
                  disabled={deletingConversation}
                  className="btn-delete-conversation"
                  title="Delete conversation"
                >
                  {deletingConversation ? 'Deleting...' : <><FaTrash /> Delete Chat</>}
                </button>
              </div>
            </div>

            {/* Match Confirmation Section - For Requester */}
            {selectedConversation && !selectedConversation.isDonor && 
             selectedConversation.requestInfo?.status === 'OPEN' && 
             !selectedConversation.requestInfo?.matchedDonor && (
              <div className="match-confirmation-section">
                <h4>Confirm Match with a Donor</h4>
                {loadingResponders ? (
                  <p>Loading responders...</p>
                ) : responders.length === 0 ? (
                  <p>No responders yet. Waiting for donors to respond...</p>
                ) : (
                  <div className="responders-list">
                    {responders.map((responder) => (
                      <div key={responder.id} className="responder-item">
                        <div className="responder-info">
                          {responder.profilePhotoUrl ? (
                            <img
                              src={responder.profilePhotoUrl.startsWith('http')
                                ? responder.profilePhotoUrl
                                : `http://localhost:8080/${responder.profilePhotoUrl}`}
                              alt={responder.fullName}
                              className="responder-avatar"
                            />
                          ) : (
                            <div className="responder-avatar-placeholder">
                              {responder.fullName?.charAt(0) || 'U'}
                            </div>
                          )}
                          <div>
                            <strong>{responder.fullName}</strong>
                            <p>Impact Score: {responder.currentImpactScore || 0}</p>
                          </div>
                        </div>
                        <button
                          onClick={() => handleConfirmMatch(responder.id)}
                          disabled={confirmingMatch}
                          className="btn-confirm-match"
                        >
                          {confirmingMatch ? 'Confirming...' : 'Confirm Match'}
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* Donation Confirmation Section */}
            {selectedConversation?.donationInfo && 
             selectedConversation.donationInfo.status === 'PENDING_CONFIRMATION' && (
              <div className="donation-confirmation-section">
                <h4>Confirm Donation</h4>
                <div className="confirmation-status">
                  <p>
                    <strong>Donor Confirmed:</strong> {selectedConversation.donationInfo.donorConfirmed ? '✅ Yes' : '❌ No'}
                  </p>
                  <p>
                    <strong>Requester Confirmed:</strong> {selectedConversation.donationInfo.requesterConfirmed ? '✅ Yes' : '❌ No'}
                  </p>
                </div>
                {((selectedConversation.isDonor && !selectedConversation.donationInfo.donorConfirmed) ||
                  (!selectedConversation.isDonor && !selectedConversation.donationInfo.requesterConfirmed)) && (
                  <button
                    onClick={handleConfirmDonation}
                    disabled={confirmingDonation}
                    className="btn-confirm-donation"
                  >
                    {confirmingDonation ? 'Confirming...' : 'I Confirm This Donation'}
                  </button>
                )}
                {selectedConversation.donationInfo.donorConfirmed && 
                 selectedConversation.donationInfo.requesterConfirmed && (
                  <p className="confirmation-success">
                    ✅ Both parties confirmed! Location sharing is now enabled.
                  </p>
                )}
              </div>
            )}

            {/* Complete Donation Section - For Requester */}
            {selectedConversation && !selectedConversation.isDonor &&
             selectedConversation.donationInfo &&
             selectedConversation.donationInfo.status === 'CONFIRMED' && (
              <div className="complete-donation-section">
                <h4>Complete Donation</h4>
                <p>Has the donor given blood at the hospital? Confirm receipt to complete the donation.</p>
                <button
                  onClick={handleCompleteDonation}
                  disabled={completingDonation}
                  className="btn-complete-donation"
                >
                  {completingDonation ? 'Completing...' : 'Confirm Receipt & Complete Donation'}
                </button>
              </div>
            )}

            {/* Donation Completed Section */}
            {selectedConversation?.donationInfo &&
             selectedConversation.donationInfo.status === 'COMPLETED' && (
              <div className="donation-completed-section">
                <h4>✅ Donation Completed</h4>
                <p>This donation has been completed. Impact scores have been updated.</p>
              </div>
            )}

            {selectedConversation.locationSharingEnabled && locationData && (
              <div className="location-display">
                <div className="location-header-controls">
                  <h4>Real-time Location Map</h4>
                  <button
                    onClick={() => setMapHeight(mapHeight === 250 ? 400 : 250)}
                    className="map-toggle-btn"
                    title="Toggle map size"
                  >
                    {mapHeight === 250 ? '⛶ Expand' : '⊟ Collapse'}
                  </button>
                </div>
                {locationData.donorLocationLat && locationData.donorLocationLng && 
                 locationData.requesterLocationLat && locationData.requesterLocationLng ? (
                  <div className="location-map-container" ref={mapContainerRef}>
                    <div 
                      className="map-resize-handle"
                      onMouseDown={handleMapResizeStart}
                      style={{ cursor: isResizing ? 'ns-resize' : 'row-resize' }}
                    />
                    <div style={{ position: 'relative', height: `${mapHeight}px` }}>
                      <MapContainer
                        center={[
                          (locationData.donorLocationLat + locationData.requesterLocationLat) / 2,
                          (locationData.donorLocationLng + locationData.requesterLocationLng) / 2
                        ]}
                        zoom={13}
                        style={{ height: '100%', width: '100%', borderRadius: '8px' }}
                        scrollWheelZoom={false}
                        dragging={true}
                        touchZoom={true}
                        doubleClickZoom={true}
                        zoomControl={true}
                      >
                      <TileLayer
                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                      />
                      <Marker position={[locationData.donorLocationLat, locationData.donorLocationLng]}>
                        <Popup>
                          <strong>Donor Location</strong><br />
                          {selectedConversation.otherUser?.fullName || 'Donor'}
                        </Popup>
                      </Marker>
                      <Marker position={[locationData.requesterLocationLat, locationData.requesterLocationLng]}>
                        <Popup>
                          <strong>Requester Location</strong><br />
                          {user?.fullName || 'Requester'}
                        </Popup>
                      </Marker>
                      </MapContainer>
                    </div>
                    <div className="location-coordinates">
                      <div>
                        <strong>Donor:</strong> {locationData.donorLocationLat.toFixed(6)}, {locationData.donorLocationLng.toFixed(6)}
                      </div>
                      <div>
                        <strong>Requester:</strong> {locationData.requesterLocationLat.toFixed(6)}, {locationData.requesterLocationLng.toFixed(6)}
                      </div>
                      <small>Locations update every 5 seconds • Drag the handle above the map to resize</small>
                    </div>
                  </div>
                ) : (
                  <div className="location-coordinates">
                    <p>Waiting for location data...</p>
                  </div>
                )}
              </div>
            )}

            <div className="messages-container" ref={messagesEndRef}>
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`message ${message.sender.id === user.id ? 'sent' : 'received'}`}
                >
                  <div className="message-content">
                    <p>{message.message}</p>
                    <span className="message-time">{formatTime(message.createdAt)}</span>
                  </div>
                </div>
              ))}
              <div ref={messagesEndRef} />
            </div>

            {isDonationCompleted ? (
              <div className="conversation-actions-completed">
                <p className="completed-notice">This donation has been completed. Messaging is now disabled.</p>
                <div className="conversation-action-buttons">
                  <button
                    onClick={handleArchiveConversation}
                    disabled={archivingConversation}
                    className="btn btn-secondary"
                  >
                    {archivingConversation ? 'Archiving...' : '📦 Archive Conversation'}
                  </button>
                </div>
              </div>
            ) : isArchivedConversation ? (
              <div className="conversation-actions-completed">
                <p className="completed-notice">📦 This conversation is archived. Read-only mode.</p>
              </div>
            ) : (
              <form onSubmit={handleSendMessage} className="message-input-form">
                <input
                  type="text"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder="Type a message... (Press Enter to send)"
                  className="message-input"
                />
                <button type="submit" className="send-button">Send</button>
              </form>
            )}
          </>
        ) : (
          <div className="no-conversation-selected">
            <p>Select a conversation to start messaging</p>
          </div>
        )}
      </div>
    </div>
  )
}

export default Inbox
