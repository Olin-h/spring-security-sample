# SpringSecurity

### 目录

[TOC]



## 0.简介

**Spring Security** 是 Spring 家族中的一个安全管理框架。相比与另外一个安全框架**Shiro**，它提供了更丰富的功能，社区资源也比Shiro丰富。

一般来说中大型的项目都是使用**SpringSecurity** 来做安全框架。小项目有Shiro的比较多，因为相比与SpringSecurity，Shiro的上手更加的简单。

一般Web应用的需要进行**认证**和**授权**。

**认证：验证当前访问系统的是不是本系统的用户，并且要确认具体是哪个用户**

**授权：经过认证后判断当前用户是否有权限进行某个操作**

而认证和授权也是SpringSecurity作为安全框架的核心功能。

## 1.快速入门
### 1.1 准备工作

我们先要搭建一个简单的SpringBoot工程

① 设置父工程 在pom中添加依赖

```xml

<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.6.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

② 创建启动类

```java

@SpringBootApplication
public class SecuritySampleQuickStartApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SecuritySampleQuickStartApplication.class, args);
        // 这里主要查看DefaultSecurityFilterChain对应的过滤器链
        DefaultSecurityFilterChain chain = run.getBean(DefaultSecurityFilterChain.class);
        // 查询过滤器链拥有的过滤器
        List<Filter> filters = chain.getFilters();
        filters.forEach(System.out::println);
    }
}
```

③ 创建Controller

```java

@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 调用该接口会自动跳转到SpringSecurity认证页面，需要输入用户名和密码
     * <p>用户名默认为：user；密码：项目启动会自动生成
     *
     * @return 输出测试信息
     */
    @GetMapping("/echo")
    public String echo() {
        return "Hello, SpringSecurity!";
    }
}
```

### 1.2 引入SpringSecurity
在SpringBoot项目中使用SpringSecurity我们只需要引入依赖即可实现入门案例。

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

引入依赖后我们在尝试去访问之前的接口就会自动跳转到一个SpringSecurity的默认登陆页面，默认用户名是```user```,密码会输出在控制台。

必须登陆之后才能对接口进行访问。

## 2.认证
### 2.1 登录校验流程

![登录校验流程图](./assets/登录校验流程图.png "登录校验流程图")

### 2.2 原理初探
想要知道如何实现自己的登陆流程就必须要先知道入门案例中SpringSecurity的流程。

#### 2.2.1 SpringSecurity完整流程
SpringSecurity的原理其实就是一个过滤器链，内部包含了提供各种功能的过滤器。这里我们可以看看入门案例中的过滤器。
![SpringSecurity过滤器链](./assets/SpringSecurity过滤器链.png "SpringSecurity过滤器链")

图中只展示了核心过滤器，其它的非核心过滤器并没有在图中展示。

**UsernamePasswordAuthenticationFilter**:负责处理我们在登陆页面填写了用户名密码后的登陆请求。入门案例的认证工作主要有它负责。

**ExceptionTranslationFilter**:处理过滤器链中抛出的任何AccessDeniedException和AuthenticationException。

**FilterSecurityInterceptor**:负责权限校验的过滤器。

我们可以通过Debug查看当前系统中SpringSecurity过滤器链中有哪些过滤器及它们的顺序。

这里debug的方式有2种方式：

第一种直接输出结果：

```java

@SpringBootApplication
public class SecuritySampleQuickStartApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(SecuritySampleQuickStartApplication.class, args);
        // 这里主要查看DefaultSecurityFilterChain对应的过滤器链
        DefaultSecurityFilterChain chain = run.getBean(DefaultSecurityFilterChain.class);
        // 查询过滤器链拥有的过滤器
        List<Filter> filters = chain.getFilters();
        filters.forEach(System.out::println);
    }
}
```

第二种用debug查看源码：

![debug查看源码](./assets/debug查看源码.png "debug查看源码")

![默认的安全过滤器链](./assets/默认的安全过滤器链.png "默认的安全过滤器链")

#### 2.2.2 认证流程详解

![认证流程详解](./assets/认证流程详解.png "认证流程详解")

概念速查:

**Authentication接口**: 它的实现类，表示当前访问系统的用户，封装了用户相关信息。

**AuthenticationManager接口**：定义了认证Authentication的方法

**UserDetailsService接口**：加载用户特定数据的核心接口。里面定义了一个根据用户名查询用户信息的方法。

**UserDetails接口**：提供核心用户信息。通过UserDetailsService根据用户名获取处理的用户信息要封装成UserDetails对象返回。然后将这些信息封装到Authentication对象中。

### 2.3 解决问题
#### 2.3.1 思路分析
**登录**

①自定义登录接口
> 调用ProviderManager的方法进行认证 如果认证通过生成jwt<br/>
把用户信息存入redis中

②自定义UserDetailsService
> 在这个实现类中去查询数据库

**校验**

①定义Jwt认证过滤器
> 获取token <br/>
解析token获取其中的userid <br/>
从redis中获取用户信息 <br/>
存入SecurityContextHolder <br/>

#### 2.3.2 准备工作
①添加依赖

```xml

<dependencies>
    <!-- redis依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>

    <!-- https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt -->
    <!--jwt依赖-->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.9.1</version>
    </dependency>

    <!-- https://mvnrepository.com/artifact/com.alibaba/fastjson -->
    <!--fastjson依赖-->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>1.2.33</version>
    </dependency>
</dependencies>
```

②添加Redis相关配置

Redis使用FastJson序列化

```java
package com.gitee.olinonee.web.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Redis使用FastJson序列化
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-23
 */
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final Class<T> clazz;

    static {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
    }

    public FastJsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String str = new String(bytes, DEFAULT_CHARSET);
        return JSON.parseObject(str, clazz);
    }

    protected JavaType getJavaType(Class<?> clazz) {
        return TypeFactory.defaultInstance().constructType(clazz);
    }
}
```

redis配置类

```java
package com.gitee.olinonee.web.config;

import com.gitee.olinonee.web.utils.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
public class RedisConfig {

