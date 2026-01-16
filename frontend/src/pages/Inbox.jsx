import { useState, useEffect, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'
import { toast } from 'react-toastify'
import './Inbox.css'

const Inbox = () => {
  const { user } = useAuth()
  const [conversations, setConversations] = useState([])
  const [selectedConversation, setSelectedConversation] = useState(null)
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [locationData, setLocationData] = useState(null)
  const messagesEndRef = useRef(null)
  const locationIntervalRef = useRef(null)

  useEffect(() => {
    fetchConversations()
    
    // Poll for new conversations every 5 seconds
    const interval = setInterval(() => {
      fetchConversations()
    }, 5000)
    return () => clearInterval(interval)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (selectedConversation) {
      fetchMessages()
      markAsRead()
      
      // Poll for new messages every 2 seconds
      const messageInterval = setInterval(() => {
        fetchMessages()
        fetchConversations() // Also refresh conversations to update unread count
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
  }, [selectedConversation])

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
      
      // Update selected conversation if it's still open
      if (selectedConversation) {
        const updated = response.data.find(c => c.requestId === selectedConversation.requestId)
        if (updated) {
          setSelectedConversation(updated)
        }
      }
    } catch (error) {
      console.error('Failed to fetch conversations', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchMessages = async () => {
    if (!selectedConversation) return
    
    try {
      const response = await api.get(`/emergency/inbox/${selectedConversation.requestId}/messages`)
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
      await api.post(`/emergency/inbox/${selectedConversation.requestId}/mark-read`)
      fetchConversations() // Refresh to update unread count
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
      await api.post(`/emergency/${selectedConversation.requestId}/chat`, { message: messageToSend })
      // Fetch messages immediately after sending
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
        <div className="conversations-list">
          {conversations.length === 0 ? (
            <div className="no-conversations">No conversations yet</div>
          ) : (
            conversations.map((conversation) => (
              <div
                key={conversation.requestId}
                className={`conversation-item ${
                  selectedConversation?.requestId === conversation.requestId ? 'active' : ''
                } ${conversation.unreadCount > 0 ? 'unread' : ''}`}
                onClick={() => handleSelectConversation(conversation)}
              >
                <div className="conversation-header-row">
                  <div className="conversation-user-info">
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
                  </div>
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
          )}
        </div>
      </div>

      <div className="inbox-main">
        {selectedConversation ? (
          <>
            <div className="chat-header">
              <div className="chat-header-user">
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
              </div>
              {selectedConversation.locationSharingEnabled && (
                <div className="location-sharing-badge">📍 Location Sharing Active</div>
              )}
            </div>

            {selectedConversation.locationSharingEnabled && locationData && (
              <div className="location-display">
                <h4>Real-time Location</h4>
                <div className="location-coordinates">
                  {locationData.donorLocationLat && locationData.donorLocationLng && (
                    <div>
                      <strong>Donor:</strong> {locationData.donorLocationLat.toFixed(6)}, {locationData.donorLocationLng.toFixed(6)}
                    </div>
                  )}
                  {locationData.requesterLocationLat && locationData.requesterLocationLng && (
                    <div>
                      <strong>Requester:</strong> {locationData.requesterLocationLat.toFixed(6)}, {locationData.requesterLocationLng.toFixed(6)}
                    </div>
                  )}
                  <small>Locations update every 5 seconds</small>
                </div>
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
