import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import { toast } from 'react-toastify'
import { useAuth } from '../context/AuthContext'
import CommentSection from './CommentSection'
import { FaTrash, FaThumbsUp, FaThumbsDown, FaComment } from 'react-icons/fa'
import './PostCard.css'

const PostCard = ({ post, onUpdate, autoOpenComments = false }) => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [voting, setVoting] = useState(false)
  const [showComments, setShowComments] = useState(autoOpenComments)
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    // If autoOpenComments is true, open the comments section
    if (autoOpenComments && !showComments) {
      setShowComments(true)
    }
  }, [autoOpenComments])

  const handleUserClick = (e) => {
    e.preventDefault()
    if (post.user?.id) {
      navigate(`/profile/${post.user.id}`)
    }
  }

  const handleVote = async (e, voteType) => {
    if (e) {
      e.preventDefault()
      e.stopPropagation()
    }
    if (voting) return
    
    setVoting(true)
    try {
      await api.post(`/feed/${post.id}/vote`, { voteType })
      if (onUpdate) {
        onUpdate()
      }
    } catch (error) {
      toast.error('Failed to vote')
    } finally {
      setVoting(false)
    }
  }

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this post?')) {
      return
    }

    setDeleting(true)
    try {
      await api.delete(`/feed/${post.id}`)
      toast.success('Post deleted successfully')
      if (onUpdate) {
        onUpdate()
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete post')
    } finally {
      setDeleting(false)
    }
  }

  const formatActivityType = (type) => {
    return type
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ')
  }

  const isOwnPost = user && post.user && (user.id === post.user.id || user.id === post.user?.id)

  return (
    <div className="post-card">
      <div className="post-header">
        <img
          src={(() => {
            const photoUrl = post.user?.profilePhotoUrl || post.user?.profile_photo_url
            if (!photoUrl) return '/default-avatar.png'
            if (photoUrl.startsWith('uploads/')) {
              return `http://localhost:8080/${photoUrl}`
            }
            if (photoUrl.startsWith('http')) return photoUrl
            return photoUrl
          })()}
          alt={post.user?.fullName || post.user?.full_name}
          className="post-avatar"
          onClick={handleUserClick}
          style={{ cursor: 'pointer' }}
        />
        <div className="post-user-info" onClick={handleUserClick} style={{ cursor: 'pointer', flex: 1 }}>
          <h3>{post.user?.fullName || post.user?.full_name}</h3>
          <p className="post-activity-type">{formatActivityType(post.activityType)}</p>
        </div>
        <div className="post-header-right">
          {isOwnPost && (
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="btn-delete-post"
              title="Delete post"
            >
              {deleting ? '...' : <FaTrash />}
            </button>
          )}
        </div>
      </div>
      
      {post.title && (
        <div className="post-title">
          <h2>{post.title}</h2>
        </div>
      )}
      
      <div className="post-content">
        <p>{post.description}</p>
        {post.location && (
          <p className="post-location">📍 {post.location}</p>
        )}
        {post.mediaProofs && Array.isArray(post.mediaProofs) && post.mediaProofs.length > 0 && (
          <div className="post-media">
            {post.mediaProofs.map((media, index) => {
              let imageUrl = media?.mediaUrl || media?.media_url
              
              if (!imageUrl || imageUrl.trim() === '') {
                console.warn('Media proof has no URL:', media)
                return null
              }
              
              // If it's a relative path (starts with uploads/), prepend the backend URL
              if (imageUrl.startsWith('uploads/')) {
                imageUrl = `http://localhost:8080/${imageUrl}`
              } else if (!imageUrl.startsWith('http')) {
                // If it doesn't start with http and doesn't start with uploads/, assume it's a relative path
                imageUrl = `http://localhost:8080/${imageUrl}`
              }
              
              // Check if it's a video
              const isVideo = media?.mediaType === 'VIDEO' || 
                             (media?.mimeType && media.mimeType.startsWith('video/'))
              
              console.log('Rendering media:', { 
                id: media?.id, 
                url: imageUrl, 
                mediaType: media?.mediaType, 
                mimeType: media?.mimeType,
                isVideo 
              })
              
              return isVideo ? (
                <video
                  key={media?.id || `media-${index}`}
                  src={imageUrl}
                  controls
                  className="post-image"
                  onError={(e) => {
                    console.error('Video failed to load:', imageUrl)
                    e.target.style.display = 'none'
                  }}
                  onLoadStart={() => {
                    console.log('Video started loading:', imageUrl)
                  }}
                />
              ) : (
                <img
                  key={media?.id || `media-${index}`}
                  src={imageUrl}
                  alt="Proof"
                  className="post-image"
                  onError={(e) => {
                    console.error('Image failed to load:', imageUrl, e)
                    e.target.style.display = 'none'
                  }}
                  onLoad={() => {
                    console.log('Image loaded successfully:', imageUrl)
                  }}
                />
              )
            })}
          </div>
        )}
      </div>

      <div className="post-footer">
        <div className="post-actions">
          <button 
            type="button"
            onClick={(e) => handleVote(e, 'UPVOTE')}
            disabled={voting}
            className={`vote-btn upvote ${post.userVote === 'UPVOTE' ? 'active' : ''}`}
          >
            <FaThumbsUp /> {post.upvotes || 0}
          </button>
          <button 
            type="button"
            onClick={(e) => handleVote(e, 'DOWNVOTE')}
            disabled={voting}
            className={`vote-btn downvote ${post.userVote === 'DOWNVOTE' ? 'active' : ''}`}
          >
            <FaThumbsDown /> {post.downvotes || 0}
          </button>
          <button 
            className="comment-btn"
            onClick={() => setShowComments(!showComments)}
          >
            <FaComment /> {post.commentsCount || 0}
          </button>
        </div>
        <div className="post-date">
          {new Date(post.createdAt).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
          })}
        </div>
      </div>
      
      {showComments && post?.id && (
        <div style={{ marginTop: '20px' }}>
          <CommentSection 
            postId={post.id}
            postOwnerId={post.user?.id}
            onCommentAdded={() => {
              if (onUpdate) onUpdate()
            }}
          />
        </div>
      )}
    </div>
  )
}

export default PostCard