    @Bean
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJsonRedisSerializer serializer = new FastJsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

③统一响应结果封装类

```java
package com.gitee.olinonee.web.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Optional;

/**
 * 统一响应状态结果封装类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-23
 */
@Getter
@Setter
@ToString
// 以下注解表示：实体类与json互转的时候 属性值为null的不参与序列化
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;
    /**
     * 状态值，是否成功
     */
    private boolean success;
    /**
     * 提示消息，如果有错误时，前端可以获取该字段进行提示
     */
    private String msg;
    /**
     * 结果数据
     */
    private T data;

    // -------------------------私有构造方法-------------------------
    private ResponseResult(HttpStatus httpStatus) {
        this(httpStatus, httpStatus.getReasonPhrase(), null);
    }

    private ResponseResult(HttpStatus httpStatus, String msg) {
        this(httpStatus, msg, null);
    }

    private ResponseResult(HttpStatus httpStatus, T data) {
        this(httpStatus, httpStatus.getReasonPhrase(), data);
    }

    private ResponseResult(HttpStatus httpStatus, String msg, T data) {
        this(httpStatus.value(), msg, data);
    }

    private ResponseResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = HttpStatus.OK.value() == code;
    }
    // -------------------------私有构造方法结束-------------------------


    /**
     * 判断返回是否为成功
     *
     * @param result 统一响应结果对象
     * @return 是否成功，为空或者操作失败返回false，操作成功返回true
     */
    public static boolean isSuccess(@Nullable ResponseResult<?> result) {
        return Optional
                .ofNullable(result)
                .map(r -> ObjectUtils.nullSafeEquals(HttpStatus.OK.value(), r.code))
                .orElse(Boolean.FALSE);
    }

    /**
     * 判断返回是否为失败
     *
     * @param result 统一响应结果对象
     * @return 是否成功，为空或者操作失败返回false，操作成功返回true
     */
    public static boolean isFailure(@Nullable ResponseResult<?> result) {
        return !ResponseResult.isSuccess(result);
    }

    /**
     * 返回包含结果数据的统一响应结果（状态码默认为200，提示消息为“操作成功”）
     *
     * @param data 结果数据
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> data(T data) {
        return data("操作成功", data);
    }

    /**
     * 返回包含结果数据的统一响应结果（状态码默认为200，自定义提示消息）
     *
     * @param msg  提示消息
     * @param data 结果数据
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> data(String msg, T data) {
        return data(HttpStatus.OK.value(), msg, data);
    }

    /**
     * 返回统一响应结果（自定义状态码，判断返回数据是否为空，为空默认显示“暂无数据”，不为空自定义提示消息）
     *
     * @param code 状态码
     * @param msg  提示消息
     * @param data 结果数据
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> data(int code, String msg, T data) {
        return new ResponseResult<>(code, data == null ? "暂无数据" : msg, data);
    }

    /**
     * 返回统一【操作成功】响应结果（状态码默认为200，自定义提示消息）
     *
     * @param msg 提示消息
     * @param <T> T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> success(String msg) {
        return new ResponseResult<>(HttpStatus.OK, msg);
    }

    /**
     * 返回统一【操作成功】响应结果（自定义状态码）
     *
     * @param httpStatus 状态码枚举
     * @param <T>        T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> success(HttpStatus httpStatus) {
        return new ResponseResult<>(httpStatus);
    }

    /**
     * 返回统一【操作成功】响应结果（自定义状态码和提示消息）
     *
     * @param httpStatus 状态码枚举
     * @param msg        提示消息
     * @param <T>        T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> success(HttpStatus httpStatus, String msg) {
        return new ResponseResult<>(httpStatus, msg);
    }

    /**
     * 返回统一【操作失败】响应结果（状态码默认为400，自定义提示消息）
     *
     * @param msg 提示消息
     * @param <T> T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> failure(String msg) {
        return new ResponseResult<>(HttpStatus.BAD_REQUEST, msg);
    }

    /**
     * 返回统一【操作失败】响应结果（自定义状态码和提示消息）
     *
     * @param code 状态码
     * @param msg  提示消息
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> failure(int code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }

    /**
     * 返回统一【操作失败】响应结果（自定义状态码）
     *
     * @param httpStatus 状态码枚举
     * @param <T>        T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> failure(HttpStatus httpStatus) {
        return new ResponseResult<>(httpStatus);
    }

    /**
     * 返回统一【操作失败】响应结果（自定义状态码和提示消息）
     *
     * @param httpStatus 状态码枚举
     * @param msg        提示消息
     * @param <T>        T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> failure(HttpStatus httpStatus, String msg) {
        return new ResponseResult<>(httpStatus, msg);
    }

    /**
     * 返回统一【成功或者失败】响应结果（根据布尔类型，为true返回“操作成功”响应结果；反之，返回false对应响应结果）
     *
     * @param flag 状态标记，布尔类型值
     * @param <T>  T 泛型标记
     * @return ResponseResult
     */
    public static <T> ResponseResult<T> status(boolean flag) {
        return flag ? success("操作成功") : failure("操作失败");
    }
}
```

④工具类

jwt工具类

```java
package com.gitee.olinonee.web.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT工具类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
public class JwtUtil {

    // 有效期为60 * 60 * 1000  一个小时
    public static final Long JWT_TTL = 60 * 60 * 1000L;
    // 设置秘钥明文
    public static final String JWT_KEY = "jwt";

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 创建token
     *
     * @param subject token中要存放的数据（json格式）
     * @return token字符串
     */
    public static String createJWT(String subject) {
        // 设置过期时间
        JwtBuilder builder = getJwtBuilder(subject, null, getUUID());
        return builder.compact();
    }

    /**
     * 创建token
     *
     * @param subject   token中要存放的数据（json格式）
     * @param ttlMillis token过期时间，以毫秒为单位
     * @return token字符串
     */
    public static String createJWT(String subject, Long ttlMillis) {
        // 设置过期时间
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, getUUID());
        return builder.compact();
    }

    /**
     * 创建token
     *
     * @param id        唯一id
     * @param subject   主题  可以是JSON数据
     * @param ttlMillis token过期时间，以毫秒为单位
     * @return token字符串
     */
    public static String createJWT(String id, String subject, Long ttlMillis) {
        // 设置过期时间
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, id);
        return builder.compact();
    }

    /**
     * jwt构建器
     *
     * @param subject   主题  可以是JSON数据
     * @param ttlMillis 过期时间
     * @param uuid      唯一id
     * @return JwtBuilder
     */
    private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis, String uuid) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if (ttlMillis == null) {
            ttlMillis = JwtUtil.JWT_TTL;
        }
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                // 唯一的ID
                .setId(uuid)
                // 主题  可以是JSON数据
                .setSubject(subject)
                // 签发者
                .setIssuer("jwt")
                // 签发时间
                .setIssuedAt(now)
                // 使用HS256对称加密算法签名, 第二个参数为秘钥
                .signWith(signatureAlgorithm, secretKey)
                .setExpiration(expDate);
    }

    /**
     * 生成加密后的秘钥 secretKey
     *
     * @return SecretKey
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    /**
     * 解析token
     *
     * @param jwt token字符串
     * @return Claims
     */
    public static Claims parseJWT(String jwt) {
        SecretKey secretKey = generalKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }

    public static void main(String[] args) {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJjYWM2ZDVhZi1mNjVlLTQ0MDAtYjcxMi0zYWEwOGIyOTIwYjQiLCJzdWIiOiJzZyIsImlzcyI6InNnIiwiaWF0IjoxNjM4MTA2NzEyLCJleHAiOjE2MzgxMTAzMTJ9.JVsSbkP94wuczb4QryQbAke3ysBDIL5ou8fWsbt_ebg";
        Claims claims = parseJWT(token);
        System.out.println(claims);
    }
}
```

redis缓存工具类

```java
package com.gitee.olinonee.web.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis缓存工具类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisCache {

    public RedisTemplate redisTemplate;

    @Autowired
    public RedisCache(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key 缓存键值
     * @return 是否删除成功
     */
    public boolean deleteObject(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return 缓存的对象
     */
    public Long deleteObject(final Collection collection) {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        for (T t : dataSet) {
            setOperation.add(t);
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key 缓存键值
     * @return 缓存的set集合
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key     缓存键值
     * @param dataMap 数据集合
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key 缓存键值
     * @return 数据集合
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 删除Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     */
    public void delCacheMapValue(final String key, final String hKey) {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }
}
```

web常用的工具类

```java
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
```

⑤用户实体类

```java
package com.gitee.olinonee.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户表(SysUser)实体类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 密码
     */
    private String password;
    /**
     * 账号状态（0正常 1停用）
     */
    private String status;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String phoneNumber;
    /**
     * 用户性别（0男，1女，2未知）
     */
    private String sex;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 用户类型（0 管理员，1 普通用户）
     */
    private String userType;
    /**
     * 创建人的用户id
     */
    private Long createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新人的用户id
     */
    private Long updateBy;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 删除标志（0 未删除，1 已删除）
     */
    private Integer delFlag;
}
```

#### 2.3.3 实现
##### 2.3.3.1 数据库校验用户
从之前的分析我们可以知道，我们可以自定义一个```UserDetailsService```,让SpringSecurity使用我们的```UserDetailsService```
实现类。我们自己的```UserDetailsService```实现类可以从数据库中查询用户名和密码。

###### 准备工作
我们先创建一个用户表， 建表语句如下：

```mysql
CREATE TABLE `sys_user`
(
    `id`           bigint                                                       NOT NULL COMMENT '主键',
    `user_name`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NULL' COMMENT '用户名',
    `nick_name`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NULL' COMMENT '昵称',
    `password`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NULL' COMMENT '密码',
    `status`       char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci              DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
    `email`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '邮箱',
    `phone_number` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci          DEFAULT NULL COMMENT '手机号',
    `sex`          char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci              DEFAULT NULL COMMENT '用户性别（0男，1女，2未知）',
    `avatar`       varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '头像',
    `user_type`    char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci     NOT NULL DEFAULT '1' COMMENT '用户类型（0管理员，1普通用户）',
    `create_by`    bigint                                                                DEFAULT NULL COMMENT '创建人的用户id',
    `create_time`  datetime                                                              DEFAULT NULL COMMENT '创建时间',
    `update_by`    bigint                                                                DEFAULT NULL COMMENT '更新人的用户id',
    `update_time`  datetime                                                              DEFAULT NULL COMMENT '更新时间',
    `del_flag`     int                                                                   DEFAULT '0' COMMENT '删除标志（0未删除，1已删除）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户表';

INSERT INTO `spring_security_sample`.`sys_user`(`id`, `user_name`, `nick_name`, `password`, `status`, `email`,
                                                `phone_number`, `sex`, `avatar`, `user_type`, `create_by`,
                                                `create_time`, `update_by`, `update_time`, `del_flag`)
VALUES (1522097140371689474, 'admin', '帅气欧巴', '$2a$10$YBl/nTuCgPBjvh/jxLSXFe9253QvDuhJUUDFruJU883I/6ejW1NgC', '0',
        'admin@qq.com', '110-120-119', '0', NULL, '0', NULL, '2022-05-05 14:13:32', NULL, NULL, 0);
INSERT INTO `spring_security_sample`.`sys_user`(`id`, `user_name`, `nick_name`, `password`, `status`, `email`,
                                                `phone_number`, `sex`, `avatar`, `user_type`, `create_by`,
                                                `create_time`, `update_by`, `update_time`, `del_flag`)
VALUES (1522098415289204737, 'dev', '欧尼酱', '$2a$10$ckatEoEl/xpUSkQ/uFpvoepQtWufFXheuWu60zuMwlDwid4e5iAqa', '0',
        'dev@qq.com', '110-120-119', '0', NULL, '1', NULL, '2022-05-05 14:18:36', NULL, NULL, 0);
```

引入MybatisPlus和MySQL驱动依赖

```xml

<project>
    <dependencies>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.1</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
    </dependencies>
    <build>
        <!--打包时，把对应的xml等文件也包括，否则的话打包之后的target目录下仅仅包含.class文件，不包含其他文件，导致项目启动报错-->
        <resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>**/*.xml</include>
                    <include>**/*.yml</include>
                    <include>**/*.yaml</include>
                    <include>**/*.properties</include>
                    <include>static/**</include>
                </includes>
                <filtering>false</filtering>
            </resource>
        </resources>
    </build>
</project>
```

配置数据库信息以及MybatisPlus配置信息

```yaml
server:
  port: 8011

# spring数据源连接信息
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring_security_sample?characterEncoding=utf-8&serverTimezone=GMT%2B8
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
# mp相关配置
mybatis-plus:
  configuration:
    # 日志实现类，这里选择ibatis的标准日志输出实现类
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  # 配置mapper的xml扫描路径
  mapper-locations:
    - classpath*:/com/gitee/olinonee/web/mapper/*.xml
```

定义mapper接口以及生成xml文件 SysUserMapper接口

```java
package com.gitee.olinonee.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.olinonee.web.entity.SysUser;

/**
 * 用户mapper接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

}
```

SysUserMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.gitee.olinonee.web.mapper.SysUserMapper">

</mapper>
```

修改SysUser实体类

```text
类名上加@TableName(value = "sys_user") ,id字段上加 @TableId
```

配置mapper扫描，新建配置类

```java
package com.gitee.olinonee.web.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis-plus配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
@MapperScan("com.gitee.olinonee.web.mapper")
public class MybatisPlusConfig {

}
```

添加SpringBoot测试相关依赖

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

编写单元测试类

```java
package com.gitee.olinonee.web;

import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * mybatis-plus测试类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@SpringBootTest
public class MybatisPlusTests {

