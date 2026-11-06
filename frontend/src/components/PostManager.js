import React, { useState, useEffect, useContext } from 'react';
import { postAPI, categoryAPI, labelAPI } from '../services/api';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import Loading from './Loading';
import Error from './Error';

const PostManager = ({ bloggerMode = false }) => {
  const { user } = useContext(AuthContext);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [currentPost, setCurrentPost] = useState(null);
  
  // 表单状态
  const [formData, setFormData] = useState({
    title: '',
    summary: '',
    content: '',
    status: 1, // 1表示发布，0表示草稿
    categories: [],
    labels: []
  });
  
  // 分类和标签列表
  const [categories, setCategories] = useState([]);
  const [labels, setLabels] = useState([]);

  useEffect(() => {
    // 检查是否需要强制刷新（例如，在状态更新后）
    const forceRefreshPosts = localStorage.getItem('forceRefreshPosts');
    
    if (forceRefreshPosts) {
      console.log('检测到文章强制刷新标记');
      localStorage.removeItem('forceRefreshPosts');
    }
    
    fetchPosts();
    fetchCategories();
    fetchLabels();
  }, []);
  
  // 添加页面可见性变化监听，当页面重新获得焦点时刷新数据
  useEffect(() => {
    const handleVisibilityChange = () => {
      // 只在页面刚变为可见时刷新一次，使用节流防止频繁刷新
      if (document.visibilityState === 'visible') {
        console.log('PostManager页面重新可见，刷新数据');
        // 延迟刷新，避免与用户操作冲突
        setTimeout(() => {
          fetchPosts();
          fetchCategories();
          fetchLabels();
        }, 500);
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  const fetchPosts = async () => {
    try {
      let response;
      if (bloggerMode && user?.id) {
        // 博主模式：只获取自己的文章
        response = await postAPI.getByAuthor(user.id);
      } else {
        // 管理员模式：获取所有文章
        response = await postAPI.getAll();
      }
      
      if (response && response.code === 200) {
        setPosts(response.data || []);
      } else {
        setError(response.msg || '获取文章列表失败');
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error('获取文章列表失败:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await categoryAPI.getAll();
      if (response && response.code === 200) {
        setCategories(response.data || []);
      }
    } catch (err) {
      console.error('获取分类列表失败:', err);
    }
  };

  const fetchLabels = async () => {
    try {
      const response = await labelAPI.getAll();
      if (response && response.code === 200) {
        setLabels(response.data || []);
      }
    } catch (err) {
      console.error('获取标签列表失败:', err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.title.trim() || !formData.content.trim()) {
      setError('标题和内容不能为空');
      return;
    }
    
    // 额外验证：确保没有禁用的分类或标签
    const categoriesStatus = formData.categories.map(cat => {
      const category = categories.find(c => c.id === cat.id);
      return category ? category.status : null;
    });
    
    const labelsStatus = formData.labels.map(label => {
      const foundLabel = labels.find(l => l.id === label.id);
      return foundLabel ? foundLabel.status : null;
    });
    
    const hasDisabledCategory = categoriesStatus.some(status => status !== 1);
    const hasDisabledLabel = labelsStatus.some(status => status !== 1);
    
    if (hasDisabledCategory || hasDisabledLabel) {
      setError('不能选择已禁用的分类或标签');
      return;
    }
    
    // 确保状态是整数类型
    const submissionData = {
      ...formData,
      status: parseInt(formData.status)
    };
    
    console.log('提交文章数据:', submissionData, '状态值类型:', typeof submissionData.status);
    
    setLoading(true);
    setError('');
    
    try {
      let response;
      
      if (editMode && currentPost) {
        // 更新文章
        console.log('正在更新文章，ID:', currentPost.id, '数据:', submissionData);
        response = await postAPI.update(currentPost.id, submissionData);
        console.log('文章更新响应:', response);
      } else {
        // 创建新文章
        response = await postAPI.create(submissionData);
      }
      
      if (response && response.code === 200) {
        setSuccess(editMode ? '文章更新成功' : '文章创建成功');
        setShowForm(false);
        setEditMode(false);
        setCurrentPost(null);
        resetForm();
        
        if (editMode && currentPost) {
          // 更新本地状态，避免依赖后端缓存
          setPosts(prevPosts => {
            const updatedPosts = prevPosts.map(post => 
              post.id === currentPost.id 
                ? { ...post, status: submissionData.status } 
                : post
            );
            
            // 调试日志
            console.log('更新后的文章状态:', {
              postId: currentPost.id,
              newStatus: submissionData.status,
              updatedPost: updatedPosts.find(p => p.id === currentPost.id)
            });
            
            return updatedPosts;
          });
        }
        
        // 延迟刷新，确保后端数据完全更新
        setTimeout(() => {
          fetchPosts(); // 刷新文章列表
        }, 1000);
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

  const handleEdit = (post) => {
    // 确保状态是整数类型
    const postStatus = post.status !== undefined ? parseInt(post.status) : 1;
    
    // 过滤掉已禁用的分类和标签
    const enabledCategories = (post.categories || []).filter(cat => cat.status === 1);
    const disabledCategories = (post.categories || []).filter(cat => cat.status !== 1);
    const enabledLabels = (post.labels || []).filter(label => label.status === 1);
    const disabledLabels = (post.labels || []).filter(label => label.status !== 1);
    
    // 如果有禁用的分类或标签，显示警告
    if (disabledCategories.length > 0 || disabledLabels.length > 0) {
      const disabledNames = [
        ...disabledCategories.map(cat => `分类"${cat.title}"`),
        ...disabledLabels.map(label => `标签"${label.title}"`)
      ];
      setError(`警告：${disabledNames.join('、')} 已被禁用，将无法保存或会被移除`);
    }
    
    setFormData({
      title: post.title,
      summary: post.summary || '',
      content: post.content || '',
      status: postStatus,
      categories: enabledCategories, // 只保留启用的分类
      labels: enabledLabels // 只保留启用的标签
    });
    setCurrentPost(post);
    setEditMode(true);
    setShowForm(true);
    console.log('编辑文章，状态值:', postStatus, typeof postStatus);
    
    if (disabledCategories.length > 0 || disabledLabels.length > 0) {
      console.log('已禁用的分类:', disabledCategories, '已禁用的标签:', disabledLabels);
    }
  };

  const handleDelete = async (postId) => {
    if (!window.confirm('确定要删除这篇文章吗？')) {
      return;
    }
    
    try {
      const response = await postAPI.delete(postId);
      if (response && response.code === 200) {
        setSuccess('文章删除成功');
        fetchPosts(); // 刷新文章列表
      } else {
        setError(response.msg || '删除失败');
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error('删除文章失败:', err);
    }
  };

  const resetForm = () => {
    setFormData({
      title: '',
      summary: '',
      content: '',
      status: 1,
      categories: [],
      labels: []
    });
  };

  const [success, setSuccess] = useState('');

  return (
    <div className="post-manager">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>文章管理</h2>
        <div>
          <button 
            className="btn btn-outline-secondary me-2" 
            onClick={() => {
              console.log('手动刷新文章列表');
              fetchPosts();
            }}
          >
            刷新列表
          </button>
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
            创建新文章
          </button>
        </div>
      </div>
      
      {error && <Error message={error} />}
      {success && <div className="alert alert-success">{success}</div>}
      
      {showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <h3 className="card-title">{editMode ? '编辑文章' : '创建新文章'}</h3>
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label htmlFor="title" className="form-label">标题</label>
                <input
                  type="text"
                  className="form-control"
                  id="title"
                  value={formData.title}
                  onChange={(e) => setFormData({...formData, title: e.target.value})}
                  required
                />
              </div>
              
              <div className="mb-3">
                <label htmlFor="summary" className="form-label">摘要</label>
                <textarea
                  className="form-control"
                  id="summary"
                  rows="3"
                  value={formData.summary}
                  onChange={(e) => setFormData({...formData, summary: e.target.value})}
                ></textarea>
              </div>
              
              <div className="mb-3">
                <label htmlFor="content" className="form-label">内容</label>
                <textarea
                  className="form-control"
                  id="content"
                  rows="10"
                  value={formData.content}
                  onChange={(e) => setFormData({...formData, content: e.target.value})}
                  required
                ></textarea>
              </div>
              
              <div className="mb-3">
                <label htmlFor="categories" className="form-label">分类</label>
                <select
                  className="form-select"
                  id="categories"
                  value={formData.categories.length > 0 ? formData.categories[0].id : ''}
                  onChange={(e) => {
                    const categoryId = e.target.value;
                    if (categoryId) {
                      const category = categories.find(c => c.id === parseInt(categoryId));
                      setFormData({...formData, categories: category ? [category] : []});
                    } else {
                      setFormData({...formData, categories: []});
                    }
                  }}
                >
                  <option value="">选择分类</option>
                  {categories.map(category => (
                    // 只显示启用的分类（status为1）
                    category.status === 1 && (
                      <option key={category.id} value={category.id}>
                        {category.title}
                      </option>
                    )
                  ))}
                </select>
                {categories.filter(c => c.status === 0).length > 0 && (
                  <small className="text-muted">部分分类已被禁用，不可选择</small>
                )}
              </div>
              
              <div className="mb-3">
                <label htmlFor="labels" className="form-label">标签</label>
                {labels.map(label => (
                  // 只显示启用的标签（status为1）
                  label.status === 1 && (
                    <div key={label.id} className="form-check mb-2">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        id={`label-${label.id}`}
                        value={label.id}
                        checked={formData.labels.some(l => l.id === label.id)}
                        onChange={(e) => {
                          const labelId = parseInt(e.target.value);
                          if (e.target.checked) {
                            // 添加标签
                            setFormData({
                              ...formData,
                              labels: [...formData.labels, label]
                            });
                          } else {
                            // 移除标签
                            setFormData({
                              ...formData,
                              labels: formData.labels.filter(l => l.id !== labelId)
                            });
                          }
                        }}
                      />
                      <label className="form-check-label" htmlFor={`label-${label.id}`}>
                        {label.title}
                      </label>
                    </div>
                  )
                ))}
                {labels.filter(l => l.status === 0).length > 0 && (
                  <small className="text-muted">部分标签已被禁用，不可选择</small>
                )}
              </div>
              
              <div className="mb-3">
                <label htmlFor="status" className="form-label">状态</label>
                <select
                  className="form-select"
                  id="status"
                  value={formData.status}
                  onChange={(e) => setFormData({...formData, status: parseInt(e.target.value)})}
                >
                  <option value="1">发布</option>
                  <option value="0">草稿</option>
                </select>
              </div>
              
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
      ) : posts.length === 0 ? (
        <p>暂无文章</p>
      ) : (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>标题</th>
                <th>状态</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              {posts.map(post => (
                <tr key={post.id}>
                  <td>{post.id}</td>
                  <td>
                    <Link to={`/post/${post.id}`} target="_blank">
                      {post.title}
                    </Link>
                  </td>
                  <td>
                    {console.log(`文章ID: ${post.id}, 状态值: ${post.status}, 类型: ${typeof post.status}`)}
                    {post.status === 1 ? 
                      <span className="badge bg-success">已发布</span> : 
                      <span className="badge bg-secondary">草稿</span>
                    }
                  </td>
                  <td>{post.createTime ? new Date(post.createTime).toLocaleDateString() : '-'}</td>
                  <td>
                    <button
                      className="btn btn-sm btn-outline-primary me-2"
                      onClick={() => handleEdit(post)}
                    >
                      编辑
                    </button>
                    <button
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => handleDelete(post.id)}
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
        .post-manager {
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
        
        /* 复选框样式 */
        .form-check {
          display: block;
          min-height: 1.5rem;
          padding-left: 1.5em;
          margin-bottom: 0.125rem;
        }
        
        .form-check-input {
          width: 1em;
          height: 1em;
          margin-top: 0.25em;
          vertical-align: top;
          background-color: #fff;
          background-repeat: no-repeat;
          background-position: center;
          background-size: contain;
          border: 1px solid rgba(0, 0, 0, 0.25);
          appearance: none;
          color-adjust: exact;
        }
        
        .form-check-input:checked {
          background-color: #0d6efd;
          border-color: #0d6efd;
        }
        
        .form-check-input:checked[type=checkbox] {
          background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20'%3e%3cpath fill='none' stroke='%23fff' stroke-linecap='round' stroke-linejoin='round' stroke-width='3' d='m6 10 3 3 6-6-6'/%3e%3c/svg%3e");
        }
        
        .form-check-label {
          color: #212529;
          cursor: pointer;
          margin-left: 0.5em;
        }
      `}</style>
    </div>
  );
};

export default PostManager;