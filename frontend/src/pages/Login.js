import React, { useState, useContext } from 'react';
import { Link, Navigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import Error from '../components/Error';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [loginSuccess, setLoginSuccess] = useState(false);
  
  const { login, isAuthenticated } = useContext(AuthContext);
  const location = useLocation();
  
  // 如果已经登录，重定向到原始路径或首页
  if (isAuthenticated || loginSuccess) {
    const from = location.state?.from?.pathname || '/';
    return <Navigate to={from} replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!username || !password) {
      setError('请输入用户名和密码');
      return;
    }
    
    setLoading(true);
    setError('');
    
    console.log('尝试登录，用户名:', username);
    const result = await login(username, password);
    console.log('登录结果:', result);
    
    if (result.success) {
      console.log('登录成功，用户信息:', result.data);
      setLoginSuccess(true);
    } else {
      setError(result.message);
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="card">
        <div className="card-body">
          <h2 className="card-title text-center mb-4">用户登录</h2>
          
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
              />
            </div>
            
            <button
              type="submit"
              className="btn btn-primary w-100"
              disabled={loading}
            >
              {loading ? '登录中...' : '登录'}
            </button>
          </form>
          
          <div className="text-center mt-3">
            <p>
              还没有账号？ <Link to="/register">立即注册</Link>
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
        
        .text-center {
          text-align: center;
        }
        
        .w-100 {
          width: 100%;
        }
        
        .mb-3 {
          margin-bottom: 1rem;
        }
        
        .mt-3 {
          margin-top: 1rem;
        }
      `}</style>
    </div>
  );
};

export default Login;