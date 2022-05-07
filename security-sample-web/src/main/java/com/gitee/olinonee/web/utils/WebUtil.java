package com.gitee.olinonee.web.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * web常用的工具类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
public class WebUtil {
    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
