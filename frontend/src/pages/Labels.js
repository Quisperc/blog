import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { labelAPI } from '../services/api';
import Loading from '../components/Loading';
import Error from '../components/Error';

const Labels = () => {
  const [labels, setLabels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
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

    fetchLabels();
  }, []);

  return (
    <div className="labels-page">
      <h1 className="mb-3">文章标签</h1>
      {loading ? (
        <Loading />
      ) : error ? (
        <Error message={error} />
      ) : labels.length === 0 ? (
        <p>暂无标签</p>
      ) : (
        <div className="row">
          <div className="col-9">
            <div className="list-group">
              {labels.map(label => (
                <div key={label.id} className="list-group-item">
                  <div className="d-flex w-100 justify-content-between">
                    <h5 className="mb-1">
                      <Link to={`/label/${label.id}`}>
                        {label.title}
                      </Link>
                    </h5>
                    <small>标签 ID: {label.id}</small>
                  </div>
                  {label.summary && <p className="mb-1">{label.summary}</p>}
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

export default Labels;