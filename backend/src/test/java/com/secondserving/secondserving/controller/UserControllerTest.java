package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.config.GeometryConfig;
import com.secondserving.secondserving.config.security.UserDetailsImpl;
import com.secondserving.secondserving.domain.FoodListing;
import com.secondserving.secondserving.domain.PickupLocation;
import com.secondserving.secondserving.domain.Reservation;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.dto.CreateFoodListingRequestDto;
import com.secondserving.secondserving.dto.CreateReservationRequestDto;
import com.secondserving.secondserving.dto.FoodListingDto;
import com.secondserving.secondserving.dto.PatchFoodListingRequestDto;
import com.secondserving.secondserving.dto.PatchReservationDto;
import com.secondserving.secondserving.dto.ReservationDto;
import com.secondserving.secondserving.exception.UpdatingUnownedFoodListingException;
import com.secondserving.secondserving.service.FoodListingService;
import com.secondserving.secondserving.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link UserController}.
 *
 * <p>These tests cover the user-scoped listing and reservation endpoints, including
 * request-to-command mapping and validation/error handling behavior.
 */
class UserControllerTest {

    /**
     * Verifies that the controller returns the authenticated user's food listings as DTOs.
     */
    @Test
    void getMyFoodListings_returnsOwnedFoodListingDtos() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        StubReservationService reservationService = new StubReservationService();
        UserController underTest = new UserController(
                reservationService,
                foodListingService,
                new GeometryConfig().geometryFactory()
        );
        User user = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        FoodListing listing = new FoodListing(
                user,
                "Soup",
                "Fresh soup",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 3,
                FoodListing.QuantityUnit.PORTION,
                Instant.parse("2026-05-01T12:00:00Z")
        );
        foodListingService.ownedListingsToReturn = List.of(listing);

