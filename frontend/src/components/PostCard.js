import React from 'react';
import { Link } from 'react-router-dom';

const PostCard = ({ post }) => {
  // 格式化日期
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN');
  };

  return (
    <div className="card">
      <div className="card-body">
        <h3 className="card-title">
          <Link to={`/post/${post.id}`}>{post.title}</Link>
        </h3>
        <div className="post-meta">
          <span>发布时间: {formatDate(post.createTime)}</span>
          {post.updateTime && <span> | 更新时间: {formatDate(post.updateTime)}</span>}
          <span> | 浏览: {post.views || 0}</span>
          <span> | 点赞: {post.likes || 0}</span>
        </div>
        <p className="card-text">{post.summary}</p>
        <Link to={`/post/${post.id}`} className="btn btn-primary">
          阅读更多
        </Link>
        {post.categorys && post.categorys.length > 0 && (
          <div className="post-tags">
            分类: {post.categorys.map(category => (
              <span key={category.id} className="badge badge-primary">
                {category.title}
              </span>
            ))}
          </div>
        )}
        {post.labels && post.labels.length > 0 && (
          <div className="post-tags">
            标签: {post.labels.map(label => (
              <span key={label.id} className="badge badge-secondary">
                {label.title}
              </span>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default PostCard;