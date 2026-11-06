import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 在这里可以添加token等认证信息
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log('发送请求:', config.method?.toUpperCase(), config.url, config.params || config.data);
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('收到响应:', response.config.method?.toUpperCase(), response.config.url, response.data);
    
    // 如果是更新请求，清除相关缓存
    if ((response.config.method === 'put' || response.config.method === 'PUT') && 
        (response.config.url.includes('/api/post/update') || 
         response.config.url.includes('/api/admin/category/update') || 
         response.config.url.includes('/api/admin/label/update'))) {
      
      console.log('检测到更新请求，清除本地缓存数据');
      // 清除相关的localStorage缓存（如果有的话）
      if (typeof localStorage !== 'undefined') {
        // 这里可以清除特定的缓存键，或者简单地标记需要刷新
        localStorage.setItem('forceRefreshPosts', Date.now().toString());
        localStorage.setItem('forceRefreshCategories', Date.now().toString());
        localStorage.setItem('forceRefreshLabels', Date.now().toString());
      }
    }
    
    // 如果是登录请求，需要返回完整的响应以便获取token和用户信息
    if (response.config.url.includes('/api/user/login')) {
      // 从响应头获取token（注意大小写可能不同）
      const token = response.headers.authorization || 
                  response.headers.Authorization || 
                  response.headers['authorization'] || 
                  response.headers['Authorization'];
      
      console.log('从响应头获取的token:', token);
      
      // 构造返回对象，包含响应数据和token
      const result = {
        ...response.data,
        token: token
      };
      
      console.log('登录响应完整对象:', result);
      return result;
    }
    
    // 直接返回data部分
    return response.data;
  },
  error => {
    // 处理错误
    console.error('API Error:', error.response?.status, error.response?.data || error.message);
    
    // 如果是401错误，可能是token过期或无效
    if (error.response && error.response.status === 401) {
      console.log('Token无效或过期，清除登录状态');
      // 清除本地存储的token和用户信息
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // 可以在这里添加重定向到登录页的逻辑
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

// 文章相关API
export const postAPI = {
  // 获取所有文章
  getAll: () => api.get('/api/post/select'),
  
  // 根据ID获取文章详情
  getById: (postId) => api.get(`/api/post/selectbypost/${postId}`),
  
  // 根据标题搜索文章
  getByTitle: (title) => api.get(`/api/post/selectbytitle/${title}`),
  
  // 根据分类ID获取文章
  getByCategory: (categoryId) => api.get(`/api/post/selectbycategory/${categoryId}`),
  
  // 根据标签ID获取文章
  getByLabel: (labelId) => api.get(`/api/post/selectbylabel/${labelId}`),
  
  // 根据用户ID获取文章（博主专用）
  getByAuthor: (userId) => api.get(`/api/post/selectbyuser/${userId}`),
  
  // 增加文章点赞数
  likePost: (postId) => api.post('/api/post/update/like', null, { params: { postId } }),
  
  // 增加文章浏览量
  viewPost: (postId) => api.post('/api/post/update/view', null, { params: { postId } }),
  
  // 创建文章 (需要管理员权限)
  create: (postData) => api.post('/api/post/add', postData),
  
  // 更新文章 (需要管理员权限)
  update: (postId, postData) => api.put('/api/post/update', postData, { params: { postId } }),
  
  // 删除文章 (需要管理员权限)
  delete: (postId) => api.delete(`/api/post/delete/${postId}`),
  
  // 根据分类删除文章 (需要管理员权限)
  deleteByCategory: (categoryId) => api.delete(`/api/post/deletebycategory/${categoryId}`),
  
  // 根据标签删除文章 (需要管理员权限)
  deleteByLabel: (labelId) => api.delete(`/api/post/deletebylabel/${labelId}`)
};

// 分类相关API
export const categoryAPI = {
  // 获取所有分类
  getAll: () => api.get('/api/admin/category/select'),
  
  // 根据ID获取分类
  getById: (categoryId) => api.get(`/api/admin/category/selectBycategoryId/${categoryId}`),
  
  // 根据标题获取分类
  getByTitle: (title) => api.get(`/api/admin/category/selectByTitle/${title}`),
  
  // 创建分类 (需要管理员权限)
  create: (title) => api.post('/api/admin/category/add', null, { params: { title } }),
  
  // 更新分类 (需要管理员权限)
  update: (categoryId, title, status) => api.put('/api/admin/category/update', null, { 
    params: { categoryId, title, status } 
  }),
  
  // 删除分类 (需要管理员权限)
  delete: (categoryId) => api.delete('/api/admin/category/delete', { params: { categoryId } })
};

// 标签相关API
export const labelAPI = {
  // 获取所有标签
  getAll: () => api.get('/api/admin/label/select'),
  
  // 根据ID获取标签
  getById: (labelId) => api.get(`/api/admin/label/selectBylabelId/${labelId}`),
  
  // 根据标题获取标签
  getByTitle: (title) => api.get(`/api/admin/label/selectByTitle/${title}`),
  
  // 创建标签 (需要管理员权限)
  create: (title) => api.post('/api/admin/label/add', null, { params: { title } }),
  
  // 更新标签 (需要管理员权限)
  update: (labelId, title, status) => api.put('/api/admin/label/update', null, { 
    params: { labelId, title, status } 
  }),
  
  // 删除标签 (需要管理员权限)
  delete: (labelId) => api.delete('/api/admin/label/delete', { params: { labelId } })
};

// 用户相关API
export const userAPI = {
  // 用户注册
  register: (username, password) => api.post('/api/user/register', null, { 
    params: { username, password } 
  }),
  
  // 用户登录
  login: (username, password) => api.post('/api/user/login', null, { 
    params: { username, password } 
  }),
  
  // 用户登出
  logout: () => api.post('/api/user/logout'),
  
  // 更新用户信息
  update: (username, password) => api.put('/api/user/update', null, { 
    params: { username, password } 
  })
};

// 文件相关API
export const fileAPI = {
  // 获取所有文件
  getAll: () => api.get('/api/file/select'),
  
  // 获取数据库中的所有文件记录
  getAllRecords: () => api.get('/api/file/all'),
  
  // 获取当前用户的文件记录
  getMyFiles: () => api.get('/api/file/my'),
  
  // 上传文件
  upload: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/api/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },
  
  // 获取下载链接
  getDownloadUrl: (objectKey) => api.get('/api/file/download/url', { params: { objectKey } }),
  
  // 下载文件
  download: (objectKey) => api.get('/api/file/download/stream', { 
    params: { objectKey },
    responseType: 'blob'
  }),
  
  // 删除文件
  delete: (filePath) => api.delete('/api/file/delete', { params: { filePath } })
};

// 管理员用户管理API
export const adminUserAPI = {
  // 获取所有用户 (需要管理员权限)
  getAll: () => api.put('/api/admin/selectAll'),
  
  // 更新用户角色 (需要管理员权限)
  updateRole: (userId, role) => {
    console.log('更新用户角色:', userId, role);
    return api.put('/api/admin/update', null, { 
      params: { userId, role } 
    });
  },
  
  // 删除用户 (需要管理员权限)
  delete: (userId) => api.delete('/api/admin/delete', { params: { userId } })
};

export default api;