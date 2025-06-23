package com.smartroom.allocation.service;

import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send booking confirmation email for a single booking.
     * @param booking Confirmed booking
     */
    public void sendBookingConfirmation(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(booking.getUser().getEmail());
            message.setSubject("Room Booking Confirmation - " + booking.getRoom().getRoomNumber());
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your room booking has been confirmed:\n\n" +
                            "Room: %s (%s)\n" +
                            "Date & Time: %s to %s\n" +
                            "Purpose: %s\n\n" +
                            "Please arrive on time. If you need to cancel, please do so at least 30 minutes before the start time.\n\n" +
                            "Best regards,\n" +
                            "Smart Room Allocation System",
                    booking.getUser().getFullName(),
                    booking.getRoom().getRoomNumber(),
                    booking.getRoom().getName(),
                    booking.getStartTime().toString(),
                    booking.getEndTime().toString(),
                    booking.getPurpose()
            ));

            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't fail the booking process
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    /**
     * ADDED: Send a single summary email for a series of recurring bookings.
     * @param bookings The list of confirmed recurring bookings.
     */
    public void sendRecurringBookingConfirmationSummary(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return; // Do nothing if there are no bookings
        }

        try {
            // Get common details from the first booking in the series
            Booking firstBooking = bookings.get(0);
            User user = firstBooking.getUser();
            Room room = firstBooking.getRoom();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

            // Build the list of dates
            StringBuilder datesList = new StringBuilder();
            for (Booking booking : bookings) {
                datesList.append("- ").append(booking.getStartTime().format(dateFormatter)).append("\n");
            }

            // Construct the summary email
            String subject = String.format("Recurring Booking Summary for Room %s", room.getRoomNumber());
            String body = String.format(
                    "Dear %s,\n\n" +
                            "Your recurring room booking has been confirmed. Details are as follows:\n\n" +
                            "Room: %s (%s)\n" +
                            "Time: %s to %s\n" +
                            "Purpose: %s\n\n" +
                            "This booking will recur on the following dates:\n" +
                            "%s\n" +
                            "Please note that each of these is a separate booking. If you need to cancel a single day, you can do so from your bookings list without affecting the rest of the series.\n\n" +
                            "Best regards,\n" +
                            "Smart Room Allocation System",
                    user.getFullName(),
                    room.getRoomNumber(),
                    room.getName(),
                    firstBooking.getStartTime().format(timeFormatter),
                    firstBooking.getEndTime().format(timeFormatter),
                    firstBooking.getPurpose(),
                    datesList.toString()
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println("Failed to send recurring booking summary email: " + e.getMessage());
        }
    }

    /**
     * Send cancellation notification email
     * @param booking Cancelled booking
     */
    public void sendCancellationNotification(Booking booking) {
        // This method remains unchanged
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(booking.getUser().getEmail());
            message.setSubject("Room Booking Cancelled - " + booking.getRoom().getRoomNumber());
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your room booking has been cancelled:\n\n" +
                            "Room: %s (%s)\n" +
                            "Date & Time: %s to %s\n" +
                            "Purpose: %s\n\n" +
                            "The room is now available for other bookings.\n\n" +
                            "Best regards,\n" +
                            "Smart Room Allocation System",
                    booking.getUser().getFullName(),
                    booking.getRoom().getRoomNumber(),
                    booking.getRoom().getName(),
                    booking.getStartTime().toString(),
                    booking.getEndTime().toString(),
                    booking.getPurpose()
            ));

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }
    }
    /**
     * Send welcome email to newly registered user
     * @param user Newly registered user
     */
    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Welcome to Smart Room Allocation System!");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Welcome to the Smart Room Allocation System! We're excited to have you on board.\n\n" +
                            "Your Account Details:\n" +
                            "Username: %s\n" +
                            "Full Name: %s\n" +
                            "Role: %s\n" +
                            "Department: %s\n\n" +
                            "You can now book rooms for your meetings, classes, or events. Visit our platform to explore available rooms and start booking.\n\n" +
                            "If you have any questions, feel free to contact our support team.\n\n" +
                            "Best regards,\n" +
                            "Smart Room Allocation System",
                    user.getFullName(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole().toString(),
                    user.getDepartment() != null ? user.getDepartment() : "Not specified"
            ));

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    /**
     * Send account deletion notification email
     * @param user Deleted user
     */
    public void sendDeletionNotification(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Smart Room Allocation System - Account Deletion Confirmation");
            message.setText(String.format(
                    "Dear %s,\n\n" +
                            "Your account with the Smart Room Allocation System has been successfully deleted.\n\n" +
                            "Account Details:\n" +
                            "Username: %s\n" +
                            "Full Name: %s\n" +
                            "Role: %s\n" +
                            "Department: %s\n\n" +
                            "All your data has been removed from our system. If you change your mind, you can register again at any time.\n\n" +
                            "Thank you for using our service. If you have any feedback, please feel free to share it with us.\n\n" +
                            "Best regards,\n" +
                            "Smart Room Allocation System",
                    user.getFullName(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole().toString(),
                    user.getDepartment() != null ? user.getDepartment() : "Not specified"
            ));

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send deletion notification email: " + e.getMessage());
        }
    }
}