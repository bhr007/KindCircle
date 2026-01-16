import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useParams } from 'react-router-dom'
import api from '../services/api'
import PostCard from '../components/PostCard'
import './Profile.css'

const Profile = () => {
  const { user: currentUser } = useAuth()
  const { userId } = useParams()
  const [profile, setProfile] = useState(null)
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [postsLoading, setPostsLoading] = useState(true)

  // Use userId from params if available, otherwise use current user's ID
  const profileUserId = userId ? parseInt(userId) : currentUser?.id

  useEffect(() => {
    if (profileUserId) {
      fetchProfile()
      fetchPosts()
    }
  }, [profileUserId])

  const fetchProfile = async () => {
    try {
      const response = await api.get(`/users/${profileUserId}`)
      setProfile(response.data)
    } catch (error) {
      console.error('Failed to load profile', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchPosts = async () => {
    try {
      setPostsLoading(true)
      const response = await api.get(`/users/${profileUserId}/posts`)
      setPosts(response.data)
    } catch (error) {
      console.error('Failed to load posts', error)
    } finally {
      setPostsLoading(false)
    }
  }

  const refreshPosts = () => {
    fetchPosts()
  }

  if (loading || !profile) {
    return <div className="loading">Loading profile...</div>
  }

  const upvotes = profile.upvotesReceived || profile.upvotes_received || 0
  const downvotes = profile.downvotesReceived || profile.downvotes_received || 0
  const trustRatio = upvotes > 0
    ? ((upvotes / (upvotes + downvotes)) * 100).toFixed(1)
    : 0

  const isOwnProfile = currentUser && profile.id === currentUser.id

  return (
    <div className="profile">
      <div className="profile-header">
        <div className="profile-avatar-container">
          <img
            src={(() => {
              const photoUrl = profile.profilePhotoUrl || profile.profile_photo_url
              if (!photoUrl) return '/default-avatar.png'
              if (photoUrl.startsWith('uploads/')) {
                return `http://localhost:8080/${photoUrl}`
              }
              if (photoUrl.startsWith('http')) return photoUrl
              return photoUrl
            })()}
            alt={profile.fullName || profile.full_name}
            className="profile-avatar"
            onError={(e) => {
              e.target.src = '/default-avatar.png'
            }}
          />
        </div>
        <div className="profile-info">
          <h1>{profile.fullName || profile.full_name}</h1>
          <p className="profile-username">@{profile.username}</p>
          <div className="profile-stats">
            <div className="stat">
              <strong>{profile.currentImpactScore || profile.current_impact_score || 0}</strong>
              <span>Impact Score</span>
            </div>
            <div className="stat">
              <strong>{profile.totalBloodDonations || profile.total_blood_donations || 0}</strong>
              <span>Blood Donations</span>
            </div>
            <div className="stat">
              <strong>{profile.totalSocialWork || profile.total_social_work || 0}</strong>
              <span>Social Works</span>
            </div>
          </div>
        </div>
      </div>

      <div className="profile-details">
        <div className="detail-card">
          <h3>Personal Information</h3>
          <p><strong>Blood Group:</strong> {profile.bloodGroup || profile.blood_group || 'Not specified'}</p>
          <p><strong>City:</strong> {profile.city || 'Not specified'}</p>
          <p><strong>Location:</strong> {profile.location || 'Not specified'}</p>
          {profile.lastDonationDate || profile.last_donation_date ? (
            <p><strong>Last Donated On:</strong> {new Date(profile.lastDonationDate || profile.last_donation_date).toLocaleDateString('en-US', {
              year: 'numeric',
              month: 'long',
              day: 'numeric'
            })}</p>
          ) : (
            <p><strong>Last Donated On:</strong> Never</p>
          )}
        </div>

        <div className="detail-card">
          <h3>Trust Metrics</h3>
          <p><strong>Upvotes:</strong> {profile.upvotesReceived || profile.upvotes_received || 0}</p>
          <p><strong>Downvotes:</strong> {profile.downvotesReceived || profile.downvotes_received || 0}</p>
          <p><strong>Trust Ratio:</strong> {trustRatio}%</p>
        </div>
      </div>

      <div className="profile-posts-section">
        <h2>{isOwnProfile ? 'My Posts' : `${profile.fullName || profile.full_name}'s Posts`}</h2>
        {postsLoading ? (
          <div className="loading">Loading posts...</div>
        ) : posts.length === 0 ? (
          <div className="no-posts">
            <p>{isOwnProfile ? "You haven't created any posts yet." : "This user hasn't created any posts yet."}</p>
          </div>
        ) : (
          <div className="posts-container">
            {posts.map((post) => (
              <PostCard
                key={post.id}
                post={post}
                onUpdate={refreshPosts}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default Profile
