import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Menu, X, User, LogOut } from 'lucide-react';
import { useState } from 'react';
import { clsx } from 'clsx';

const Navbar = () => {
    const { user, logout } = useAuth();
    const location = useLocation();
    const [isOpen, setIsOpen] = useState(false);

    const isActive = (path) => location.pathname === path;

    const navLinkClass = (path) => clsx(
        'px-3 py-2 rounded-md text-sm font-medium transition-colors',
        isActive(path)
            ? 'bg-gray-900 text-white'
            : 'text-gray-300 hover:bg-gray-700 hover:text-white'
    );

    const mobileNavLinkClass = (path) => clsx(
        'block px-3 py-2 rounded-md text-base font-medium',
        isActive(path)
            ? 'bg-gray-900 text-white'
            : 'text-gray-300 hover:bg-gray-700 hover:text-white'
    );

    return (
        <nav className="bg-gray-800">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex items-center justify-between h-16">
                    <div className="flex items-center">
                        <div className="flex-shrink-0">
                            <Link to="/" className="text-white font-bold text-xl">SmartRoom</Link>
                        </div>
                        <div className="hidden md:block">
                            <div className="ml-10 flex items-baseline space-x-4">
                                {user && (
                                    <>
                                        {user.role === 'ADMIN' ? (
                                            <>
                                                <Link to="/admin/dashboard" className={navLinkClass('/admin/dashboard')}>Dashboard</Link>
                                                <Link to="/admin/rooms" className={navLinkClass('/admin/rooms')}>Rooms</Link>
                                                <Link to="/admin/users" className={navLinkClass('/admin/users')}>Users</Link>
                                                <Link to="/admin/equipment" className={navLinkClass('/admin/equipment')}>Equipment</Link>
                                            </>
                                        ) : (
                                            <>
                                                <Link to="/dashboard" className={navLinkClass('/dashboard')}>Dashboard</Link>
                                                <Link to="/book-room" className={navLinkClass('/book-room')}>Book Room</Link>
                                                <Link to="/my-bookings" className={navLinkClass('/my-bookings')}>My Bookings</Link>
                                            </>
                                        )}
                                    </>
                                )}
                            </div>
                        </div>
                    </div>
                    <div className="hidden md:block">
                        <div className="ml-4 flex items-center md:ml-6">
                            {user ? (
                                <div className="flex items-center space-x-4">
                                    <Link to="/profile" className="text-gray-300 hover:text-white p-1 rounded-full focus:outline-none">
                                        <User className="h-6 w-6" />
                                    </Link>
                                    <button
                                        onClick={logout}
                                        className="text-gray-300 hover:text-white p-1 rounded-full focus:outline-none"
                                    >
                                        <LogOut className="h-6 w-6" />
                                    </button>
                                </div>
                            ) : (
                                <div className="space-x-4">
                                    <Link to="/login" className={navLinkClass('/login')}>Login</Link>
                                    <Link to="/register" className={navLinkClass('/register')}>Register</Link>
                                </div>
                            )}
                        </div>
                    </div>
                    <div className="-mr-2 flex md:hidden">
                        <button
                            onClick={() => setIsOpen(!isOpen)}
                            className="bg-gray-800 inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-white hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-white"
                        >
                            <span className="sr-only">Open main menu</span>
                            {isOpen ? <X className="block h-6 w-6" /> : <Menu className="block h-6 w-6" />}
                        </button>
                    </div>
                </div>
            </div>

            {isOpen && (
                <div className="md:hidden">
                    <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3">
                        {user ? (
                            <>
                                {user.role === 'ROLE_ADMIN' ? (
                                    <>
                                        <Link to="/admin/dashboard" className={mobileNavLinkClass('/admin/dashboard')}>Dashboard</Link>
                                        <Link to="/admin/rooms" className={mobileNavLinkClass('/admin/rooms')}>Rooms</Link>
                                        <Link to="/admin/users" className={mobileNavLinkClass('/admin/users')}>Users</Link>
                                        <Link to="/admin/equipment" className={mobileNavLinkClass('/admin/equipment')}>Equipment</Link>
                                    </>
                                ) : (
                                    <>
                                        <Link to="/dashboard" className={mobileNavLinkClass('/dashboard')}>Dashboard</Link>
                                        <Link to="/book-room" className={mobileNavLinkClass('/book-room')}>Book Room</Link>
                                        <Link to="/my-bookings" className={mobileNavLinkClass('/my-bookings')}>My Bookings</Link>
                                    </>
                                )}
                                <Link to="/profile" className={mobileNavLinkClass('/profile')}>Profile</Link>
                                <button onClick={logout} className="block w-full text-left px-3 py-2 rounded-md text-base font-medium text-gray-300 hover:bg-gray-700 hover:text-white">
                                    Logout
                                </button>
                            </>
                        ) : (
                            <>
                                <Link to="/login" className={mobileNavLinkClass('/login')}>Login</Link>
                                <Link to="/register" className={mobileNavLinkClass('/register')}>Register</Link>
                            </>
                        )}
                    </div>
                </div>
            )}
        </nav>
    );
};

export default Navbar;
