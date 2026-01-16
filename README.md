# KindCircle - Humanitarian Social Platform

KindCircle is a social media-style platform for blood donors and people who actively help humanity. The platform uses an **Impact Score** system as social currency, where users earn points through verified good works and can spend them to receive help.

## Project Structure

```
KindCircle/
├── frontend/          # React application
├── backend/           # Spring Boot REST API
├── database/          # MySQL schema and migrations
└── README.md          # This file
```

## Features

### Core Features
- **Impact Score System**: Earn points through verified humanitarian work
- **News Feed**: Social feed showcasing verified good works
- **Emergency Feed**: Urgent blood request system with real-time matching
- **Awareness Campaigns**: Admin-managed medical and seasonal awareness articles
- **User Profiles**: Complete profiles with Impact Score, donation history, and trust metrics
- **Verification System**: Admin-verified submissions with photo/video proof

### Impact Score Rules
- **Earning**:
  - Blood donation: +100 points
  - Feeding helpless people: +40 points
  - Winter clothes donation: +30 points
  - Tree plantation: +20 points
- **Spending**:
  - 1 bag of blood: -100 points
  - Minimum 100 points required per blood bag request

## Technology Stack

- **Frontend**: React.js
- **Backend**: Spring Boot (Java)
- **Database**: MySQL

## Prerequisites

- Node.js (v16 or higher)
- Java JDK 17 or higher
- MySQL 8.0 or higher
- Maven 3.6+

## Setup Instructions

### 1. Database Setup

```bash
cd database
mysql -u root -p < schema.sql
```

### 2. Backend Setup

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will run on `http://localhost:8080`

### 3. Frontend Setup

```bash
cd frontend
npm install
npm start
```

The frontend will run on `http://localhost:3000`

## Environment Variables

### Backend (.env or application.properties)
- `DB_URL`: MySQL connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: Secret key for JWT tokens

### Frontend (.env)
- `REACT_APP_API_URL`: Backend API URL (default: http://localhost:8080/api)

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### News Feed
- `GET /api/feed` - Get news feed posts
- `POST /api/feed` - Create new post (admin verified)
- `GET /api/feed/{id}` - Get specific post

### Emergency Feed
- `GET /api/emergency` - Get emergency blood requests
- `POST /api/emergency` - Create emergency request
- `POST /api/emergency/{id}/respond` - Respond to emergency

### Impact Score
- `GET /api/impact-score` - Get user's Impact Score
- `GET /api/impact-score/history` - Get Impact Score transaction history

### Admin
- `POST /api/admin/verify/{id}` - Verify submission
- `POST /api/admin/awareness` - Create awareness article
- `GET /api/admin/pending` - Get pending verifications

## Database Schema

Key tables:
- `users` - User accounts and profiles
- `impact_score_ledger` - Impact Score transactions
- `blood_requests` - Emergency blood requests
- `blood_donations` - Completed blood donations
- `social_work_posts` - Verified social work submissions
- `media_proofs` - Photo/video proofs
- `comments` - Post comments
- `votes` - Upvotes/downvotes
- `awareness_articles` - Campaign articles
- `chats` - Private messaging
- `location_sessions` - Location sharing sessions

## Development

### Running Tests
```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm test
```

## License

This project is for educational purposes.
