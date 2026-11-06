import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { postAPI } from '../services/api';
import PostCard from '../components/PostCard';
import Loading from '../components/Loading';
import Error from '../components/Error';

const Home = () => {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const location = useLocation();

  const fetchPosts = async () => {
    try {
      setLoading(true);
      const response = await postAPI.getAll();
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

  useEffect(() => {
    // 检查是否需要强制刷新（例如，在状态更新后）
    const forceRefreshPosts = localStorage.getItem('forceRefreshPosts');
    
    if (forceRefreshPosts) {
      console.log('检测到文章强制刷新标记');
      localStorage.removeItem('forceRefreshPosts');
    }
    
    fetchPosts();
  }, []);

  // 如果从文章详情页返回，刷新列表以获取最新的浏览量和点赞量
  useEffect(() => {
    if (location.state?.from === 'postDetail') {
      console.log('从文章详情页返回，刷新列表');
      fetchPosts();
      // 清除状态，避免重复刷新
      window.history.replaceState({}, document.title);
    }
  }, [location]);
  
  // 添加页面可见性变化监听，当页面重新获得焦点时刷新数据
  useEffect(() => {
    const handleVisibilityChange = () => {
      // 只在页面刚变为可见时刷新一次，使用节流防止频繁刷新
      if (document.visibilityState === 'visible') {
        console.log('页面重新可见，刷新文章列表');
        // 延迟刷新，避免与用户操作冲突
        setTimeout(() => {
          fetchPosts();
        }, 500);
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  return (
    <div className="home-page">
      <h1 className="mb-3">最新文章</h1>
      {loading ? (
        <Loading />
      ) : error ? (
        <Error message={error} />
      ) : posts.length === 0 ? (
        <p>暂无文章</p>
      ) : (
        <div className="row">
          <div className="col-9">
            {posts.map(post => (
              <PostCard key={post.id} post={post} />
            ))}
          </div>
          <div className="col-3">
            {/* 侧边栏可以在这里添加 */}
          </div>
        </div>
      )}
    </div>
  );
};

export default Home;