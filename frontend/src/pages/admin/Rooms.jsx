import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import roomService from '../../api/roomService';
import Button from '../../components/Button';
import Card, { CardHeader, CardBody } from '../../components/Card';
import { Plus, Edit, Trash2 } from 'lucide-react';
import { toast } from 'react-toastify';

const Rooms = () => {
    const [rooms, setRooms] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchRooms();
    }, []);

    const fetchRooms = async () => {
        try {
            const response = await roomService.getAllRooms();
            if (response.Status === 1) {
                setRooms(response.Data);
            } else {
                toast.error(response.Message);
            }
        } catch (error) {
            toast.error("Failed to fetch rooms");
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Are you sure you want to delete this room?")) {
            try {
                const response = await roomService.deleteRoom(id);
                if (response.Status === 1) {
                    toast.success("Room deleted successfully");
                    fetchRooms();
                } else {
                    toast.error(response.Message);
                }
            } catch (error) {
                toast.error("Failed to delete room");
            }
        }
    };

    if (loading) return <div>Loading...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-gray-900">Manage Rooms</h1>
                <Link to="/admin/rooms/new">
                    <Button>
                        <Plus className="w-4 h-4 mr-2" />
                        Add Room
                    </Button>
                </Link>
            </div>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
                {rooms.map((room) => (
                    <Card key={room.id}>
                        <CardHeader className="flex justify-between items-center">
                            <h3 className="text-lg font-medium text-gray-900">{room.name}</h3>
                            <span className={`px-2 py-1 text-xs font-semibold rounded-full ${room.status === 'AVAILABLE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                                }`}>
                                {room.status}
                            </span>
                        </CardHeader>
                        <CardBody>
                            <p className="text-sm text-gray-500 mb-2">Room Number: {room.roomNumber}</p>
                            <p className="text-sm text-gray-500 mb-2">Capacity: {room.capacity}</p>
                            <p className="text-sm text-gray-500 mb-4">Type: {room.type}</p>

                            <div className="flex justify-end space-x-2">
                                <Link to={`/admin/rooms/edit/${room.id}`}>
                                    <Button variant="outline" size="sm">
                                        <Edit className="w-4 h-4" />
                                    </Button>
                                </Link>
                                <Button variant="danger" size="sm" onClick={() => handleDelete(room.id)}>
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

export default Rooms;
