import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'
import { AuthProvider } from './context/AuthContext'
import Login from './pages/Login'
import Register from './pages/Register'
import VerifyEmail from './pages/VerifyEmail'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import HomePage from './pages/HomePage'
import EmergencyFeed from './pages/EmergencyFeed'
import AwarenessCampaign from './pages/AwarenessCampaign'
import CampaignDetail from './pages/CampaignDetail'
import CreateCampaign from './pages/CreateCampaign'
import Profile from './pages/Profile'
import Settings from './pages/Settings'
import AdminPanel from './pages/AdminPanel'
import PostSubmission from './pages/PostSubmission'
import Inbox from './pages/Inbox'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import './App.css'

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/verify-email" element={<VerifyEmail />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<HomePage />} />
            <Route path="emergency" element={<EmergencyFeed />} />
            <Route path="inbox" element={<Inbox />} />
            <Route path="awareness" element={<AwarenessCampaign />} />
            <Route path="awareness/create" element={<CreateCampaign />} />
            <Route path="awareness/:id" element={<CampaignDetail />} />
            <Route path="profile" element={<Profile />} />
            <Route path="profile/:userId" element={<Profile />} />
            <Route path="settings" element={<Settings />} />
            <Route path="post" element={<PostSubmission />} />
            <Route path="admin" element={<AdminPanel />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
        <ToastContainer position="top-right" autoClose={3000} />
      </Router>
    </AuthProvider>
  )
}

export default App