    @Resource
    private SysUserMapper userMapper;

    @Test
    public void testSysUserMapper() {
        List<SysUser> users = userMapper.selectList(null);
        System.out.println(users);
    }
}
```

###### 核心代码实现
创建一个类实现UserDetailsService接口，重写其中的方法。更加用户名从数据库中查询用户信息。

```java
package com.gitee.olinonee.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gitee.olinonee.web.entity.LoginUser;
import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.mapper.SysUserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 用户详情服务接口实现类 （主要是实现SpringSecurity对应的接口）
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户信息
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, username);
        SysUser user = userMapper.selectOne(wrapper);

        // 如果查询不到数据就通过抛出异常来给出提示
        if (Objects.isNull(user)) {
            throw new RuntimeException("用户名或密码错误");
        }
        // TODO 根据用户查询权限信息 添加到LoginUser中

        // 封装成UserDetails对象返回
        return new LoginUser(user);
    }
}
```

因为UserDetailsService方法的返回值是UserDetails类型，所以需要定义一个类，实现该接口，把用户信息封装在其中。

```java
package com.gitee.olinonee.web.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 登录用户封装类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private SysUser user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

注意：如果要测试，需要往用户表中写入用户数据，并且如果你想让用户的密码是明文存储，需要在密码前加`{noop}`。例如

![数据库用户信息](./assets/数据库用户信息.png "数据库用户信息")

这样登陆的时候就可以用sg作为用户名，1234作为密码来登陆了。

或者使用MP插入数据，增加单元测试插入方法

```java
package com.gitee.olinonee.web;

import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * mybatis-plus测试类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@SpringBootTest
public class MybatisPlusTests {

    @Resource
    private SysUserMapper userMapper;

    @Test
    public void testInsertUser() {
        SysUser user = new SysUser();
        user.setUserName("admin");
        user.setNickName("帅气欧巴");
        user.setPassword("{noop}123456");
        user.setStatus("0");
        user.setEmail("admin@qq.com");
        user.setSex("0");
        user.setCreateTime(new Date());
        user.setDelFlag(0);
        int result = userMapper.insert(user);
        System.out.println("插入影响的行数：" + result);
    }
}
```

##### 2.3.3.2 密码加密存储
实际项目中我们不会把密码明文存储在数据库中。

默认使用的`PasswordEncoder`要求数据库中的密码格式为：`{id}password` ，比如`{noop}1234`这种方式，直接输入`1234`
就可以登录系统。它会根据id去判断密码的加密方式。但是我们一般不会采用这种方式。所以就需要替换`PasswordEncoder`。

我们一般使用SpringSecurity为我们提供的`BCryptPasswordEncoder`。

我们只需要使用把`BCryptPasswordEncoder`对象注入Spring容器中，SpringSecurity就会使用该`PasswordEncoder`来进行密码校验。

我们可以定义一个SpringSecurity的配置类，在 **spring-security-config 5.7.0 版本前** SpringSecurity要求这个配置类要继承`WebSecurityConfigurerAdapter`，5.7.0 版本开始不使用继承 `WebSecurityConfigurerAdapter` 方式。

