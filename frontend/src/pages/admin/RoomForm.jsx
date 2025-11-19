import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, useParams } from 'react-router-dom';
import roomService from '../../api/roomService';
import Button from '../../components/Button';
import Input from '../../components/Input';
import Card, { CardHeader, CardBody, CardFooter } from '../../components/Card';
import { toast } from 'react-toastify';

const RoomForm = () => {
    const { register, handleSubmit, setValue, formState: { errors, isSubmitting } } = useForm();
    const navigate = useNavigate();
    const { id } = useParams();
    const isEditMode = !!id;

    useEffect(() => {
        if (isEditMode) {
            fetchRoom();
        }
    }, [id]);

    const fetchRoom = async () => {
        try {
            const response = await roomService.getRoomById(id);
            if (response.Status === 1) {
                const room = response.Data;
                setValue('name', room.name);
                setValue('roomNumber', room.roomNumber);
                setValue('capacity', room.capacity);
                setValue('type', room.type);
                setValue('status', room.status);
            }
        } catch (error) {
            toast.error("Failed to fetch room details");
            navigate('/admin/rooms');
        }
    };

    const onSubmit = async (data) => {
        try {
            let response;
            if (isEditMode) {
                response = await roomService.updateRoom(id, data);
            } else {
                response = await roomService.createRoom(data);
            }

            if (response.Status === 1) {
                toast.success(`Room ${isEditMode ? 'updated' : 'created'} successfully`);
                navigate('/admin/rooms');
            } else {
                toast.error(response.Message);
            }
        } catch (error) {
            toast.error(`Failed to ${isEditMode ? 'update' : 'create'} room`);
        }
    };

    return (
        <div className="max-w-2xl mx-auto">
            <Card>
                <CardHeader>
                    <h2 className="text-xl font-bold text-gray-900">{isEditMode ? 'Edit Room' : 'Add New Room'}</h2>
                </CardHeader>
                <CardBody>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                        <Input
                            label="Room Name"
                            id="name"
                            {...register('name', { required: 'Room Name is required' })}
                            error={errors.name}
                        />
                        <Input
                            label="Room Number"
                            id="roomNumber"
                            {...register('roomNumber', { required: 'Room Number is required' })}
                            error={errors.roomNumber}
                        />
                        <Input
                            label="Capacity"
                            id="capacity"
                            type="number"
                            {...register('capacity', { required: 'Capacity is required', min: 1 })}
                            error={errors.capacity}
                        />

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Room Type</label>
                            <select
                                {...register('type', { required: 'Room Type is required' })}
                                className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm border p-2"
                            >
                                <option value="">Select Type</option>
                                <option value="LECTURE_HALL">Lecture Hall</option>
                                <option value="LABORATORY">Laboratory</option>
                                <option value="CONFERENCE_ROOM">Conference Room</option>
                                <option value="CLASSROOM">Classroom</option>
                                <option value="STUDY_AREA">Study Area</option>
                            </select>
                            {errors.type && <p className="mt-1 text-sm text-red-600">{errors.type.message}</p>}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
                            <select
                                {...register('status', { required: 'Status is required' })}
                                className="block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm border p-2"
                            >
                                <option value="AVAILABLE">Available</option>
                                <option value="OCCUPIED">Occupied</option>
                                <option value="MAINTENANCE">Maintenance</option>
                            </select>
                            {errors.status && <p className="mt-1 text-sm text-red-600">{errors.status.message}</p>}
                        </div>

                        <div className="flex justify-end space-x-3">
                            <Button variant="secondary" onClick={() => navigate('/admin/rooms')}>
                                Cancel
                            </Button>
                            <Button type="submit" isLoading={isSubmitting}>
                                {isEditMode ? 'Update Room' : 'Create Room'}
                            </Button>
                        </div>
                    </form>
                </CardBody>
            </Card>
        </div>
    );
};

export default RoomForm;
