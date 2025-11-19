import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import userService from '../../api/userService';
import Button from '../../components/Button';
import Input from '../../components/Input';
import Card, { CardHeader, CardBody } from '../../components/Card';
import { toast } from 'react-toastify';

const Profile = () => {
    const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm();
    const [user, setUser] = useState(null);

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            const response = await userService.getProfile();
            if (response.Status === 1) {
                setUser(response.Data);
            }
        } catch (error) {
            toast.error("Failed to fetch profile");
        }
    };

    const onSubmitPassword = async (data) => {
        try {
            const response = await userService.updatePassword({
                oldPassword: data.oldPassword,
                newPassword: data.newPassword
            });
            if (response.Status === 1) {
                toast.success("Password updated successfully");
                reset();
            } else {
                toast.error(response.Message);
            }
        } catch (error) {
            toast.error("Failed to update password");
        }
    };

    if (!user) return <div>Loading...</div>;

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <Card>
                <CardHeader>
                    <h2 className="text-xl font-bold text-gray-900">My Profile</h2>
                </CardHeader>
                <CardBody>
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Username</label>
                            <p className="mt-1 text-gray-900">{user.username}</p>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Email</label>
                            <p className="mt-1 text-gray-900">{user.email}</p>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Role</label>
                            <p className="mt-1 text-gray-900">{user.role}</p>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Total Bookings</label>
                            <p className="mt-1 text-gray-900">{user.totalBookings}</p>
                        </div>
                    </div>
                </CardBody>
            </Card>

            <Card>
                <CardHeader>
                    <h2 className="text-xl font-bold text-gray-900">Change Password</h2>
                </CardHeader>
                <CardBody>
                    <form onSubmit={handleSubmit(onSubmitPassword)} className="space-y-4">
                        <Input
                            label="Current Password"
                            type="password"
                            {...register('oldPassword', { required: 'Current Password is required' })}
                            error={errors.oldPassword}
                        />
                        <Input
                            label="New Password"
                            type="password"
                            {...register('newPassword', { required: 'New Password is required', minLength: { value: 6, message: 'Minimum 6 characters' } })}
                            error={errors.newPassword}
                        />
                        <Button type="submit" isLoading={isSubmitting}>
                            Update Password
                        </Button>
                    </form>
                </CardBody>
            </Card>
        </div>
    );
};

export default Profile;
