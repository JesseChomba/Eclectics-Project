import api from './axios';

const adminService = {
    getDashboardStats: async () => {
        const response = await api.get('/api/admin/dashboard/stats');
        return response.data;
    }
};

export default adminService;
