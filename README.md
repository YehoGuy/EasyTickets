# EasyTickets

EasyTickets is a Java + XML Android app for discovering nearby events around a hotel through the Ticketmaster Discovery API.

The current version supports a single search flow:

- search by hotel

The UI is built around a single-activity, fragment-based flow:

- Home screen with a styled Google Map background and a hotel search surface
- Results map with grouped event markers
- Event details screen with venue, category, timing, and external Ticketmaster link

There is no login, backend, or user management in this version. All searches are executed on-device.

## Repository Layout

The Android project lives inside the nested [`EasyTickets/`](./EasyTickets) directory.

```text
.
├── README.md
└── EasyTickets/
    ├── app/
    ├── gradle/
    ├── build.gradle
    ├── settings.gradle
    ├── secrets.properties
    └── local.defaults.properties
```

## Project Structure

Main app source lives under [`EasyTickets/app/src/main/java/com/example/easytickets`](./EasyTickets/app/src/main/java/com/example/easytickets).

### UI layer

- [`ui/home`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/home)
  - home screen container
  - hotel search form
  - search request creation
- [`ui/results`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/results)
  - results map screen
  - marker rendering
  - grouped event bottom sheet
  - results `ViewModel`
- [`ui/details`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/details)
  - event details screen
  - external Ticketmaster page opening
- [`ui/common`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/common)
  - shared fragment base class
  - RecyclerView adapters
  - shared filter helpers

### Data layer

- [`data/ticketmaster`](./EasyTickets/app/src/main/java/com/example/easytickets/data/ticketmaster)
  - Retrofit API service
  - query builder
  - repository implementation
  - Ticketmaster response DTOs
- [`data/places`](./EasyTickets/app/src/main/java/com/example/easytickets/data/places)
  - Google Places hotel autocomplete and place details repository

### Domain layer

- [`domain/model`](./EasyTickets/app/src/main/java/com/example/easytickets/domain/model)
  - core app models such as `SearchRequest`, `SearchFilters`, `PlaceSelection`, `EventSummary`, and `EventMapGroup`
- [`domain/mapper`](./EasyTickets/app/src/main/java/com/example/easytickets/domain/mapper)
  - maps Ticketmaster DTOs into UI-safe domain models

### App wiring and utilities

- [`di/AppContainer.java`](./EasyTickets/app/src/main/java/com/example/easytickets/di/AppContainer.java)
  - central dependency construction for repositories, Retrofit, and Places
- [`util`](./EasyTickets/app/src/main/java/com/example/easytickets/util)
  - app config validation
  - distance unit selection
  - geohash encoding
  - event grouping helpers

## Architecture

The app uses a simple, explicit structure:

- single `MainActivity`
- Android Navigation Component with fragments
- `ViewModel` + `LiveData` for screen state
- repository layer for external integrations
- plain Java domain models passed between screens

Navigation flow:

```text
HomeFragment
  -> ResultsMapFragment
      -> EventDetailsFragment
```

The home screen owns search entry. The hotel form builds a normalized `SearchRequest`, and the results screen uses that request to load nearby events.

## External APIs and Services

### 1. Ticketmaster Discovery API v2

Used for:

- loading event categories
- searching events

Current behavior:

- endpoint base: `https://app.ticketmaster.com/discovery/v2/`
- appends `apikey` automatically through an OkHttp interceptor
- fetches up to `50` events per search
- hotel searches use `geoPoint`
- nearby searches sort by `distance,asc`

Category filters are loaded from Ticketmaster classifications and cached in memory. If that fetch fails, the app falls back to a built-in top-level category list:

- Music
- Sports
- Arts & Theatre
- Film
- Miscellaneous

### 2. Google Maps SDK for Android

Used for:

- decorative background map on the home screen
- interactive results map
- event and origin markers

The home map is intentionally non-interactive and styled. The results map is interactive and shows:

- the hotel origin
- grouped event markers
- grouped-marker bottom sheets

### 3. Google Places SDK for Android

Used for:

- hotel autocomplete
- place details lookup after selection

Current restrictions:

- supported countries: `US`, `CA`, `MX`, `AU`, `NZ`
- hotel search uses Places type filter: `lodging`

Autocomplete returns lightweight suggestions first, then a second request loads the selected place details including:

- place ID
- display name
- formatted address
- latitude/longitude
- country code

## Key Files

- [`MainActivity.java`](./EasyTickets/app/src/main/java/com/example/easytickets/MainActivity.java)
  - entry point
  - edge-to-edge setup
  - setup-required fallback screen when keys are missing
