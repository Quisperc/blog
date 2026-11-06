import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { categoryAPI, postAPI } from '../services/api';
import PostCard from '../components/PostCard';
import Loading from '../components/Loading';
import Error from '../components/Error';

const CategoryDetail = () => {
  const { categoryId } = useParams();
  const [category, setCategory] = useState(null);
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchCategoryData = async () => {
      try {
        // 获取分类信息
        const categoryResponse = await categoryAPI.getById(categoryId);
        
        if (categoryResponse && categoryResponse.code === 200) {
          setCategory(categoryResponse.data);
          
          // 获取该分类下的文章
          const postsResponse = await postAPI.getByCategory(categoryId);
          
          if (postsResponse && postsResponse.code === 200) {
            setPosts(postsResponse.data || []);
          } else {
            setError(postsResponse.msg || '获取分类文章失败');
          }
        } else {
          setError(categoryResponse.msg || '获取分类信息失败');
        }
      } catch (err) {
        setError('网络请求失败，请稍后重试');
        console.error('获取分类数据失败:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchCategoryData();
  }, [categoryId]);

  if (loading) {
    return <Loading />;
  }

  if (error) {
    return <Error message={error} />;
  }

  if (!category) {
    return <p>分类不存在</p>;
  }

  return (
    <div className="category-detail">
      <h1>{category.title}</h1>
      {category.summary && <p className="mb-3">{category.summary}</p>}
      
      <h2 className="mt-4 mb-3">分类文章</h2>
      {posts.length === 0 ? (
        <p>该分类下暂无文章</p>
      ) : (
        posts.map(post => (
          <PostCard key={post.postId} post={post} />
        ))
      )}
    </div>
  );
};

export default CategoryDetail;