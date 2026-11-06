import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { categoryAPI } from '../services/api';
import Loading from '../components/Loading';
import Error from '../components/Error';

const Categories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await categoryAPI.getAll();
        if (response && response.code === 200) {
          setCategories(response.data || []);
        } else {
          setError(response.msg || '获取分类列表失败');
        }
      } catch (err) {
        setError('网络请求失败，请稍后重试');
        console.error('获取分类列表失败:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  return (
    <div className="categories-page">
      <h1 className="mb-3">文章分类</h1>
      {loading ? (
        <Loading />
      ) : error ? (
        <Error message={error} />
      ) : categories.length === 0 ? (
        <p>暂无分类</p>
      ) : (
        <div className="row">
          <div className="col-9">
            <div className="list-group">
              {categories.map(category => (
                <div key={category.id} className="list-group-item">
                  <div className="d-flex w-100 justify-content-between">
                    <h5 className="mb-1">
                      <Link to={`/category/${category.id}`}>
                        {category.title}
                      </Link>
                    </h5>
                    <small>分类 ID: {category.id}</small>
                  </div>
                  {category.summary && <p className="mb-1">{category.summary}</p>}
                </div>
              ))}
            </div>
          </div>
          <div className="col-3">
            {/* 侧边栏可以在这里添加 */}
          </div>
        </div>
      )}
    </div>
  );
};

export default Categories;