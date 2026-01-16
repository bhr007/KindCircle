import { useState, useEffect } from 'react'
import api from '../services/api'
import { toast } from 'react-toastify'
import './AwarenessCampaign.css'

const AwarenessCampaign = () => {
  const [articles, setArticles] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchArticles()
  }, [])

  const fetchArticles = async () => {
    try {
      const response = await api.get('/awareness')
      setArticles(response.data)
    } catch (error) {
      toast.error('Failed to load awareness articles')
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="loading">Loading articles...</div>
  }

  return (
    <div className="awareness-campaign">
      <h1>Medical & Seasonal Awareness</h1>
      <div className="articles-container">
        {articles.length === 0 ? (
          <div className="no-articles">No awareness articles available.</div>
        ) : (
          articles.map((article) => (
            <div key={article.id} className="article-card">
              {article.image_url && (
                <img src={article.image_url} alt={article.title} className="article-image" />
              )}
              <div className="article-content">
                <div className="article-category">{article.category}</div>
                <h2>{article.title}</h2>
                <p>{article.content}</p>
                <div className="article-footer">
                  <span className="article-date">
                    {new Date(article.created_at).toLocaleDateString()}
                  </span>
                  <button className="btn btn-primary">Share</button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default AwarenessCampaign
