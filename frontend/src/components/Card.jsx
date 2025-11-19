import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

const Card = ({ children, className, ...props }) => {
    return (
        <div className={twMerge(clsx('bg-white shadow rounded-lg overflow-hidden', className))} {...props}>
            {children}
        </div>
    );
};

export const CardHeader = ({ children, className, ...props }) => {
    return (
        <div className={twMerge(clsx('px-4 py-5 sm:px-6 border-b border-gray-200', className))} {...props}>
            {children}
        </div>
    );
};

export const CardBody = ({ children, className, ...props }) => {
    return (
        <div className={twMerge(clsx('px-4 py-5 sm:p-6', className))} {...props}>
            {children}
        </div>
    );
};

export const CardFooter = ({ children, className, ...props }) => {
    return (
        <div className={twMerge(clsx('px-4 py-4 sm:px-6 bg-gray-50 border-t border-gray-200', className))} {...props}>
            {children}
        </div>
    );
};

export default Card;
