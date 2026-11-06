import React, { useState, useEffect } from 'react';
import { fileAPI } from '../services/api';
import Loading from '../components/Loading';
import Error from '../components/Error';

const Gallery = () => {
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedImage, setSelectedImage] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const imagesPerPage = 12;

  useEffect(() => {
    fetchImages();
  }, []);

  const fetchImages = async () => {
    try {
      const response = await fileAPI.getAllRecords();
      if (response && response.code === 200 && response.data) {
        // 过滤出图片文件
        const imageFiles = response.data.filter(file => 
          file.objectKey.match(/\.(jpg|jpeg|png|gif)$/i)
        );
        
        // 为每个图片获取下载URL
        const imagesWithUrls = await Promise.all(imageFiles.map(async (file) => {
          let downloadUrl = null;
          try {
            const urlResponse = await fileAPI.getDownloadUrl(file.objectKey);
            if (urlResponse && urlResponse.code === 200) {
              downloadUrl = urlResponse.data;
            }
          } catch (err) {
            console.error(`获取图片 ${file.objectKey} 的下载URL失败:`, err);
          }
          
          return {
            ...file,
            downloadUrl
          };
        }));
        
        setImages(imagesWithUrls);
      } else {
        setError(response.msg || '获取图片列表失败');
      }
    } catch (err) {
      setError('网络请求失败，请稍后重试');
      console.error('获取图片列表失败:', err);
    } finally {
      setLoading(false);
    }
  };

  const openImageModal = (image) => {
    setSelectedImage(image);
  };

  const closeImageModal = () => {
    setSelectedImage(null);
  };

  const downloadImage = async (image) => {
    try {
      if (!image.downloadUrl) {
        setError('图片URL未准备好，请稍后再试');
        return;
      }
      
      // 创建临时下载链接并点击
      const link = document.createElement('a');
      link.href = image.downloadUrl;
      link.download = image.originName;
      link.target = '_blank';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      setError('下载失败，请稍后重试');
      console.error('下载图片失败:', err);
    }
  };

  // 分页计算
  const indexOfLastImage = currentPage * imagesPerPage;
  const indexOfFirstImage = indexOfLastImage - imagesPerPage;
  const currentImages = images.slice(indexOfFirstImage, indexOfLastImage);
  const totalPages = Math.ceil(images.length / imagesPerPage);

  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  return (
    <div className="gallery-page">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>图片库</h1>
        <div className="image-count">
          共 {images.length} 张图片
        </div>
      </div>
      
      {error && <Error message={error} />}
      
      {loading ? (
        <Loading />
      ) : images.length === 0 ? (
        <div className="no-images">
          <p>暂无图片</p>
        </div>
      ) : (
        <>
          <div className="image-grid">
            {currentImages.map((image, index) => (
              <div className="image-card" key={image.id}>
                <div className="image-container" onClick={() => openImageModal(image)}>
                  <img 
                    src={image.downloadUrl} 
                    alt={image.originName}
                    className="image-thumbnail"
                  />
                  <div className="image-overlay">
                    <div className="image-info">
                      <p>{image.originName}</p>
                      <small>{image.uploadTime ? new Date(image.uploadTime).toLocaleDateString() : ''}</small>
                    </div>
                  </div>
                </div>
                <div className="image-actions">
                  <button 
                    className="btn btn-sm btn-outline-primary"
                    onClick={(e) => {
                      e.stopPropagation();
                      openImageModal(image);
                    }}
                  >
                    查看
                  </button>
                  <button 
                    className="btn btn-sm btn-outline-success"
                    onClick={(e) => {
                      e.stopPropagation();
                      downloadImage(image);
                    }}
                  >
                    下载
                  </button>
                </div>
              </div>
            ))}
          </div>
          
          {/* 分页 */}
          {totalPages > 1 && (
            <div className="pagination-container">
              <nav>
                <ul className="pagination">
                  <li className={`page-item ${currentPage === 1 ? 'disabled' : ''}`}>
                    <button 
                      className="page-link" 
                      onClick={() => paginate(currentPage - 1)}
                      disabled={currentPage === 1}
                    >
                      上一页
                    </button>
                  </li>
                  {Array.from({ length: totalPages }, (_, i) => i + 1).map(number => (
                    <li 
                      key={number} 
                      className={`page-item ${currentPage === number ? 'active' : ''}`}
                    >
                      <button 
                        className="page-link" 
                        onClick={() => paginate(number)}
                      >
                        {number}
                      </button>
                    </li>
                  ))}
                  <li className={`page-item ${currentPage === totalPages ? 'disabled' : ''}`}>
                    <button 
                      className="page-link" 
                      onClick={() => paginate(currentPage + 1)}
                      disabled={currentPage === totalPages}
                    >
                      下一页
                    </button>
                  </li>
                </ul>
              </nav>
            </div>
          )}
        </>
      )}
      
      {/* 图片模态框 */}
      {selectedImage && (
        <div className="image-modal" onClick={closeImageModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h4>{selectedImage.originName}</h4>
              <button className="btn-close" onClick={closeImageModal}>&times;</button>
            </div>
            <div className="modal-body">
              <img 
                src={selectedImage.downloadUrl} 
                alt={selectedImage.originName}
                className="modal-image"
              />
              <div className="image-details">
                <p><strong>文件名:</strong> {selectedImage.originName}</p>
                <p><strong>上传时间:</strong> {selectedImage.uploadTime ? new Date(selectedImage.uploadTime).toLocaleString() : ''}</p>
                <p><strong>文件路径:</strong> {selectedImage.objectKey}</p>
              </div>
            </div>
            <div className="modal-footer">
              <button 
                className="btn btn-outline-secondary"
                onClick={closeImageModal}
              >
                关闭
              </button>
              <button 
                className="btn btn-success"
                onClick={() => downloadImage(selectedImage)}
              >
                下载
              </button>
            </div>
          </div>
        </div>
      )}
      
      <style jsx>{`
        .gallery-page {
          max-width: 1200px;
          margin: 0 auto;
          padding: 20px;
        }
        
        .image-count {
          color: #6c757d;
          font-size: 1rem;
        }
        
        .no-images {
          text-align: center;
          padding: 50px 0;
          color: #6c757d;
        }
        
        .image-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 20px;
          margin-bottom: 30px;
        }
        
        .image-card {
          background-color: #fff;
          border-radius: 8px;
          overflow: hidden;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
          transition: transform 0.3s ease;
        }
        
        .image-card:hover {
          transform: translateY(-5px);
        }
        
        .image-container {
          position: relative;
          height: 200px;
          overflow: hidden;
          cursor: pointer;
        }
        
        .image-thumbnail {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s ease;
        }
        
        .image-container:hover .image-thumbnail {
          transform: scale(1.05);
        }
        
        .image-overlay {
          position: absolute;
          bottom: 0;
          left: 0;
          right: 0;
          background-color: rgba(0, 0, 0, 0.7);
          color: white;
          padding: 10px;
          transform: translateY(100%);
          transition: transform 0.3s ease;
        }
        
        .image-container:hover .image-overlay {
          transform: translateY(0);
        }
        
        .image-info {
          margin: 0;
        }
        
        .image-info p {
          margin: 0 0 5px 0;
          font-weight: 500;
        }
        
        .image-info small {
          font-size: 0.8rem;
          opacity: 0.8;
        }
        
        .image-actions {
          display: flex;
          justify-content: space-between;
          padding: 10px;
          background-color: #f8f9fa;
        }
        
        .pagination-container {
          display: flex;
          justify-content: center;
          margin-top: 20px;
        }
        
        .pagination {
          display: flex;
          list-style: none;
          padding: 0;
          margin: 0;
        }
        
        .page-item {
          margin: 0 5px;
        }
        
        .page-link {
          display: block;
          padding: 8px 12px;
          border: 1px solid #dee2e6;
          background-color: #fff;
          color: #0d6efd;
          text-decoration: none;
          border-radius: 4px;
          cursor: pointer;
          transition: all 0.3s ease;
        }
        
        .page-link:hover {
          background-color: #e9ecef;
        }
        
        .page-item.active .page-link {
          background-color: #0d6efd;
          color: #fff;
          border-color: #0d6efd;
        }
        
        .page-item.disabled .page-link {
          color: #6c757d;
          pointer-events: none;
          background-color: #f8f9fa;
          border-color: #dee2e6;
        }
        
        /* 模态框样式 */
        .image-modal {
          position: fixed;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background-color: rgba(0, 0, 0, 0.8);
          display: flex;
          justify-content: center;
          align-items: center;
          z-index: 1000;
        }
        
        .modal-content {
          background-color: #fff;
          border-radius: 8px;
          max-width: 800px;
          max-height: 90vh;
          width: 90%;
          overflow-y: auto;
        }
        
        .modal-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px 20px;
          border-bottom: 1px solid #dee2e6;
        }
        
        .modal-header h4 {
          margin: 0;
          font-size: 1.2rem;
        }
        
        .btn-close {
          background: none;
          border: none;
          font-size: 1.5rem;
          cursor: pointer;
          color: #6c757d;
        }
        
        .modal-body {
          padding: 20px;
        }
        
        .modal-image {
          max-width: 100%;
          max-height: 60vh;
          display: block;
          margin: 0 auto;
        }
        
        .image-details {
          margin-top: 20px;
          padding: 15px;
          background-color: #f8f9fa;
          border-radius: 4px;
        }
        
        .image-details p {
          margin: 5px 0;
        }
        
        .modal-footer {
          display: flex;
          justify-content: flex-end;
          gap: 10px;
          padding: 15px 20px;
          border-top: 1px solid #dee2e6;
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
        
        .btn-sm {
          padding: 0.25rem 0.5rem;
          font-size: 0.875rem;
          border-radius: 0.2rem;
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
        
        .btn-outline-success {
          color: #198754;
          border-color: #198754;
        }
        
        .btn-outline-success:hover {
          color: #fff;
          background-color: #198754;
          border-color: #198754;
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
        
        .btn-success {
          color: #fff;
          background-color: #198754;
          border-color: #198754;
        }
        
        .btn-success:hover {
          color: #fff;
          background-color: #157347;
          border-color: #146c43;
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
        
        .mb-4 {
          margin-bottom: 1.5rem;
        }
      `}</style>
    </div>
  );
};

export default Gallery;