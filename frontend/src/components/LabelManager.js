import React, { useState, useEffect, useContext } from 'react';
import { labelAPI } from '../services/api';
import { AuthContext } from '../context/AuthContext';
import Loading from './Loading';
import Error from './Error';

const LabelManager = ({ bloggerMode = false }) => {
  const { user } = useContext(AuthContext);
  const [labels, setLabels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [currentLabel, setCurrentLabel] = useState(null);
  
  // 表单状态
  const [formData, setFormData] = useState({
    title: '',
    status: 1 // 1表示启用，0表示禁用
  });

  useEffect(() => {
    // 检查是否需要强制刷新（例如，在状态更新后）
    const forceRefreshLabels = localStorage.getItem('forceRefreshLabels');
    
    if (forceRefreshLabels) {
      console.log('检测到标签强制刷新标记');
      localStorage.removeItem('forceRefreshLabels');
    }
    
    fetchLabels();
  }, []);
  
  // 添加页面可见性变化监听，当页面重新获得焦点时刷新数据
  useEffect(() => {
    const handleVisibilityChange = () => {
      // 只在页面刚变为可见时刷新一次，使用节流防止频繁刷新
      if (document.visibilityState === 'visible') {
        console.log('LabelManager页面重新可见，刷新标签列表');
        // 延迟刷新，避免与用户操作冲突
        setTimeout(() => {
          fetchLabels();
        }, 500);
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  const fetchLabels = async () => {
    try {
      const response = await labelAPI.getAll();
      if (response && response.code === 200) {
        setLabels(response.data || []);
      } else {
        setError(response.msg || '获取标签列表失败');
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error('获取标签列表失败:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.title.trim()) {
      setError('标签名称不能为空');
      return;
    }
    
    setLoading(true);
    setError('');
    
    try {
      let response;
      
      if (editMode && currentLabel) {
        // 更新标签
        response = await labelAPI.update(currentLabel.id, formData.title, formData.status);
      } else {
        // 创建新标签
        response = await labelAPI.create(formData.title);
      }
      
      if (response && response.code === 200) {
        setSuccess(editMode ? '标签更新成功' : '标签创建成功');
        setShowForm(false);
        setEditMode(false);
        setCurrentLabel(null);
        resetForm();
        fetchLabels(); // 刷新标签列表
      } else {
        setError(response.msg || '操作失败');
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error('操作失败:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (label) => {
    setFormData({
      title: label.title,
      status: label.status !== undefined ? label.status : 1
    });
    setCurrentLabel(label);
    setEditMode(true);
    setShowForm(true);
    setError('');
    setSuccess('');
  };

  const handleDelete = async (labelId) => {
    if (!window.confirm('确定要删除这个标签吗？删除后，该标签下的所有文章也将被删除。')) {
      return;
    }
    
    try {
      const response = await labelAPI.delete(labelId);
      if (response && response.code === 200) {
        setSuccess('标签删除成功');
        fetchLabels(); // 刷新标签列表
      } else {
        setError(response.msg || '删除失败');
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error('删除标签失败:', err);
    }
  };

  const handleToggleStatus = async (label) => {
    // 防止多次点击
    if (loading) {
      console.log('操作进行中，忽略点击');
      return;
    }
    
    // 状态值1表示启用，其他值（0或其他）都视为非启用
    const isCurrentlyEnabled = label.status === 1;
    const newStatus = isCurrentlyEnabled ? 0 : 1; // 切换状态
    const statusText = newStatus === 1 ? '启用' : '禁用';
    
    console.log('切换标签状态:', label.id, '从', label.status, '到', newStatus);
    
    try {
      setLoading(true);
      const response = await labelAPI.update(label.id, label.title, newStatus);
      console.log('标签状态更新响应:', response);
      
      if (response && response.code === 200) {
        setSuccess(`标签${statusText}成功`);
        
        // 更新本地状态，避免依赖后端缓存
        setLabels(prevLabels => 
          prevLabels.map(lbl => 
            lbl.id === label.id 
              ? { ...lbl, status: newStatus } 
              : lbl
          )
        );
        
        // 延迟刷新，确保后端数据完全更新
        setTimeout(() => {
          fetchLabels(); // 刷新标签列表
        }, 1000);
      } else {
        setError(response.msg || `${statusText}失败`);
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error(`${statusText}标签失败:`, err);
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      title: '',
      status: 1
    });
  };

  const [success, setSuccess] = useState('');

  return (
    <div className="label-manager">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>标签管理</h2>
        <button 
          className="btn btn-primary" 
          onClick={() => {
            setShowForm(true);
            setEditMode(false);
            resetForm();
            setError('');
            setSuccess('');
          }}
        >
          创建新标签
        </button>
      </div>
      
      {error && <Error message={error} />}
      {success && <div className="alert alert-success">{success}</div>}
      
      {showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <h3 className="card-title">{editMode ? '编辑标签' : '创建新标签'}</h3>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="title" className="form-label">标签名称</label>
                <input
                  type="text"
                  className="form-control"
                  id="title"
                  value={formData.title}
                  onChange={(e) => setFormData({...formData, title: e.target.value})}
                  required
                />
              </div>
              
              {editMode && (
                <div className="mb-3">
                  <label htmlFor="status" className="form-label">状态</label>
                  <select
                    className="form-select"
                    id="status"
                    value={formData.status}
                    onChange={(e) => setFormData({...formData, status: parseInt(e.target.value)})}
                  >
                    <option value="1">启用</option>
                    <option value="0">禁用</option>
                  </select>
                </div>
              )}
              
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? '保存中...' : (editMode ? '更新' : '创建')}
              </button>
              <button 
                type="button" 
                className="btn btn-outline-secondary ms-2" 
                onClick={() => {
                  setShowForm(false);
                  resetForm();
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
      ) : labels.length === 0 ? (
        <p>暂无标签</p>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>标签名称</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {labels.map(label => (
                <tr key={label.id}>
                  <td>{label.id}</td>
                  <td>{label.title}</td>
                  <td>
                    {label.status === 1 ? 
                      <span className="badge bg-success">启用</span> : 
                      <span className="badge bg-secondary">禁用</span>
                    }
                  </td>
                  <td>{label.createTime ? new Date(label.createTime).toLocaleDateString() : '-'}</td>
                  <td>
                    <button
                      className={`btn btn-sm ${label.status === 1 ? 'btn-outline-warning' : 'btn-outline-success'} me-2`}
                      onClick={() => handleToggleStatus(label)}
                    >
                      {label.status === 1 ? '禁用' : '启用'}
                    </button>
                    <button
                      className="btn btn-sm btn-outline-primary me-2"
                      onClick={() => handleEdit(label)}
                    >
                      编辑
                    </button>
                    <button
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => handleDelete(label.id)}
                    >
                      删除
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      
      <style jsx>{`
        .label-manager {
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
        
        .bg-success {
          background-color: #198754 !important;
        }
        
        .bg-secondary {
          background-color: #6c757d !important;
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
  );
};

export default LabelManager;