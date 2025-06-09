package com.smartroom.allocation.service;

import com.smartroom.allocation.entity.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send booking confirmation email
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
     * Send cancellation notification email
     * @param booking Cancelled booking
     */
    public void sendCancellationNotification(Booking booking) {
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
}