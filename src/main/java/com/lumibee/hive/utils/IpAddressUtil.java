package com.lumibee.hive.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import lombok.extern.log4j.Log4j2;

/**
 * IP地址工具类
 * 用于获取客户端真实IP地址，支持代理和负载均衡场景
 */
@Log4j2
public class IpAddressUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端真实IP地址
     * 支持通过代理、负载均衡器访问的场景
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址，如果无法获取则返回 "unknown"
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            log.warn("HttpServletRequest is null, cannot get IP address");
            return UNKNOWN;
        }

        String ip = null;

        // 1. 尝试从 X-Forwarded-For 获取（标准代理头）
        ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For 可能包含多个IP，格式: client, proxy1, proxy2
            // 取第一个IP（客户端真实IP）
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }

        // 2. 尝试从 X-Real-IP 获取（Nginx常用）
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 3. 尝试从 Proxy-Client-IP 获取（Apache常用）
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 4. 尝试从 WL-Proxy-Client-IP 获取（WebLogic常用）
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 5. 尝试从 HTTP_CLIENT_IP 获取
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 6. 尝试从 HTTP_X_FORWARDED_FOR 获取
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }

        // 7. 最后从 RemoteAddr 获取（直连场景）
        ip = request.getRemoteAddr();

        // 处理 IPv6 本地地址
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        return ip != null ? ip : UNKNOWN;
    }

    /**
     * 检查IP地址是否有效
     *
     * @param ip IP地址字符串
     * @return true=有效，false=无效
     */
    private static boolean isValidIp(String ip) {
        return StringUtils.hasText(ip) && !UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 对IP地址进行哈希处理（用于缩短长度）
     * 适用于IPv6地址或需要匿名化的场景
     *
     * @param ip IP地址
     * @return 哈希后的字符串（16位）
     */
    public static String hashIpAddress(String ip) {
        if (!isValidIp(ip)) {
            return UNKNOWN;
        }

        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(ip.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            log.error("Failed to hash IP address: {}", ip, e);
            return ip; // 降级处理，返回原始IP
        }
    }
}