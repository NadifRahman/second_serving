package com.secondserving.secondserving.controller;

import com.secondserving.secondserving.config.security.UserDetailsImpl;
import com.secondserving.secondserving.domain.User;
import com.secondserving.secondserving.dto.FoodListingDto;
import com.secondserving.secondserving.service.FoodListingService;
import com.secondserving.secondserving.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    private FoodListingService foodListingService;

    @Autowired
    public UserController(UserService userService, FoodListingService foodListingService) {
        this.userService = userService;
        this.foodListingService = foodListingService;
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

//    /**
//     * Method for handling requests to get all reservations by the authenticated user.
//     * @param userDetail the principal authenticated object
//     * @return
//     */
//    @GetMapping(ME_PATH + RESERVATIONS_PATH)
//    public ResponseEntity<?> getMyReservations(@AuthenticationPrincipal UserDetailsImpl userDetail) {
//        User user = userDetail.getUser();
//        List<Re> dtos = foodListingService.getFoodListingsOwnedBy(user).stream().map(FoodListingDto::from).toList();
//        return ResponseEntity.status(HttpStatus.OK).body(dtos);
//    }

}