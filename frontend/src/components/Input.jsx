import React, { forwardRef } from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

const Input = forwardRef(({
    label,
    error,
    className,
    type = 'text',
    id,
    ...props
}, ref) => {
    return (
        <div className="w-full">
            {label && (
                <label htmlFor={id} className="block text-sm font-medium text-gray-700 mb-1">
                    {label}
                </label>
            )}
            <input
                ref={ref}
                type={type}
                id={id}
                className={twMerge(clsx(
                    'block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm border p-2',
                    error && 'border-red-500 focus:border-red-500 focus:ring-red-500',
                    className
                ))}
                {...props}
            />
            {error && (
                <p className="mt-1 text-sm text-red-600">{error.message}</p>
            )}
        </div>
    );
});

Input.displayName = 'Input';

export default Input;