使用详情参考文档[WebSecurityConfigurerAdapter被遗弃文档说明](./WebSecurityConfigurerAdapter被遗弃文档说明.md) 或者参考官方说明：[https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter](https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)



```java
package com.gitee.olinonee.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 使用的加密器 bean对象
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

修改了上述加密规则之后，继续使用123456是无法进行登录的，需要进行加密替换加密之后的密钥重新替换数据库的明文密码即可登录。

由于数据库存放的是密文，为了匹配登录的密码，请用如下测试类进行测试，把加密后的数据替换数据库的密码值。

关于BCrypt的加密原理可以参考[BCrypt加密原理.md](./BCrypt加密原理.md)或者博客[Bcrypt加密](https://www.cnblogs.com/soft-engineer/p/15724281.html)

```java
package com.gitee.olinonee.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;

/**
 * mybatis-plus测试类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@SpringBootTest
public class MybatisPlusTests {
    /**
     * 测试SpringSecurity自带的BCrypt加密器
     * <Strong>注意：</Strong>
     * <p>使用相同明文进行BCrypt加密，每次产生的秘钥都不同，但是验证能进行成功匹配。
     * <a href="https://www.cnblogs.com/soft-engineer/p/15724281.html">BCrypt加密原理</a>
     */
    @Test
    public void testBCryptPassword() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        System.out.println("BCryptPasswordEncoder进行加密的秘钥:" + encode);
    }
}
```

##### 2.3.3.3 登录接口
接下我们需要自定义登陆接口，然后让SpringSecurity对这个接口放行，让用户访问这个接口的时候不用登录也能访问。

在接口中我们通过`AuthenticationManager`的`authenticate`方法来进行用户认证，所以需要在`SecurityConfig`中配置把`AuthenticationManager`注入容器。

认证成功的话要生成一个jwt，放入响应中返回。并且为了让用户下回请求时能通过jwt识别出具体的是哪个用户，我们需要把用户信息存入redis，可以把用户id作为key。

登录控制器

```java
package com.gitee.olinonee.web.controller;

import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.service.ILoginService;
import com.gitee.olinonee.web.utils.ResponseResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-25
 */
@RestController
@AllArgsConstructor
public class LoginController {

    private ILoginService loginService;

    /**
     * 用户登录接口
     */
    @PostMapping("/user/login")
    public ResponseResult<Object> login(@RequestBody SysUser user) {
        return loginService.login(user);
    }
}
```

SpringSecurity配置类

```java
package com.gitee.olinonee.web.config;

import com.gitee.olinonee.web.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 使用的加密器 bean对象
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭csrf
                .csrf().disable()
                // 不通过Session获取SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 对于登录接口 允许匿名访问
                .antMatchers("/user/login").anonymous()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();
    }

    /**
     * 认证管理器 bean对象
     *
     * @return AuthenticationManager
     * @throws Exception 异常
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
```

登录服务接口

```java
package com.gitee.olinonee.web.service;

import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.utils.ResponseResult;

/**
 * 登录服务接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-25
 */
public interface ILoginService {
    /**
     * 用户登录
     *
     * @param user 用户信息
     * @return 统一封装的返回结果
     */
    ResponseResult<Object> login(SysUser user);
}
```

登录服务接口实现类

```java
package com.gitee.olinonee.web.service.impl;

import com.gitee.olinonee.web.entity.LoginUser;
import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.service.ILoginService;
import com.gitee.olinonee.web.utils.JwtUtil;
import com.gitee.olinonee.web.utils.RedisCache;
import com.gitee.olinonee.web.utils.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 登录服务接口实现类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-25
 */
@Service
public class LoginServiceImpl implements ILoginService {
    private final AuthenticationManager authenticationManager;
    private final RedisCache redisCache;

    @Autowired
    public LoginServiceImpl(AuthenticationManager authenticationManager, RedisCache redisCache) {
        this.authenticationManager = authenticationManager;
        this.redisCache = redisCache;
    }

    /**
     * 用户登录
     *
     * @param user 用户信息
     * @return 统一封装的返回结果
     */
    @Override
    public ResponseResult<Object> login(SysUser user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 使用userid生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        // authenticate存入redis
        redisCache.setCacheObject("login:" + userId, loginUser);
        // 把token响应给前端
        Map<String, String> map = new HashMap<>();
        map.put("token", jwt);
        return ResponseResult.data(200, "登陆成功", map);
    }
}
```

用户登录测试接口测试案例（成功）效果

![用户登录接口测试案例（成功）](./assets/用户登录接口测试案例（成功）.png "用户登录接口测试案例（成功）")

用户登录测试接口测试案例（失败）效果

![用户登录接口测试案例（失败）](./assets/用户登录接口测试案例（失败）.png "用户登录接口测试案例（失败）")

##### 2.3.3.4 认证过滤器
我们需要自定义一个过滤器，这个过滤器会去获取请求头中的token，对token进行解析取出其中的userid。

使用userid去redis中获取对应的`LoginUser`对象。

然后封装`Authentication`对象存入`SecurityContextHolder`。

jwt认证token过滤器

```java
package com.gitee.olinonee.web.filter;

import com.gitee.olinonee.web.entity.LoginUser;
import com.gitee.olinonee.web.utils.JwtUtil;
import com.gitee.olinonee.web.utils.RedisCache;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * jwt认证token过滤器
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final RedisCache redisCache;

    @Autowired
    public JwtAuthenticationTokenFilter(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取token
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            // 放行
            filterChain.doFilter(request, response);
            return;
        }
        // 解析token
        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("token非法");
        }
        // 从redis中获取用户信息
        String redisKey = "login:" + userid;
        LoginUser loginUser = redisCache.getCacheObject(redisKey);
        if (Objects.isNull(loginUser)) {
            throw new RuntimeException("用户未登录");
        }
        // 存入SecurityContextHolder
        // TODO 获取权限信息封装到Authentication中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // 放行
        filterChain.doFilter(request, response);
    }
}
```

将jwt认证token过滤器添加到SpringSecurity配置类中

```java
package com.gitee.olinonee.web.config;

import com.gitee.olinonee.web.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter) {
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
    }

    /**
     * 使用的加密器 bean对象
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭csrf
                .csrf().disable()
                // 不通过Session获取SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 对于登录接口 允许匿名访问
                .antMatchers("/user/login").anonymous()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        // 把token校验过滤器添加到过滤器链中
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * 认证管理器 bean对象
     *
     * @return AuthenticationManager
     * @throws Exception 异常
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
```

请求头携带token进行接口测试效果

![测试接口案例-携带token请求头（正常）](./assets/测试接口案例-携带token请求头（正常）.png "测试接口案例-携带token请求头（正常）")

##### 2.3.3.5 退出登录
我们只需要定义一个登陆接口，然后获取SecurityContextHolder中的认证信息，删除redis中对应的数据即可。

登录服务接口

```java
package com.gitee.olinonee.web.service;

import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.utils.ResponseResult;

/**
 * 登录服务接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-25
 */
public interface ILoginService {
    /**
     * 用户登录
     *
     * @param user 用户信息
     * @return 统一封装的返回结果
     */
    ResponseResult<Object> login(SysUser user);

    /**
     * 退出登录
     *
     * @return 统一封装的返回结果
     */
    ResponseResult<Object> logout();
}
```

登录服务接口实现类

```java
package com.gitee.olinonee.web.service.impl;

import com.gitee.olinonee.web.entity.LoginUser;
import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.service.ILoginService;
import com.gitee.olinonee.web.utils.JwtUtil;
import com.gitee.olinonee.web.utils.RedisCache;
import com.gitee.olinonee.web.utils.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 登录服务接口实现类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-25
 */
