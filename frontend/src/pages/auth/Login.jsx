import React from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Button from '../../components/Button';
import Input from '../../components/Input';
import Card, { CardHeader, CardBody, CardFooter } from '../../components/Card';

const Login = () => {
    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
    const { login } = useAuth();
    const navigate = useNavigate();

    const onSubmit = async (data) => {
        const success = await login(data.username, data.password);
        if (success) {
            // Redirect based on role is handled in AuthContext or we can do it here
            // But since login sets user state, we might need to wait or check user role
            // For now, let's redirect to home, and let the protected route or dashboard logic handle it
            // Actually, AuthContext login doesn't return user role directly in the boolean
            // But we can fetch user from context or just redirect to / which will redirect to dashboard
            navigate('/');
        }
    };

    return (
        <div className="flex items-center justify-center min-h-[calc(100vh-4rem)]">
            <Card className="w-full max-w-md">
                <CardHeader>
                    <h2 className="text-2xl font-bold text-center text-gray-900">Sign in to your account</h2>
                </CardHeader>
                <CardBody>
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                        <Input
                            label="Username"
                            id="username"
                            {...register('username', { required: 'Username is required' })}
                            error={errors.username}
                        />
                        <Input
                            label="Password"
                            id="password"
                            type="password"
                            {...register('password', { required: 'Password is required' })}
                            error={errors.password}
                        />
                        <Button
                            type="submit"
                            className="w-full"
                            isLoading={isSubmitting}
                        >
                            Sign in
                        </Button>
                    </form>
                </CardBody>
                <CardFooter>
                    <p className="text-center text-sm text-gray-600">
                        Don't have an account?{' '}
                        <Link to="/register" className="font-medium text-blue-600 hover:text-blue-500">
                            Register here
                        </Link>
                    </p>
                </CardFooter>
            </Card>
        </div>
    );
};

export default Login;
