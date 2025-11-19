import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import UserDashboard from './pages/user/UserDashboard';
import BookRoom from './pages/user/BookRoom';
import MyBookings from './pages/user/MyBookings';
import AdminDashboard from './pages/admin/AdminDashboard';
import Rooms from './pages/admin/Rooms';
import RoomForm from './pages/admin/RoomForm';
import Equipment from './pages/admin/Equipment';
import Users from './pages/admin/Users';
import Profile from './pages/common/Profile';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuth } from './context/AuthContext';

const App = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="flex justify-center items-center h-screen">Loading...</div>;
  }

  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={
          user ? (
            user.role === 'ADMIN' ? <Navigate to="/admin/dashboard" /> : <Navigate to="/dashboard" />
          ) : (
            <Navigate to="/login" />
          )
        } />
        <Route path="login" element={<Login />} />
        <Route path="register" element={<Register />} />

        {/* User Routes */}
        <Route path="dashboard" element={
          <ProtectedRoute allowedRoles={['STUDENT', 'LECTURER']}>
            <UserDashboard />
          </ProtectedRoute>
        } />
        <Route path="book-room" element={
          <ProtectedRoute allowedRoles={['STUDENT', 'LECTURER']}>
            <BookRoom />
          </ProtectedRoute>
        } />
        <Route path="my-bookings" element={
          <ProtectedRoute allowedRoles={['STUDENT', 'LECTURER']}>
            <MyBookings />
          </ProtectedRoute>
        } />
        <Route path="profile" element={
          <ProtectedRoute allowedRoles={['STUDENT', 'LECTURER', 'ADMIN']}>
            <Profile />
          </ProtectedRoute>
        } />

        {/* Admin Routes */}
        <Route path="admin/dashboard" element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminDashboard />
          </ProtectedRoute>
        } />
        <Route path="admin/rooms" element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <Rooms />
          </ProtectedRoute>
        } />
        <Route path="admin/rooms/new" element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <RoomForm />
          </ProtectedRoute>
        } />
        <Route path="admin/rooms/edit/:id" element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <RoomForm />
          </ProtectedRoute>
        } />
        <Route path="admin/equipment" element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <Equipment />
          </ProtectedRoute>
        } />
        <Route path="admin/users" element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <Users />
          </ProtectedRoute>
        } />

        {/* Catch all */}
        <Route path="*" element={<Navigate to="/" />} />
      </Route>
    </Routes>
  );
};

export default App;