@Service
public class LoginServiceImpl implements ILoginService {
    private final AuthenticationManager authenticationManager;
    private final RedisCache redisCache;

    @Autowired
    public LoginServiceImpl(AuthenticationManager authenticationManager, RedisCache redisCache) {
        this.authenticationManager = authenticationManager;
        this.redisCache = redisCache;
    }

    /**
     * 用户登录
     *
     * @param user 用户信息
     * @return 统一封装的返回结果
     */
    @Override
    public ResponseResult<Object> login(SysUser user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误");
        }
        // 使用userid生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        // authenticate存入redis
        redisCache.setCacheObject("login:" + userId, loginUser);
        // 把token响应给前端
        Map<String, String> map = new HashMap<>();
        map.put("token", jwt);
        return ResponseResult.data(200, "登陆成功", map);
    }

    /**
     * 退出登录
     *
     * @return 统一封装的返回结果
     */
    @Override
    public ResponseResult<Object> logout() {
        // 获取SecurityContextHolder中的用户id
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 这里为什么不需要判空？
        // 是因为登录之前已经做了认证处理、非法token的处理以及redis键过期之后的处理，可以查看对应JwtAuthenticationTokenFilter类处理逻辑
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        // 删除redis中对应的键
        redisCache.deleteObject("login:" + userId);
        return ResponseResult.success("退出成功!");
    }
}
```

登录控制器

```java
package com.gitee.olinonee.web.controller;

import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.service.ILoginService;
import com.gitee.olinonee.web.utils.ResponseResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-25
 */
@RestController
@AllArgsConstructor
public class LoginController {

    private ILoginService loginService;

    /**
     * 用户登录接口
     */
    @PostMapping("/user/login")
    public ResponseResult<Object> login(@RequestBody SysUser user) {
        return loginService.login(user);
    }

    /**
     * 退出登录
     */
    @GetMapping("/user/logout")
    public ResponseResult<Object> logout() {
        return loginService.logout();
    }
}
```

请求头携带token进行测试退出登录接口效果

![退出登录接口测试案例-携带token请求头（成功）](./assets/退出登录接口测试案例-携带token请求头（成功）.png "退出登录接口测试案例-携带token请求头（成功）")

未请求头携带token进行测试退出登录接口效果

![退出登录接口测试案例-未携带token请求头（失败）](./assets/退出登录接口测试案例-未携带token请求头（失败）.png "退出登录接口测试案例-未携带token请求头（失败）")

使用相同的token重新进行接口测试效果
![测试接口案例-携带token请求头失效（失败）](./assets/测试接口案例-携带token请求头失效（失败）.png "测试接口案例-携带token请求头失效（失败）")

## 3.授权
### 3.1 授权系统的作用
例如一个学校图书馆的管理系统，如果是普通学生登录就能看到借书还书相关的功能，不可能让他看到并且去使用添加书籍信息，删除书籍信息等功能。但是如果是一个图书馆管理员的账号登录了，应该就能看到并使用添加书籍信息，删除书籍信息等功能。

总结起来就是**不同的用户可以使用不同的功能**。这就是权限系统要去实现的效果。

我们不能只依赖前端去判断用户的权限来选择显示哪些菜单哪些按钮。因为如果只是这样，如果有人知道了对应功能的接口地址就可以不通过前端，直接去发送请求来实现相关功能操作。

所以我们还需要在后台进行用户权限的判断，判断当前用户是否有相应的权限，必须具有所需权限才能进行相应的操作。

### 3.2 授权基本流程
在SpringSecurity中，会使用默认的`FilterSecurityInterceptor`来进行权限校验。在`FilterSecurityInterceptor`中会从`SecurityContextHolder`
获取其中的`Authentication`，然后获取其中的权限信息。当前用户是否拥有访问当前资源所需的权限。

所以我们在项目中只需要把当前登录用户的权限信息也存入`Authentication`。

最后设置我们的资源所需要的权限即可。

### 3.3 授权实现

#### 3.3.1 限制访问资源所需权限
SpringSecurity为我们提供了基于注解的权限控制方案，这也是我们项目中主要采用的方式。我们可以使用注解去指定访问对应的资源所需的权限。

但是要使用它我们需要先开启相关配置。

```java
package com.gitee.olinonee.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // ....省略其他内容
}
```

然后就可以使用对应的注解。例如：`@PreAuthorize`

```java
package com.gitee.olinonee.web.controller;

