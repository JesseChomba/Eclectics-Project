import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import roomService from '../../api/roomService';
import bookingService from '../../api/bookingService';
import Button from '../../components/Button';
import Input from '../../components/Input';
import Card, { CardHeader, CardBody } from '../../components/Card';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';

const BookRoom = () => {
    const { register, handleSubmit, watch, formState: { errors } } = useForm();
    const [availableRooms, setAvailableRooms] = useState([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const onSearch = async (data) => {
        setLoading(true);
        try {
            // Format dates to ISO string as expected by backend (yyyy-MM-dd'T'HH:mm:ss)
            // Input type="datetime-local" returns "yyyy-MM-ddTHH:mm"
            // We might need to append seconds if backend requires it, but usually ISO format without seconds works or we append :00
            const startTime = data.startTime + ":00";
            const endTime = data.endTime + ":00";

            const response = await roomService.getAvailableRooms(startTime, endTime);
            if (response.Status === 1) {
                setAvailableRooms(response.Data);
                if (response.Data.length === 0) {
                    toast.info("No rooms available for the selected time.");
                }
            } else {
                toast.error(response.Message);
            }
        } catch (error) {
            toast.error("Failed to search rooms");
        } finally {
            setLoading(false);
        }
    };

    const handleBook = async (roomId) => {
        const startTime = watch('startTime') + ":00";
        const endTime = watch('endTime') + ":00";
        const purpose = watch('purpose');

        if (!purpose) {
            toast.error("Please enter a purpose for the booking");
            return;
        }

        try {
            const bookingData = {
                room: { id: roomId },
                startTime: startTime,
                endTime: endTime,
                purpose: purpose
            };

            const response = await bookingService.createBooking(bookingData);
            if (response.Status === 1) {
                toast.success("Booking created successfully!");
                navigate('/my-bookings');
            } else {
                toast.error(response.Message);
            }
        } catch (error) {
            toast.error("Failed to create booking");
        }
    };

    return (
        <div className="space-y-6">
            <Card>
                <CardHeader>
                    <h2 className="text-xl font-bold text-gray-900">Book a Room</h2>
                </CardHeader>
                <CardBody>
                    <form onSubmit={handleSubmit(onSearch)} className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <Input
                                label="Start Time"
                                type="datetime-local"
                                {...register('startTime', { required: 'Start Time is required' })}
                                error={errors.startTime}
                            />
                            <Input
                                label="End Time"
                                type="datetime-local"
                                {...register('endTime', { required: 'End Time is required' })}
                                error={errors.endTime}
                            />
                        </div>
                        <Input
                            label="Purpose"
                            placeholder="Meeting, Class, etc."
                            {...register('purpose', { required: 'Purpose is required' })}
                            error={errors.purpose}
                        />
                        <Button type="submit" isLoading={loading}>
                            Search Available Rooms
                        </Button>
                    </form>
                </CardBody>
            </Card>

            {availableRooms.length > 0 && (
                <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {availableRooms.map((room) => (
                        <Card key={room.id}>
                            <CardHeader>
                                <h3 className="text-lg font-medium text-gray-900">{room.name}</h3>
                                <span className="text-sm text-gray-500">{room.roomNumber}</span>
                            </CardHeader>
                            <CardBody>
                                <p className="text-sm text-gray-500 mb-2">Capacity: {room.capacity}</p>
                                <p className="text-sm text-gray-500 mb-4">Type: {room.type}</p>
                                <Button className="w-full" onClick={() => handleBook(room.id)}>
                                    Book Now
                                </Button>
                            </CardBody>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
};

export default BookRoom;
