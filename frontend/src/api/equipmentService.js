import api from './axios';

const equipmentService = {
    getAllEquipment: async () => {
        const response = await api.get('/api/equipment');
        return response.data;
    },
    getEquipmentById: async (id) => {
        const response = await api.get(`/api/equipment/${id}`);
        return response.data;
    },
    createEquipment: async (equipmentData) => {
        const response = await api.post('/api/equipment', equipmentData);
        return response.data;
    },
    updateEquipment: async (id, equipmentData) => {
        const response = await api.put(`/api/equipment/${id}`, equipmentData);
        return response.data;
    },
    deleteEquipment: async (id) => {
        const response = await api.delete(`/api/equipment/${id}`);
        return response.data;
    }
};

export default equipmentService;
