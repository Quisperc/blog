import React, { useState, useEffect, useContext } from 'react';
import { fileAPI } from '../services/api';
import { AuthContext } from '../context/AuthContext';
import Loading from './Loading';
import Error from './Error';

const FileManager = ({ bloggerMode = false }) => {
  const { user } = useContext(AuthContext);
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [uploading, setUploading] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');

  useEffect(() => {
    fetchFiles();
  }, []);

  const fetchFiles = async () => {
    try {
      // 根据用户角色使用不同的API
      let response;
      if (bloggerMode) {
        // 博主模式：只获取当前用户的文件
        response = await fileAPI.getMyFiles();
      } else {
        // 管理员模式：获取所有文件
        response = await fileAPI.getAllRecords();
      }
      
      if (response && response.code === 200 && response.data) {
        // 处理数据库中的文件记录
        const fileList = await Promise.all(response.data.map(async (file) => {
          // 为每个文件获取下载URL
          let downloadUrl = null;
          try {
            const urlResponse = await fileAPI.getDownloadUrl(file.objectKey);
            if (urlResponse && urlResponse.code === 200) {
              downloadUrl = urlResponse.data;
            }
          } catch (err) {
            console.error(`获取文件 ${file.objectKey} 的下载URL失败:`, err);
          }
          
          return {
            id: file.id,
            objectKey: file.objectKey,
            originName: file.originName,
            authorId: file.authorId,
            uploadTime: file.uploadTime,
            isImage: file.objectKey.match(/\.(jpg|jpeg|png|gif)$/i) ? true : false,
            size: file.objectKey.includes('.') ? file.objectKey.substring(file.objectKey.lastIndexOf('.') + 1).toUpperCase() : 'Unknown',
            downloadUrl
          };
        }));
        setFiles(fileList);
      } else {
        setError(response.msg || '获取文件列表失败');
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error('获取文件列表失败:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      setError('');
      setSuccess('');
      
      // 如果是图片，显示预览
      if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onloadend = () => {
          setPreviewUrl(reader.result);
        };
        reader.readAsDataURL(file);
      } else {
        setPreviewUrl('');
      }
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setError('请选择要上传的文件');
      return;
    }

    setUploading(true);
    setError('');

    try {
      const response = await fileAPI.upload(selectedFile);
      if (response && response.code === 200) {
        setSuccess('文件上传成功');
        setSelectedFile(null);
        setPreviewUrl('');
        document.getElementById('file-input').value = ''; // 重置文件输入
        fetchFiles(); // 刷新文件列表
      } else {
        setError(response.msg || '文件上传失败');
      }
    } catch (err) {
      setError('文件上传失败，请稍后重试');
      console.error('文件上传失败:', err);
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (file) => {
    try {
      // 先获取下载URL
      const urlResponse = await fileAPI.getDownloadUrl(file.objectKey);
      if (urlResponse && urlResponse.code === 200) {
        // 创建临时下载链接并点击
        const link = document.createElement('a');
        link.href = urlResponse.data;
        link.download = file.originName; // 使用原始文件名作为下载文件名
        link.target = '_blank';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      } else {
        setError('获取下载链接失败');
      }
    } catch (err) {
      setError('下载失败，请稍后重试');
      console.error('下载失败:', err);
    }
  };

  const handleDelete = async (file) => {
    if (!window.confirm(`确定要删除文件 ${file.originName} 吗？`)) {
      return;
    }

    try {
      const response = await fileAPI.delete(file.objectKey);
      if (response && response.code === 200) {
        setSuccess('文件删除成功');
        fetchFiles(); // 刷新文件列表
      } else {
        setError(response.msg || '删除失败');
      }
    } catch (err) {
      setError('删除失败，请稍后重试');
      console.error('删除文件失败:', err);
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  };

  return (
    <div className="file-manager">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>文件管理</h2>
      </div>
      
      {error && <Error message={error} />}
      {success && <div className="alert alert-success">{success}</div>}
      
      {/* 文件上传区域 */}
      <div className="card mb-4">
        <div className="card-body">
          <h3 className="card-title">上传新文件</h3>
          <div className="mb-3">
            <label htmlFor="file-input" className="form-label">选择文件</label>
            <input
              type="file"
              className="form-control"
              id="file-input"
              onChange={handleFileSelect}
            />
          </div>
          
          {previewUrl && (
            <div className="mb-3">
              <label className="form-label">预览</label>
              <div className="preview-container">
                <img src={previewUrl} alt="预览" className="img-fluid" style={{ maxHeight: '200px' }} />
              </div>
            </div>
          )}
          
          {selectedFile && (
            <div className="mb-3">
              <p><strong>文件名:</strong> {selectedFile.name}</p>
              <p><strong>文件大小:</strong> {formatFileSize(selectedFile.size)}</p>
              <p><strong>文件类型:</strong> {selectedFile.type || '未知'}</p>
            </div>
          )}
          
          <button 
            className="btn btn-primary" 
            onClick={handleUpload} 
            disabled={!selectedFile || uploading}
          >
            {uploading ? '上传中...' : '上传文件'}
          </button>
        </div>
      </div>
      
      {/* 文件列表 */}
      <div className="card">
        <div className="card-body">
          <h3 className="card-title">已上传文件</h3>
          {loading ? (
            <Loading />
          ) : files.length === 0 ? (
            <p>暂无文件</p>
          ) : (
            <div className="table-responsive">
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>预览</th>
                    <th>文件名</th>
                  <th>文件路径</th>
                    <th>文件类型</th>
                    <th>修改时间</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  {files.map((file, index) => (
                    <tr key={index}>
                      <td>
                        {file.isImage && file.downloadUrl ? (
                          <img 
                            src={file.downloadUrl} 
                            alt={file.originName} 
                            className="img-thumbnail" 
                            style={{ maxHeight: '50px' }} 
                          />
                        ) : file.isImage ? (
                          <div className="image-loading-placeholder">
                            加载中...
                          </div>
                        ) : (
                          <div className="file-type-placeholder">
                            {file.size}
                          </div>
                        )}
                      </td>
                      <td title={file.objectKey}>{file.originName}</td>
                      <td style={{fontSize: '0.8em', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap'}} title={file.objectKey}>{file.objectKey}</td>
                      <td>{file.size}</td>
                      <td>{formatDate(file.lastModified)}</td>
                      <td>
                        <button
                          className="btn btn-sm btn-outline-primary me-2"
                          onClick={() => handleDownload(file)}
                        >
                          下载
                        </button>
                        {/* 只有管理员或文件所有者才能删除文件 */}
                        {(user && (user.role === 'manager' || file.authorId == user.id)) && (
                          <button
                            className="btn btn-sm btn-outline-danger"
                            onClick={() => handleDelete(file)}
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
        </div>
      </div>
      
      <style jsx>{`
        .file-manager {
          max-width: 1000px;
          margin: 0 auto;
        }
        
        .preview-container {
          margin-top: 10px;
          border: 1px solid #ddd;
          padding: 10px;
          border-radius: 4px;
          text-align: center;
        }
        
        .file-type-placeholder {
          display: inline-block;
          width: 50px;
          height: 50px;
          line-height: 50px;
          text-align: center;
          background-color: #f8f9fa;
          border: 1px solid #e9ecef;
          border-radius: 4px;
          font-size: 12px;
        }
        
        .image-loading-placeholder {
          display: inline-block;
          width: 50px;
          height: 50px;
          line-height: 50px;
          text-align: center;
          background-color: #f8f9fa;
          border: 1px solid #e9ecef;
          border-radius: 4px;
          font-size: 10px;
          color: #6c757d;
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
        
        .img-thumbnail {
          padding: .25rem;
          background-color: #fff;
          border: 1px solid #dee2e6;
          border-radius: .25rem;
          max-width: 100%;
          height: auto;
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
        
        .card {
          position: relative;
          display: flex;
          flex-direction: column;
          min-width: 0;
          word-wrap: break-word;
          background-color: #fff;
          background-clip: border-box;
          border: 1px solid rgba(0, 0, 0, 0.125);
          border-radius: 0.25rem;
          margin-bottom: 1.5rem;
        }
        
        .card-body {
          flex: 1 1 auto;
          padding: 1rem 1rem;
        }
        
        .card-title {
          margin-bottom: 0.5rem;
        }
        
        .img-fluid {
          max-width: 100%;
          height: auto;
        }
      `}</style>
    </div>
  );
};

export default FileManager;