- [`HomeFragment.java`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/home/HomeFragment.java)
  - hosts the home map and hotel search container
- [`HotelSearchFragment.java`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/home/HotelSearchFragment.java)
  - manages hotel autocomplete, selected place presentation, and filter submission
- [`HomeViewModel.java`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/home/HomeViewModel.java)
  - loads categories
  - drives hotel autocomplete and place selection
- [`TicketmasterQueryFactory.java`](./EasyTickets/app/src/main/java/com/example/easytickets/data/ticketmaster/TicketmasterQueryFactory.java)
  - builds Ticketmaster request parameters from `SearchRequest`
- [`ResultsMapFragment.java`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/results/ResultsMapFragment.java)
  - renders markers
  - opens grouped event bottom sheets
  - navigates to details
- [`ResultsViewModel.java`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/results/ResultsViewModel.java)
  - executes event search
  - groups events for marker display
- [`EventDetailsFragment.java`](./EasyTickets/app/src/main/java/com/example/easytickets/ui/details/EventDetailsFragment.java)
  - binds event details
  - opens the event website

## Main User Flows

### 1. App startup

1. `MainActivity` starts.
2. `AppContainer` reads API keys from `BuildConfig`.
3. If required keys are missing, the app shows the setup-required screen instead of the main app.
4. If keys are valid, navigation starts at `HomeFragment`.

### 2. Search by hotel

1. User types a hotel name on the home screen.
2. Google Places autocomplete returns lodging suggestions.
3. User selects a hotel.
4. The app fetches place details and stores the resolved hotel location.
5. User picks category filters and a radius.
6. The app creates a `SearchRequest` with:
   - `SearchMode.HOTEL`
   - hotel coordinates
   - country code
   - selected filters
7. Results screen searches Ticketmaster with `geoPoint`, radius, unit, and categories.

### 3. Results map

1. `ResultsViewModel` executes the Ticketmaster search.
2. Raw events are mapped into `EventSummary` domain models.
3. Events are grouped by venue/location into `EventMapGroup`.
4. The results map shows:
   - one origin marker
   - one marker per grouped venue/location
5. If a marker contains:
   - one event: a bottom sheet opens with the single event action
   - multiple events: a bottom sheet opens with a list
6. Selecting an event navigates to the details screen.

### 4. Event details

1. The selected `EventSummary` is converted to `EventDetails`.
2. The details screen shows:
   - image
   - title
   - date/time
   - venue
   - address
   - category
   - Ticketmaster URL
3. The `Open Ticketmaster` button launches the event link in the browser.

## Configuration

The Android project root is:

```text
EasyTickets/
```

Create this file:

```text
EasyTickets/secrets.properties
```

Add:

```properties
GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_KEY
TICKETMASTER_API_KEY=YOUR_TICKETMASTER_CONSUMER_KEY
```

Notes:

- the Ticketmaster `Consumer_Key` is used as `TICKETMASTER_API_KEY`
- the Google key must allow `Maps SDK for Android` and `Places API`
- keys are injected through the Secrets Gradle Plugin
- default placeholders live in [`EasyTickets/local.defaults.properties`](./EasyTickets/local.defaults.properties)

## Build and Run

From the Android project root:

```bash
cd EasyTickets
./gradlew assembleDebug
```

Run unit tests:

```bash
./gradlew testDebugUnitTest
```

## Current Test Coverage

Unit tests currently cover:

- Ticketmaster query construction
- Ticketmaster DTO mapping
- fallback category behavior
- event grouping
- distance unit resolution

See:

- [`TicketmasterQueryFactoryTest.java`](./EasyTickets/app/src/test/java/com/example/easytickets/TicketmasterQueryFactoryTest.java)
- [`TicketmasterMapperTest.java`](./EasyTickets/app/src/test/java/com/example/easytickets/TicketmasterMapperTest.java)
- [`TicketmasterRepositoryFallbackTest.java`](./EasyTickets/app/src/test/java/com/example/easytickets/TicketmasterRepositoryFallbackTest.java)
- [`EventGroupingUtilsTest.java`](./EasyTickets/app/src/test/java/com/example/easytickets/EventGroupingUtilsTest.java)
- [`DistanceUnitResolverTest.java`](./EasyTickets/app/src/test/java/com/example/easytickets/DistanceUnitResolverTest.java)

## Implementation Notes

- The app is intentionally Java-first. No Jetpack Compose is used.
- The current version calls Ticketmaster directly from the client app.
- For a production deployment, Ticketmaster traffic should ideally move behind a backend proxy so the key is not distributed in the APK.
