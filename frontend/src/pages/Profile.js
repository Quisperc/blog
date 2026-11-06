import React, { useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import ProtectedRoute from '../components/ProtectedRoute';
import Error from '../components/Error';
import PostManager from '../components/PostManager';
import CategoryManager from '../components/CategoryManager';
import LabelManager from '../components/LabelManager';
import FileManager from '../components/FileManager';

const Profile = () => {
  const { user, logout, updateUser } = useContext(AuthContext);
  const [username, setUsername] = useState(user?.username || '');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPasswordForm, setShowPasswordForm] = useState(false);
  const [activeTab, setActiveTab] = useState('profile'); // 'profile', 'posts', 'categories', 'labels', 'files'

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    
    if (!username.trim()) {
      setError('用户名不能为空');
      return;
    }
    
    setLoading(true);
    setError('');
    setSuccess('');
    
    const result = await updateUser(username, password);
    
    if (result.success) {
      setSuccess('用户信息更新成功');
      setPassword('');
      setConfirmPassword('');
      setShowPasswordForm(false);
    } else {
      setError(result.message);
    }
    
    setLoading(false);
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <ProtectedRoute>
      <div className="profile-page">
        <h1 className="mb-4">个人中心</h1>
        
        {/* 标签页导航 */}
        <ul className="nav nav-tabs mb-4">
          <li className="nav-item">
            <button 
              className={`nav-link ${activeTab === 'profile' ? 'active' : ''}`}
              onClick={() => setActiveTab('profile')}
            >
              基本信息
            </button>
          </li>
          {(user?.role === 'manager' || user?.role === 'poster') && (
            <li className="nav-item">
              <button 
                className={`nav-link ${activeTab === 'posts' ? 'active' : ''}`}
                onClick={() => setActiveTab('posts')}
              >
                文章管理
              </button>
            </li>
          )}
          
          {user?.role === 'manager' && (
            <>
              <li className="nav-item">
                <button 
                  className={`nav-link ${activeTab === 'categories' ? 'active' : ''}`}
                  onClick={() => setActiveTab('categories')}
                >
                  分类管理
                </button>
              </li>
              <li className="nav-item">
                <button 
                  className={`nav-link ${activeTab === 'labels' ? 'active' : ''}`}
                  onClick={() => setActiveTab('labels')}
                >
                  标签管理
                </button>
              </li>
              <li className="nav-item">
                <button 
                  className={`nav-link ${activeTab === 'files' ? 'active' : ''}`}
                  onClick={() => setActiveTab('files')}
                >
                  文件管理
                </button>
              </li>
            </>
          )}
          
          {user?.role === 'poster' && (
            <li className="nav-item">
              <button 
                className={`nav-link ${activeTab === 'files' ? 'active' : ''}`}
                onClick={() => setActiveTab('files')}
              >
                文件管理
              </button>
            </li>
          )}
        </ul>
        
        {/* 标签页内容 */}
        {activeTab === 'profile' && (
          <div>
            <div className="card mb-4">
              <div className="card-body">
                <h2 className="card-title">基本信息</h2>
                
                {error && <Error message={error} />}
                {success && <div className="alert alert-success">{success}</div>}
                
                <form onSubmit={handleUpdateProfile}>
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
                    <label htmlFor="role" className="form-label">角色</label>
                    <input
                      type="text"
                      className="form-control"
                      id="role"
                      value={user?.role || '未知'}
                      readOnly
                    />
                  </div>
                  
                  {!showPasswordForm && (
                    <button
                      type="button"
                      className="btn btn-outline-primary"
                      onClick={() => setShowPasswordForm(true)}
                    >
                      修改密码
                    </button>
                  )}
                  
                  {showPasswordForm && (
                    <>
                      <div className="mb-3">
                        <label htmlFor="password" className="form-label">新密码</label>
                        <input
                          type="password"
                          className="form-control"
                          id="password"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          minLength="6"
                        />
                        <div className="form-text">留空则不修改密码，至少需要6个字符</div>
                      </div>
                      
                      <div className="mb-3">
                        <label htmlFor="confirmPassword" className="form-label">确认密码</label>
                        <input
                          type="password"
                          className="form-control"
                          id="confirmPassword"
                          value={confirmPassword}
                          onChange={(e) => setConfirmPassword(e.target.value)}
                        />
                      </div>
                      
                      <button
                        type="button"
                        className="btn btn-outline-secondary me-2"
                        onClick={() => {
                          setShowPasswordForm(false);
                          setPassword('');
                          setConfirmPassword('');
                        }}
                      >
                        取消
                      </button>
                    </>
                  )}
                  
                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={loading}
                  >
                    {loading ? '更新中...' : '更新信息'}
                  </button>
                </form>
              </div>
            </div>
            
            <div className="card">
              <div className="card-body">
                <h2 className="card-title">账户操作</h2>
                
                <div className="d-flex flex-column gap-2">
                  <Link to="/" className="btn btn-outline-primary">
                    返回首页
                  </Link>
                  
                  {user?.role === 'manager' && (
                    <Link to="/admin" className="btn btn-outline-success">
                      进入管理后台
                    </Link>
                  )}
                  
                  <button className="btn btn-outline-danger" onClick={handleLogout}>
                    退出登录
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {activeTab === 'posts' && (user?.role === 'manager' || user?.role === 'poster') && <PostManager bloggerMode={user?.role === 'poster'} />}
        {activeTab === 'categories' && user?.role === 'manager' && <CategoryManager />}
        {activeTab === 'labels' && user?.role === 'manager' && <LabelManager />}
        {activeTab === 'files' && (user?.role === 'manager' || user?.role === 'poster') && (
          <FileManager bloggerMode={user?.role === 'poster'} />
        )}
        
        <style jsx>{`
          .profile-page {
            max-width: 800px;
            margin: 0 auto;
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
          
          .form-control[readOnly] {
            background-color: #e9ecef;
            opacity: 1;
          }
          
          .form-text {
            font-size: 0.875em;
            color: #6c757d;
            margin-top: 0.25rem;
          }
          
          .mb-3 {
            margin-bottom: 1rem;
          }
          
          .mb-4 {
            margin-bottom: 1.5rem;
          }
          
          .me-2 {
            margin-right: 0.5rem;
          }
          
          .alert {
            padding: 0.75rem 1.25rem;
            margin-bottom: 1rem;
            border: 1px solid transparent;
            border-radius: 0.25rem;
          }
          
          .alert-success {
            color: #155724;
            background-color: #d4edda;
            border-color: #c3e6cb;
          }
          
          .d-flex {
            display: flex;
          }
          
          .flex-column {
            flex-direction: column;
          }
          
          .gap-2 {
            gap: 0.5rem;
          }
          
          .btn {
            display: inline-block;
            font-weight: 400;
            line-height: 1.5;
            color: #212529;
            text-align: center;
            text-decoration: none;
            vertical-align: middle;
            cursor: pointer;
            user-select: none;
            background-color: transparent;
            border: 1px solid transparent;
            padding: 0.375rem 0.75rem;
            font-size: 1rem;
            border-radius: 0.25rem;
            transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
          }
          
          .btn-primary {
            color: #fff;
            background-color: #0d6efd;
            border-color: #0d6efd;
          }
          
          .btn-outline-primary {
            color: #0d6efd;
            border-color: #0d6efd;
          }
          
          .btn-outline-primary:hover {
            color: #fff;
            background-color: #0d6efd;
            border-color: #0d6efd;
          }
          
          .btn-outline-secondary {
            color: #6c757d;
            border-color: #6c757d;
          }
          
          .btn-outline-secondary:hover {
            color: #fff;
            background-color: #6c757d;
            border-color: #6c757d;
          }
          
          .btn-outline-success {
            color: #198754;
            border-color: #198754;
          }
          
          .btn-outline-success:hover {
            color: #fff;
            background-color: #198754;
            border-color: #198754;
          }
          
          .btn-outline-danger {
            color: #dc3545;
            border-color: #dc3545;
          }
          
          .btn-outline-danger:hover {
            color: #fff;
            background-color: #dc3545;
            border-color: #dc3545;
          }
          
          /* 标签页样式 */
          .nav-tabs {
            border-bottom: 1px solid #dee2e6;
            padding-left: 0;
            list-style: none;
            display: flex;
          }
          
          .nav-tabs .nav-link {
            margin-bottom: -1px;
            background: none;
            border: 1px solid transparent;
            border-top-left-radius: 0.375rem;
            border-top-right-radius: 0.375rem;
            padding: 0.5rem 1rem;
            color: #495057;
            cursor: pointer;
            transition: color .15s ease-in-out,background-color .15s ease-in-out,border-color .15s ease-in-out;
          }
          
          .nav-tabs .nav-link:hover {
            border-color: #e9ecef #e9ecef #dee2e6;
            isolation: isolate;
          }
          
          .nav-tabs .nav-link.active {
            color: #495057;
            background-color: #fff;
            border-color: #dee2e6 #dee2e6 #fff;
          }
          
          .nav-item {
            margin-bottom: 0;
          }
        `}</style>
      </div>
    </ProtectedRoute>
  );
};

export default Profile;