        ResponseEntity<?> response = underTest.getMyFoodListings(new UserDetailsImpl(user));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<FoodListingDto> body = assertInstanceOf(List.class, response.getBody());
        assertEquals(1, body.size());
        assertEquals(listing.getListingId(), body.get(0).listingId());
        assertSame(user, foodListingService.ownerPassedToGetListings);
    }

    /**
     * Verifies that the controller returns the authenticated user's reservations as DTOs.
     */
    @Test
    void getMyReservations_returnsReservationDtos() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        StubReservationService reservationService = new StubReservationService();
        UserController underTest = new UserController(
                reservationService,
                foodListingService,
                new GeometryConfig().geometryFactory()
        );
        User owner = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        User requester = new User("requester", "passwordHash", "Requester Name", "requester@example.com");
        FoodListing listing = new FoodListing(
                owner,
                "Bread",
                "Fresh bread",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 2,
                FoodListing.QuantityUnit.ITEM,
                Instant.parse("2026-05-01T12:00:00Z")
        );
        Reservation reservation = new Reservation(
                listing,
                requester,
                (short) 1,
                Reservation.ReservationStatus.REQUESTED
        );
        reservationService.reservationsToReturn = List.of(reservation);

        ResponseEntity<?> response = underTest.getMyReservations(new UserDetailsImpl(requester));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<ReservationDto> body = assertInstanceOf(List.class, response.getBody());
        assertEquals(1, body.size());
        assertEquals(reservation.getReservationId().getListingId(), body.get(0).listingId());
        assertSame(requester, reservationService.requesterPassedToGetReservations);
    }

    /**
     * Verifies that creating a food listing returns a created DTO response and correctly maps
     * pickup-location request coordinates into the service command's geometry point.
     */
    @Test
    void createMyFoodListing_returnsCreatedFoodListingDtoAndMapsCommand() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        StubReservationService reservationService = new StubReservationService();
        GeometryFactory geometryFactory = new GeometryConfig().geometryFactory();
        UserController underTest = new UserController(
                reservationService,
                foodListingService,
                geometryFactory
        );
        User user = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        FoodListing createdListing = new FoodListing(
                user,
                "Bagels",
                "Left over from catering",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 6,
                FoodListing.QuantityUnit.ITEM,
                Instant.parse("2026-05-01T12:00:00Z")
        );
        new PickupLocation(
                createdListing,
                "123 Queen St W",
                geometryFactory.createPoint(new Coordinate(-79.3832, 43.6532)),
                Instant.parse("2026-05-01T13:00:00Z"),
                Instant.parse("2026-05-01T15:00:00Z"),
                "Ring the bell"
        );
        foodListingService.createdListingToReturn = createdListing;
        CreateFoodListingRequestDto request = new CreateFoodListingRequestDto(
                "Bagels",
                "Left over from catering",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 6,
                FoodListing.QuantityUnit.ITEM,
                Instant.parse("2026-05-01T12:00:00Z"),
                new CreateFoodListingRequestDto.PickupLocationRequestDto(
                        "123 Queen St W",
                        -79.3832,
                        43.6532,
                        Instant.parse("2026-05-01T13:00:00Z"),
                        Instant.parse("2026-05-01T15:00:00Z"),
                        "Ring the bell"
                )
        );

        ResponseEntity<FoodListingDto> response = underTest.createMyFoodListing(new UserDetailsImpl(user), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdListing.getListingId(), response.getBody().listingId());
        assertSame(user, foodListingService.ownerPassedToCreate);
        assertNotNull(foodListingService.commandPassedToCreate);
        assertEquals("Bagels", foodListingService.commandPassedToCreate.title());
        assertEquals("123 Queen St W", foodListingService.commandPassedToCreate.pickupLocation().fullAddress());
        assertEquals(-79.3832, foodListingService.commandPassedToCreate.pickupLocation().locationPoint().getX(), 0.0001);
        assertEquals(43.6532, foodListingService.commandPassedToCreate.pickupLocation().locationPoint().getY(), 0.0001);
    }

    /**
     * Verifies that creating a reservation returns a created DTO response and forwards the
     * authenticated requester, listing id, and quantity to the service layer.
     */
    @Test
    void createMyReservation_returnsCreatedReservationDtoAndMapsRequest() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        StubReservationService reservationService = new StubReservationService();
        UserController underTest = new UserController(
                reservationService,
                foodListingService,
                new GeometryConfig().geometryFactory()
        );
        User owner = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        User requester = new User("requester", "passwordHash", "Requester Name", "requester@example.com");
        FoodListing listing = new FoodListing(
                owner,
                "Soup",
                "Fresh soup",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 4,
                FoodListing.QuantityUnit.PORTION,
                Instant.parse("2026-05-01T12:00:00Z")
        );
        Reservation createdReservation = new Reservation(
                listing,
                requester,
                (short) 2,
                Reservation.ReservationStatus.REQUESTED
        );
        reservationService.createdReservationToReturn = createdReservation;
        CreateReservationRequestDto request = new CreateReservationRequestDto(listing.getListingId(), (short) 2);

        ResponseEntity<ReservationDto> response = underTest.createMyReservation(new UserDetailsImpl(requester), request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdReservation.getReservationId().getListingId(), response.getBody().listingId());
        assertSame(requester, reservationService.requesterPassedToCreate);
        assertEquals(listing.getListingId(), reservationService.listingIdPassedToCreate);
        assertEquals((short) 2, reservationService.quantityPassedToCreate);
    }

    /**
     * Verifies that patching a reservation returns the patched DTO and forwards the
     * authenticated requester, listing id, and patch command to the service layer.
     */
    @Test
    void updateMyReservation_returnsOkReservationDtoAndMapsPatchCommand() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        StubReservationService reservationService = new StubReservationService();
        UserController underTest = new UserController(
                reservationService,
                foodListingService,
                new GeometryConfig().geometryFactory()
        );
        User owner = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        User requester = new User("requester", "passwordHash", "Requester Name", "requester@example.com");
        FoodListing listing = new FoodListing(
                owner,
                "Soup",
                "Fresh soup",
                FoodListing.FoodListingStatus.AVAILABLE,
                (short) 4,
                FoodListing.QuantityUnit.PORTION,
                Instant.parse("2026-05-01T12:00:00Z")
        );
        Reservation patchedReservation = new Reservation(
                listing,
                requester,
                (short) 1,
                Reservation.ReservationStatus.COLLECTED
        );
        reservationService.patchedReservationToReturn = patchedReservation;
        PatchReservationDto request = new PatchReservationDto(
                Reservation.ReservationStatus.COLLECTED,
                (short) 1
        );

        ResponseEntity<ReservationDto> response =
                underTest.updateMyReservation(new UserDetailsImpl(requester), listing.getListingId(), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(listing.getListingId(), response.getBody().listingId());
        assertSame(requester, reservationService.requesterPassedToPatch);
        assertEquals(listing.getListingId(), reservationService.listingIdPassedToPatch);
        assertNotNull(reservationService.commandPassedToPatch);
        assertEquals(Reservation.ReservationStatus.COLLECTED, reservationService.commandPassedToPatch.reservationStatus());
        assertEquals((short) 1, reservationService.commandPassedToPatch.quantityRequested());
    }

    /**
     * Verifies that patching a food listing returns the patched DTO and forwards only the
     * patchable fields to the service command.
     */
    @Test
    void updateMyFoodListing_returnsOkFoodListingDtoAndMapsPatchCommand() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        StubReservationService reservationService = new StubReservationService();
        UserController underTest = new UserController(
                reservationService,
                foodListingService,
                new GeometryConfig().geometryFactory()
        );
        User user = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        UUID listingId = UUID.randomUUID();
        FoodListing patchedListing = new FoodListing(
                user,
                "Soup",
                "Fresh soup",
                FoodListing.FoodListingStatus.FINISHED,
                (short) 1,
                FoodListing.QuantityUnit.PORTION,
                Instant.parse("2026-05-01T12:00:00Z")
        );
        foodListingService.patchedListingToReturn = patchedListing;
        PatchFoodListingRequestDto request = new PatchFoodListingRequestDto(
                FoodListing.FoodListingStatus.FINISHED,
                (short) 1,
                null
        );

        ResponseEntity<FoodListingDto> response = underTest.updateMyFoodListing(new UserDetailsImpl(user), listingId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(patchedListing.getListingId(), response.getBody().listingId());
        assertSame(user, foodListingService.ownerPassedToPatch);
        assertEquals(listingId, foodListingService.listingIdPassedToPatch);
        assertNotNull(foodListingService.commandPassedToPatch);
        assertEquals(FoodListing.FoodListingStatus.FINISHED, foodListingService.commandPassedToPatch.status());
        assertEquals((short) 1, foodListingService.commandPassedToPatch.quantity());
        assertNull(foodListingService.commandPassedToPatch.expiresAt());
    }

    /**
     * Verifies that deleting a food listing returns no content and forwards the authenticated
     * owner and listing id to the service layer.
     */
    @Test
    void deleteMyFoodListing_returnsNoContentAndMapsOwnerAndListingId() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        StubReservationService reservationService = new StubReservationService();
        UserController underTest = new UserController(
                reservationService,
                foodListingService,
                new GeometryConfig().geometryFactory()
        );
        User user = new User("owner", "passwordHash", "Owner Name", "owner@example.com");
        UUID listingId = UUID.randomUUID();

        ResponseEntity<Void> response = underTest.deleteMyFoodListing(new UserDetailsImpl(user), listingId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        assertSame(user, foodListingService.ownerPassedToDelete);
        assertEquals(listingId, foodListingService.listingIdPassedToDelete);
    }

    /**
     * Verifies that deleting a reservation returns no content and forwards the authenticated
     * requester and listing id to the service layer.
     */
    @Test
    void deleteMyReservation_returnsNoContentAndMapsRequesterAndListingId() {
        StubFoodListingService foodListingService = new StubFoodListingService();
        StubReservationService reservationService = new StubReservationService();
        UserController underTest = new UserController(
                reservationService,
                foodListingService,
                new GeometryConfig().geometryFactory()
        );
        User requester = new User("requester", "passwordHash", "Requester Name", "requester@example.com");
        UUID listingId = UUID.randomUUID();

        ResponseEntity<Void> response = underTest.deleteMyReservation(new UserDetailsImpl(requester), listingId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        assertSame(requester, reservationService.requesterPassedToDelete);
        assertEquals(listingId, reservationService.listingIdPassedToDelete);
    }

    /**
     * Verifies that an invalid food-listing creation payload is rejected with a single-string
     * bad-request response generated by the global exception handler.
     */
    @Test
    void createMyFoodListing_withInvalidRequest_returnsBadRequestSingleStringMessage() throws Exception {
        UserController underTest = new UserController(
                new StubReservationService(),
                new StubFoodListingService(),
                new GeometryConfig().geometryFactory()
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String invalidBody = """
                {
                  "title": "",
                  "description": "description",
                  "status": "AVAILABLE",
                  "quantity": 1,
                  "quantityUnit": "ITEM",
                  "expiresAt": "2025-01-01T12:00:00Z",
                  "pickupLocation": {
                    "fullAddress": "",
                    "longitude": -200.0,
                    "latitude": 91.0,
                    "pickupStartAt": null,
                    "pickupEndAt": null,
                    "instructions": "instructions"
                  }
                }
                """;

        mockMvc.perform(post("/users/me/food-listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "owner")
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("title: must not be blank"),
                        org.hamcrest.Matchers.containsString("expiresAt: must be a future date"),
                        org.hamcrest.Matchers.containsString("pickupLocation.fullAddress: must not be blank")
                )));
    }

    /**
     * Verifies that an invalid food-listing patch payload is rejected before reaching the
     * service layer.
     */
    @Test
    void updateMyFoodListing_withInvalidRequest_returnsBadRequestSingleStringMessage() throws Exception {
        UserController underTest = new UserController(
                new StubReservationService(),
                new StubFoodListingService(),
                new GeometryConfig().geometryFactory()
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String invalidBody = """
                {
                  "status": "FINISHED",
                  "quantity": -1,
                  "expiresAt": "2025-01-01T12:00:00Z"
                }
                """;

        mockMvc.perform(patch("/users/me/food-listings/{listingId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "owner")
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("quantity: must be greater than or equal to 0"),
                        org.hamcrest.Matchers.containsString("expiresAt: must be a future date")
                )));
    }

    /**
     * Verifies that an invalid reservation patch payload is rejected before reaching the
     * service layer.
     */
    @Test
    void updateMyReservation_withInvalidRequest_returnsBadRequestSingleStringMessage() throws Exception {
        UserController underTest = new UserController(
                new StubReservationService(),
                new StubFoodListingService(),
                new GeometryConfig().geometryFactory()
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String invalidBody = """
                {
                  "reservationStatus": "COLLECTED",
                  "quantityRequested": -1
                }
                """;

        mockMvc.perform(patch("/users/me/reservations/{listingId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "requester")
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "quantityRequested: must be greater than or equal to 0"
                )));
    }

    /**
     * Verifies that the controller advice maps unowned listing updates to 403 responses.
     */
    @Test
    void updateMyFoodListing_whenListingIsUnowned_returnsForbidden() throws Exception {
        StubFoodListingService foodListingService = new StubFoodListingService();
        foodListingService.patchException =
                new UpdatingUnownedFoodListingException("You cannot update a food listing owned by another user.");
        UserController underTest = new UserController(
                new StubReservationService(),
                foodListingService,
                new GeometryConfig().geometryFactory()
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String validBody = """
                {
                  "status": "FINISHED"
                }
                """;

        mockMvc.perform(patch("/users/me/food-listings/{listingId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "owner")
                        .content(validBody))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot update a food listing owned by another user."));
    }

    /**
     * Verifies that an invalid reservation creation payload is rejected with a single-string
     * bad-request response generated by the global exception handler.
     */
    @Test
    void createMyReservation_withInvalidRequest_returnsBadRequestSingleStringMessage() throws Exception {
        UserController underTest = new UserController(
                new StubReservationService(),
                new StubFoodListingService(),
                new GeometryConfig().geometryFactory()
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(authenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String invalidBody = """
                {
                  "listingId": null,
                  "quantityRequested": null
                }
                """;

        mockMvc.perform(post("/users/me/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(() -> "requester")
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("listingId: must not be null"),
                        org.hamcrest.Matchers.containsString("quantityRequested: must not be null")
                )));
    }

    /**
     * Test double for {@link FoodListingService} that captures controller inputs without requiring
     * repository wiring.
     */
    private static class StubFoodListingService extends FoodListingService {
        private List<FoodListing> ownedListingsToReturn = List.of();
        private FoodListing createdListingToReturn;
        private User ownerPassedToGetListings;
        private User ownerPassedToCreate;
        private CreateFoodListingCommand commandPassedToCreate;
        private FoodListing patchedListingToReturn;
        private User ownerPassedToPatch;
        private UUID listingIdPassedToPatch;
        private PatchFoodListingCommand commandPassedToPatch;
        private UpdatingUnownedFoodListingException patchException;
        private User ownerPassedToDelete;
        private UUID listingIdPassedToDelete;

        StubFoodListingService() {
            super(null, null);
        }

        /**
         * Records the owner passed by the controller and returns the configured listing set.
         */
        @Override
        public List<FoodListing> getFoodListingsOwnedBy(User owner) {
            ownerPassedToGetListings = owner;
            return ownedListingsToReturn;
        }

        /**
         * Records the listing-creation inputs passed by the controller and returns the configured
         * created listing.
         */
        @Override
        public FoodListing createListing(User owner, CreateFoodListingCommand command) {
            ownerPassedToCreate = owner;
            commandPassedToCreate = command;
            return createdListingToReturn;
        }

        /**
         * Records listing-patch inputs passed by the controller and returns the configured
         * patched listing.
         */
        @Override
        public FoodListing patchListingIfOwnedByUserOrThrow(User owner, UUID listingId, PatchFoodListingCommand command) {
            if (patchException != null) {
                throw patchException;
            }
            ownerPassedToPatch = owner;
            listingIdPassedToPatch = listingId;
            commandPassedToPatch = command;
            return patchedListingToReturn;
        }

        /**
         * Records listing-delete inputs passed by the controller.
         */
        @Override
        public void deleteFoodListingIfOwnedOrThrow(User user, UUID listingId) {
            ownerPassedToDelete = user;
            listingIdPassedToDelete = listingId;
        }
    }

    /**
     * Test double for {@link ReservationService} that captures controller inputs without requiring
     * repository wiring.
     */
    private static class StubReservationService extends ReservationService {
        private List<Reservation> reservationsToReturn = List.of();
        private Reservation createdReservationToReturn;
        private Reservation patchedReservationToReturn;
        private User requesterPassedToGetReservations;
        private User requesterPassedToCreate;
        private User requesterPassedToPatch;
        private UUID listingIdPassedToCreate;
        private UUID listingIdPassedToPatch;
        private UUID listingIdPassedToDelete;
        private short quantityPassedToCreate;
        private PatchReservationCommand commandPassedToPatch;
        private User requesterPassedToDelete;

        StubReservationService() {
            super(null, null);
        }

        /**
         * Records the requester passed by the controller and returns the configured reservations.
         */
        @Override
        public List<Reservation> getReservationsRequestedBy(User requester) {
            requesterPassedToGetReservations = requester;
            return reservationsToReturn;
        }

        /**
         * Records the reservation-creation inputs passed by the controller and returns the
         * configured created reservation.
         */
        @Override
        public Reservation createReservation(User requester, UUID listingId, short quantityRequested) {
            requesterPassedToCreate = requester;
            listingIdPassedToCreate = listingId;
            quantityPassedToCreate = quantityRequested;
            return createdReservationToReturn;
        }

        /**
         * Records reservation-patch inputs passed by the controller and returns the configured
         * patched reservation.
         */
        @Override
        public Reservation patchReservationRequestedByUserOrThrow(User requester, UUID listingId, PatchReservationCommand command) {
            requesterPassedToPatch = requester;
            listingIdPassedToPatch = listingId;
            commandPassedToPatch = command;
            return patchedReservationToReturn;
        }

        /**
         * Records reservation-delete inputs passed by the controller.
         */
        @Override
        public void deleteReservationRequestedByUserOrThrow(User requester, UUID listingId) {
            requesterPassedToDelete = requester;
            listingIdPassedToDelete = listingId;
        }
    }

    /**
     * Provides a fixed authenticated principal for standalone MockMvc validation tests so that
     * {@code @AuthenticationPrincipal UserDetailsImpl} parameters can be resolved without full
     * Spring Security integration.
     */
    private static HandlerMethodArgumentResolver authenticationPrincipalResolver() {
        User authenticatedUser = new User("authenticated", "passwordHash", "Authenticated User", "auth@example.com");
        UserDetailsImpl userDetails = new UserDetailsImpl(authenticatedUser);

        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(UserDetailsImpl.class)
                        && !parameter.hasParameterAnnotation(RequestBody.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return userDetails;
            }
        };
    }
}
