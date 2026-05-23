-- Demo seed data for Second Serving.
--
-- All seeded users share the password: password
-- The bcrypt hash below is for demo data only. Do not use it for real users.

BEGIN;

WITH demo_users(user_id, username, password_hash, full_name, email) AS (
  VALUES
    ('10000000-0000-0000-0000-000000000001'::uuid, 'harbourbakery', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Harbour Bakery', 'hello@harbourbakery.example'),
    ('10000000-0000-0000-0000-000000000002'::uuid, 'parkdalecafe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Parkdale Community Cafe', 'pickup@parkdalecafe.example'),
    ('10000000-0000-0000-0000-000000000003'::uuid, 'annexgarden', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Annex Garden Co-op', 'sharing@annexgarden.example'),
    ('10000000-0000-0000-0000-000000000004'::uuid, 'mississaugameals', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Mississauga Meal Prep', 'team@mississaugameals.example'),
    ('10000000-0000-0000-0000-000000000005'::uuid, 'hamiltonharvest', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Hamilton Harvest Kitchen', 'hello@hamiltonharvest.example'),
    ('10000000-0000-0000-0000-000000000006'::uuid, 'waterloostudent', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Waterloo Student Pantry', 'pantry@waterloostudent.example'),
    ('10000000-0000-0000-0000-000000000007'::uuid, 'demoalex', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alex Demo', 'alex.demo@example.com'),
    ('10000000-0000-0000-0000-000000000008'::uuid, 'demopriya', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Priya Demo', 'priya.demo@example.com')
)
INSERT INTO user_account (user_id, username, password_hash, full_name, email, created_at, updated_at)
SELECT user_id, username, password_hash, full_name, email, NOW() - INTERVAL '14 days', NOW()
FROM demo_users
ON CONFLICT (user_id) DO UPDATE SET
  username = EXCLUDED.username,
  password_hash = EXCLUDED.password_hash,
  full_name = EXCLUDED.full_name,
  email = EXCLUDED.email,
  updated_at = NOW();

WITH cities(city_index, city_name, latitude, longitude, address_stub) AS (
  VALUES
    (0, 'Toronto', 43.6532, -79.3832, 'Queen St W'),
    (1, 'Mississauga', 43.5890, -79.6441, 'City Centre Dr'),
    (2, 'Brampton', 43.7315, -79.7624, 'Main St N'),
    (3, 'Oakville', 43.4675, -79.6877, 'Lakeshore Rd E'),
    (4, 'Burlington', 43.3255, -79.7990, 'Brant St'),
    (5, 'Hamilton', 43.2557, -79.8711, 'King St E'),
    (6, 'Kitchener', 43.4516, -80.4925, 'King St W'),
    (7, 'Waterloo', 43.4643, -80.5204, 'University Ave W'),
    (8, 'Markham', 43.8561, -79.3370, 'Highway 7'),
    (9, 'Scarborough', 43.7764, -79.2318, 'Ellesmere Rd'),
    (10, 'Etobicoke', 43.6205, -79.5132, 'Bloor St W'),
    (11, 'North York', 43.7615, -79.4111, 'Yonge St')
),
food_templates(template_index, title, description, quantity_unit, base_quantity, instructions) AS (
  VALUES
    (0, 'Fresh sourdough loaves', 'End-of-day sourdough and rye loaves from a neighbourhood bakery.', 'ITEM', 12, 'Ask for the Second Serving box at the counter.'),
    (1, 'Vegetarian chili portions', 'Hearty bean chili packed in sealed single-serving containers.', 'SERVING', 18, 'Pickup from the side window.'),
    (2, 'Community garden vegetables', 'Washed greens, carrots, herbs, and seasonal vegetables.', 'PACKAGE', 10, 'Pickup bin is inside the main gate.'),
    (3, 'Prepared rice bowls', 'Refrigerated rice bowls with roasted vegetables and protein.', 'PORTION', 24, 'Use the loading door near the north entrance.'),
    (4, 'Soup and dinner rolls', 'Prepared soup with packaged dinner rolls for same-day pickup.', 'SERVING', 16, 'Ring the kitchen bell at the back entrance.'),
    (5, 'Produce pantry bags', 'Mixed bags with apples, potatoes, onions, and greens.', 'PACKAGE', 20, 'Pickup from the front desk.'),
    (6, 'Assorted pastries', 'Croissants, muffins, and danishes packed after the morning rush.', 'ITEM', 30, 'Pickup at the cafe counter.'),
    (7, 'Fruit cups for families', 'Fresh cut melon, grapes, and berries in sealed cups.', 'ITEM', 36, 'Call from the parking lot for handoff.'),
    (8, 'Pantry staples bundle', 'Rice, lentils, canned tomatoes, and unopened pasta.', 'PACKAGE', 14, 'Pickup table is in the lobby.'),
    (9, 'Sandwich trays', 'Vegetarian and turkey sandwiches from a lunch event, individually wrapped.', 'ITEM', 22, 'Ask security for the pickup cart.'),
    (10, 'Frozen vegetarian meals', 'Labelled frozen lentil curry and vegetable pasta meals.', 'PORTION', 28, 'Bring a cooler bag if possible.'),
    (11, 'Bagels and cream cheese', 'Plain and sesame bagels with unopened cream cheese tubs.', 'DOZEN', 8, 'Pickup from the community fridge shelf.'),
    (12, 'Catering salad boxes', 'Sealed salad boxes with dressing packed separately.', 'ITEM', 26, 'Use the reception entrance.'),
    (13, 'Grocery rescue boxes', 'Mixed shelf-stable groceries from a local food drive.', 'PACKAGE', 18, 'Show the reservation at pickup.'),
    (14, 'Prepared pasta trays', 'Family-size pasta trays from an event cancellation.', 'PORTION', 32, 'Pickup at the kitchen side door.')
),
demo_listings AS (
  SELECT
    ('20000000-0000-0000-0000-' || lpad(series_index::text, 12, '0'))::uuid AS listing_id,
    ('10000000-0000-0000-0000-' || lpad((((series_index - 1) % 6) + 1)::text, 12, '0'))::uuid AS owner_id,
    left(food_templates.title || ' #' || series_index, 50) AS title,
    food_templates.description AS description,
    'AVAILABLE' AS status,
    (food_templates.base_quantity + (series_index % 7))::smallint AS quantity,
    food_templates.quantity_unit AS quantity_unit,
    (((series_index % 14) + 2) || ' days')::interval AS expires_offset,
    ('30000000-0000-0000-0000-' || lpad(series_index::text, 12, '0'))::uuid AS pickup_id,
    ((100 + series_index)::text || ' ' || cities.address_stub || ', ' || cities.city_name || ', ON') AS full_address,
    (cities.longitude + ((((series_index % 9) - 4) * 0.009) + (((series_index / 9) % 3) * 0.003)))::double precision AS longitude,
    (cities.latitude + ((((series_index % 7) - 3) * 0.007) - (((series_index / 7) % 3) * 0.002)))::double precision AS latitude,
    (((series_index % 8) + 1) || ' hours')::interval AS pickup_start_offset,
    (((series_index % 8) + 5) || ' hours')::interval AS pickup_end_offset,
    food_templates.instructions AS instructions
  FROM generate_series(1, 150) AS generated(series_index)
  JOIN cities ON cities.city_index = ((series_index - 1) % 12)
  JOIN food_templates ON food_templates.template_index = ((series_index - 1) % 15)
)
INSERT INTO food_listing (listing_id, owner_id, title, description, status, quantity, quantity_unit, expires_at, created_at, updated_at)
SELECT
  listing_id,
  owner_id,
  title,
  description,
  status,
  quantity,
  quantity_unit,
  NOW() + expires_offset,
  NOW() - INTERVAL '2 days',
  NOW()
FROM demo_listings
ON CONFLICT (listing_id) DO UPDATE SET
  owner_id = EXCLUDED.owner_id,
  title = EXCLUDED.title,
  description = EXCLUDED.description,
  status = EXCLUDED.status,
  quantity = EXCLUDED.quantity,
  quantity_unit = EXCLUDED.quantity_unit,
  expires_at = EXCLUDED.expires_at,
  updated_at = NOW();

WITH cities(city_index, city_name, latitude, longitude, address_stub) AS (
  VALUES
    (0, 'Toronto', 43.6532, -79.3832, 'Queen St W'),
    (1, 'Mississauga', 43.5890, -79.6441, 'City Centre Dr'),
    (2, 'Brampton', 43.7315, -79.7624, 'Main St N'),
    (3, 'Oakville', 43.4675, -79.6877, 'Lakeshore Rd E'),
    (4, 'Burlington', 43.3255, -79.7990, 'Brant St'),
    (5, 'Hamilton', 43.2557, -79.8711, 'King St E'),
    (6, 'Kitchener', 43.4516, -80.4925, 'King St W'),
    (7, 'Waterloo', 43.4643, -80.5204, 'University Ave W'),
    (8, 'Markham', 43.8561, -79.3370, 'Highway 7'),
    (9, 'Scarborough', 43.7764, -79.2318, 'Ellesmere Rd'),
    (10, 'Etobicoke', 43.6205, -79.5132, 'Bloor St W'),
    (11, 'North York', 43.7615, -79.4111, 'Yonge St')
),
pickup_templates(template_index, instructions) AS (
  VALUES
    (0, 'Ask for the Second Serving box at the counter.'),
    (1, 'Pickup from the side window.'),
    (2, 'Pickup bin is inside the main gate.'),
    (3, 'Use the loading door near the north entrance.'),
    (4, 'Ring the kitchen bell at the back entrance.'),
    (5, 'Pickup from the front desk.'),
    (6, 'Pickup at the cafe counter.'),
    (7, 'Call from the parking lot for handoff.'),
    (8, 'Pickup table is in the lobby.'),
    (9, 'Ask security for the pickup cart.'),
    (10, 'Bring a cooler bag if possible.'),
    (11, 'Pickup from the community fridge shelf.'),
    (12, 'Use the reception entrance.'),
    (13, 'Show the reservation at pickup.'),
    (14, 'Pickup at the kitchen side door.')
),
demo_pickups AS (
  SELECT
    ('30000000-0000-0000-0000-' || lpad(series_index::text, 12, '0'))::uuid AS pickup_id,
    ('20000000-0000-0000-0000-' || lpad(series_index::text, 12, '0'))::uuid AS listing_id,
    ((100 + series_index)::text || ' ' || cities.address_stub || ', ' || cities.city_name || ', ON') AS full_address,
    (cities.longitude + ((((series_index % 9) - 4) * 0.009) + (((series_index / 9) % 3) * 0.003)))::double precision AS longitude,
    (cities.latitude + ((((series_index % 7) - 3) * 0.007) - (((series_index / 7) % 3) * 0.002)))::double precision AS latitude,
    (((series_index % 8) + 1) || ' hours')::interval AS pickup_start_offset,
    (((series_index % 8) + 5) || ' hours')::interval AS pickup_end_offset,
    pickup_templates.instructions AS instructions
  FROM generate_series(1, 150) AS generated(series_index)
  JOIN cities ON cities.city_index = ((series_index - 1) % 12)
  JOIN pickup_templates ON pickup_templates.template_index = ((series_index - 1) % 15)
)
INSERT INTO pickup_location (pickup_id, listing_id, full_address, location_point, pickup_start_at, pickup_end_at, created_at, updated_at, instructions)
SELECT
  pickup_id,
  listing_id,
  full_address,
  ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography,
  NOW() + pickup_start_offset,
  NOW() + pickup_end_offset,
  NOW() - INTERVAL '2 days',
  NOW(),
  instructions
FROM demo_pickups
ON CONFLICT (pickup_id) DO UPDATE SET
  listing_id = EXCLUDED.listing_id,
  full_address = EXCLUDED.full_address,
  location_point = EXCLUDED.location_point,
  pickup_start_at = EXCLUDED.pickup_start_at,
  pickup_end_at = EXCLUDED.pickup_end_at,
  instructions = EXCLUDED.instructions,
  updated_at = NOW();

DELETE FROM reservation
WHERE listing_id::text LIKE '20000000-0000-0000-0000-%'
  AND requester_id::text LIKE '10000000-0000-0000-0000-%';

WITH reservation_candidates AS (
  SELECT
    series_index,
    ('20000000-0000-0000-0000-' || lpad(series_index::text, 12, '0'))::uuid AS listing_id,
    (((series_index - 1) % 6) + 1) AS owner_number,
    CASE
      WHEN series_index % 2 = 0 THEN 7
      ELSE 8
    END AS requester_number
  FROM generate_series(1, 120) AS generated(series_index)
),
demo_reservations(listing_id, requester_id, quantity_requested, status) AS (
  SELECT
    listing_id,
    ('10000000-0000-0000-0000-' || lpad(requester_number::text, 12, '0'))::uuid AS requester_id,
    ((series_index % 4) + 1)::smallint AS quantity_requested,
    CASE
      WHEN series_index % 11 = 0 THEN 'COLLECTED'
      WHEN series_index % 13 = 0 THEN 'CANCELLED'
      ELSE 'REQUESTED'
    END AS status
  FROM reservation_candidates
  WHERE owner_number <> requester_number
)
INSERT INTO reservation (listing_id, requester_id, quantity_requested, status, created_at, updated_at)
SELECT listing_id, requester_id, quantity_requested, status, NOW() - INTERVAL '1 day', NOW()
FROM demo_reservations
ON CONFLICT (listing_id, requester_id) DO UPDATE SET
  quantity_requested = EXCLUDED.quantity_requested,
  status = EXCLUDED.status,
  updated_at = NOW();

COMMIT;
