# Airbnb Clone — Backend

REST API for the Airbnb Clone project. Built with Spring Boot 3, secured with Auth0 OAuth2, and backed by PostgreSQL with Liquibase migrations.

---

## Tech Stack

| | |
|--|--|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Build Tool | Maven |
| Database | PostgreSQL 16 |
| Migrations | Liquibase |
| Auth | Auth0 / Okta OAuth2 + JWT |
| Mapping | MapStruct |
| Containerization | Docker |

---

## Features

- JWT-based OAuth2 authentication via Auth0
- Landlord and Tenant role separation (`@PreAuthorize`)
- Property listings with multi-image upload (stored as blobs)
- Booking system with date conflict checking
- Geo-based and category-based listing search
- Audit trail on all entities (created/modified dates)
- Database schema managed by Liquibase

---

## Project Structure

```
src/main/java/com/mostafa/airbnbbackend/
├── booking/
│   ├── controller/    BookingController
│   ├── service/       BookingService
│   ├── entity/        Booking
│   ├── dto/           NewBookingDTO, BookedListingDTO, BookedDateDTO
│   ├── mapper/        BookingMapper
│   └── repository/    BookingRepository
├── listing/
│   ├── controller/    LandlordController, TenantController
│   ├── service/       LandlordService, TenantService, PictureService
│   ├── entity/        Listing, ListingPicture
│   ├── dto/           SaveListingDTO, DisplayCardListingDTO, ...
│   ├── enums/         BookingCategory
│   ├── mapper/        ListingMapper, ListingPictureMapper
│   └── repository/    ListingRepository, ListingPictureRepository
├── user/
│   ├── controller/    AuthResourceController
│   ├── service/       UserService
│   ├── entity/        User, Authority
│   ├── dto/           ReadUserDTO
│   ├── mapper/        UserMapper
│   └── repository/    UserRepository
├── config/            SecurityConfiguration, DatabaseConfiguration, SecurityUtils
├── shared/            State, StatusNotification
└── AirbnbBackendApplication.java
```

---

## API Endpoints

### Auth — `/api/auth`
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/auth/get-authenticated-user` | Get current logged-in user | Public |
| POST | `/api/auth/logout` | Logout and return redirect URL | Public |

### Tenant Listings — `/api/tenant-listing`
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/tenant-listing/get-all-by-category` | Browse listings by category (paginated) | Public |
| GET | `/api/tenant-listing/get-one` | Get single listing details | Public |
| POST | `/api/tenant-listing/search` | Search with filters (location, dates, guests) | Public |

### Landlord Listings — `/api/landlord-listing`
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/landlord-listing/create` | Create property (multipart: JSON + images) | LANDLORD |
| GET | `/api/landlord-listing/get-all` | List own properties | LANDLORD |
| DELETE | `/api/landlord-listing/delete` | Delete property | LANDLORD |

### Bookings — `/api/booking`
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/booking/create` | Book a property | Authenticated |
| GET | `/api/booking/check-availability` | Get unavailable dates for a listing | Public |
| GET | `/api/booking/get-booked-listing` | Get tenant's bookings | Authenticated |
| GET | `/api/booking/get-booked-listing-for-landlord` | Get landlord's reservations | LANDLORD |
| DELETE | `/api/booking/cancel` | Cancel a booking | Authenticated |

---

## Database Schema

Managed by **Liquibase** under `src/main/resources/db/changelog/`:

| Table | Description |
|-------|-------------|
| `airbnb_backend.airbnb_user` | User accounts (synced from Auth0) |
| `airbnb_backend.authority` | Roles (ROLE_LANDLORD, ROLE_TENANT) |
| `airbnb_backend.user_authority` | User ↔ Role mapping |
| `airbnb_backend.listing` | Property listings |
| `airbnb_backend.listing_picture` | Property images (blob) |
| `airbnb_backend.booking` | Reservations |

---

## Running Locally

### Prerequisites
- Java 17
- Maven
- PostgreSQL (or Docker)

### With Docker for the database

```bash
docker compose -f compose.yaml up -d   # starts postgres on :5432
./mvnw spring-boot:run                 # starts app on :8081
```

### Manually

```bash
# Make sure PostgreSQL is running on localhost:5432
# Database: airbnb_backend, User: postgres, Password: password

./mvnw spring-boot:run
```

App runs on **http://localhost:8081**

---

## Configuration

| File | Profile | Purpose |
|------|---------|---------|
| `application.yml` | Base | Port, logging, JPA settings |
| `application-dev.yml` | `dev` (default) | Local DB and Liquibase context |
| `application-prod.yml` | `prod` | Env var–driven DB and Auth0 config |

### Production Environment Variables

```env
SPRING_PROFILES_ACTIVE=prod

POSTGRES_URL=<db_host>
POSTGRES_DB=airbnb_backend
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_password

OKTA_OAUTH2_ISSUER=https://airbnb-backend.us.auth0.com/
OKTA_OAUTH2_CLIENT_ID=your_client_id
OKTA_OAUTH2_CLIENT_SECRET=your_client_secret
AUTH0_ROLE_LANDLORD_ID=your_role_id
```

---

## Build & Docker

```bash
# Build JAR
./mvnw clean package -DskipTests

# Build Docker image
docker build -t airbnb-backend .
```

The Dockerfile uses a multi-stage build — Maven compiles the JAR, then a slim JRE image runs it.
