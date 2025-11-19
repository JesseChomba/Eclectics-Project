import api from './axios';

const bookingService = {
    createBooking: async (bookingData) => {
        const response = await api.post('/api/bookings', bookingData);
        return response.data;
    },
    getMyBookings: async () => {
        const response = await api.get('/api/bookings/my-bookings');
        return response.data;
    },
    cancelBooking: async (id) => {
        const response = await api.put(`/api/bookings/${id}/cancel`);
        return response.data;
    },
    getUpcomingBookingsForRoom: async (roomId) => {
        const response = await api.get(`/api/bookings/room/${roomId}/upcoming`);
        return response.data;
    }
};

export default bookingService;
