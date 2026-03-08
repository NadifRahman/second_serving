CREATE EXTENSION IF NOT EXISTS postgis; -- enable postgis extension

-- application code will auto-generate all UUIDs
CREATE TABLE user_account (
    user_id UUID PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE food_listing (
    listing_id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES user_account(user_id) ON DELETE CASCADE, -- Delete the food_listings if we delete the user
    title VARCHAR(50) NOT NULL,
    description VARCHAR(255), -- can be null, its ok if its nothing
    status VARCHAR(30) NOT NULL, -- this will be an enum enforced in hibernate
    quantity smallint CHECK (quantity >= 0) NOT NULL, -- quantity should never go under 0
    quantity_unit VARCHAR(30) NOT NULL, -- we will enforce this as an ENUM in hibernate instead of native postgres enum types
    expires_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

-- useful for getting a user's listings
CREATE INDEX idx_food_listing_owner_id
ON food_listing (owner_id);

CREATE TABLE pickup_location (
    pickup_id UUID PRIMARY KEY,
    listing_id UUID NOT NULL UNIQUE REFERENCES food_listing(listing_id) ON DELETE CASCADE, -- unique because only one pickup location per listing
    full_address VARCHAR(150) NOT NULL,
    location_point geography(POINT, 4326) NOT NULL,
    pickup_start_at timestamptz NOT NULL,
    pickup_end_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    instructions VARCHAR(255) -- can be null, user can decide not to put any
);

-- Create index for location_point for efficient geospatial queries
CREATE INDEX idx_pickup_location_point
ON pickup_location
USING GIST (location_point);

CREATE TABLE reservation (
    listing_id UUID NOT NULL REFERENCES food_listing(listing_id) ON DELETE CASCADE, -- delete the reservation if the listing or the requester is deleted
    requester_id UUID NOT NULL REFERENCES user_account(user_id) ON DELETE CASCADE,
    quantity_requested smallint CHECK (quantity_requested >= 0) NOT NULL,
    status VARCHAR(30) NOT NULL, -- we will enforce this as an ENUM in hibernate instead of native postgres enum types
    created_at timestamptz NOT NULL,
    status_updated_at timestamptz,
    PRIMARY KEY (listing_id, requester_id)
);

-- useful index for getting all reservations by a user
CREATE INDEX idx_reservation_requester_id
ON reservation (requester_id);



