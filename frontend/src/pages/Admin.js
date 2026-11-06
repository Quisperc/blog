import React, { useState, useEffect, useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import ProtectedRoute from '../components/ProtectedRoute';
import { adminUserAPI } from '../services/api';
import Loading from '../components/Loading';
import Error from '../components/Error';

const Admin = () => {
  const { user: currentUser } = useContext(AuthContext);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');
  
  // 用于用户角色更新的表单状态
  const [updateForm, setUpdateForm] = useState({
    userId: '',
    role: ''
  });
  
  const [showUpdateForm, setShowUpdateForm] = useState(false);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const response = await adminUserAPI.getAll();
        if (response && response.code === 200) {
          setUsers(response.data || []);
        } else {
          setError(response.msg || '获取用户列表失败');
        }
      } catch (err) {
        setError('网络请求失败，请稍后重试');
        console.error('获取用户列表失败:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  const handleUpdateRole = async (e) => {
    e.preventDefault();
    
    if (!updateForm.userId || !updateForm.role) {
      setError('请选择用户和角色');
      return;
    }
    
    try {
      const response = await adminUserAPI.updateRole(updateForm.userId, updateForm.role);
      
      if (response && response.code === 200) {
        setSuccessMessage('用户角色更新成功');
        setShowUpdateForm(false);
        
        // 更新本地用户列表
        setUsers(users.map(user => 
          user.id && user.id.toString() === updateForm.userId
            ? { ...user, role: updateForm.role }
            : user
        ));
        
        setUpdateForm({ userId: '', role: '' });
      } else {
        setError(response.msg || '更新用户角色失败');
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error('更新用户角色失败:', err);
    }
  };

  const handleDeleteUser = async (userId) => {
    if (!window.confirm('确定要删除该用户吗？此操作将同时删除该用户的所有文章。')) {
      return;
    }
    
    try {
      const response = await adminUserAPI.delete(userId);
      
      if (response && response.code === 200) {
        setSuccessMessage('用户删除成功');
        
      // 更新本地用户列表
      setUsers(users.filter(user => !user.id || user.id.toString() !== userId.toString()));
      } else {
        setError(response.msg || '删除用户失败');
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error('删除用户失败:', err);
    }
  };

  const openUpdateForm = (user) => {
    // 检查user和user.id是否存在
    if (!user || !user.id) {
      console.error('用户数据不完整:', user);
      setError('用户数据不完整，无法编辑');
      return;
    }
    
    setUpdateForm({
      userId: user.id.toString(),
      role: user.role
    });
    setShowUpdateForm(true);
    setError('');
    setSuccessMessage('');
  };

  const getRoleDisplayText = (role) => {
    switch (role) {
      case 'manager':
        return '管理员';
      case 'poster':
        return '博主';
      case 'viewer':
        return '普通用户';
      default:
        return '未知';
    }
  };

  const getRoleBadgeClass = (role) => {
    switch (role) {
      case 'manager':
        return 'badge bg-danger';
      case 'poster':
        return 'badge bg-primary';
      case 'viewer':
        return 'badge bg-secondary';
      default:
        return 'badge bg-secondary';
    }
  };

  return (
    <ProtectedRoute adminOnly>
      <div className="admin-page">
        <h1 className="mb-4">管理员控制台</h1>
        
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h2>用户管理</h2>
          <Link to="/profile" className="btn btn-outline-primary">
            返回个人中心
          </Link>
        </div>
        
        {error && <Error message={error} />}
        {successMessage && <div className="alert alert-success">{successMessage}</div>}
        
        {showUpdateForm && (
          <div className="card mb-4">
            <div className="card-body">
              <h3 className="card-title">更新用户角色</h3>
              <form onSubmit={handleUpdateRole}>
                <div className="mb-3">
                  <label htmlFor="userId" className="form-label">用户ID</label>
                  <select
                    className="form-select"
                    id="userId"
                    value={updateForm.userId}
                    onChange={(e) => setUpdateForm({ ...updateForm, userId: e.target.value })}
                    required
                  >
                    <option value="">选择用户</option>
                    {users.map(user => (
                      <option key={user.id} value={user.id}>
                        {user.username} (ID: {user.id})
                      </option>
                    ))}
                  </select>
                </div>
                
                <div className="mb-3">
                  <label htmlFor="role" className="form-label">角色</label>
                  <select
                    className="form-select"
                    id="role"
                    value={updateForm.role}
                    onChange={(e) => setUpdateForm({ ...updateForm, role: e.target.value })}
                    required
                  >
                    <option value="">选择角色</option>
                    <option value="manager">管理员</option>
                    <option value="poster">博主</option>
                    <option value="viewer">普通用户</option>
                  </select>
                </div>
                
                <button type="submit" className="btn btn-primary">
                  更新角色
                </button>
                <button
                  type="button"
                  className="btn btn-outline-secondary ms-2"
                  onClick={() => {
                    setShowUpdateForm(false);
                    setUpdateForm({ userId: '', role: '' });
                  }}
                >
                  取消
                </button>
              </form>
            </div>
          </div>
        )}
        
        {loading ? (
          <Loading />
        ) : users.length === 0 ? (
          <p>暂无用户</p>
        ) : (
          <div className="table-responsive">
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>角色</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user.id}>
                    <td>{user.id}</td>
                    <td>{user.username}</td>
                    <td>
                      <span className={getRoleBadgeClass(user.role)}>
                        {getRoleDisplayText(user.role)}
                      </span>
                    </td>
                    <td>
                      <button
                        className="btn btn-sm btn-outline-primary me-2"
                        onClick={() => openUpdateForm(user)}
                      >
                        更改角色
                      </button>
                      
                      {user.id && user.id.toString() !== currentUser?.id?.toString() && (
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleDeleteUser(user.id)}
                        >
                          删除
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        
        <style jsx>{`
          .admin-page {
            max-width: 1000px;
            margin: 0 auto;
          }
          
          .table {
            width: 100%;
            margin-bottom: 1rem;
            color: #212529;
            vertical-align: top;
            border-color: #dee2e6;
          }
          
          .table-striped > tbody > tr:nth-of-type(odd) {
            background-color: rgba(0, 0, 0, 0.05);
          }
          
          .table th,
          .table td {
            padding: 0.75rem;
            vertical-align: top;
            border-top: 1px solid #dee2e6;
          }
          
          .table thead th {
            vertical-align: bottom;
            border-bottom: 2px solid #dee2e6;
            border-top: 0;
          }
          
          .table-responsive {
            overflow-x: auto;
            -webkit-overflow-scrolling: touch;
          }
          
          .badge {
            display: inline-block;
            padding: 0.35em 0.65em;
            font-size: 0.75em;
            font-weight: 700;
            line-height: 1;
            color: #fff;
            text-align: center;
            white-space: nowrap;
            vertical-align: baseline;
            border-radius: 0.25rem;
          }
          
          .bg-primary {
            background-color: #0d6efd !important;
          }
          
          .bg-danger {
            background-color: #dc3545 !important;
          }
          
          .bg-secondary {
            background-color: #6c757d !important;
          }
          
          .form-label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 500;
          }
          
          .form-select {
            display: block;
            width: 100%;
            padding: 0.375rem 2.25rem 0.375rem 0.75rem;
            font-size: 1rem;
            font-weight: 400;
            line-height: 1.5;
            color: #212529;
            background-color: #fff;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='m1 6 7 7 7-7'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right 0.75rem center;
            background-size: 16px 12px;
            border: 1px solid #ced4da;
            border-radius: 0.25rem;
            transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
          }
          
          .form-select:focus {
            border-color: #86b7fe;
            outline: 0;
            box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
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
          
          .ms-2 {
            margin-left: 0.5rem;
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
          
          .justify-content-between {
            justify-content: space-between;
          }
          
          .align-items-center {
            align-items: center;
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
          
          .btn-outline-danger {
            color: #dc3545;
            border-color: #dc3545;
          }
          
          .btn-outline-danger:hover {
            color: #fff;
            background-color: #dc3545;
            border-color: #dc3545;
          }
          
          .btn-sm {
            padding: 0.25rem 0.5rem;
            font-size: 0.875rem;
            border-radius: 0.2rem;
          }
        `}</style>
      </div>
    </ProtectedRoute>
  );
};

export default Admin;