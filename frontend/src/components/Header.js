import React, { useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const Header = () => {
  const { user, isAuthenticated, logout } = useContext(AuthContext);
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
  };

  const toggleMenu = () => {
    setMenuOpen(!menuOpen);
  };

  return (
    <header className="navbar">
      <Link to="/" className="navbar-brand">
        个人博客
      </Link>
      
      {/* 移动端菜单按钮 */}
      <button 
        className="navbar-toggler"
        type="button"
        onClick={toggleMenu}
      >
        <span className="navbar-toggler-icon"></span>
      </button>
      
      <nav className={menuOpen ? "navbar-mobile-open" : ""}>
        <ul className="navbar-nav">
          <li className="nav-item">
            <Link to="/" className="nav-link" onClick={() => setMenuOpen(false)}>首页</Link>
          </li>
          <li className="nav-item">
            <Link to="/categories" className="nav-link" onClick={() => setMenuOpen(false)}>分类</Link>
          </li>
          <li className="nav-item">
            <Link to="/labels" className="nav-link" onClick={() => setMenuOpen(false)}>标签</Link>
          </li>
          <li className="nav-item">
            <Link to="/gallery" className="nav-link" onClick={() => setMenuOpen(false)}>图片库</Link>
          </li>
          
          {/* 已登录用户的导航 */}
          {isAuthenticated ? (
            <>
              <li className="nav-item">
                <span className="nav-link user-info">
                  欢迎, {user?.username}
                  {user?.role === 'manager' && <span className="badge admin-badge">管理员</span>}
                </span>
              </li>
              <li className="nav-item">
                <Link to="/profile" className="nav-link" onClick={() => setMenuOpen(false)}>个人中心</Link>
              </li>
              {user?.role === 'manager' && (
                <li className="nav-item">
                  <Link to="/admin" className="nav-link" onClick={() => setMenuOpen(false)}>管理后台</Link>
                </li>
              )}
              <li className="nav-item">
                <button className="nav-link logout-btn" onClick={handleLogout}>
                  退出登录
                </button>
              </li>
            </>
          ) : (
            <>
              <li className="nav-item">
                <Link to="/login" className="nav-link" onClick={() => setMenuOpen(false)}>登录</Link>
              </li>
              <li className="nav-item">
                <Link to="/register" className="nav-link" onClick={() => setMenuOpen(false)}>注册</Link>
              </li>
            </>
          )}
        </ul>
      </nav>
      
      <style jsx>{`
        .navbar {
          position: relative;
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          justify-content: space-between;
          padding: 15px;
          margin-bottom: 20px;
          background-color: #fff;
          box-shadow: 0 2px 4px rgba(0,0,0,.1);
        }
        
        .navbar-brand {
          display: inline-block;
          padding-top: .3125rem;
          padding-bottom: .3125rem;
          margin-right: 1rem;
          font-size: 1.25rem;
          line-height: inherit;
          white-space: nowrap;
          font-weight: 600;
        }
        
        .navbar-toggler {
          display: none;
          padding: 0.25rem 0.75rem;
          font-size: 1.25rem;
          line-height: 1;
          background-color: transparent;
          border: 1px solid transparent;
          border-radius: 0.25rem;
          cursor: pointer;
        }
        
        .navbar-toggler-icon {
          display: inline-block;
          width: 1.5em;
          height: 1.5em;
          vertical-align: middle;
          background-repeat: no-repeat;
          background-position: center;
          background-size: 100%;
        }
        
        .navbar-toggler-icon:before {
          content: "≡";
          font-size: 24px;
          line-height: 1;
        }
        
        .navbar-nav {
          display: flex;
          flex-direction: row;
          padding-left: 0;
          margin-bottom: 0;
          list-style: none;
        }
        
        .nav-item {
          margin-right: 15px;
        }
        
        .nav-link {
          display: block;
          padding: 0.5rem 1rem;
          color: rgba(0,0,0,.55);
          text-decoration: none;
          transition: color .15s ease-in-out,background-color .15s ease-in-out,border-color .15s ease-in-out;
        }
        
        .nav-link:hover {
          color: rgba(0,0,0,.7);
          text-decoration: none;
        }
        
        .user-info {
          display: flex;
          align-items: center;
          gap: 5px;
        }
        
        .badge {
          display: inline-block;
          padding: 3px 7px;
          font-size: 12px;
          font-weight: bold;
          line-height: 1;
          color: #fff;
          text-align: center;
          white-space: nowrap;
          vertical-align: baseline;
          border-radius: 10px;
          background-color: #dc3545;
        }
        
        .admin-badge {
          background-color: #dc3545;
        }
        
        .logout-btn {
          background: none;
          border: none;
          padding: 0.5rem 1rem;
          cursor: pointer;
          color: rgba(0,0,0,.55);
          font-family: inherit;
        }
        
        .logout-btn:hover {
          color: rgba(0,0,0,.7);
        }
        
        /* 移动端样式 */
        @media (max-width: 768px) {
          .navbar-toggler {
            display: block;
          }
          
          .navbar-nav {
            display: none;
            flex-direction: column;
            width: 100%;
            margin-top: 10px;
          }
          
          .navbar-mobile-open .navbar-nav {
            display: flex;
          }
          
          .nav-item {
            margin-right: 0;
            margin-bottom: 5px;
          }
          
          .user-info {
            margin: 5px 0;
          }
        }
      `}</style>
    </header>
  );
};

export default Header;