import React, { useState, useContext } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import Error from '../components/Error';

const Register = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  
  const { register, isAuthenticated } = useContext(AuthContext);
  
  // 如果已经登录，重定向到首页
  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!username || !password) {
      setError('请输入用户名和密码');
      return;
    }
    
    if (password !== confirmPassword) {
      setError('两次输入的密码不一致');
      return;
    }
    
    if (password.length < 6) {
      setError('密码长度不能少于6个字符');
      return;
    }
    
    setLoading(true);
    setError('');
    
    const result = await register(username, password);
    
    if (result.success) {
      setSuccess(true);
    } else {
      setError(result.message);
    }
    
    setLoading(false);
  };

  // 注册成功后显示提示信息
  if (success) {
    return (
      <div className="auth-page">
        <div className="card">
          <div className="card-body text-center">
            <h2 className="card-title mb-4">注册成功</h2>
            <p className="mb-4">您的账号已创建成功，请使用新账号登录。</p>
            <Link to="/login" className="btn btn-primary">
              前往登录
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <div className="card">
        <div className="card-body">
          <h2 className="card-title text-center mb-4">用户注册</h2>
          
          {error && <Error message={error} />}
          
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label htmlFor="username" className="form-label">用户名</label>
              <input
                type="text"
                className="form-control"
                id="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
            
            <div className="mb-3">
              <label htmlFor="password" className="form-label">密码</label>
              <input
                type="password"
                className="form-control"
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength="6"
              />
              <div className="form-text">密码至少需要6个字符</div>
            </div>
            
            <div className="mb-3">
              <label htmlFor="confirmPassword" className="form-label">确认密码</label>
              <input
                type="password"
                className="form-control"
                id="confirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
            
            <button
              type="submit"
              className="btn btn-primary w-100"
              disabled={loading}
            >
              {loading ? '注册中...' : '注册'}
            </button>
          </form>
          
          <div className="text-center mt-3">
            <p>
              已有账号？ <Link to="/login">立即登录</Link>
            </p>
          </div>
        </div>
      </div>
      
      <style jsx>{`
        .auth-page {
          max-width: 400px;
          margin: 50px auto;
        }
        
        .form-label {
          display: block;
          margin-bottom: 0.5rem;
          font-weight: 500;
        }
        
        .form-control {
          display: block;
          width: 100%;
          padding: 0.375rem 0.75rem;
          font-size: 1rem;
          line-height: 1.5;
          color: #212529;
          background-color: #fff;
          background-clip: padding-box;
          border: 1px solid #ced4da;
          border-radius: 0.25rem;
          transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
        }
        
        .form-control:focus {
          color: #212529;
          background-color: #fff;
          border-color: #86b7fe;
          outline: 0;
          box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
        }
        
        .form-text {
          font-size: 0.875em;
          color: #6c757d;
          margin-top: 0.25rem;
        }
        
        .text-center {
          text-align: center;
        }
        
        .w-100 {
          width: 100%;
        }
        
        .mb-3 {
          margin-bottom: 1rem;
        }
        
        .mb-4 {
          margin-bottom: 1.5rem;
        }
        
        .mt-3 {
          margin-top: 1rem;
        }
      `}</style>
    </div>
  );
};

export default Register;