import com.gitee.olinonee.web.utils.ResponseResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SpringSecurity测试接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-22
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/echo")
    @PreAuthorize("hasAuthority('test')")
    public ResponseResult<String> echo() {
        return ResponseResult.data("Spring Security!");
    }
}
```

#### 3.3.2 封装权限信息
我们前面在写`UserDetailsServiceImpl`的时候说过，在查询出用户后还要获取对应的权限信息，封装到`UserDetails`中返回。

我们先直接把权限信息写死封装到`UserDetails`中进行测试。

我们之前定义了`UserDetails`的实现类`LoginUser`，想要让其能封装权限信息就要对其进行修改。

```java
package com.gitee.olinonee.web.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录用户封装类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private SysUser user;
    // 存储权限信息
    private List<String> permissionList;
    // 存储SpringSecurity所需要的权限信息的集合，@JSONField(serialize = false)将该字段不序列化到redis中
    @JSONField(serialize = false)
    private List<SimpleGrantedAuthority> authorities;

    public LoginUser(SysUser user, List<String> permissionList) {
        this.user = user;
        this.permissionList = permissionList;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (!authorities.isEmpty()) {
            return authorities;
        }
        // 把permissionList中String类型的权限信息封装成SimpleGrantedAuthority对象
        /* authorities = new ArrayList<>();
        for (String permission : permissionList) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(permission);
            authorities.add(authority);
        }*/
        authorities = permissionList.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

`LoginUser`修改完后我们就可以在`UserDetailsServiceImpl`中去把权限信息封装到`LoginUser`中了。我们写死权限进行测试，后面我们再从数据库中查询权限信息。

```java
package com.gitee.olinonee.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gitee.olinonee.web.entity.LoginUser;
import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.mapper.SysUserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户详情服务接口实现类 （主要是实现SpringSecurity对应的接口）
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户信息
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, username);
        SysUser user = userMapper.selectOne(wrapper);

        // 如果查询不到数据就通过抛出异常来给出提示
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        // TODO 根据用户查询权限信息 添加到LoginUser中，这里暂时写死，用于测试
        List<String> permissionList = Collections.singletonList("test");

        // 封装成UserDetails对象返回
        return new LoginUser(user, permissionList);
    }
}
```

修改jwt认证token过滤器获取权限的方式，通过`LoginUser`的`getAuthorities`方法进行获取权限信息

```java
package com.gitee.olinonee.web.filter;

import com.gitee.olinonee.web.entity.LoginUser;
import com.gitee.olinonee.web.utils.JwtUtil;
import com.gitee.olinonee.web.utils.RedisCache;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * jwt认证token过滤器
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final RedisCache redisCache;

    @Autowired
    public JwtAuthenticationTokenFilter(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 获取token
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            // 放行
            filterChain.doFilter(request, response);
            return;
        }
        // 解析token
        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("token非法");
        }
        // 从redis中获取用户信息
        String redisKey = "login:" + userid;
        LoginUser loginUser = redisCache.getCacheObject(redisKey);
        if (Objects.isNull(loginUser)) {
            throw new UsernameNotFoundException("用户未登录");
        }
        // 存入SecurityContextHolder
        // 获取权限信息封装到Authentication中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // 放行
        filterChain.doFilter(request, response);
    }
}
```

#### 3.3.3 从数据库查询权限信息

##### 3.3.3.1 RBAC权限模型
**RBAC**权限模型（**Role-Based Access Control**）即：基于角色的权限控制。这是目前最常被开发者使用也是相对易用、通用权限模型。

![RBAC权限模型](./assets/RBAC权限模型.png "RBAC权限模型")

##### 3.3.3.2 准备工作

```mysql
/*Table structure for table `sys_menu` */

DROP TABLE IF EXISTS `sys_menu`;

CREATE TABLE `sys_menu`
(
    `id`          bigint                                                       NOT NULL COMMENT '主键',
    `menu_name`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NULL' COMMENT '菜单名',
    `path`        varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '路由地址',
    `component`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '组件路径',
    `visible`     char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci              DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci              DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
    `perms`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '权限标识',
    `icon`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT '#' COMMENT '菜单图标',
    `create_by`   bigint                                                                DEFAULT NULL COMMENT '创建人的用户id',
    `create_time` datetime                                                              DEFAULT NULL COMMENT '创建时间',
    `update_by`   bigint                                                                DEFAULT NULL COMMENT '修改人的用户id',
    `update_time` datetime                                                              DEFAULT NULL COMMENT '修改时间',
    `del_flag`    int                                                                   DEFAULT '0' COMMENT '是否删除（0未删除 1已删除）',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='菜单表';

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role`
(
    `id`          bigint NOT NULL COMMENT '主键',
    `name`        varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色权限名称',
    `role_key`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色权限字符串',
    `status`      char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci      DEFAULT '0' COMMENT '角色状态（0正常 1停用）',
    `create_by`   bigint                                                        DEFAULT NULL COMMENT '创建人的用户id',
    `create_time` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_by`   bigint                                                        DEFAULT NULL COMMENT '修改人的用户id',
    `update_time` datetime                                                      DEFAULT NULL COMMENT '修改时间',
    `del_flag`    int                                                           DEFAULT '0' COMMENT '是否删除（0未删除 1已删除）',
    `remark`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='角色表';

/*Table structure for table `sys_role_menu` */

DROP TABLE IF EXISTS `sys_role_menu`;

CREATE TABLE `sys_role_menu`
(
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `menu_id` bigint NOT NULL COMMENT '菜单id',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role`
(
    `user_id` bigint NOT NULL COMMENT '用户id',
    `role_id` bigint NOT NULL COMMENT '角色id',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

/*sys_menu示例数据*/
INSERT INTO `spring_security_sample`.`sys_menu`(`id`, `menu_name`, `path`, `component`, `visible`, `status`, `perms`,
                                                `icon`, `create_by`, `create_time`, `update_by`, `update_time`,
                                                `del_flag`, `remark`)
VALUES (1522096373053149186, '部门管理', 'dept', 'system/dept/index', '0', '0', 'system:dept:list', '#', NULL,
        '2022-05-05 14:10:29', NULL, NULL, 0, NULL);
INSERT INTO `spring_security_sample`.`sys_menu`(`id`, `menu_name`, `path`, `component`, `visible`, `status`, `perms`,
                                                `icon`, `create_by`, `create_time`, `update_by`, `update_time`,
                                                `del_flag`, `remark`)
VALUES (1522098058680999938, '测试管理', 'test', 'system/test/index', '0', '0', 'system:test:list', '#', NULL,
        '2022-05-05 14:17:11', NULL, NULL, 0, NULL);

/*sys_role示例数据*/
INSERT INTO `spring_security_sample`.`sys_role`(`id`, `name`, `role_key`, `status`, `create_by`, `create_time`,
                                                `update_by`, `update_time`, `del_flag`, `remark`)
VALUES (1522096801455202305, 'Coder', 'coder', '0', NULL, '2022-05-05 14:12:11', NULL, NULL, 0, NULL);
INSERT INTO `spring_security_sample`.`sys_role`(`id`, `name`, `role_key`, `status`, `create_by`, `create_time`,
                                                `update_by`, `update_time`, `del_flag`, `remark`)
VALUES (1522096931520634882, 'CEO', 'ceo', '0', NULL, '2022-05-05 14:12:42', NULL, NULL, 0, NULL);

/*sys_role_menu示例数据*/
INSERT INTO `spring_security_sample`.`sys_role_menu`(`role_id`, `menu_id`)
VALUES (1522096801455202305, 1522098058680999938);
INSERT INTO `spring_security_sample`.`sys_role_menu`(`role_id`, `menu_id`)
VALUES (1522096931520634882, 1522096373053149186);
INSERT INTO `spring_security_sample`.`sys_role_menu`(`role_id`, `menu_id`)
VALUES (1522096931520634882, 1522098058680999938);

/*sys_user_role示例数据*/
INSERT INTO `spring_security_sample`.`sys_user_role`(`user_id`, `role_id`)
VALUES (1522097140371689474, 1522096931520634882);
INSERT INTO `spring_security_sample`.`sys_user_role`(`user_id`, `role_id`)
VALUES (1522098415289204737, 1522096801455202305);
```

##### 3.3.3.3 代码实现
新建实体类

`Menu.java`

```java
package com.gitee.olinonee.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 菜单表(Menu)实体类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-05
 */
@Data
@TableName(value = "sys_menu")
@AllArgsConstructor
@NoArgsConstructor
public class Menu implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 菜单名
     */
    private String menuName;
    /**
     * 路由地址
     */
    private String path;
    /**
     * 组件路径
     */
    private String component;
    /**
     * 菜单状态（0显示 1隐藏）
     */
    private String visible;
    /**
     * 菜单状态（0正常 1停用）
     */
    private String status;
    /**
     * 权限标识
     */
    private String perms;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 创建用户
     */
    private Long createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改用户
     */
    private Long updateBy;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 是否删除（0未删除 1已删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
```

`Role.java`，`RoleMenu.java`，`UserRole.java`实体类同理。
`xxxMapper.java`也同理。

我们只需要根据用户id去查询到其所对应的权限信息即可。

`SysUserMapper`接口中新增`selectPermsByUserId`方法可以根据userid查询权限信息。

SysUserMapper.java

```java
package com.gitee.olinonee.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.olinonee.web.entity.SysUser;

import java.util.List;

/**
 * 用户mapper接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 通过用户id查询对应的权限菜单
     *
     * @param id 用户id
     * @return 权限菜单集合
     */
    List<String> selectPermsByUserId(Long id);
}
```

SysUserMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.gitee.olinonee.web.mapper.SysUserMapper">

    <select id="selectPermsByUserId" resultType="java.lang.String">
        SELECT DISTINCT m.`perms`
        FROM sys_user_role ur
        LEFT JOIN `sys_role` r ON ur.`role_id` = r.`id`
        LEFT JOIN `sys_role_menu` rm ON ur.`role_id` = rm.`role_id`
        LEFT JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
        WHERE user_id = #{userid}
        AND r.`status` = 0
        AND m.`status` = 0
    </select>
</mapper>
```

在`UserDetailsServiceImpl`中去调用该mapper的方法查询权限信息封装到`LoginUser`对象中即可。

```java
package com.gitee.olinonee.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gitee.olinonee.web.entity.LoginUser;
import com.gitee.olinonee.web.entity.SysUser;
import com.gitee.olinonee.web.mapper.SysUserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 用户详情服务接口实现类 （主要是实现SpringSecurity对应的接口）
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询用户信息
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserName, username);
        SysUser user = userMapper.selectOne(wrapper);

        // 如果查询不到数据就通过抛出异常来给出提示
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        // 测试写法
        // List<String> permissionList = Collections.singletonList("test");
        // 根据用户查询权限信息 添加到LoginUser中
        List<String> permissionList = userMapper.selectPermsByUserId(user.getId());

        // 封装成UserDetails对象返回
        return new LoginUser(user, permissionList);
    }
}
```

修改测试接口的echo方法的是授权权限

```java
package com.gitee.olinonee.web.controller;

import com.gitee.olinonee.web.utils.ResponseResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SpringSecurity测试接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-22
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/echo")
    @PreAuthorize("hasAuthority('system:test:list')")
    public ResponseResult<String> echo() {
        return ResponseResult.data("Spring Security!");
    }
}
```

## 4.自定义失败处理
我们还希望在认证失败或者是授权失败的情况下也能和我们的接口一样返回相同结构的json，这样可以让前端能对响应进行统一的处理。要实现这个功能我们需要知道SpringSecurity的异常处理机制。

在SpringSecurity中，如果我们在认证或者授权的过程中出现了异常会被`ExceptionTranslationFilter`捕获到。在`ExceptionTranslationFilter`
中会去判断是认证失败还是授权失败出现的异常。

如果是认证过程中出现的异常会被封装成`AuthenticationException`然后调用**AuthenticationEntryPoint**对象的方法去进行异常处理。

如果是授权过程中出现的异常会被封装成`AccessDeniedException`然后调用**AccessDeniedHandler**对象的方法去进行异常处理。

所以如果我们需要自定义异常处理，我们只需要自定义`AuthenticationEntryPoint`和`AccessDeniedHandler`然后配置给SpringSecurity即可。

①自定义实现类

SpringSecurity自定义授权失败访问拒绝处理器实现类

```java
package com.gitee.olinonee.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.gitee.olinonee.web.utils.ResponseResult;
import com.gitee.olinonee.web.utils.WebUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SpringSecurity自定义授权失败访问拒绝处理器实现类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-05
 */
@Service
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    /**
     * 处理拒绝访问失败。
     *
     * @param request               请求
     * @param response              响应
     * @param accessDeniedException 访问拒绝异常
     * @throws IOException      io异常
     * @throws ServletException servlet异常
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResponseResult<Object> failureResult = ResponseResult.failure(HttpStatus.FORBIDDEN, "权限不足");
        String failureStr = JSON.toJSONString(failureResult);
        WebUtil.renderString(response, failureStr);
    }
}
```

SpringSecurity自定义认证失败处理实现类

```java
package com.gitee.olinonee.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.gitee.olinonee.web.utils.ResponseResult;
import com.gitee.olinonee.web.utils.WebUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  SpringSecurity自定义认证失败处理实现类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-05
 */
@Service
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    /**
     * 启动身份验证方案
     *
     *
     * @param request       请求
     * @param response      响应
     * @param authException 认证异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        ResponseResult<Object> failureResult = ResponseResult.failure(HttpStatus.UNAUTHORIZED, "认证失败请重新登录");
        String failureStr = JSON.toJSONString(failureResult);
        WebUtil.renderString(response, failureStr);
    }
}
```

②配置SecurityConfig对应属性

先注入对应的处理器，然后我们可以使用HttpSecurity对象的方法去配置。

```java
package com.gitee.olinonee.web.config;

import com.gitee.olinonee.web.filter.JwtAuthenticationTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public SecurityConfig(JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter, AccessDeniedHandler accessDeniedHandler, AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * 使用的加密器 bean对象
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭csrf
                .csrf().disable()
                // 不通过Session获取SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 对于登录接口 允许匿名访问
                .antMatchers("/user/login").anonymous()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        // 把token校验过滤器添加到过滤器链中
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // SpringSecurity统一异常拦截处理
        http.exceptionHandling()
                // 认证失败相关异常处理
                .authenticationEntryPoint(authenticationEntryPoint)
                // 授权失败相关异常处理
                .accessDeniedHandler(accessDeniedHandler);
    }

    /**
     * 认证管理器 bean对象
     *
     * @return AuthenticationManager
     * @throws Exception 异常
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
```

token过期之后，重新调用接口的效果（认证异常拦截）

![认证失败的响应体](./assets/认证失败的响应体.png "认证失败的响应体")

使用正确token之后，访问未拥有权限的接口效果（授权异常拦截）

![授权失败的响应体](./assets/授权失败的响应体.png "授权失败的响应体")

## 5.跨域
浏览器出于安全的考虑，使用`XMLHttpRequest`对象发起 HTTP请求时必须遵守同源策略，否则就是跨域的HTTP请求，默认情况下是被禁止的。 同源策略要求源相同才能正常进行通信，即**协议、域名、端口号都完全一致**。

前后端分离项目，前端项目和后端项目一般都不是同源的，所以肯定会存在跨域请求的问题。

所以我们就要处理一下，让前端能进行跨域请求。

未跨域请求出现的问题如下：
> Access to XMLHttpRequest at 'http://localhost:18081/user/login' from origin 'http://localhost:8080' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.

**网上提供了2种解决方式，实际测试第二种有效，有可能是版本导致的问题，官网未找到相关说明**

**①方式一**：先配置SpringBoot跨域请求，然后开启SpringSecurity的跨域请求

跨域请求配置类

```java
package com.gitee.olinonee.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域请求相关配置
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-06
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路径
        registry
                .addMapping("/**")
                // 设置允许跨域请求的域名
                .allowedOriginPatterns("*")
                // 是否允许cookie
                .allowCredentials(true)
                // 设置允许的请求方式
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                // 设置允许的header属性
                .allowedHeaders("*")
                // 跨域允许时间
                .maxAge(3600);
    }
}
```

启用SpringSecurity配置功能

```java
package com.gitee.olinonee.web.config;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // ...省略其他方法

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // ...省略其他http方法

        // 允许跨域
        http.cors();
    }
}
```

**说明**：_SpringBoot2.5.0和SpringSecurity2.5.0版本进行测试上述方式是可以的，笔者使用2.6.7版本该方法（启不启用http.cors()都能成功访问接口）失效。_

**②方式二**：添加`CorsConfigurationSource`类型的bean对象，并开启SpringSecurity的跨域请求

```java
package com.gitee.olinonee.web.config;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // ...省略其他方法

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // ...省略其他属性

        // 允许跨域
        http.cors().configurationSource(corsConfigurationSource());
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();
        // 是否允许cookie
        configuration.setAllowCredentials(true);
        // 设置允许跨域请求的域名
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        // 设置允许的请求方式
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT"));
        // 设置允许的header属性
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        // 跨域允许时间为1小时
        configuration.setMaxAge(Duration.ofHours(1));
        // 设置允许跨域的路径
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**在测试跨域请求前请启动resources目录下的web-ui前端项目**
，启动命令参考文档：[web-ui前端操作文档](../security-sample-web/src/main/resources/web-ui/README.md)

前端项目启动成功的效果

![web-ui启动成功案例](./assets/web-ui启动成功案例.png "web-ui启动成功案例")

## 6.遗留小问题

### 6.1 其他权限校验注解方法
我们前面都是使用`@PreAuthorize`注解，然后在在其中使用的是`hasAuthority`方法进行校验。SpringSecurity还为我们提供了其它方法例如：`hasAnyAuthority`，`hasRole`
，`hasAnyRole`等。

这里我们先不急着去介绍这些方法，我们先去理解`hasAuthority`的原理，然后再去学习其他方法你就更容易理解，而不是死记硬背区别。并且我们也可以选择定义校验方法，实现我们自己的校验逻辑。

`hasAuthority`方法实际是执行到了`SecurityExpressionRoot`的`hasAuthority`，大家只要断点调试既可知道它内部的校验原理。

它内部其实是调用`authentication`的`getAuthorities`方法获取用户的权限列表。然后判断我们存入的方法参数数据在权限列表中。

`hasAnyAuthority`方法可以传入多个权限，只有用户有其中任意一个权限都可以访问对应资源。

```java

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/echo")
    @PreAuthorize("hasAnyAuthority('admin','test','system:dept:list')")
    public ResponseResult<String> echo() {
        return ResponseResult.data("Spring Security!");
    }
}
```

`hasRole`要求有对应的角色才可以访问，但是它内部会把我们传入的参数拼接上 **ROLE_** 后再去比较。所以这种情况下要用用户对应的权限也要有 **ROLE_** 这个前缀才可以。

```java

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/echo")
    @PreAuthorize("hasRole('system:dept:list')")
    public ResponseResult<String> echo() {
        return ResponseResult.data("Spring Security!");
    }
}
```

`hasAnyRole`有任意的角色就可以访问。它内部也会把我们传入的参数拼接上 **ROLE_** 后再去比较。所以这种情况下要用用户对应的权限也要有 **ROLE_** 这个前缀才可以。

```java

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/echo")
    @PreAuthorize("hasAnyRole('admin','system:dept:list')")
    public ResponseResult<String> echo() {
        return ResponseResult.data("Spring Security!");
    }
}
```

### 6.2 自定义权限校验注解方法
我们也可以定义自己的权限校验方法，在`@PreAuthorize`注解中使用我们的方法。

```java
package com.gitee.olinonee.web.component;

