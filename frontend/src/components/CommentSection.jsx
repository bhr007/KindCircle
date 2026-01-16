import { useState, useEffect, useRef } from 'react'
import api from '../services/api'
import { toast } from 'react-toastify'
import { useAuth } from '../context/AuthContext'
import './CommentSection.css'

const CommentSection = ({ postId, postOwnerId, onCommentAdded }) => {
  const { user } = useAuth()
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(true)
  const [newComment, setNewComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [replyingTo, setReplyingTo] = useState(null)
  const [replyContent, setReplyContent] = useState({})
  const [votingCommentId, setVotingCommentId] = useState(null)
  const [openReplyForms, setOpenReplyForms] = useState({})
  const [deletingCommentId, setDeletingCommentId] = useState(null)
  const replyTextareaRefs = useRef({})

  useEffect(() => {
    if (postId) {
      fetchComments()
    } else {
      console.error('CommentSection: postId is missing')
      setLoading(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [postId])

  const fetchComments = async () => {
    try {
      setLoading(true)
      const response = await api.get(`/feed/${postId}/comments`)
      if (response.data && Array.isArray(response.data)) {
        setComments(response.data)
      } else {
        console.warn('Unexpected response format:', response.data)
        setComments([])
      }
    } catch (error) {
      console.error('Failed to load comments:', error)
      console.error('Error details:', error.response)
      toast.error('Failed to load comments')
      setComments([])
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!newComment.trim()) return

    setSubmitting(true)
    try {
      await api.post(`/feed/${postId}/comments`, { 
        content: newComment.trim(),
        parentCommentId: replyingTo || null
      })
      setNewComment('')
      setReplyingTo(null)
      fetchComments()
      if (onCommentAdded) {
        onCommentAdded()
      }
      toast.success(replyingTo ? 'Reply added!' : 'Comment added!')
    } catch (error) {
      toast.error('Failed to add comment')
    } finally {
      setSubmitting(false)
    }
  }


  const handleVote = async (commentId, voteType) => {
    if (votingCommentId === commentId) return
    
    setVotingCommentId(commentId)
    try {
      await api.post(`/feed/comments/${commentId}/vote`, { voteType })
      fetchComments()
    } catch (error) {
      toast.error('Failed to vote')
    } finally {
      setVotingCommentId(null)
    }
  }

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('Are you sure you want to delete this comment?')) return
    
    setDeletingCommentId(commentId)
    try {
      await api.delete(`/feed/comments/${commentId}`)
      toast.success('Comment deleted successfully')
      fetchComments()
      if (onCommentAdded) {
        onCommentAdded()
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete comment')
    } finally {
      setDeletingCommentId(null)
    }
  }

  const canDeleteComment = (comment) => {
    if (!user) return false
    const isCommentOwner = comment.user?.id === user.id
    const isPostOwner = postOwnerId && postOwnerId === user.id
    return isCommentOwner || isPostOwner
  }

  const handleMainCommentKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      if (newComment.trim()) {
        handleSubmit(e)
      }
    }
  }

  const totalCommentsCount = (comments) => {
    if (!comments || !Array.isArray(comments)) return 0
    let count = comments.length
    comments.forEach(comment => {
      if (comment && comment.replies && Array.isArray(comment.replies) && comment.replies.length > 0) {
        count += comment.replies.length
      }
    })
    return count
  }

  const CommentItem = ({ comment, isReply = false }) => {
    const currentUserVote = comment.userVote
    const showReplyForm = openReplyForms[comment.id] || false

    const setReplyTextareaRef = (element) => {
      if (element) {
        replyTextareaRefs.current[comment.id] = element
      } else {
        delete replyTextareaRefs.current[comment.id]
      }
    }

    const toggleReplyForm = () => {
      setOpenReplyForms(prev => ({
        ...prev,
        [comment.id]: !prev[comment.id]
      }))
      if (!openReplyForms[comment.id]) {
        setReplyContent({ ...replyContent, [comment.id]: '' })
        // Focus the textarea after state update
        setTimeout(() => {
          if (replyTextareaRefs.current[comment.id]) {
            replyTextareaRefs.current[comment.id].focus()
          }
        }, 0)
      }
    }

    const handleReplyKeyDown = (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        const content = replyContent[comment.id] || ''
        if (content.trim()) {
          handleReplySubmit(e)
        }
      }
    }

    const handleReplySubmit = async (e) => {
      e.preventDefault()
      const content = replyContent[comment.id] || ''
      if (!content.trim()) return

      try {
        await api.post(`/feed/${postId}/comments`, { 
          content: content.trim(),
          parentCommentId: comment.id 
        })
        setReplyContent({ ...replyContent, [comment.id]: '' })
        setOpenReplyForms(prev => {
          const newState = { ...prev }
          delete newState[comment.id]
          return newState
        })
        fetchComments()
        if (onCommentAdded) {
          onCommentAdded()
        }
        toast.success('Reply added!')
      } catch (error) {
        toast.error('Failed to add reply')
      }
    }

    if (!comment || !comment.id) {
      console.warn('Invalid comment in CommentItem:', comment)
      return null
    }

    return (
      <div className={`comment-item ${isReply ? 'comment-reply' : ''}`}>
        <div className="comment-header">
          <img
            src={(() => {
              const photoUrl = comment.user?.profilePhotoUrl || comment.user?.profile_photo_url
              if (!photoUrl) return '/default-avatar.png'
              if (photoUrl.startsWith('uploads/')) {
                return `http://localhost:8080/${photoUrl}`
              }
              if (photoUrl.startsWith('http')) return photoUrl
              return photoUrl
            })()}
            alt={comment.user?.fullName || 'User'}
            className="comment-avatar"
            onError={(e) => {
              e.target.src = '/default-avatar.png'
            }}
          />
          <div className="comment-user-info">
            <strong>{comment.user?.fullName || 'Unknown User'}</strong>
            <span className="comment-date">
              {comment.createdAt ? new Date(comment.createdAt).toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric'
              }) : ''}
            </span>
          </div>
        </div>
        <p className="comment-content">{comment.content || ''}</p>
        <div className="comment-actions">
          <button
            onClick={() => handleVote(comment.id, 'UPVOTE')}
            disabled={votingCommentId === comment.id}
            className={`comment-vote-btn upvote ${currentUserVote === 'UPVOTE' ? 'active' : ''}`}
            title="Upvote"
          >
            👍 {comment.upvotes || 0}
          </button>
          <button
            onClick={() => handleVote(comment.id, 'DOWNVOTE')}
            disabled={votingCommentId === comment.id}
            className={`comment-vote-btn downvote ${currentUserVote === 'DOWNVOTE' ? 'active' : ''}`}
            title="Downvote"
          >
            👎 {comment.downvotes || 0}
          </button>
          <button
            onClick={toggleReplyForm}
            className={`comment-reply-btn ${showReplyForm ? 'active' : ''}`}
          >
            💬 Reply {comment.repliesCount > 0 && `(${comment.repliesCount})`}
          </button>
          {canDeleteComment(comment) && (
            <button
              onClick={() => handleDeleteComment(comment.id)}
              disabled={deletingCommentId === comment.id}
              className="comment-delete-btn"
              title="Delete comment"
            >
              {deletingCommentId === comment.id ? '...' : '🗑️'}
            </button>
          )}
        </div>

        {showReplyForm && (
          <form onSubmit={handleReplySubmit} className="reply-form" dir="ltr">
            <textarea
              ref={setReplyTextareaRef}
              dir="ltr"
              value={replyContent[comment.id] || ''}
              onChange={(e) => setReplyContent({ ...replyContent, [comment.id]: e.target.value })}
              onKeyDown={handleReplyKeyDown}
              placeholder={`Reply to ${comment.user?.fullName || 'this comment'}... (Press Enter to submit, Shift+Enter for new line)`}
              rows="3"
              className="reply-input"
              style={{ direction: 'ltr', textAlign: 'left' }}
            />
            <div className="reply-form-actions">
              <button
                type="submit"
                disabled={!replyContent[comment.id]?.trim()}
                className="btn-reply-submit"
              >
                Reply
              </button>
              <button
                type="button"
                onClick={() => {
                  setOpenReplyForms(prev => {
                    const newState = { ...prev }
                    delete newState[comment.id]
                    return newState
                  })
                  setReplyContent({ ...replyContent, [comment.id]: '' })
                }}
                className="btn-reply-cancel"
              >
                Cancel
              </button>
            </div>
          </form>
        )}

        {comment.replies && comment.replies.length > 0 && (
          <div className="replies-list">
            {comment.replies.map((reply) => {
              if (!reply || !reply.id) {
                console.warn('Invalid reply:', reply)
                return null
              }
              return <CommentItem key={reply.id} comment={reply} isReply={true} />
            })}
          </div>
        )}
      </div>
    )
  }

  if (loading) {
    return <div className="comment-section-loading">Loading comments...</div>
  }

  if (!postId) {
    return <div className="comment-section-loading" style={{ color: '#dc3545' }}>Error: Post ID is missing</div>
  }

  return (
    <div className="comment-section">
      <h3>Comments ({totalCommentsCount(comments)})</h3>
      
      {!replyingTo && (
        <form onSubmit={handleSubmit} className="comment-form" dir="ltr">
          <textarea
            dir="ltr"
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            onKeyDown={handleMainCommentKeyDown}
            placeholder="Write a comment... (Press Enter to submit, Shift+Enter for new line)"
            rows="3"
            className="comment-input"
            style={{ direction: 'ltr', textAlign: 'left' }}
          />
          <div className="comment-form-actions">
            <button type="submit" disabled={submitting || !newComment.trim()} className="btn-comment-submit">
              {submitting ? 'Posting...' : 'Post Comment'}
            </button>
          </div>
        </form>
      )}

      <div className="comments-list">
        {comments.length === 0 ? (
          <p className="no-comments">No comments yet. Be the first to comment!</p>
        ) : (
          comments.map((comment) => {
            if (!comment || !comment.id) {
              console.warn('Invalid comment:', comment)
              return null
            }
            try {
              return <CommentItem key={comment.id} comment={comment} />
            } catch (error) {
              console.error('Error rendering comment:', error, comment)
              return null
            }
          })
        )}
      </div>
    </div>
  )
}

export default CommentSection
