import { useState, useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import api from '../services/api'
import { toast } from 'react-toastify'
import PostCard from '../components/PostCard'
import './HomePage.css'

const HomePage = () => {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [sortBy, setSortBy] = useState('recent')
  const postRefs = useRef({})

  useEffect(() => {
    fetchPosts()
  }, [sortBy])

  useEffect(() => {
    // Check if we need to scroll to a specific post and open comments
    const postId = searchParams.get('postId')
    const openComments = searchParams.get('openComments') === 'true'
    
    if (postId && posts.length > 0) {
      // Wait for posts to render, then scroll
      setTimeout(() => {
        const postElement = postRefs.current[postId]
        if (postElement) {
          postElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
          
          // Trigger opening comments if requested (this will be handled by PostCard)
          if (openComments) {
            // The PostCard will handle opening comments based on the prop
          }
          
          // Clean up URL params after scrolling
          setSearchParams({})
        }
      }, 300)
    }
  }, [posts, searchParams, setSearchParams])

  const fetchPosts = async (preserveScroll = false) => {
    try {
      // Save scroll position if requested
      const scrollPosition = preserveScroll ? window.scrollY || document.documentElement.scrollTop : null
      
      // Only show loading state on initial load, not when refreshing
      if (!preserveScroll) {
        setLoading(true)
      }
      
      const response = await api.get(`/feed?sort=${sortBy}`)
      console.log('Fetched posts response:', response)
      console.log('Fetched posts data:', response.data)
      if (response.data && Array.isArray(response.data)) {
        setPosts(response.data)
        
        // Restore scroll position after DOM updates
        if (preserveScroll && scrollPosition !== null) {
          // Use requestAnimationFrame to ensure DOM has updated
          requestAnimationFrame(() => {
            requestAnimationFrame(() => {
              window.scrollTo({
                top: scrollPosition,
                behavior: 'auto' // Instant scroll, not smooth
              })
            })
          })
        }
      } else {
        console.warn('Unexpected response format:', response.data)
        setPosts([])
      }
    } catch (error) {
      console.error('Failed to load posts:', error)
      console.error('Error response:', error.response)
      toast.error(error.response?.data?.message || 'Failed to load posts')
      setPosts([])
    } finally {
      setLoading(false)
    }
  }

  const refreshPosts = () => {
    fetchPosts(true) // Preserve scroll position when refreshing
  }

  if (loading) {
    return <div className="loading">Loading posts...</div>
  }

  return (
    <div className="homepage">
      <div className="feed-header">
        <h1>News Feed</h1>
        <div className="header-actions">
          <div className="sort-options">
            <button
              className={sortBy === 'recent' ? 'active' : ''}
              onClick={() => setSortBy('recent')}
            >
              Most Recent
            </button>
            <button
              className={sortBy === 'trending' ? 'active' : ''}
              onClick={() => setSortBy('trending')}
            >
              Trending
            </button>
            <button
              className={sortBy === 'votes' ? 'active' : ''}
              onClick={() => setSortBy('votes')}
            >
              Top Voted
            </button>
            <button
              className={sortBy === 'impactful' ? 'active' : ''}
              onClick={() => setSortBy('impactful')}
            >
              Most Impactful
            </button>
          </div>
          <button
            className="btn-create-post"
            onClick={() => navigate('/post')}
          >
            + Create Post
          </button>
        </div>
      </div>

      <div className="posts-container">
        {posts.length === 0 ? (
          <div className="no-posts">
            <p>No posts yet. Be the first to share your good work!</p>
            <button
              className="btn-create-post"
              onClick={() => navigate('/post')}
            >
              Create Your First Post
            </button>
          </div>
        ) : (
          posts.map((post) => {
            const postId = searchParams.get('postId')
            const openComments = searchParams.get('openComments') === 'true'
            const shouldOpenComments = postId === String(post.id) && openComments
            
            return (
              <div
                key={post.id}
                ref={(el) => {
                  if (el) postRefs.current[post.id] = el
                }}
              >
                <PostCard
                  post={post}
                  onUpdate={refreshPosts}
                  autoOpenComments={shouldOpenComments}
                />
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}

export default HomePage
