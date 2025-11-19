import React, { useEffect, useState } from 'react';
import equipmentService from '../../api/equipmentService';
import Button from '../../components/Button';
import Card, { CardHeader, CardBody } from '../../components/Card';
import { Plus, Edit, Trash2 } from 'lucide-react';
import { toast } from 'react-toastify';
import { Link } from 'react-router-dom';

const Equipment = () => {
    const [equipmentList, setEquipmentList] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchEquipment();
    }, []);

    const fetchEquipment = async () => {
        try {
            const response = await equipmentService.getAllEquipment();
            if (response.Status === 1) {
                setEquipmentList(response.Data);
            } else {
                toast.error(response.Message);
            }
        } catch (error) {
            toast.error("Failed to fetch equipment");
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Are you sure you want to delete this equipment?")) {
            try {
                const response = await equipmentService.deleteEquipment(id);
                if (response.Status === 1) {
                    toast.success("Equipment deleted successfully");
                    fetchEquipment();
                } else {
                    toast.error(response.Message);
                }
            } catch (error) {
                toast.error("Failed to delete equipment");
            }
        }
    };

    if (loading) return <div>Loading...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-gray-900">Manage Equipment</h1>
                {/* Link to Add Equipment Page - To be implemented if needed, or use modal */}
                {/* For now, let's just list them. Adding requires a form. */}
                <Button disabled title="Not implemented yet">
                    <Plus className="w-4 h-4 mr-2" />
                    Add Equipment
                </Button>
            </div>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                {equipmentList.map((item) => (
                    <Card key={item.id}>
                        <CardHeader>
                            <h3 className="text-lg font-medium text-gray-900">{item.name}</h3>
                            <span className={`px-2 py-1 text-xs font-semibold rounded-full ${item.working ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                                }`}>
                                {item.working ? 'Working' : 'Faulty'}
                            </span>
                        </CardHeader>
                        <CardBody>
                            <p className="text-sm text-gray-500 mb-2">Type: {item.type}</p>
                            <p className="text-sm text-gray-500 mb-2">Room: {item.roomName || 'Unassigned'}</p>
                            <p className="text-sm text-gray-500 mb-4">{item.description}</p>

                            <div className="flex justify-end space-x-2">
                                {/* Edit button placeholder */}
                                <Button variant="danger" size="sm" onClick={() => handleDelete(item.id)}>
                                    <Trash2 className="w-4 h-4" />
                                </Button>
                            </div>
                        </CardBody>
                    </Card>
                ))}
            </div>
        </div>
    );
};

export default Equipment;
