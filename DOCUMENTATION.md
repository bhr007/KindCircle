# KindCircle - Complete Documentation

## Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [Technology Stack](#technology-stack)
4. [Architecture](#architecture)
5. [Project Structure](#project-structure)
6. [Setup Instructions](#setup-instructions)
7. [Configuration](#configuration)
8. [Database Schema](#database-schema)
9. [API Documentation](#api-documentation)
10. [Frontend Components](#frontend-components)
11. [Key Features Explained](#key-features-explained)
12. [Development Guide](#development-guide)
13. [Deployment](#deployment)

---

## Overview

**KindCircle** is a humanitarian social media platform that connects blood donors and people engaged in social work. The platform uses an **Impact Score** system as a social currency where users earn points through verified good works and can spend them to receive help, particularly blood donations.

### Core Concept

- **Earn Impact Points**: Users earn points by performing verified humanitarian activities (blood donation, feeding people, donating clothes, tree plantation)
- **Spend Impact Points**: Users can request help (especially blood) by spending their earned points
- **Verified Activities**: All submissions require admin verification with photo/video proof
- **Community Building**: Social feed features allow users to share their work and engage with the community
- **Emergency System**: Real-time blood request matching system with GPS location sharing

---

## Features

### 1. User Management
- User registration with email verification
- JWT-based authentication
- Password reset functionality
- Profile management with photo upload
- Role-based access control (USER/ADMIN)

### 2. Impact Score System
**Earning Points:**
- Blood donation: +100 points
- Feeding helpless people: +40 points
- Winter clothes donation: +30 points
- Tree plantation: +20 points

**Spending Points:**
- 1 bag of blood: -100 points
- Minimum 100 points required per blood bag request

### 3. News Feed
- Social feed showcasing verified humanitarian work
- Multiple sorting options (Recent, Trending, Impactful, Top)
- Post interactions:
  - Upvote/Downvote posts
  - Comment on posts (with nested replies)
  - Upvote/Downvote comments
- Media support (photos/videos) for posts
- Admin verification workflow

### 4. Emergency Blood Request System
- Create urgent blood requests
- Blood group-based filtering (only matching donors see requests)
- Real-time responder matching
- GPS location sharing during active requests
- Chat/messaging between requester and donor
- Match confirmation workflow:
  - Requester selects donor
  - Both parties confirm match
  - Donation completion tracking
- Inbox system with:
  - Active conversations
  - Archived conversations (read-only)
  - Conversation deletion
  - New message indicators

### 5. Awareness Campaigns
- Admin-managed medical and seasonal awareness articles
- Public access to educational content

### 6. User Profiles
- Complete user statistics:
  - Impact Score and transaction history
  - Total blood donations
  - Total social work count
  - Upvotes/Downvotes received
  - Response count (emergency requests responded to)
  - Successful donations count
  - Trust ratio (calculated from responses and successful donations)
- View user's posts
- Profile photo support

### 7. Notifications
- Real-time notifications for:
  - Post upvotes/downvotes
  - Comments and replies
  - Comment votes
  - Match confirmations
  - Donation updates
- Unread notification count badge
- Mark as read functionality

### 8. Admin Panel
- Verify/reject social work post submissions
- Manage awareness campaigns
- View pending verifications

### 9. New Content Indicators
- Badge indicators showing new posts in News Feed
- Badge indicators showing new emergency requests
- Indicators reset when user views the respective feed

---

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Security**: Spring Security with JWT
- **ORM**: Spring Data JPA / Hibernate
- **Database**: MySQL 8.0+
- **Build Tool**: Maven 3.6+
- **Email**: Spring Mail (SMTP)

### Frontend
- **Framework**: React 18.2.0
- **Build Tool**: Vite 5.0.8
- **Routing**: React Router DOM 6.20.0
- **HTTP Client**: Axios 1.6.2
- **Notifications**: React Toastify 9.1.3
- **Icons**: React Icons 4.12.0
- **Maps**: Leaflet 1.9.4, React-Leaflet 4.2.1
- **Date Utilities**: date-fns 2.30.0

### Database
- **RDBMS**: MySQL 8.0+
- **Migration**: SQL scripts in `database/` directory

---

## Architecture

### System Architecture
```
┌─────────────┐
│   React     │  Frontend (Port 3000)
│  Frontend   │
└──────┬──────┘
       │ HTTP/REST API
       │ JWT Authentication
┌──────▼──────┐
│ Spring Boot │  Backend (Port 8080)
│   REST API  │
└──────┬──────┘
       │ JPA/Hibernate
┌──────▼──────┐
│    MySQL    │  Database (Port 3306)
│  Database   │
└─────────────┘
```

### Key Components

**Backend Layers:**
1. **Controllers**: Handle HTTP requests, validate input, return responses
2. **Services**: Business logic layer
3. **Repositories**: Data access layer (JPA repositories)
4. **Models**: Entity classes representing database tables
5. **DTOs**: Data Transfer Objects for API requests/responses
6. **Security**: JWT authentication and authorization

**Frontend Layers:**
1. **Pages**: Route components (Login, Register, HomePage, etc.)
2. **Components**: Reusable UI components (PostCard, CommentSection, Layout)
3. **Services**: API integration (axios-based API service)
4. **Context**: Global state management (AuthContext)
5. **Utils**: Helper functions

---

## Project Structure

```
KindCircle/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/kindcircle/
│   │   │   │   ├── config/          # Security, Web configuration
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── model/           # JPA entities
│   │   │   │   ├── repository/      # JPA repositories
│   │   │   │   ├── security/        # JWT filters
│   │   │   │   └── service/         # Business logic
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/                    # Unit tests
│   ├── pom.xml                      # Maven dependencies
│   └── uploads/                     # Uploaded media files
│
├── frontend/
│   ├── src/
│   │   ├── components/              # React components
│   │   ├── context/                 # React contexts
│   │   ├── pages/                   # Page components
│   │   ├── services/                # API services
│   │   ├── utils/                   # Utility functions
│   │   ├── App.jsx                  # Main app component
│   │   └── main.jsx                 # Entry point
│   ├── package.json                 # npm dependencies
│   └── vite.config.js               # Vite configuration
│
├── database/
│   ├── schema.sql                   # Main database schema
│   ├── add_email_verification.sql   # Email verification migration
│   ├── add_chat_archive_delete.sql  # Chat archive/delete migration
│   └── ...                          # Other migrations
│
└── README.md                        # Quick start guide
```

---

## Setup Instructions

### Prerequisites

- **Node.js**: v16 or higher
- **Java JDK**: 17 or higher
- **MySQL**: 8.0 or higher
- **Maven**: 3.6 or higher
- **Git**: For cloning the repository

### Step 1: Clone Repository

```bash
git clone <repository-url>
cd KindCircle
```

### Step 2: Database Setup

1. Create MySQL database:
```bash
mysql -u root -p
```

2. Run the main schema:
```bash
cd database
mysql -u root -p < schema.sql
```

3. Apply migrations (if needed):
```bash
mysql -u root -p kindcircle < add_email_verification.sql
mysql -u root -p kindcircle < create_chats_table.sql
mysql -u root -p kindcircle < create_comment_votes_table.sql
mysql -u root -p kindcircle < create_notifications_table.sql
mysql -u root -p kindcircle < add_chat_archive_delete.sql
mysql -u root -p kindcircle < add_last_donation_date.sql
```

### Step 3: Backend Configuration

1. Navigate to backend directory:
```bash
cd backend
```

2. Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/kindcircle?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Secret (change in production!)
jwt.secret=your-secret-key-minimum-256-bits

# Email Configuration (for email verification)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

3. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

Backend will run on `http://localhost:8080`

### Step 4: Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. (Optional) Create `.env` file for custom API URL:
```env
VITE_API_URL=http://localhost:8080/api
```

4. Start development server:
```bash
npm run dev
# or
npm start
```

Frontend will run on `http://localhost:3000` (or port shown in terminal)

---

## Configuration

### Backend Configuration (`application.properties`)

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/kindcircle?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# File Upload
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# JWT
jwt.secret=kindcircle-secret-key-change-in-production-minimum-256-bits
jwt.expiration=86400000  # 24 hours in milliseconds

# CORS
spring.web.cors.allowed-origins=http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Logging
logging.level.com.kindcircle=INFO
logging.level.org.springframework.security=WARN
logging.level.org.hibernate=WARN
```

### Frontend Configuration

API service is configured in `frontend/src/services/api.js`. The base URL defaults to `http://localhost:8080/api`.

---

## Database Schema

### Core Tables

#### `users`
Stores user accounts and profile information.

**Key Fields:**
- `id`: Primary key
- `username`, `email`, `phone_number`: Unique identifiers
- `password_hash`: Bcrypt hashed password
- `full_name`, `blood_group`, `city`, `location`: Profile data
- `profile_photo_url`: Profile picture path
- `current_impact_score`: Current points balance
- `total_blood_donations`, `total_social_work`: Statistics
- `upvotes_received`, `downvotes_received`: Engagement metrics
- `role`: USER or ADMIN
- `is_active`: Account status

#### `impact_score_ledger`
Transaction history for Impact Score.

**Key Fields:**
- `transaction_type`: EARNED, SPENT, ADJUSTED
- `activity_type`: Activity that triggered the transaction
- `points`: Amount of points (+ or -)
- `reference_id`, `reference_type`: Link to related entity

#### `social_work_posts`
User-submitted social work posts (pending approval).

**Key Fields:**
- `activity_type`: BLOOD_DONATION, FEEDING, WINTER_CLOTHES, TREE_PLANTATION
- `verification_status`: PENDING, APPROVED, REJECTED
- `impact_score_earned`: Points awarded after approval
- `verified_by`, `verified_at`: Admin verification info

#### `blood_requests`
Emergency blood requests.

**Key Fields:**
- `blood_group`: Required blood type
- `bags_required`: Number of bags needed
- `hospital_name`, `hospital_address`, `city`: Location info
- `urgency_level`: LOW, MEDIUM, HIGH, CRITICAL
- `status`: OPEN, MATCHED, IN_PROGRESS, COMPLETED, CANCELLED
- `impact_score_required`: Points needed to request
- `matched_donor_id`: Selected donor
- `location_sharing_enabled`: GPS sharing flag

#### `blood_donations`
Completed blood donation records.

**Key Fields:**
- `donation_date`: Date of donation
- `status`: PENDING_CONFIRMATION, CONFIRMED, COMPLETED
- `donor_confirmed`, `requester_confirmed`: Confirmation flags
- `impact_score_donor`: Points earned by donor (+100)
- `impact_score_requester`: Points spent by requester (-100)

#### `chats`
Private messaging between requester and donor.

**Key Fields:**
- `request_id`: Related blood request
- `sender_id`, `receiver_id`: Message participants
- `message`: Message content
- `is_read`: Read status
- `is_archived`: Archive status
- `is_deleted`: Soft delete flag

#### `comments`
Comments on social work posts (supports nested replies).

**Key Fields:**
- `post_id`: Related post
- `parent_comment_id`: For nested replies (NULL for top-level)
- `content`: Comment text

#### `comment_votes`
Votes on comments.

**Key Fields:**
- `comment_id`: Related comment
- `vote_type`: UPVOTE or DOWNVOTE

#### `votes`
Votes on posts.

**Key Fields:**
- `post_id`: Related post
- `vote_type`: UPVOTE or DOWNVOTE

#### `notifications`
User notifications.

**Key Fields:**
- `user_id`: Notification recipient
- `type`: Notification type (COMMENT, UPVOTE, etc.)
- `message`: Notification text
- `is_read`: Read status
- `related_post_id`, `related_user_id`: Context

#### `location_sessions`
GPS location sharing sessions.

**Key Fields:**
- `request_id`: Related blood request
- `user_id`: User sharing location
- `latitude`, `longitude`: GPS coordinates
- `updated_at`: Last location update

### Relationships

- Users → Posts (One-to-Many)
- Users → Blood Requests (One-to-Many)
- Posts → Comments (One-to-Many)
- Posts → Votes (One-to-Many)
- Comments → CommentVotes (One-to-Many)
- Blood Requests → Chats (One-to-Many)
- Blood Requests → Blood Donations (One-to-One)

---

## API Documentation

Base URL: `http://localhost:8080/api`

All endpoints require JWT authentication (except registration, login, and public endpoints). Include the token in the `Authorization` header:
```
Authorization: Bearer <token>
```

### Authentication Endpoints

#### `POST /api/auth/register`
Register a new user.

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "phoneNumber": "+1234567890",
  "fullName": "John Doe",
  "bloodGroup": "O+",
  "city": "New York"
}
```

**Response:** `AuthResponse` with JWT token

#### `POST /api/auth/login`
User login.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:** `AuthResponse` with JWT token

#### `POST /api/auth/logout`
Logout (client-side token removal).

#### `POST /api/auth/verify-email`
Verify email with token.

**Request Body:**
```json
{
  "token": "verification-token"
}
```

#### `POST /api/auth/resend-verification`
Resend verification email.

#### `POST /api/auth/forgot-password`
Request password reset.

#### `POST /api/auth/reset-password`
Reset password with token.

#### `GET /api/auth/me`
Get current authenticated user.

---

### News Feed Endpoints

#### `GET /api/feed`
Get approved posts from news feed.

**Query Parameters:**
- `sort` (optional): `recent`, `trending`, `impactful`, `top` (default: `recent`)

**Response:** `List<PostResponse>`

#### `POST /api/feed`
Create a new post (requires authentication).

**Request:** `multipart/form-data`
- `post`: JSON string with `PostRequest`
- `mediaFiles`: Array of image/video files (optional)

**PostRequest:**
```json
{
  "activityType": "BLOOD_DONATION",
  "title": "Blood Donation Drive",
  "description": "Donated blood today",
  "location": "Red Cross Center",
  "activityDate": "2024-01-15"
}
```

#### `GET /api/feed/{postId}/comments`
Get comments for a post.

**Response:** `List<CommentResponse>` (nested tree structure)

#### `POST /api/feed/{postId}/comments`
Add a comment to a post.

**Request Body:**
```json
{
  "content": "Great work!",
  "parentCommentId": null  // null for top-level, ID for reply
}
```

#### `DELETE /api/feed/comments/{commentId}`
Delete a comment (comment owner or post owner only).

#### `POST /api/feed/{postId}/vote`
Vote on a post.

**Request Body:**
```json
{
  "voteType": "UPVOTE"  // or "DOWNVOTE"
}
```

#### `POST /api/feed/comments/{commentId}/vote`
Vote on a comment.

**Request Body:**
```json
{
  "voteType": "UPVOTE"  // or "DOWNVOTE"
}
```

#### `DELETE /api/feed/{postId}`
Delete a post (post owner only).

---

### Emergency Feed Endpoints

#### `GET /api/emergency`
Get open blood requests (filtered by user's blood group).

**Response:** `List<BloodRequestResponse>`

#### `POST /api/emergency`
Create a blood request (requires authentication and minimum Impact Score).

**Request Body:**
```json
{
  "bloodGroup": "O+",
  "bagsRequired": 1,
  "hospitalName": "City Hospital",
  "hospitalAddress": "123 Main St",
  "city": "New York",
  "urgencyLevel": "HIGH",
  "description": "Urgent need for blood"
}
```

#### `POST /api/emergency/{requestId}/respond`
Respond to a blood request (donor).

**Response:** Success message

#### `POST /api/emergency/{requestId}/confirm-match/{donorId}`
Confirm match between requester and donor (requester only).

#### `POST /api/emergency/donations/{donationId}/confirm`
Confirm donation (donor or requester).

#### `POST /api/emergency/donations/{donationId}/complete`
Mark donation as completed.

#### `GET /api/emergency/{requestId}/responders`
Get list of responders for a request (requester only).

#### `DELETE /api/emergency/{requestId}`
Delete a blood request (requester only, OPEN or CANCELLED status).

---

### Inbox/Messaging Endpoints

#### `GET /api/emergency/inbox/conversations`
Get user's active conversations.

**Response:** `List<ConversationResponse>`

#### `GET /api/emergency/inbox/archived`
Get user's archived conversations.

**Response:** `List<ConversationResponse>`

#### `GET /api/emergency/inbox/{requestId}/messages`
Get messages for a conversation.

**Response:** `List<Chat>`

#### `POST /api/emergency/{requestId}/chat`
Send a message in a conversation.

**Request Body:**
```json
{
  "message": "Hello, I'm available to donate."
}
```

#### `POST /api/emergency/inbox/{requestId}/mark-read`
Mark all messages in a conversation as read.

#### `POST /api/emergency/inbox/{requestId}/archive`
Archive a conversation (read-only after archiving).

#### `DELETE /api/emergency/inbox/{requestId}/conversation`
Delete a conversation (soft delete).

#### `DELETE /api/emergency/inbox/messages/{messageId}`
Delete a specific message (soft delete).

---

### Location Sharing Endpoints

#### `POST /api/emergency/{requestId}/location`
Update user's GPS location.

**Request Body:**
```json
{
  "latitude": 40.7128,
  "longitude": -74.0060
}
```

#### `GET /api/emergency/{requestId}/location`
Get location session for a request.

---

### User Endpoints

#### `GET /api/users/{id}`
Get user profile by ID.

**Response:** `UserProfileResponse` (includes trust ratio, response count, etc.)

#### `GET /api/users/me`
Get current user's profile.

**Response:** `User`

#### `GET /api/users/{id}/posts`
Get all posts by a user.

**Response:** `List<PostResponse>`

#### `PUT /api/users/me/profile`
Update user profile.

**Request Body:**
```json
{
  "fullName": "John Doe",
  "bloodGroup": "O+",
  "city": "New York",
  "location": "123 Main St"
}
```

#### `PUT /api/users/me/profile-picture`
Update profile picture.

**Request:** `multipart/form-data`
- `file`: Image file

---

### Notification Endpoints

#### `GET /api/notifications`
Get all notifications for current user.

**Response:** `List<Notification>`

#### `GET /api/notifications/unread`
Get unread notifications.

**Response:** `List<Notification>`

#### `GET /api/notifications/unread/count`
Get unread notification count.

**Response:**
```json
{
  "count": 5
}
```

#### `PUT /api/notifications/{notificationId}/read`
Mark a notification as read.

#### `PUT /api/notifications/read-all`
Mark all notifications as read.

---

### Admin Endpoints

**Note:** All admin endpoints require `ADMIN` role.

#### `GET /api/admin/pending`
Get all pending post verifications.

**Response:** `List<PostResponse>`

#### `POST /api/admin/verify/{postId}`
Verify or reject a post.

**Request Body:**
```json
{
  "approved": true,
  "rejectionReason": null  // Required if approved is false
}
```

---

## Frontend Components

### Pages

- **Login.jsx**: User login page
- **Register.jsx**: User registration
- **HomePage.jsx**: News feed with posts
- **EmergencyFeed.jsx**: Emergency blood requests feed
- **PostSubmission.jsx**: Create new social work post
- **Profile.jsx**: User profile view
- **Settings.jsx**: User settings and profile edit
- **Inbox.jsx**: Conversation/messaging interface with map
- **AdminPanel.jsx**: Admin post verification panel
- **AwarenessCampaign.jsx**: Public awareness articles
- **VerifyEmail.jsx**: Email verification page
- **ForgotPassword.jsx**: Password reset request
- **ResetPassword.jsx**: Password reset form

### Components

- **Layout.jsx**: Main app layout with navigation, new content badges
- **PostCard.jsx**: Individual post display with voting, comments
- **CommentSection.jsx**: Comment thread with nested replies
- **ProtectedRoute.jsx**: Route guard for authenticated routes

### Services

- **api.js**: Axios-based API service with JWT token injection

### Context

- **AuthContext.jsx**: Global authentication state management

---

## Key Features Explained

### Impact Score System

The Impact Score acts as a social currency:

1. **Earning**: Users submit proof of humanitarian work, admins verify, and points are awarded
2. **Spending**: Users can request help (blood) by spending points
3. **Balance**: Users must maintain a minimum balance (100 points) to request blood

### Emergency Request Matching

1. User creates blood request (must have ≥100 points × bags required)
2. Request is visible only to users with matching blood group
3. Matching donors can respond
4. Requester selects a donor
5. Both parties confirm the match
6. Location sharing can be enabled
7. After donation, status is updated and Impact Scores are adjusted

### Post Verification Workflow

1. User submits post with media proof
2. Post status: `PENDING`
3. Admin reviews and verifies/rejects
4. If approved:
   - Status: `APPROVED`
   - Impact Score added to user
   - Post appears in News Feed

### Comment System

- Supports nested replies (parent-child relationships)
- Vote system (upvote/downvote)
- Comment owners and post owners can delete comments
- Vote counts displayed in real-time

### Location Sharing

- Enabled after match confirmation
- Both parties share GPS coordinates
- Updates every 5 seconds
- Displayed on interactive map (Leaflet) in Inbox

### Trust Ratio Calculation

Trust ratio is calculated as:
```
trustRatio = (successfulDonations / max(responseCount, 1)) * 100
```

This metric helps users assess donor reliability.

---

## Development Guide

### Backend Development

#### Running in Development Mode
```bash
cd backend
mvn spring-boot:run
```

#### Running Tests
```bash
mvn test
```

#### Adding a New Endpoint

1. Create DTOs in `dto/` package
2. Add service method in appropriate service class
3. Create controller endpoint in `controller/` package
4. Update security config if authentication/authorization needed

#### Database Migrations

1. Create SQL script in `database/` directory
2. Run manually: `mysql -u root -p kindcircle < database/new_migration.sql`
3. Or use `spring.jpa.hibernate.ddl-auto=update` for schema changes

### Frontend Development

#### Running in Development Mode
```bash
cd frontend
npm run dev
```

#### Adding a New Page

1. Create component in `src/pages/`
2. Add route in `App.jsx`
3. Update navigation in `Layout.jsx` if needed

#### API Integration

Use the `api` service from `services/api.js`:

```javascript
import api from '../services/api'

// GET request
const response = await api.get('/feed')

// POST request
const response = await api.post('/feed', data)

// With authentication (automatic via interceptor)
const response = await api.get('/users/me')
```

### Code Style

- **Backend**: Follow Java naming conventions
- **Frontend**: Use camelCase for variables, PascalCase for components
- **SQL**: Use snake_case for database columns

---

## Deployment

### Backend Deployment

1. Build JAR file:
```bash
cd backend
mvn clean package
```

2. Run JAR:
```bash
java -jar target/kindcircle-backend-1.0.0.jar
```

3. Environment variables (use system properties or environment variables):
   - Database credentials
   - JWT secret (use strong secret in production!)
   - Email SMTP credentials

### Frontend Deployment

1. Build for production:
```bash
cd frontend
npm run build
```

2. Deploy `dist/` folder to static hosting (Netlify, Vercel, etc.)

3. Configure environment variables:
   - `VITE_API_URL`: Backend API URL

### Database Deployment

1. Create production database
2. Run migrations in order:
   - `schema.sql`
   - All migration scripts

3. Configure backups (recommended)

### Security Checklist

- [ ] Change JWT secret to strong random value (minimum 256 bits)
- [ ] Use HTTPS in production
- [ ] Configure CORS for production domain only
- [ ] Set up database backups
- [ ] Use environment variables for sensitive config
- [ ] Enable rate limiting
- [ ] Review and test all authentication flows
- [ ] Validate all user inputs
- [ ] Set up monitoring and logging

---

## Troubleshooting

### Common Issues

**Backend won't start:**
- Check MySQL is running
- Verify database credentials in `application.properties`
- Check port 8080 is not in use

**Frontend can't connect to backend:**
- Verify backend is running on port 8080
- Check CORS configuration
- Verify API URL in frontend config

**Database connection errors:**
- Ensure MySQL is running
- Check database name exists
- Verify user permissions

**JWT authentication failing:**
- Check token is included in Authorization header
- Verify token hasn't expired
- Check JWT secret matches between token generation and validation

---

## License

This project is for educational purposes.

---

## Contributing

This is an academic project. For questions or issues, please contact the development team.

---

**Last Updated**: January 2025
**Version**: 1.0.0
