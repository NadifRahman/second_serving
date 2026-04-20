package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.config.security.UserDetailsImpl;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.dto.CreateFoodListingRequestDto;
import com.secondserving.secondserving.dto.CreateReservationRequestDto;
import com.secondserving.secondserving.dto.FoodListingDto;
import com.secondserving.secondserving.dto.ReservationDto;
import com.secondserving.secondserving.service.FoodListingService;
import com.secondserving.secondserving.service.ReservationService;
import jakarta.validation.Valid;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

@RequestMapping(UserController.USER_BASE_PATH)
public class UserController {

    public static final String USER_BASE_PATH = "/users";
    public static final String ME_PATH = "/me";
    public static final String FOOD_LISTINGS_PATH = "/food-listings";
    public static final String RESERVATIONS_PATH = "/reservations";

    private final FoodListingService foodListingService;
    private final ReservationService reservationService;
    private final GeometryFactory geometryFactory;

    public UserController(ReservationService reservationService,
                          FoodListingService foodListingService,
                          GeometryFactory geometryFactory) {
        this.reservationService = reservationService;
        this.foodListingService = foodListingService;
        this.geometryFactory = geometryFactory;
    }

    @GetMapping(ME_PATH)
    // TODO just a test endpoint...delete later
    public ResponseEntity<String> getUser(Authentication authentication) {
        authentication.getName();
        return ResponseEntity.status(HttpStatus.OK).body(authentication.getName());
    }

    /**
     * Method for handling requests to get all food listings by the authenticated user.
     * @param userDetail the principal authenticated object
     * @return
     */
    @GetMapping(ME_PATH + FOOD_LISTINGS_PATH)
    public ResponseEntity<?> getMyFoodListings(@AuthenticationPrincipal UserDetailsImpl userDetail) {
        User user = userDetail.getUser();
        List<FoodListingDto> dtos = foodListingService.getFoodListingsOwnedBy(user).stream().map(FoodListingDto::from).toList();
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    /**
     * Method for handling requests to get all reservations by the authenticated user.
     * @param userDetail the principal authenticated object
     * @return
     */
    @GetMapping(ME_PATH + RESERVATIONS_PATH)
    // TODO write unit tests for this
    public ResponseEntity<?> getMyReservations(@AuthenticationPrincipal UserDetailsImpl userDetail) {
        User user = userDetail.getUser();
        List<ReservationDto> dtos = reservationService.getReservationsRequestedBy(user).stream().map(ReservationDto::from).toList();
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    @PostMapping(ME_PATH + FOOD_LISTINGS_PATH)
    // TODO write unit tests for this
    public ResponseEntity<FoodListingDto> createMyFoodListing(@AuthenticationPrincipal UserDetailsImpl userDetail,
                                                              @Valid @RequestBody CreateFoodListingRequestDto request) {
        User user = userDetail.getUser();
        CreateFoodListingRequestDto.PickupLocationRequestDto pickupLocation = request.pickupLocation();

        FoodListingService.CreateFoodListingCommand command = new FoodListingService.CreateFoodListingCommand(
                request.title(),
                request.description(),
                request.status(),
                request.quantity(),
                request.quantityUnit(),
                request.expiresAt(),
                new FoodListingService.PickupLocationCommand(
                        pickupLocation.fullAddress(),
                        geometryFactory.createPoint(new Coordinate(pickupLocation.longitude(), pickupLocation.latitude())),
                        pickupLocation.pickupStartAt(),
                        pickupLocation.pickupEndAt(),
                        pickupLocation.instructions()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FoodListingDto.from(foodListingService.createListing(user, command)));
    }

    @PostMapping(ME_PATH + RESERVATIONS_PATH)
    public ResponseEntity<ReservationDto> createMyReservation(@AuthenticationPrincipal UserDetailsImpl userDetail,
                                                              @Valid @RequestBody CreateReservationRequestDto request) {
        User user = userDetail.getUser();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationDto.from(
                        reservationService.createReservation(user, request.listingId(), request.quantityRequested())
                ));
    }

}
