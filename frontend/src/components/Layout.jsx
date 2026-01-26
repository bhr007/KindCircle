import { Outlet, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useState, useEffect } from 'react'
import { FaInbox, FaBell } from 'react-icons/fa'
import api from '../services/api'
import './Layout.css'

const Layout = () => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [showDropdown, setShowDropdown] = useState(false)
  const [showNotifications, setShowNotifications] = useState(false)
  const [notifications, setNotifications] = useState([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [inboxUnreadCount, setInboxUnreadCount] = useState(0)
  const [newPostsCount, setNewPostsCount] = useState(0)
  const [newRequestsCount, setNewRequestsCount] = useState(0)

  const NEWS_FEED_LAST_VIEW_KEY = 'newsFeedLastView'
  const EMERGENCY_FEED_LAST_VIEW_KEY = 'emergencyFeedLastView'
  
  useEffect(() => {
    if (user) {
      fetchNotifications()
      fetchUnreadCount()
      fetchInboxUnreadCount()
      fetchNewPostsCount()
      fetchNewRequestsCount()
      // Poll for new notifications every 30 seconds
      const interval = setInterval(() => {
        fetchNotifications()
        fetchUnreadCount()
        fetchInboxUnreadCount()
        fetchNewPostsCount()
        fetchNewRequestsCount()
      }, 30000)
      return () => clearInterval(interval)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user])
  
  const fetchInboxUnreadCount = async () => {
    try {
      const response = await api.get('/emergency/inbox/conversations')
      const totalUnread = response.data?.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0) || 0
      setInboxUnreadCount(totalUnread)
    } catch (error) {
      console.error('Failed to fetch inbox unread count', error)
    }
  }

  const fetchNewPostsCount = async () => {
    try {
      const response = await api.get('/feed?sort=recent')
      const posts = response.data || []
      const lastView = localStorage.getItem(NEWS_FEED_LAST_VIEW_KEY)
      
      if (!lastView) {
        localStorage.setItem(NEWS_FEED_LAST_VIEW_KEY, new Date().toISOString())
        setNewPostsCount(0)
        return
      }

      const lastViewDate = new Date(lastView)
      const newPosts = posts.filter(post => {
        const postDate = new Date(post.createdAt || post.created_at)
        return postDate > lastViewDate
      })

      setNewPostsCount(newPosts.length)
    } catch (error) {
      console.error('Failed to fetch new posts count', error)
    }
  }

  const fetchNewRequestsCount = async () => {
    try {
      const response = await api.get('/emergency')
      const requests = response.data || []
      const lastView = localStorage.getItem(EMERGENCY_FEED_LAST_VIEW_KEY)
      
      if (!lastView) {
        localStorage.setItem(EMERGENCY_FEED_LAST_VIEW_KEY, new Date().toISOString())
        setNewRequestsCount(0)
        return
      }

      const lastViewDate = new Date(lastView)
      // Filter: only count requests where current user is NOT the requester (only for potential donors)
      const newRequests = requests.filter(request => {
        const requestDate = new Date(request.createdAt || request.created_at)
        const isNewRequest = requestDate > lastViewDate
        // Exclude requests where current user is the requester
        const isNotRequester = request.requester?.id !== user?.id && 
                               request.requester_id !== user?.id &&
                               request.requester?.id?.toString() !== user?.id?.toString()
        return isNewRequest && isNotRequester
      })

      setNewRequestsCount(newRequests.length)
    } catch (error) {
      console.error('Failed to fetch new requests count', error)
    }
  }

  const handleNewsFeedClick = () => {
    localStorage.setItem(NEWS_FEED_LAST_VIEW_KEY, new Date().toISOString())
    setNewPostsCount(0)
  }

  const handleEmergencyClick = () => {
    localStorage.setItem(EMERGENCY_FEED_LAST_VIEW_KEY, new Date().toISOString())
    setNewRequestsCount(0)
  }

  const fetchNotifications = async () => {
    try {
      const response = await api.get('/notifications')
      setNotifications(response.data || [])
    } catch (error) {
      console.error('Failed to fetch notifications', error)
    }
  }

  const fetchUnreadCount = async () => {
    try {
      const response = await api.get('/notifications/unread/count')
      setUnreadCount(response.data?.count || 0)
    } catch (error) {
      console.error('Failed to fetch unread count', error)
    }
  }

  const handleMarkAsRead = async (notificationId) => {
    try {
      await api.put(`/notifications/${notificationId}/read`)
      fetchNotifications()
      fetchUnreadCount()
    } catch (error) {
      console.error('Failed to mark notification as read', error)
    }
  }

  const handleMarkAllAsRead = async () => {
    try {
      await api.put('/notifications/read-all')
      fetchNotifications()
      fetchUnreadCount()
    } catch (error) {
      console.error('Failed to mark all as read', error)
    }
  }

  const handleNotificationClick = (notification) => {
    if (!notification.isRead || notification.isRead === false) {
      handleMarkAsRead(notification.id)
    }
    setShowNotifications(false)
    const type = notification.notificationType || notification.notification_type
    const postId = notification.relatedPostId || notification.related_post_id
    const campaignId = notification.relatedCampaignId || notification.related_campaign_id

    if (type === 'POST_PENDING_APPROVAL') {
      navigate('/admin?tab=posts')
      return
    }
    if (type === 'CAMPAIGN_PENDING_APPROVAL') {
      navigate('/admin?tab=campaigns')
      return
    }
    if (postId) {
      const openComments = type === 'COMMENT'
      navigate(`/?postId=${postId}${openComments ? '&openComments=true' : ''}`)
    }
  }

  const formatNotificationTime = (createdAt) => {
    const date = new Date(createdAt)
    const now = new Date()
    const diffMs = now - date
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)

    if (diffMins < 1) return 'Just now'
    if (diffMins < 60) return `${diffMins}m ago`
    if (diffHours < 24) return `${diffHours}h ago`
    if (diffDays < 7) return `${diffDays}d ago`
    return date.toLocaleDateString()
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="layout">
      <nav className="navbar">
        <div className="nav-container">
          <Link to="/" className="logo">
            <h2>KindCircle</h2>
          </Link>
          
          <div className="nav-links">
            <Link to="/" className="nav-link-with-badge" onClick={handleNewsFeedClick}>
              News Feed
              {newPostsCount > 0 && (
                <span className="notification-badge">{newPostsCount > 99 ? '99+' : newPostsCount}</span>
              )}
            </Link>
            <Link to="/emergency" className="nav-link-with-badge" onClick={handleEmergencyClick}>
              Emergency
              {newRequestsCount > 0 && (
                <span className="notification-badge">{newRequestsCount > 99 ? '99+' : newRequestsCount}</span>
              )}
            </Link>
            <Link to="/awareness">Awareness</Link>
          </div>

          <div className="nav-user">
            <Link 
              to="/inbox" 
              className="inbox-link"
              onClick={() => {
                setShowNotifications(false)
                setShowDropdown(false)
              }}
              title="Emergency Inbox"
            >
              <FaInbox />
              {inboxUnreadCount > 0 && (
                <span className="notification-badge">{inboxUnreadCount > 99 ? '99+' : inboxUnreadCount}</span>
              )}
            </Link>
            <div className="notification-dropdown">
              <button
                className="notification-button"
                onClick={() => {
                  setShowNotifications(!showNotifications)
                  setShowDropdown(false)
                }}
                title="Notifications"
              >
                <FaBell />
                {unreadCount > 0 && (
                  <span className="notification-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
                )}
              </button>
              {showNotifications && (
                <div className="notifications-menu">
                  <div className="notifications-header">
                    <h3>Notifications</h3>
                    {unreadCount > 0 && (
                      <button className="mark-all-read-btn" onClick={handleMarkAllAsRead}>
                        Mark all as read
                      </button>
                    )}
                  </div>
                  <div className="notifications-list">
                    {notifications.length === 0 ? (
                      <div className="no-notifications">No notifications</div>
                    ) : (
                      notifications.map((notification) => (
                        <div
                          key={notification.id}
                          className={`notification-item ${!notification.isRead ? 'unread' : ''}`}
                          onClick={() => handleNotificationClick(notification)}
                        >
                          <div className="notification-content">
                            <p className="notification-message">{notification.message}</p>
                            <span className="notification-time">{formatNotificationTime(notification.createdAt)}</span>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              )}
            </div>
            <div className="user-dropdown">
              <button
                className="user-button"
                onClick={() => {
                  setShowDropdown(!showDropdown)
                  setShowNotifications(false)
                }}
              >
                <img
                  src={(() => {
                    const photoUrl = user?.profilePhotoUrl || user?.profile_photo_url
                    if (!photoUrl) return '/default-avatar.png'
                    if (photoUrl.startsWith('uploads/')) {
                      return `http://localhost:8080/${photoUrl}`
                    }
                    if (photoUrl.startsWith('http')) return photoUrl
                    return photoUrl
                  })()}
                  alt={user?.fullName || user?.full_name}
                  className="user-avatar"
                />
              </button>
              {showDropdown && (
                <div className="dropdown-menu">
                  <Link to="/profile" onClick={() => setShowDropdown(false)}>
                    My Profile
                  </Link>
                  <Link to="/settings" onClick={() => setShowDropdown(false)}>
                    Settings
                  </Link>
                  {user?.role === 'ADMIN' && (
                    <Link to="/admin" onClick={() => setShowDropdown(false)}>
                      Admin Panel
                    </Link>
                  )}
                  <button onClick={handleLogout}>Logout</button>
                </div>
              )}
            </div>
            <div className="impact-score">
              Impact Score: <strong>{user?.currentImpactScore || user?.current_impact_score || 0}</strong>
            </div>
          </div>
        </div>
      </nav>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}

export default Layout
