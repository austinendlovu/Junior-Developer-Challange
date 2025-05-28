
import React from 'react';
import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
  const token = localStorage.getItem('teacher_token');
  const role = localStorage.getItem('teacher_role');
  
  if (!token || !role) {
    return <Navigate to="/" replace />;
  }
  
  // For now, only allow TEACHER role to access protected routes
  if (role !== 'TEACHER') {
    return <Navigate to="/" replace />;
  }
  
  return <>{children}</>;
};

export default ProtectedRoute;
