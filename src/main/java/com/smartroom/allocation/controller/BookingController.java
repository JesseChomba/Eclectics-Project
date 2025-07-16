package com.smartroom.allocation.controller;

import com.smartroom.allocation.dto.BookingResponseDTO;
import com.smartroom.allocation.dto.BookingUpdateDTO;
import com.smartroom.allocation.dto.RecurringBookingRequest;
import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.User;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.exception.ResourceNotFoundException;
import com.smartroom.allocation.service.BookingService;
import com.smartroom.allocation.service.RoomService;
import com.smartroom.allocation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    /**
     * Create a new booking
     * @param booking Booking details (should contain roomId)
     * @param auth Authentication object (contains current user info)
     * @return Created booking in standardized format
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()") //only a logged in user can make a booking
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Booking booking, Authentication auth) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate authentication
            Optional<User> currentUser = userService.findByUsername(auth.getName());
            if (!currentUser.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "User not found");
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            //Fetch the full Room entity using the ID provided in the booking request
            //This is crucial to ensure roomNumber and roomName are available
            Optional<Room> roomOpt = roomService.findById(booking.getRoom().getId());
            if (!roomOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Room not found with id: " + booking.getRoom().getId());
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            booking.setUser(currentUser.get());
            booking.setRoom(roomOpt.get()); // Set the fully fetched Room entity

            Booking createdBooking = bookingService.createBooking(booking);

            response.put("Status", 1);
            response.put("Message", "Booking created successfully");
            response.put("Data", new BookingResponseDTO(createdBooking)); // Now DTO will have room details
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("Status", 0);
            response.put("Message", e.getMessage());
            response.put("Data", "");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to create booking: " + e.getMessage());
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        // Validate room ID in the booking(old code starts here)
//            if (booking.getRoom() == null || booking.getRoom().getId() == null) {
//                response.put("Status", 0);
//                response.put("Message", "Room ID is required");
//                response.put("Data", "");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            // Retrieve the Room object from the database
//            Optional<Room> roomOpt = roomService.findById(booking.getRoom().getId());
//            if (!roomOpt.isPresent()) {
//                response.put("Status", 0);
//                response.put("Message", "Room not found with ID: " + booking.getRoom().getId());
//                response.put("Data", "");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            // Set the user and room on the booking
//            booking.setUser(currentUser.get());
//            booking.setRoom(roomOpt.get());
//
//            // Create the booking
//            Booking createdBooking = bookingService.createBooking(booking);
//
//            response.put("Status", 1);
//            response.put("Message", "Booking created successfully");
//            response.put("Data", new BookingResponseDTO(createdBooking));
//            return ResponseEntity.ok(response);
//        } catch (IllegalArgumentException e) {
//            response.put("Status", 0);
//            response.put("Message", e.getMessage());
//            response.put("Data", "");
//            return ResponseEntity.badRequest().body(response);
//        } catch (Exception e) {
//            response.put("Status", 0);
//            response.put("Message", "Failed to create booking: " + e.getMessage());
//            response.put("Data", "");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
    }
    /*
     * ADDED: Create a new recurring booking for a semester.
     * @param request the details of the recurrent booking.
     * @param auth The authentication object for the current user.
     * @return  A list of Created bookings or an error message*/
    @PostMapping("/recurring")
    @PreAuthorize("isAuthenticated()") //only a user can make a recurring booking
    public ResponseEntity<Map<String, Object>> createRecurringBooking(@RequestBody RecurringBookingRequest request,
                                                                      Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> currentUser = userService.findByUsername(auth.getName());
            if (!currentUser.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }

            List<Booking> createdBookings = bookingService.createRecurringBookings(request, currentUser.get());

            List<BookingResponseDTO> bookingDTOs = createdBookings.stream()
                    .map(BookingResponseDTO::new)
                    .collect(Collectors.toList());

            response.put("Status", 1);
            response.put("Message", "Recurring bookings created successfully. Total bookings made: " + bookingDTOs.size());
            response.put("Data", bookingDTOs);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("Status", 0);
            response.put("Message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to create recurring bookings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/by-room-number/{roomNumber}")
    public ResponseEntity<Map<String, Object>> createBookingByRoomNumber(@PathVariable String roomNumber,
                                                                         @RequestBody Booking booking,
                                                                         Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> currentUser = userService.findByUsername(auth.getName());
            if (!currentUser.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "User not found");
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Room> roomOpt = roomService.findByRoomNumber(roomNumber);
            if (!roomOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Room not found");
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            booking.setUser(currentUser.get());
            booking.setRoom(roomOpt.get());

            Booking createdBooking = bookingService.createBooking(booking);

            response.put("Status", 1);
            response.put("Message", "Booking created successfully");
            response.put("Data", new BookingResponseDTO(createdBooking));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Error: " + e.getMessage());
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint to update an existing booking.
     * A user can only update their own future bookings.*/
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Or hasRole('USER')
    public ResponseEntity<Map<String, Object>> updateBooking(@PathVariable Long id, @Valid @RequestBody BookingUpdateDTO updateDTO) {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        try {
            BookingResponseDTO updatedBookingDTO = bookingService.updateBooking(id, updateDTO, username);
            response.put("Status", 1);
            response.put("Message", "Booking updated successfully");
            response.put("Data", updatedBookingDTO);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            response.put("Status", 0);
            response.put("Message", e.getMessage());
            response.put("Data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (SecurityException e) {
            response.put("Status", 0);
            response.put("Message", e.getMessage());
            response.put("Data", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (IllegalArgumentException e) {
            response.put("Status", 0);
            response.put("Message", e.getMessage());
            response.put("Data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "An unexpected error occurred: " + e.getMessage());
            response.put("Data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cancel a booking
     * @param bookingId Booking ID to cancel
     * @param auth Authentication object
     * @return Updated booking
     */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long bookingId,
                                                 Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> currentUser = userService.findByUsername(auth.getName());
            if (!currentUser.isPresent()) {
                response.put("Status",0);
                response.put("Message","Bad request");
                response.put("Data","");
                return ResponseEntity.badRequest().build();
            }

            Booking cancelledBooking = bookingService.cancelBooking(bookingId, currentUser.get().getId());
            response.put("Status",1);
            response.put("Message","Booking cancelled successfully");
            response.put("Data","");
            return ResponseEntity.ok(cancelledBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get current user's bookings
     * @param auth Authentication object
     * @return List of user's bookings in standardized format
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<Map<String, Object>> getMyBookings(Authentication auth) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> currentUser = userService.findByUsername(auth.getName());
            if (!currentUser.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "User not found");
                response.put("Data", "");
                //response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            List<Booking> bookings = bookingService.getUserBookings(currentUser.get());
            List<BookingResponseDTO> bookingDTOs = bookings.stream()
                    .map(BookingResponseDTO::new)
                    .collect(Collectors.toList());

            response.put("Status", 1);
            response.put("Message", "Bookings retrieved successfully");
            response.put("Data", bookingDTOs);
            //response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Failed to retrieve bookings: " + e.getMessage());
            response.put("Data", "");
            // response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    /**
     * Get all current bookings (Admin only)
     * @return List of current bookings
     */
    @GetMapping("/current")
    public ResponseEntity<List<Booking>> getCurrentBookings() {
        List<Booking> currentBookings = bookingService.getCurrentBookings();
        return ResponseEntity.ok(currentBookings);
    }

    @GetMapping("/room/{roomId}/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingBookingsForRoom(@PathVariable Long roomId, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Verify user authentication
            Optional<User> currentUser = userService.findByUsername(auth.getName());
            if (!currentUser.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "User not found");
                response.put("Data", "");
                //response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            // Fetch room from RoomService
            Optional<Room> roomOpt = roomService.findById(roomId);
            if (!roomOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Room not found with id: " + roomId);
                response.put("Data", "");
                response.put("Token", "");
                return ResponseEntity.badRequest().body(response);
            }

            // Get upcoming bookings
            List<Booking> bookings = bookingService.getUpcomingBookingsForRoom(roomOpt.get());
            List<BookingResponseDTO> bookingDTOs = bookings.stream()
                    .map(BookingResponseDTO::new)
                    .collect(Collectors.toList());

            response.put("Status", 1);
            response.put("Message", "Upcoming bookings retrieved successfully");
            response.put("Data", bookingDTOs);
            //response.put("Token", "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0 );
            response.put("Message", "Failed to retrieve upcoming bookings: " + e.getMessage());
            response.put("Data", "");
            //response.put("Token", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/room-number/{roomNumber}/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingBookingsForRoomNumber(@PathVariable String roomNumber,
                                                                                Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<User> currentUser = userService.findByUsername(auth.getName());
            if (!currentUser.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "User not found");
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Room> roomOpt = roomService.findByRoomNumber(roomNumber);
            if (!roomOpt.isPresent()) {
                response.put("Status", 0);
                response.put("Message", "Room not found");
                response.put("Data", "");
                return ResponseEntity.badRequest().body(response);
            }

            List<Booking> bookings = bookingService.getUpcomingBookingsForRoom(roomOpt.get());
            List<BookingResponseDTO> bookingDTOs = bookings.stream()
                    .map(BookingResponseDTO::new)
                    .collect(Collectors.toList());

            response.put("Status", 1);
            response.put("Message", "Upcoming bookings retrieved successfully");
            response.put("Data", bookingDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("Status", 0);
            response.put("Message", "Error: " + e.getMessage());
            response.put("Data", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
