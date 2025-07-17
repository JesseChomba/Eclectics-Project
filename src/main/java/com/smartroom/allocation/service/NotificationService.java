package com.smartroom.allocation.service;
import com.smartroom.allocation.dto.BookingResponseDTO;
import com.smartroom.allocation.entity.Booking;
import com.smartroom.allocation.entity.Room;
import com.smartroom.allocation.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger= LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter FORMATTER= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private JavaMailSender mailSender;

    /*
     * Send Email to users after Equipment details have been changed after a booking
     * has been made to update them of the changes so they're not caught unaware*/
    @Async
    public void sendEquipmentUpdateNotification(List<String> recipientEmails, String subject, String message){
        try{
            for (String email : recipientEmails){
                MimeMessage mimeMessage= mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(message,true);
                mailSender.send(mimeMessage);
                logger.info("Notification sent to {}",email);
            }
        } catch (MessagingException e) {
            logger.error("Failed to send notification to {}:{}",recipientEmails,e.getMessage());
        }
    }

    /**
     * Send booking confirmation email for a single booking.
     * @param booking Confirmed booking
     */
    public void sendBookingConfirmation(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(booking.getUser().getEmail());
            //Ensure room and user objects are not null before accessing their properties
            String roomNumber =booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "N/A";
            String roomName = booking.getRoom() != null ? booking.getRoom().getName() : "N/A";
            String userName = booking.getUser() != null ? booking.getUser().getFullName() : "User";

            message.setSubject("Room Booking Confirmation - " + booking.getRoom().getRoomNumber());
            message.setText(String.format(
                    """
                            Dear %s,
                            
                            Your room booking has been confirmed:
                            
                            Room: %s (%s)
                            Date & Time: %s to %s
                            Purpose: %s
                            
                            Please arrive on time. If you need to cancel, please do so at least 30 minutes before the start time.
                            
                            Best regards,
                            Smart Room Allocation System""",
                    //booking.getUser().getFullName(),
                    userName,
                    //booking.getRoom().getRoomNumber(),
                    roomNumber,
                    //booking.getRoom().getName(),
                    roomName,
                    booking.getStartTime().toString(),
                    booking.getEndTime().toString(),
                    booking.getPurpose()
            ));

            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't fail the booking process
            System.err.println("Failed to send confirmation email: " + e.getMessage());
            logger.error("Failed to send confirmation email: {}", e.getMessage(),e); //Using logger for better error handling
        }
    }

    /*
     * Send email notification for a booking update
     * @param oldBooking The original booking details
     * @param newBooking The updated booking details
     * */
    public void sendBookingUpdatedEmail(Booking oldBooking, Booking newBooking){
        SimpleMailMessage message =new SimpleMailMessage();
        message.setTo(newBooking.getUser().getEmail());
        message.setSubject("Booking Updated: " +newBooking.getRoom().getName());
        String text = String.format (
                """
                        Dear %s,
                        
                        Your booking details for room %s have been updated.
                        
                        Original Details:
                           Room:   %s
                           Start Time: %s
                           End Time:   %s
                           Purpose:    %s
                        New Details:
                           Room:   %s
                           Start Time: %s
                           End Time:   %s
                           Purpose:    %s
                        Please arrive on time. If you need to cancel, please do so at least 30 minutes before the start time.
                        
                        Best regards,
                        Smart Room Allocation System""",
                newBooking.getUser().getFullName(),
                oldBooking.getRoom().getRoomNumber(),
                oldBooking.getRoom().getName(),
                oldBooking.getStartTime().format(FORMATTER),
                oldBooking.getEndTime().format(FORMATTER),
                oldBooking.getPurpose(),
                newBooking.getRoom().getName(),
                newBooking.getStartTime().format(FORMATTER),
                newBooking.getEndTime().format(FORMATTER),
                newBooking.getPurpose());
        message.setText(text);
        try {
            mailSender.send(message);
            logger.info("Booking update email sent to {}", newBooking.getUser().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send booking update email to {}: {}", newBooking.getUser().getEmail(), e.getMessage());
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
                    """
                            Dear %s,
                            
                            Your recurring room booking has been confirmed. Details are as follows:
                            
                            Room: %s (%s)
                            Time: %s to %s
                            Purpose: %s
                            
                            This booking will recur on the following dates:
                            %s
                            Please note that each of these is a separate booking. If you need to cancel a single day, you can do so from your bookings list without affecting the rest of the series.
                            
                            Best regards,
                            Smart Room Allocation System""",
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
                    """
                            Dear %s,
                            
                            Your room booking has been cancelled:
                            
                            Room: %s (%s)
                            Date & Time: %s to %s
                            Purpose: %s
                            
                            The room is now available for other bookings.
                            
                            Best regards,
                            Smart Room Allocation System""",
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
                    """
                            Dear %s,
                            
                            Welcome to the Smart Room Allocation System! We're excited to have you on board.
                            
                            Your Account Details:
                            Username: %s
                            Full Name: %s
                            Role: %s
                            Department: %s
                            
                            You can now book rooms for your meetings, classes, or events. Visit our platform to explore available rooms and start booking.
                            
                            If you have any questions, feel free to contact our support team.
                            
                            Best regards,
                            Smart Room Allocation System""",
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
                    """
                            Dear %s,
                            
                            Your account with the Smart Room Allocation System has been successfully deleted.
                            
                            Account Details:
                            Username: %s
                            Full Name: %s
                            Role: %s
                            Department: %s
                            
                            All your data has been removed from our system. If you change your mind, you can register again at any time.
                            
                            Thank you for using our service. If you have any feedback, please feel free to share it with us.
                            
                            Best regards,
                            Smart Room Allocation System""",
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

    /**
     * Send user update notification email
     * @param user Updated user
     */

    @Async
    public void sendUserUpdateNotification(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Smart Room Allocation System - Account Update Notification");
            message.setText(String.format(
                    """
                            Dear %s,
                            
                            Your account details in the Smart Room Allocation System have been updated.
                            
                            Updated Account Details:
                            Username: %s
                            Full Name: %s
                            Email: %s
                            Role: %s
                            Department: %s
                            Active Status: %s
                            
                            If you did not request these changes, please contact our support team immediately.
                            
                            Best regards,
                            Smart Room Allocation System""",
                    user.getFullName(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().toString(),
                    user.getDepartment() != null ? user.getDepartment() : "Not specified",
                    user.getActive() !=null && user.getActive() ? "Active" : "Inactive"
            ));

            mailSender.send(message);
            logger.info("User update notification sent to {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send user update notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * Send room update notification email to users with upcoming bookings
     * @param room Updated room
     * @param recipientEmails List of user emails to notify */
    @Async
    public void sendRoomUpdateNotification(Room room, List<String> recipientEmails) {
        try {
            for (String email : recipientEmails) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Smart Room Allocation System - Room Update Notification");
                message.setText(String.format(
                        """
                                Dear User,
                                
                                The details of a room you have booked have been updated.
                                
                                Updated Room Details:
                                Room Number: %s
                                Name: %s
                                Building: %s
                                Floor: %s
                                Location: %s
                                Room Type: %s
                                Capacity: %d
                                Status: %s
                                Active: %s
                                
                                Please review your upcoming bookings to ensure this room still meets your needs.
                                
                                Best regards,
                                Smart Room Allocation System""",
                        room.getRoomNumber(),
                        room.getName(),
                        room.getBuilding() != null ? room.getBuilding() : "Not specified",
                        room.getFloor() != null ? room.getFloor() : "Not specified",
                        room.getLocation() != null ? room.getLocation() : "Not specified",
                        room.getRoomType() != null ? room.getRoomType().toString() : "Not specified",
                        room.getCapacity(),
                        room.getStatus() != null ? room.getStatus().toString() : "Not specified",
                        room.isActive() ? "Active" : "Inactive"
                ));

                mailSender.send(message);
                logger.info("Room update notification sent to {}", email);
            }
        } catch (Exception e) {
            logger.error("Failed to send room update notification to {}: {}", recipientEmails, e.getMessage());
        }
    }
}