import React from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Button from '../../components/Button';
import Input from '../../components/Input';
import Card, { CardHeader, CardBody, CardFooter } from '../../components/Card';

const Register = () => {
    const { register, handleSubmit, formState: { errors, isSubmitting }, watch } = useForm();
    const { register: registerUser } = useAuth();
    const navigate = useNavigate();

    const onSubmit = async (data) => {
        const success = await registerUser({
            username: data.username,
            password: data.password,
            email: data.email,
            role: 'STUDENT' // Default role
        });
        if (success) {
            navigate('/login');
        }
    };

    const password = watch('password');

    return (
        <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
            <Card className="w-full max-w-md">
                <CardHeader>
                    <h2 className="text-2xl font-bold text-center text-gray-900">Create a new account</h2>
                </CardHeader>
                <CardBody>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                        <Input
                            label="Username"
                            id="username"
                            {...register('username', { required: 'Username is required', minLength: { value: 3, message: 'Minimum 3 characters' } })}
                            error={errors.username}
                        />
                        <Input
                            label="Email"
                            id="email"
                            type="email"
                            {...register('email', {
                                required: 'Email is required',
                                pattern: {
                                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                    message: "Invalid email address"
                                }
                            })}
                            error={errors.email}
                        />
                        <Input
                            label="Password"
                            id="password"
                            type="password"
                            {...register('password', { required: 'Password is required', minLength: { value: 6, message: 'Minimum 6 characters' } })}
                            error={errors.password}
                        />
                        <Input
                            label="Confirm Password"
                            id="confirmPassword"
                            type="password"
                            {...register('confirmPassword', {
                                required: 'Please confirm your password',
                                validate: value => value === password || "Passwords do not match"
                            })}
                            error={errors.confirmPassword}
                        />
                        <Button
                            type="submit"
                            className="w-full"
                            isLoading={isSubmitting}
                        >
                            Register
                        </Button>
                    </form>
                </CardBody>
                <CardFooter>
                    <p className="text-center text-sm text-gray-600">
                        Already have an account?{' '}
                        <Link to="/login" className="font-medium text-blue-600 hover:text-blue-500">
                            Sign in
                        </Link>
                    </p>
                </CardFooter>
            </Card>
        </div>
    );
};

export default Register;
