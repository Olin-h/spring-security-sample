## Spring Security 弃用 WebSecurityConfigurerAdapter

### 官网博客说明

官网博客链接地址：[https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter](https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)

在**Spring Security 5.7.0-M2**，我们弃用了 `WebSecurityConfigurerAdapter` ，因为我们鼓励用户转向使用基于组件的安全配置。

为了帮助大家熟悉这种新的配置风格，我们编制了一份常见用例表和推荐的新写法。

在下面的例子中，我们遵循最佳实践——使用 Spring Security lambda 领域专用语言（DSL）和 `HttpSecurity#authorizeHttpRequests` 方法来定义我们的授权规则。如果你对lambda领域专用语言（DSL）不熟悉，你可以在这篇[博客](https://spring.io/blog/2019/11/21/spring-security-lambda-dsl)了解它。如果你想知道为什么我们选择选择使用 `HttpSecurity#authorizeHttpRequests` ，你可以看这篇 [参考文档](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)。

### 配置HttpSecurity

在Spring Security 5.4，我们[介绍](https://github.com/spring-projects/spring-security/issues/8804)了创建一个`SecurityFilterChain` bean来配置`HttpSecurity`的功能。

下面是一个使用`WebSecurityConfigurerAdapter`和HTTP Basic保护所有端点的示例配置：

```java
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authz) -> authz
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults());
    }
}
```

往后，我们建议注册一个`SecurityFilterChain` bean来做这件事：

```java
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authz) -> authz
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults());
        return http.build();
    }
}
```

### 配置WebSecurity

在Spring Security 5.4中，我们还[引入](https://github.com/spring-projects/spring-security/issues/8978)了`WebSecurityCustomizer`。

`WebSecurityCustomizer`是一个回调接口，可以用来定制`WebSecurity`。

下面是一个使用`WebSecurityConfigurerAdapter`忽略匹配`/ignore1`或`/ignore2`的请求的示例配置：

```java
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/ignore1", "/ignore2");
    }
}
```

往后，我们建议注册一个`WebSecurityCustomizer` bean来做这件事：

```java
@Configuration
public class SecurityConfiguration {
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/ignore1", "/ignore2");
    }
}
```

**警告**：如果你正在配置`WebSecurity`来忽略请求，建议你改为在`HttpSecurity#authorizeHttpRequests`内使用`permitAll`。想了解更多请参考`configure` [Javadoc](https://docs.spring.io/spring-security/site/docs/5.7.0-M2/api/org/springframework/security/config/annotation/web/configuration/WebSecurityConfigurerAdapter.html#configure%25org.springframework.security.config.annotation.web.builders.WebSecurity%29)。

### LDAP认证

在Spring Security 5.7，我们[引入](https://github.com/spring-projects/spring-security/pull/10138)了`EmbeddedLdapServerContextSourceFactoryBean`、`LdapBindAuthenticationManagerFactory`和`LdapPasswordComparisonAuthenticationManagerFactory`，这些类都可以用来创建一个嵌入式的LDAP服务器；并且我们还引入一个`AuthenticationManager`类，它可以用来执行LDAP认证。

下面是一个使用是一个使用绑定验证的示例配置，它使用了`WebSecurityConfigurerAdapter`创建嵌入式LDAP服务器并且使用`AuthenticationManager`执行LDAP认证：

```java
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .ldapAuthentication()
            .userDetailsContextMapper(new PersonContextMapper())
            .userDnPatterns("uid={0},ou=people")
            .contextSource()
            .port(0);
    }
}
```

往后，我们建议使用新的LDAP类来做这件事：

```java
@Configuration
public class SecurityConfiguration {
    @Bean
    public EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean() {
        EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean =
            EmbeddedLdapServerContextSourceFactoryBean.fromEmbeddedLdapServer();
        contextSourceFactoryBean.setPort(0);
        return contextSourceFactoryBean;
    }

    @Bean
    AuthenticationManager ldapAuthenticationManager(
            BaseLdapPathContextSource contextSource) {
        LdapBindAuthenticationManagerFactory factory = 
            new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserDnPatterns("uid={0},ou=people");
        factory.setUserDetailsContextMapper(new PersonContextMapper());
        return factory.createAuthenticationManager();
    }
}
```

### JDBC认证

下面是一个示例配置，它在`WebSecurityConfigurerAdapter`内创建了一个使用默认模式初始化并且只有一个用户的内嵌`DataSource`：

```java
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        auth.jdbcAuthentication()
            .withDefaultSchema()
            .dataSource(dataSource())
            .withUser(user);
    }
}
```
推荐的做法是创建一个`JdbcUserDetailsManager` bean来做这件事：

```java
@Configuration
public class SecurityConfiguration {
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript(JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION)
            .build();
    }

    @Bean
    public UserDetailsManager users(DataSource dataSource) {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        users.createUser(user);
        return users;
    }
}
```
**注意**：在这些例子中，我们为了可读性使用了`User.withDefaultPasswordEncoder()`。这不适合生产项目，我们建议在生产项目中使用散列密码。请按照[参考文档](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-boot-cli)所说的用Spring Boot命令行工具来做。

### 内存内认证

下面是一个示例配置，它在`WebSecurityConfigurerAdapter`配置了一个只存有一个用户的内存内用户：

```java
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        auth.inMemoryAuthentication()
            .withUser(user);
    }
}
```
我们建议注册一个`InMemoryUserDetailsManager` bean来做这件事：

```java
@Configuration
public class SecurityConfiguration {
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
```
**注意**：在这些例子中，我们为了可读性使用了`User.withDefaultPasswordEncoder()`。这不适合生产项目，我们建议在生产项目中使用散列密码。请按照[参考文档](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-boot-cli)所说的用Spring Boot命令行工具来做。

### 全局认证管理器

要创建一个整个应用都可以使用的`AuthenticationManager`，只需要使用`@Bean`将`AuthenticationManager`注册为bean就可以了。

这种配置已经在上面的LDAP认证示例展示过了。

### 局部认证管理器

在Spring Security 5.6中，我们[引入](https://github.com/spring-projects/spring-security/issues/10040)了`HttpSecurity#authenticationManager`方法，这个方法可以为特定的`SecurityFilterChain`覆盖默认的`AuthenticationManager`。

下面是一个示例配置，它设置了一个自定义的`AuthenticationManager`：

```java
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authz) -> authz
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults())
            .authenticationManager(new CustomAuthenticationManager());
        return http.build();
    }
}
```

### 访问局部认证管理器

可以使用自定义领域专用语言（DSL）访问局部`AuthenticationManager`。这实际上是Spring Security内部实现`HttpSecurity.authorizeRequests()`等方法的方式。

```java
public class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http.addFilter(new CustomFilter(authenticationManager));
    }

    public static MyCustomDsl customDsl() {
        return new MyCustomDsl();
    }
}
```
然后，在构建`SecurityFilterChain`时可以应用自定义领域专用语言（DSL）：

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // ...
    http.apply(customDsl());
    return http.build();
}
```
### 欢迎加入

我们很高兴与您分享这些更新，我们期待通过您的反馈进一步增强 Spring 安全性!如果你有兴趣贡献，你可以在[GitHub](https://github.com/spring-projects/spring-security)上找到我们。
