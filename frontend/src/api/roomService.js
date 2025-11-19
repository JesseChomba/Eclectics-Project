import api from './axios';

const roomService = {
    getAllRooms: async () => {
        const response = await api.get('/api/rooms');
        return response.data;
    },
    getRoomById: async (id) => {
        const response = await api.get(`/api/rooms/${id}`);
        return response.data;
    },
    getAvailableRooms: async (startTime, endTime) => {
        const response = await api.get('/api/rooms/available', {
            params: { startTime, endTime }
        });
        return response.data;
    },
    createRoom: async (roomData) => {
        const response = await api.post('/api/rooms', roomData);
        return response.data;
    },
    updateRoom: async (id, roomData) => {
        const response = await api.put(`/api/rooms/${id}`, roomData);
        return response.data;
    },
    deleteRoom: async (id) => {
        const response = await api.delete(`/api/rooms/${id}`);
        return response.data;
    },
    updateRoomStatus: async (id, status) => {
        const response = await api.put(`/api/rooms/${id}/status`, { status });
        return response.data;
    }
};

export default roomService;
