import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../api/axios';
import { toast } from 'react-toastify';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const checkAuth = async () => {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const response = await api.get('/api/users/me');
                    if (response.data.Status === 1) {
                        setUser(response.data.Data);
                    } else {
                        localStorage.removeItem('token');
                        localStorage.removeItem('user');
                    }
                } catch (error) {
                    console.error("Auth check failed", error);
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                }
            }
            setLoading(false);
        };

        checkAuth();
    }, []);

    const login = async (username, password) => {
        try {
            const response = await api.post('/api/auth/login', { username, password });
            if (response.data.Status === 1) {
                const { token, ...userData } = response.data.Data;
                localStorage.setItem('token', token);
                // We might want to fetch the full profile immediately or just use what we have
                // The login response only gives username and role.
                // Let's fetch the full profile to be consistent.
                // But for now, let's just set what we have and maybe fetch 'me' in background.
                // Actually, let's call 'me' to get the ID and other details which might be needed.

                // Set token first so the next request works

                const meResponse = await api.get('/api/users/me');
                if (meResponse.data.Status === 1) {
                    setUser(meResponse.data.Data);
                    localStorage.setItem('user', JSON.stringify(meResponse.data.Data));
                } else {
                    // Fallback
                    setUser({ username, role: userData.role });
                }

                toast.success('Login successful!');
                return true;
            } else {
                toast.error(response.data.Message || 'Login failed');
                return false;
            }
        } catch (error) {
            toast.error(error.response?.data?.Message || 'Login failed');
            return false;
        }
    };

    const register = async (userData) => {
        try {
            const response = await api.post('/api/users/register', userData);
            if (response.data.Status === 1) {
                toast.success('Registration successful! Please login.');
                return true;
            } else {
                toast.error(response.data.Message || 'Registration failed');
                return false;
            }
        } catch (error) {
            toast.error(error.response?.data?.Message || 'Registration failed');
            return false;
        }
    }

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        toast.info('Logged out');
        window.location.href = '/login';
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
