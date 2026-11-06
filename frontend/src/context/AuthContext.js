import React, { createContext, useState, useEffect } from 'react';
import { userAPI } from '../services/api';
import { parseJWT, getUserIdFromToken, getUsernameFromToken, getRoleFromToken, isTokenExpired } from '../utils/jwt';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // 初始化时检查本地存储中是否有token
  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (token) {
      // 检查token是否过期
      if (isTokenExpired(token)) {
        console.warn('Token已过期，清除登录状态');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setLoading(false);
        return;
      }
      
      try {
        // 首先尝试从localStorage中获取保存的用户信息
        if (savedUser) {
          const userData = JSON.parse(savedUser);
          // 如果保存的用户信息完整，直接使用
          if (userData && userData.id && userData.username && userData.role) {
            setUser(userData);
            setLoading(false);
            return;
          }
        }
        
        // 如果没有保存的用户信息或信息不完整，从token中解析
        const userId = getUserIdFromToken(token);
        const username = getUsernameFromToken(token);
        const role = getRoleFromToken(token);
        
        if (userId && username) {
          const userData = {
            id: userId,  // 改为id，与后端实体一致
            username,
            role
          };
          
          setUser(userData);
          localStorage.setItem('user', JSON.stringify(userData));
        }
      } catch (error) {
        console.error('解析用户数据失败:', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    try {
      const response = await userAPI.login(username, password);
      
      // 输出响应数据以便调试
      console.log('登录响应:', response);
      
      if (response && response.code === 200) {
        // 后端返回的结构是 { code: 200, msg: '...', data: UserDTO }
        // token在响应头中
        let token = response.token;
        let userData = response.data;
        
        if (!token) {
          console.error('未获取到token，完整的响应对象:', response);
          return { success: false, message: '登录失败：未获取到有效token' };
        }
        
        // 如果token是 "Bearer xxx" 格式，需要去掉 "Bearer " 前缀
        if (token.startsWith('Bearer ')) {
          token = token.substring(7);
        }
        
        // 从token中解析用户信息（作为备用）
        const parsedUserId = getUserIdFromToken(token);
        const parsedUsername = getUsernameFromToken(token);
        const parsedRole = getRoleFromToken(token);
        
        // 处理角色信息：优先使用后端返回的用户数据中的角色，如果是从token解析的，确保格式正确
        let role = userData?.role || parsedRole || 'viewer';
        
        // 如果角色是枚举对象，可能需要转换为字符串
        if (typeof role === 'object' && role !== null) {
          // 处理可能的枚举对象
          role = role.toString().toLowerCase();
        }
        
        // 确保角色值是小写的
        if (typeof role === 'string') {
          role = role.toLowerCase();
        }
        
        const user = {
          id: userData?.id || parsedUserId,  // 改为id，与后端实体一致
          username: userData?.username || parsedUsername,
          role: role
        };
        
        console.log('解析出的用户信息:', user);
        console.log('保存的token:', token);
        
        // 存储token和用户信息到本地
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        
        setUser(user);
        return { success: true, data: user };
      } else {
        return { success: false, message: response.msg || '登录失败' };
      }
    } catch (error) {
      console.error('登录失败:', error);
      return { success: false, message: '网络请求失败，请稍后重试' };
    }
  };

  const register = async (username, password) => {
    try {
      const response = await userAPI.register(username, password);
      
      if (response && response.code === 200) {
        return { success: true };
      } else {
        return { success: false, message: response.msg || '注册失败' };
      }
    } catch (error) {
      console.error('注册失败:', error);
      return { success: false, message: '网络请求失败，请稍后重试' };
    }
  };

  const logout = async () => {
    try {
      await userAPI.logout();
    } catch (error) {
      console.error('登出请求失败:', error);
    } finally {
      // 无论后端请求是否成功，都清除本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setUser(null);
    }
  };

  const updateUser = async (username, password) => {
    try {
      const response = await userAPI.update(username, password);
      
      if (response && response.code === 200) {
        // 更新本地用户信息
        const updatedUser = { ...user, username };
        localStorage.setItem('user', JSON.stringify(updatedUser));
        setUser(updatedUser);
        return { success: true };
      } else {
        return { success: false, message: response.msg || '更新失败' };
      }
    } catch (error) {
      console.error('更新用户信息失败:', error);
      return { success: false, message: '网络请求失败，请稍后重试' };
    }
  };

  const value = {
    user,
    isAuthenticated: !!user,
    isAdmin: user && user.role === 'manager',
    isPoster: user && user.role === 'poster',
    loading,
    login,
    register,
    logout,
    updateUser
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};