import com.gitee.olinonee.web.entity.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义权限校验方法
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-06
 */
@Component(value = "ca")
public class CustomizeAuthorize {

    public boolean hasAuthority(String authority) {
        // 获取当前用户的权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> permissions = loginUser.getPermissionList();
        // 判断用户权限集合中是否存在authority
        return permissions.contains(authority);
    }
}
```

在`SPEL`表达式中使用 `@ca`相当于获取容器中bean的名字为ca的对象。然后再调用这个对象的`hasAuthority`方法

```java
/**
 * SpringSecurity测试接口
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-22
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/testCustomizeAuthorize")
    @PreAuthorize("@ca.hasAuthority('system:test:list')")
    public ResponseResult<String> testCustomizeAuthorize() {
        return ResponseResult.success("CustomizeAuthorize！");
    }
}
```

使用自定义权限校验方法测试接口效果

![自定义权限校验方法接口测试案例](./assets/自定义权限校验方法接口测试案例.png "自定义权限校验方法接口测试案例")

### 6.3 基于配置的权限控制
我们也可以在配置类中使用使用配置的方式对资源进行权限控制。

```java
package com.gitee.olinonee.web.config;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-04-24
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    // ...省略其他方法

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 关闭csrf
                .csrf().disable()
                // 不通过Session获取SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 对于登录接口 允许匿名访问
                .antMatchers("/user/login").anonymous()
                .antMatchers("/test/testCors").hasAuthority("system:test:list")
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();

        // ...省略其他http方法
    }
}
```

### 6.4 CSRF
CSRF是指跨站请求伪造（Cross-site request forgery），是web常见的攻击之一。

![CSRF漏洞原理](./assets/CSRF漏洞原理.jpg "CSRF漏洞原理")

参考地址：[https://blog.csdn.net/freeking101/article/details/86537087](https://blog.csdn.net/freeking101/article/details/86537087)

SpringSecurity去防止CSRF攻击的方式就是通过**csrf_token**。后端会生成一个**csrf_token**，前端发起请求的时候需要携带这个**csrf_token**
,后端会有过滤器进行校验，如果没有携带或者是伪造的就不允许访问。

我们可以发现CSRF攻击依靠的是cookie中所携带的认证信息。但是在前后端分离的项目中我们的认证信息其实是**token**，而**token**并不是存储中cookie中，并且需要前端代码去把**token**
设置到请求头中才可以，所以CSRF攻击也就不用担心了。

### 6.5 认证成功处理器
实际上在`UsernamePasswordAuthenticationFilter`进行登录认证的时候，如果登录成功了是会调用`AuthenticationSuccessHandler`
的方法进行认证成功后的处理的。`AuthenticationSuccessHandler`就是登录成功处理器。

我们也可以自己去自定义成功处理器进行成功后的相应处理。

【**注意**】_认证成功处理器适用于自定义表单登录时才有效_，也就是在security-sample-quickstart项目中进行测试有效，在security-sample-web项目中是无效的。

认证成功处理器

```java
package com.gitee.olinonee.boot.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证成功处理器（用于认证成功之后的逻辑）
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Component
@Slf4j
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 当用户成功通过身份验证时调用。
     *
     * @param request        请求
     * @param response       响应
     * @param authentication 认证
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info(">>>> 认证成功了！");
    }
}
```

SpringSecurity配置类

```java
package com.gitee.olinonee.boot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    public SecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 表单登录
                .formLogin()
                .successHandler(authenticationSuccessHandler)
                .and()
                // 所有请求全部需要鉴权认证
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }
}
```

### 6.6 认证失败处理器
实际上在`UsernamePasswordAuthenticationFilter`进行登录认证的时候，如果认证失败了是会调用`AuthenticationFailureHandler`
的方法进行认证失败后的处理的。`AuthenticationFailureHandler`就是登录失败处理器。

我们也可以自己去自定义失败处理器进行失败后的相应处理。

【**注意**】_认证失败处理器也适用于自定义表单登录时才有效_，与认证成功处理器同理。

认证失败处理

```java
package com.gitee.olinonee.boot.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证失败处理器（用于认证失败之后的逻辑）
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Component
@Slf4j
public class AuthFailureHandler implements AuthenticationFailureHandler {
    /**
     * 当身份验证尝试失败时调用
     *
     * @param request   请求
     * @param response  响应
     * @param exception 异常
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error(">>>> 认证失败了！");
    }
}
```

SpringSecurity配置类

```java
package com.gitee.olinonee.boot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    public SecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 表单登录
                .formLogin()
                // 配置认证成功处理器
                .successHandler(authenticationSuccessHandler)
                // 配置认证失败处理器
                .failureHandler(authenticationFailureHandler)
                .and()
                // 所有请求全部需要鉴权认证
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }
}
```

### 6.7 登出成功处理器
我们也可以自己去自定义登出成功处理器进行登出的相应处理。

自定义退出成功的处理器

```java
package com.gitee.olinonee.boot.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义退出成功的处理器
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Component
@Slf4j
public class CustomizeLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info(">>>> 注销成功！");
    }
}
```

SpringSecurity配置类

```java
package com.gitee.olinonee.boot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * SpringSecurity配置类
 *
 * @author olinH, olinone666@gmail.com
 * @version v1.0.0
 * @since 2022-05-07
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    public SecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler, AuthenticationFailureHandler authenticationFailureHandler, LogoutSuccessHandler logoutSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 表单登录
                .formLogin()
                // 配置认证成功处理器
                .successHandler(authenticationSuccessHandler)
                // 配置认证失败处理器
                .failureHandler(authenticationFailureHandler)
                // 配置注销成功处理器
                .and()
                .logout()
                .logoutSuccessHandler(logoutSuccessHandler)
                .and()
                // 所有请求全部需要鉴权认证
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }
}
```

## 7.参考
参考地址：[https://www.bilibili.com/video/BV1mm4y1X7Hc](https://www.bilibili.com/video/BV1mm4y1X7Hc)
