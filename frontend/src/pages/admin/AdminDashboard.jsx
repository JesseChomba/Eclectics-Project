import React, { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import adminService from '../../api/adminService';
import Card, { CardBody } from '../../components/Card';
import { Users, Calendar, Home, Activity } from 'lucide-react';
import { toast } from 'react-toastify';

const AdminDashboard = () => {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await adminService.getDashboardStats();
                if (response.Status === 1) {
                    setStats(response.Data);
                } else {
                    toast.error(response.Message);
                }
            } catch (error) {
                toast.error("Failed to fetch dashboard stats");
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, []);

    if (loading) return <div>Loading...</div>;

    const statItems = [
        { label: 'Total Bookings', value: stats?.totalBookings, icon: Calendar, color: 'bg-blue-500' },
        { label: 'Total Available Rooms', value: stats?.totalAvailableRooms, icon: Home, color: 'bg-green-500' },
        { label: 'Total Active Rooms', value: stats?.totalActiveRooms, icon: Activity, color: 'bg-indigo-500' },
        { label: 'Total Active Users', value: stats?.totalActiveUsers, icon: Users, color: 'bg-purple-500' },
        { label: 'Total Upcoming Bookings', value: stats?.totalUpcomingBookings, icon: Calendar, color: 'bg-yellow-500' },
    ];

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
            <p className="text-gray-600">Welcome back, {user?.username}!</p>

            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
                {statItems.map((item, index) => (
                    <Card key={index} className="overflow-hidden">
                        <CardBody className="p-5">
                            <div className="flex items-center">
                                <div className={`flex-shrink-0 rounded-md p-3 ${item.color}`}>
                                    <item.icon className="h-6 w-6 text-white" />
                                </div>
                                <div className="ml-5 w-0 flex-1">
                                    <dl>
                                        <dt className="text-sm font-medium text-gray-500 truncate">{item.label}</dt>
                                        <dd className="text-lg font-medium text-gray-900">{item.value}</dd>
                                    </dl>
                                </div>
                            </div>
                        </CardBody>
                    </Card>
                ))}
            </div>
        </div>
    );
};

export default AdminDashboard;
