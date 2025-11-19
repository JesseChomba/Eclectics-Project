import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Link } from 'react-router-dom';
import Button from '../../components/Button';
import Card, { CardBody } from '../../components/Card';
import { Calendar, Plus } from 'lucide-react';

const UserDashboard = () => {
    const { user } = useAuth();

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900">User Dashboard</h1>
            <p className="text-gray-600">Welcome back, {user?.username}!</p>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                <Card>
                    <CardBody className="flex flex-col items-center justify-center p-8 text-center">
                        <div className="bg-blue-100 p-4 rounded-full mb-4">
                            <Plus className="h-8 w-8 text-blue-600" />
                        </div>
                        <h3 className="text-lg font-medium text-gray-900 mb-2">Book a Room</h3>
                        <p className="text-gray-500 mb-6">Find and book available rooms for your meetings or classes.</p>
                        <Link to="/book-room">
                            <Button>Book Now</Button>
                        </Link>
                    </CardBody>
                </Card>

                <Card>
                    <CardBody className="flex flex-col items-center justify-center p-8 text-center">
                        <div className="bg-green-100 p-4 rounded-full mb-4">
                            <Calendar className="h-8 w-8 text-green-600" />
                        </div>
                        <h3 className="text-lg font-medium text-gray-900 mb-2">My Bookings</h3>
                        <p className="text-gray-500 mb-6">View and manage your upcoming and past bookings.</p>
                        <Link to="/my-bookings">
                            <Button variant="outline">View Bookings</Button>
                        </Link>
                    </CardBody>
                </Card>
            </div>
        </div>
    );
};

export default UserDashboard;
