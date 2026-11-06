import React from 'react';

const Footer = () => {
  return (
    <footer className="app-footer">
      <div className="container">
        <p>&copy; {new Date().getFullYear()} 个人博客. 保留所有权利.</p>
      </div>
    </footer>
  );
};

export default Footer;