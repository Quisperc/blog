import React, { useContext } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import Loading from './Loading';

const ProtectedRoute = ({ children, adminOnly = false, posterOnly = false }) => {
  const { isAuthenticated, isAdmin, isPoster, loading } = useContext(AuthContext);
  const location = useLocation();

  if (loading) {
    return <Loading />;
  }

  if (!isAuthenticated) {
    // 保存当前路径，登录后可以重定向回来
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (adminOnly && !isAdmin) {
    // 如果需要管理员权限但用户不是管理员
    return <Navigate to="/" replace />;
  }

  if (posterOnly && !isPoster && !isAdmin) {
    // 如果需要博主权限但用户既不是博主也不是管理员
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ProtectedRoute;