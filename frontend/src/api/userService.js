import api from './axios';

const userService = {
    getAllUsers: async () => {
        const response = await api.get('/api/users');
        return response.data;
    },
    getUserById: async (id) => {
        const response = await api.get(`/api/users/${id}`);
        return response.data;
    },
    updateUser: async (id, userData) => {
        const response = await api.put(`/api/users/${id}`, userData);
        return response.data;
    },
    deleteUser: async (id) => {
        const response = await api.delete(`/api/users/${id}`);
        return response.data;
    },
    getProfile: async () => {
        const response = await api.get('/api/users/me');
        return response.data;
    },
    updateProfile: async (userData) => {
        const response = await api.put('/api/users/me', userData);
        return response.data;
    },
    updatePassword: async (passwordData) => {
        const response = await api.put('/api/users/me/password', passwordData);
        return response.data;
    }
};

export default userService;
