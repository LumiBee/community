/**
 * 大整数ID处理工具
 * 解决JavaScript处理64位长整型ID时的精度丢失问题
 */

/**
 * 将大整数ID转换为字符串，确保精度不丢失
 * @param {number|string} id - 用户ID
 * @returns {string} 字符串形式的ID
 */
export function ensureBigIntAsString(id) {
  if (id === null || id === undefined) {
    return null;
  }
  
  // 如果已经是字符串，直接返回
  if (typeof id === 'string') {
    return id;
  }
  
  // 如果是数字，转换为字符串
  if (typeof id === 'number') {
    return id.toString();
  }
  
  // 其他类型，尝试转换为字符串
  return String(id);
}

/**
 * 安全地解析JSON，确保大整数ID不被精度丢失
 * @param {string} jsonString - JSON字符串
 * @returns {object} 解析后的对象
 */
export function safeJsonParse(jsonString) {
  try {
    // 使用自定义的reviver函数来处理大整数
    return JSON.parse(jsonString, (key, value) => {
      // 检查是否是可能的用户ID字段
      if (key === 'id' || key === 'userId' || key === 'followerId' || key === 'targetUserId') {
        return ensureBigIntAsString(value);
      }
      return value;
    });
  } catch (error) {
    console.error('JSON解析失败:', error);
    return null;
  }
}

/**
 * 安全地序列化对象，确保大整数ID不被精度丢失
 * @param {object} obj - 要序列化的对象
 * @returns {string} JSON字符串
 */
export function safeJsonStringify(obj) {
  try {
    return JSON.stringify(obj, (key, value) => {
      // 检查是否是可能的用户ID字段
      if (key === 'id' || key === 'userId' || key === 'followerId' || key === 'targetUserId') {
        return ensureBigIntAsString(value);
      }
      return value;
    });
  } catch (error) {
    console.error('JSON序列化失败:', error);
    return null;
  }
}

/**
 * 从localStorage安全地获取用户信息
 * @param {string} key - localStorage键名
 * @returns {object|null} 用户信息对象
 */
export function getSafeUserFromStorage(key = 'hive_auth_user') {
  try {
    const stored = localStorage.getItem(key);
    if (!stored) {
      return null;
    }
    return safeJsonParse(stored);
  } catch (error) {
    console.error('从localStorage获取用户信息失败:', error);
    return null;
  }
}

/**
 * 安全地将用户信息保存到localStorage
 * @param {object} userData - 用户信息对象
 * @param {string} key - localStorage键名
 */
export function setSafeUserToStorage(userData, key = 'hive_auth_user') {
  try {
    const safeData = safeJsonStringify(userData);
    if (safeData) {
      localStorage.setItem(key, safeData);
    }
  } catch (error) {
    console.error('保存用户信息到localStorage失败:', error);
  }
}

/**
 * 验证ID是否为大整数（超过JavaScript安全整数范围）
 * @param {number|string} id - 要验证的ID
 * @returns {boolean} 是否为大整数
 */
export function isBigInt(id) {
  if (typeof id === 'string') {
    const num = Number(id);
    return num > Number.MAX_SAFE_INTEGER;
  }
  if (typeof id === 'number') {
    return id > Number.MAX_SAFE_INTEGER;
  }
  return false;
}

/**
 * 调试工具：显示ID的详细信息
 * @param {number|string} id - 要调试的ID
 * @param {string} label - 标签
 */
export function debugId(id, label = 'ID') {
  const stringId = ensureBigIntAsString(id);
  const isBig = isBigInt(id);

  return stringId;
}
