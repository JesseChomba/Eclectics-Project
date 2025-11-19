import React, { useEffect, useState } from 'react';
import bookingService from '../../api/bookingService';
import Button from '../../components/Button';
import Card, { CardHeader, CardBody } from '../../components/Card';
import { toast } from 'react-toastify';
import { Calendar, dateFnsLocalizer } from 'react-big-calendar';
import { format, parse, startOfWeek, getDay } from 'date-fns';
import { enUS } from 'date-fns/locale';
import 'react-big-calendar/lib/css/react-big-calendar.css';

const locales = {
    'en-US': enUS,
};

const localizer = dateFnsLocalizer({
    format,
    parse,
    startOfWeek,
    getDay,
    locales,
});

const MyBookings = () => {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [viewMode, setViewMode] = useState('list'); // 'list' or 'calendar'
    const [calendarView, setCalendarView] = useState('month'); // 'month', 'week', 'day', 'agenda'
    const [calendarDate, setCalendarDate] = useState(new Date());

    useEffect(() => {
        fetchBookings();
    }, []);

    const fetchBookings = async () => {
        try {
            const response = await bookingService.getMyBookings();
            if (response.Status === 1) {
                setBookings(response.Data);
            } else {
                toast.error(response.Message);
            }
        } catch (error) {
            toast.error("Failed to fetch bookings");
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = async (id) => {
        if (window.confirm("Are you sure you want to cancel this booking?")) {
            try {
                const response = await bookingService.cancelBooking(id);
                if (response.Status === 1) {
                    toast.success("Booking cancelled successfully");
                    fetchBookings();
                } else {
                    toast.error(response.Message);
                }
            } catch (error) {
                toast.error("Failed to cancel booking");
            }
        }
    };

    const calendarEvents = bookings.map(booking => ({
        title: `${booking.roomName} (${booking.roomNumber})`,
        start: new Date(booking.startTime),
        end: new Date(booking.endTime),
        resource: booking,
    }));

    if (loading) return <div>Loading...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-gray-900">My Bookings</h1>
                <div className="flex space-x-2">
                    <Button
                        variant={viewMode === 'list' ? 'primary' : 'outline'}
                        onClick={() => setViewMode('list')}
                    >
                        List View
                    </Button>
                    <Button
                        variant={viewMode === 'calendar' ? 'primary' : 'outline'}
                        onClick={() => setViewMode('calendar')}
                    >
                        Calendar View
                    </Button>
                </div>
            </div>

            {viewMode === 'list' ? (
                bookings.length === 0 ? (
                    <p>No bookings found.</p>
                ) : (
                    <div className="space-y-4">
                        {bookings.map((booking) => (
                            <Card key={booking.id}>
                                <CardBody className="flex flex-col md:flex-row justify-between items-start md:items-center">
                                    <div>
                                        <h3 className="text-lg font-medium text-gray-900">
                                            {booking.roomName} ({booking.roomNumber})
                                        </h3>
                                        <p className="text-sm text-gray-500">
                                            {new Date(booking.startTime).toLocaleString()} - {new Date(booking.endTime).toLocaleString()}
                                        </p>
                                        <p className="text-sm text-gray-600 mt-1">Purpose: {booking.purpose}</p>
                                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium mt-2 ${booking.status === 'CONFIRMED' ? 'bg-green-100 text-green-800' :
                                            booking.status === 'CANCELLED' ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'
                                            }`}>
                                            {booking.status}
                                        </span>
                                    </div>
                                    {booking.status !== 'CANCELLED' && new Date(booking.startTime) > new Date() && (
                                        <Button variant="danger" size="sm" className="mt-4 md:mt-0" onClick={() => handleCancel(booking.id)}>
                                            Cancel Booking
                                        </Button>
                                    )}
                                </CardBody>
                            </Card>
                        ))}
                    </div>
                )
            ) : (
                <div className="h-[600px] bg-white p-4 rounded-lg shadow">
                    <Calendar
                        localizer={localizer}
                        events={calendarEvents}
                        startAccessor="start"
                        endAccessor="end"
                        style={{ height: '100%' }}
                        view={calendarView}
                        onView={(view) => setCalendarView(view)}
                        date={calendarDate}
                        onNavigate={(date) => setCalendarDate(date)}
                        onSelectEvent={(event) => {
                            if (event.resource.status !== 'CANCELLED' && new Date(event.start) > new Date()) {
                                handleCancel(event.resource.id);
                            } else {
                                toast.info(`Booking: ${event.title}\nStatus: ${event.resource.status}`);
                            }
                        }}
                        eventPropGetter={(event) => {
                            let newStyle = {
                                backgroundColor: '#3174ad',
                                color: 'white',
                                borderRadius: '0px',
                                border: 'none'
                            };
                            if (event.resource.status === 'CANCELLED') {
                                newStyle.backgroundColor = '#ef4444'; // red-500
                            } else if (event.resource.status === 'PENDING') {
                                newStyle.backgroundColor = '#eab308'; // yellow-500
                            } else {
                                newStyle.backgroundColor = '#22c55e'; // green-500
                            }
                            return {
                                className: "",
                                style: newStyle
                            };
                        }}
                    />
                </div>
            )}
        </div>
    );
};

export default MyBookings;
