# Product

## Vision

Second Serving is a platform that connects people with surplus
food to individuals nearby who can use it.

The goal is to reduce food waste while making local food sharing simple,
fast, and frictionless.

## Target Users

### Primary Users

- Individuals with surplus food
- Individuals seeking nearby free food

### Secondary Users (Future Expansion)

- Restaurants
- Food banks
- Community organizations
- Event organizers

---

## Core Value Proposition

- Post surplus food in under 30 seconds
- Discover nearby listings within a chosen radius
- Claim listings instantly
- Reduce food waste at a local level

The primary differentiator is location-based discovery powered by
geospatial search.

---

## MVP Scope

### Must Have (MVP)

- User registration and authentication
- Create food listing
- Attach geospatial location to listing
- Search listings by proximity (radius-based)
- View listing details
- Claim listing
- Prevent double-claiming
- Mark listing as completed
- Exclude completed or expired listings from search results

### Post-MVP (Future Features)

- Ratings / reputation system
- Direct messaging between users
- Image uploads
- Push/email notifications
- Listing expiration automation
- Admin moderation tools

---

## Requirements

### Functional Requirements

The system must:

- Allow users to register and authenticate.
- Allow authenticated users to create food listings.
- Store a geospatial location for each listing.
- Allow only one location for pickup per listing
- Allow users to search for listings within a configurable radius.
- Return listings sorted by distance.
- Allow users to claim/request a listing.
- Allow users to view the listings they have claimed.
- Multiple users can request the same listing
- Allow listing owners to mark listings as completed.
- Allow listing owners to edit the quantity amount (as users take the item)
- Exclude completed or expired listings from active search results.
- Maintain audit timestamps for major entities (created_at, updated_at).

---

### Non-Functional Requirements

- Database schema changes must be version-controlled via Flyway.
- The backend must support concurrent users without inconsistent state.
- The system must be deployable via Docker.

---

### Constraints & Assumptions

- No payments are handled.
- No delivery logistics are provided.
- Communication between users happens outside the platform (MVP).
- Users are responsible for food safety and pickup coordination.

---

## Core User Flows

### Flow 1: Post Food

1. User logs in
2. User creates a listing
3. User enters title and description
4. User sets pickup location
5. Listing becomes visible to nearby users

---

### Flow 2: Discover & Claim Food

1. User logs in
2. User searches within a chosen radius
3. System returns nearby listings sorted by distance
4. User views listing details
5. User claims listing
6. Listing is marked as claimed and removed from public search

---

### Flow 3: Complete Listing

1. Pickup occurs
2. Owner marks listing as completed
3. Listing becomes archived

---

## Key Product Decisions

- Location stored as GEOGRAPHY(Point, 4326)
- Distance-based filtering using spatial queries
- Listings are time-sensitive
- Claiming must be atomic (transactionally safe)
- Search prioritizes proximity over recency (MVP)

---

## Risks & Considerations

- Abuse or spam listings
- Food safety concerns
- No-show pickups
- Scaling spatial queries with increased listings

---

## Out of Scope (MVP)

- Payment processing
- In-app delivery coordination
- Real-time chat
- Complex analytics dashboards
- Social networking features