import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'
import { toast } from 'react-toastify'
import './AwarenessCampaign.css'

const CATEGORIES = ['Health', 'Environment', 'Blood', 'Hunger', 'Education', 'Other']

const AwarenessCampaign = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [campaigns, setCampaigns] = useState([])
  const [loading, setLoading] = useState(true)
  const [category, setCategory] = useState('')
  const [status, setStatus] = useState('')
  const [search, setSearch] = useState('')

  useEffect(() => {
    fetchCampaigns()
  }, [category, status, search])

  const fetchCampaigns = async () => {
    try {
      setLoading(true)
      const params = new URLSearchParams()
      if (category) params.set('category', category)
      if (status) params.set('status', status)
      if (search.trim()) params.set('q', search.trim())
      const res = await api.get(`/awareness/campaigns?${params}`)
      setCampaigns(Array.isArray(res.data) ? res.data : [])
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to load campaigns')
      setCampaigns([])
    } finally {
      setLoading(false)
    }
  }

  const formatDates = (start, end) => {
    if (!start || !end) return ''
    const s = new Date(start).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
    const e = new Date(end).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
    return `${s} – ${e}`
  }

  return (
    <div className="awareness-campaign">
      <div className="campaign-header">
        <h1>Awareness Campaigns</h1>
        {user && (
          <button className="btn-create-campaign" onClick={() => navigate('/awareness/create')}>
            + Create Campaign
          </button>
        )}
      </div>

      <div className="filters-bar">
        <input
          type="text"
          placeholder="Search by title..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="search-input"
        />
        <select value={category} onChange={(e) => setCategory(e.target.value)} className="filter-select">
          <option value="">All categories</option>
          {CATEGORIES.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
        <select value={status} onChange={(e) => setStatus(e.target.value)} className="filter-select">
          <option value="">All status</option>
          <option value="ACTIVE">Active</option>
          <option value="COMPLETED">Completed</option>
        </select>
      </div>

      {loading ? (
        <div className="campaign-loading">Loading campaigns...</div>
      ) : campaigns.length === 0 ? (
        <div className="no-campaigns">
          <p>No campaigns found.</p>
          {user && (
            <button className="btn-create-campaign" onClick={() => navigate('/awareness/create')}>
              Create the first campaign
            </button>
          )}
        </div>
      ) : (
        <div className="campaign-grid">
          {campaigns.map((c) => (
            <div
              key={c.id}
              className="campaign-card"
              onClick={() => navigate(`/awareness/${c.id}`)}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => e.key === 'Enter' && navigate(`/awareness/${c.id}`)}
            >
              <span className={`campaign-card__badge campaign-card__badge--${(c.status || '').toLowerCase()}`}>
                {c.status || 'Active'}
              </span>
              <h3 className="campaign-card__title">{c.title}</h3>
              <p className="campaign-card__desc">
                {(() => {
                  const t = (c.shortDescription || c.description || '').slice(0, 120)
                  return t + ((c.shortDescription || c.description || '').length > 120 ? '…' : '')
                })()}
              </p>
              <div className="campaign-card__meta">
                <span className="campaign-card__category">{c.category}</span>
                <span className="campaign-card__organizer">by {c.organizer?.fullName || 'Unknown'}</span>
              </div>
              <div className="campaign-card__footer">
                <span className="campaign-card__dates">{formatDates(c.startDate, c.endDate)}</span>
                <span className="campaign-card__participants">{c.participantsCount ?? 0} participants</span>
                <span className="campaign-card__impact">+{c.impactScoreValue ?? 0} impact</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default AwarenessCampaign
