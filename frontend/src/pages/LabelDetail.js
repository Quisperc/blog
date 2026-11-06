import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { labelAPI, postAPI } from '../services/api';
import PostCard from '../components/PostCard';
import Loading from '../components/Loading';
import Error from '../components/Error';

const LabelDetail = () => {
  const { labelId } = useParams();
  const [label, setLabel] = useState(null);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchLabelData = async () => {
      try {
        // 获取标签信息
        const labelResponse = await labelAPI.getById(labelId);
        
        if (labelResponse && labelResponse.code === 200) {
          setLabel(labelResponse.data);
          
          // 获取该标签下的文章
          const postsResponse = await postAPI.getByLabel(labelId);
          
          if (postsResponse && postsResponse.code === 200) {
            setPosts(postsResponse.data || []);
          } else {
            setError(postsResponse.msg || '获取标签文章失败');
          }
        } else {
          setError(labelResponse.msg || '获取标签信息失败');
        }
      } catch (err) {
        setError('网络请求失败，请稍后重试');
        console.error('获取标签数据失败:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchLabelData();
  }, [labelId]);

  if (loading) {
    return <Loading />;
  }

  if (error) {
    return <Error message={error} />;
  }

  if (!label) {
    return <p>标签不存在</p>;
  }

  return (
    <div className="label-detail">
      <h1>{label.title}</h1>
      {label.summary && <p className="mb-3">{label.summary}</p>}
      
      <h2 className="mt-4 mb-3">标签文章</h2>
      {posts.length === 0 ? (
        <p>该标签下暂无文章</p>
      ) : (
        posts.map(post => (
          <PostCard key={post.postId} post={post} />
        ))
      )}
    </div>
  );
};

export default LabelDetail;