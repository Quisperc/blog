// 简单的JWT解析工具，从JWT中获取用户信息
export const parseJWT = (token) => {
  if (!token) return null;
  
  try {
    // JWT由三部分组成：header.payload.signature，用.分隔
    // payload部分包含用户信息，是Base64编码的
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    
    return JSON.parse(jsonPayload);
  } catch (error) {
    console.error('JWT解析失败:', error);
    return null;
  }
};

// 检查JWT是否过期
export const isTokenExpired = (token) => {
  if (!token) return true;
  
  const payload = parseJWT(token);
  if (!payload) return true;
  
  const currentTime = Math.floor(Date.now() / 1000);
  return payload.exp < currentTime;
};

// 从JWT中获取用户ID
export const getUserIdFromToken = (token) => {
  const payload = parseJWT(token);
  return payload ? payload.sub || payload.userId : null;
};

// 从JWT中获取用户名
export const getUsernameFromToken = (token) => {
  const payload = parseJWT(token);
  return payload ? payload.username : null;
};

// 从JWT中获取用户角色
export const getRoleFromToken = (token) => {
  const payload = parseJWT(token);
  console.log('解析JWT payload:', payload); // 添加调试日志
  
  if (!payload) return 'viewer';
  
  // 检查多种可能的字段名
  const roles = payload.roles || payload.authorities || payload.role || [];
  
  // 如果roles是字符串，转换为数组
  const roleArray = Array.isArray(roles) ? roles : [roles];
  
  // 如果有'manager'角色，则是管理员
  if (roleArray.includes('manager') || roleArray.includes('ROLE_MANAGER')) {
    console.log('识别为manager角色');
    return 'manager';
  } else if (roleArray.includes('poster') || roleArray.includes('ROLE_POSTER')) {
    console.log('识别为poster角色');
    return 'poster';
  } else {
    console.log('识别为viewer角色，roles:', roleArray);
    return 'viewer';
  }
};