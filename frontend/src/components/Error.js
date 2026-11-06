import React from 'react';

const Error = ({ message }) => {
  return (
    <div className="error">
      错误: {message || '发生未知错误，请稍后重试。'}
    </div>
  );
};

export default Error;