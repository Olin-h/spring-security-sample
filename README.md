<p>
    <img src="https://img.shields.io/badge/JDK-%3E%3D1.8-green" alt="JDK1.8+"/>
    <img src="https://img.shields.io/badge/license-GPL3.0-blue" alt="GPL3.0"/>
    <img src="https://img.shields.io/badge/Spring%20Boot-2.6.7-blue" alt="SpringBoot2.6.7"/>
    <img src="https://img.shields.io/badge/Spring%20Security-2.6.7-blue" alt="SpringSecurity2.6.7"/>
    <a target="_blank" href="https://gitee.com/OlinOnee">
        <img src="https://img.shields.io/badge/Author-OlinOnee-ff69b4" alt="OlinOnee">
    </a>
    <a target="_blank" href="https://gitee.com/OlinOnee/spring-security-sample">
        <img src="https://img.shields.io/badge/Copyright-%40spring--security--sample-ff3f59" alt="spring-security-sample">
    </a>
</p>

# spring-security-sample

#### 介绍

一个SpringSecurity学习样本，包含了前后端项目基础的认证和授权功能。

#### 工程结构

```shell
spring-security-sample
├─doc                         # 文档
│  ├─assets                   # 截图文件
│  └─sql                      # SQL脚本
├─security-sample-quickstart  # SpringSecurity快速启动案例
└─security-sample-web         # SpringSecurity认证和授权web案例
```

#### 技术文档

1. [BCrypt加密原理](./doc/BCrypt加密原理.md "BCrypt加密原理")
2. [SpringSecurity使用说明文档](./doc/SpringSecurity使用说明文档.md "SpringSecurity使用说明文档")

#### 使用说明

1. 拉取代码
```shell
git clone https://gitee.com/OlinOnee/spring-security-sample.git
```
2. 编译打包
```shell
cd spring-security-sample
mvn clean install -Dmaven.test.skip=true 
```
3. 项目启动
```shell
cd [security-sample-quickstart | security-sample-web]
java -jar [security-sample-quickstart | security-sample-web]-1.0.jar
```

#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request

#### 开源协议

1. GPL(GNU General Public License)，GNU通⽤公共许可协议。GNU官⽅的定义：GNU is Not Unix
2. GPL 3.0协议第4条、第5条的规定，只要后续版本中有使⽤先前开源版本中的源代码，并且先前版本使⽤了GPL 3.0协议，则后续版本 也必然受GPL 3.0协议的约束。
3. GPL 3.0协议并未限制⽤户进⾏商⽤，只是必须遵守开源的规定。GPL的精髓就是开源，和是否商⽤，是否收费完全没有关系。
4. GPL 其实从字⾯上就可以理解为公共许可证，也就是说遵循GPL的软件是公共的，其实不存在什么版权问题，或者说公众都有版 权，GPL提出了和版权 （copyright）完全相反的概念（copyleft）。
