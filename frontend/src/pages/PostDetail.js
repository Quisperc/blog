import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { postAPI } from '../services/api';
import Loading from '../components/Loading';
import Error from '../components/Error';

const PostDetail = () => {
  const { postId } = useParams();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [likeCount, setLikeCount] = useState(0);
  const [viewCount, setViewCount] = useState(0);

  useEffect(() => {
    const fetchPost = async () => {
      try {
        const response = await postAPI.getById(postId);
        if (response && response.code === 200) {
          setPost(response.data);
          setLikeCount(response.data.likes || 0);
          setViewCount(response.data.views || 0);
        } else {
          setError(response.msg || '获取文章详情失败');
        }
      } catch (err) {
        setError('网络请求失败，请稍后重试');
        console.error('获取文章详情失败:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchPost();
  }, [postId]);

  const handleLike = async () => {
    try {
      const response = await postAPI.likePost(postId);
      if (response && response.code === 200) {
        setLikeCount(prev => prev + 1);
        // 同时更新文章对象中的likes，确保一致性
        setPost(prev => ({
          ...prev,
          likes: likeCount + 1
        }));
      } else {
        alert(response.msg || '点赞失败');
      }
    } catch (err) {
      alert('点赞失败，请稍后重试');
      console.error('点赞失败:', err);
    }
  };

  // 格式化日期
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN');
  };

  if (loading) {
    return <Loading />;
  }

  if (error) {
    return <Error message={error} />;
  }

  if (!post) {
    return <p>文章不存在</p>;
  }

  return (
    <div className="post-detail">
      <h1>{post.title}</h1>
      <div className="post-meta">
        <span>发布时间: {formatDate(post.createdAt)}</span>
        {post.updateTime && <span> | 更新时间: {formatDate(post.updateTime)}</span>}
        <span> | 浏览: {viewCount}</span>
        <span> | 点赞: {likeCount}</span>
      </div>
      
      {post.categories && post.categories.length > 0 && (
        <div className="post-tags">
          分类: {post.categories.map(category => (
            <Link key={category.id} to={`/category/${category.id}`} className="badge badge-primary">
              {category.title}
            </Link>
          ))}
        </div>
      )}
      
      {post.labels && post.labels.length > 0 && (
        <div className="post-tags">
          标签: {post.labels.map(label => (
            <Link key={label.id} to={`/label/${label.id}`} className="badge badge-secondary">
              {label.title}
            </Link>
          ))}
        </div>
      )}
      
      <div className="post-content mt-4">
        {post.content ? (
          // 如果内容是Markdown格式，可以在这里添加Markdown解析器
          <div dangerouslySetInnerHTML={{ __html: post.content.replace(/\n/g, '<br>') }} />
        ) : (
          <p>暂无内容</p>
        )}
      </div>
      
      <div className="post-actions mt-4">
        <button className="btn btn-primary" onClick={handleLike}>
          点赞 ({likeCount})
        </button>
        <Link to={{ pathname: "/", state: { from: 'postDetail' } }} className="btn btn-primary ml-3">
          返回首页
        </Link>
      </div>
    </div>
  );
};

export default PostDetail;