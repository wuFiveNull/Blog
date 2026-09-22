# Blog 项目设计文档

## 1. 项目概述

这是一个面向个人知识沉淀和实习记录的私有 Blog 系统，主要用于记录：

- 实习日常
- 项目经验
- 技术文章
- Java/后端八股文
- 项目截图、流程图和其他图片资料

系统不开放注册，只有已经存在于数据库中的账号可以登录和使用。文章、评论、收藏等内容均要求用户登录后访问。

项目优先考虑代码简单、容易维护和后续扩展，不以高并发为目标。

## 2. 已确定的需求

### 2.1 账号与登录

- 不提供注册功能。
- 账号由管理员通过数据库初始化脚本或后台维护。
- 只有登录用户才能访问 Blog 内容。
- 支持登录、退出登录和登录状态保持。
- 密码只保存加密后的密文，不保存明文密码。

### 2.2 角色

系统暂时保留三个独立角色：

| 角色 | 说明 | 当前定位 |
|---|---|---|
| `ADMIN` | 管理员 | 管理所有用户、文章、评论和系统内容 |
| `OWNER` | 站长本人 | 你的账号，当前权限与普通用户相同 |
| `USER` | 其他已注册用户 | 当前权限与站长本人相同 |

`OWNER` 和 `USER` 当前权限相同，但不合并成一个角色。这样以后可以单独调整“你本人”和其他用户的权限。

### 2.3 内容功能

登录用户可以：

- 查看自己有权限查看的文章
- 使用 Markdown 编写文章
- 发布文章
- 保存草稿
- 编辑和删除自己的文章
- 给可见文章发表评论
- 删除自己的评论
- 收藏和取消收藏文章
- 在文章中上传和使用图片

管理员可以：

- 查看所有文章，包括草稿和仅限特定角色的文章
- 新建、编辑、发布、归档和删除所有文章
- 管理用户和角色
- 管理所有评论
- 查看和删除所有收藏关系

### 2.4 文章可见范围

文章支持按角色组合设置可见范围，不限制为单一角色。例如：

- 仅管理员和站长可见
- 管理员、站长和普通用户都可见
- 仅管理员和普通用户可见

管理员拥有全局查看权限，不需要每篇文章单独勾选 `ADMIN`。

## 3. 技术方案

### 3.1 推荐技术栈

| 层次 | 技术选择 | 选择原因 |
|---|---|---|
| 后端 | Spring Boot MVC | 结构成熟，适合单体应用，维护成本低 |
| 页面 | Thymeleaf + HTML/CSS/少量 JavaScript | 不需要维护独立前端工程，部署简单 |
| 样式 | 自定义简洁 CSS | 页面风格可控，避免引入过多 UI 依赖 |
| 安全 | Spring Security | 处理登录、会话、角色和权限校验 |
| 数据库 | MySQL | 关系模型清晰，适合用户、文章、角色和评论关系 |
| 数据访问 | Spring Data JPA | 常规 CRUD 开发量小，实体关系表达清晰 |
| 数据库迁移 | Flyway | 保证数据库结构可版本化、可重复部署 |
| Markdown | CommonMark/Flexmark 类 Markdown 解析器 | 支持标准 Markdown 并便于安全过滤 |
| 图片存储 | 本地文件系统 + 数据库元数据 | 第一版简单易部署，后续可替换为对象存储 |

### 3.2 为什么暂时不使用前后端分离

当前系统用户量不大，也没有高并发需求。采用 Thymeleaf 可以：

- 减少前端工程和接口联调成本
- 减少部署组件
- 让登录、权限和页面展示集中管理
- 更适合个人 Blog 的维护方式

如果以后需要移动端、独立前端或开放 API，再把业务层拆成 REST API 即可，不需要重新设计数据库。

### 3.3 系统结构

```text
浏览器
  |
  v
Spring Boot MVC
  |
  +-- Spring Security：登录、会话、权限
  +-- Controller：页面请求和表单处理
  +-- Service：文章、评论、收藏、权限业务
  +-- Repository：数据库访问
  +-- Markdown 渲染与安全过滤
  +-- 本地图片文件存储
  |
  +-- MySQL
```

## 4. 权限模型

### 4.1 角色权限矩阵

| 功能 | ADMIN | OWNER | USER |
|---|---:|---:|---:|
| 登录 | 是 | 是 | 是 |
| 查看已授权文章 | 是 | 是 | 是 |
| 查看所有文章 | 是 | 否 | 否 |
| 查看所有草稿 | 是 | 否 | 否 |
| 创建文章 | 是 | 是 | 是 |
| 编辑自己的文章 | 是 | 是 | 是 |
| 编辑他人的文章 | 是 | 否 | 否 |
| 删除自己的文章 | 是 | 是 | 是 |
| 删除他人的文章 | 是 | 否 | 否 |
| 设置自己文章的可见角色 | 是 | 是 | 是 |
| 发布自己的文章 | 是 | 是 | 是 |
| 评论可见文章 | 是 | 是 | 是 |
| 删除自己的评论 | 是 | 是 | 是 |
| 删除他人的评论 | 是 | 否 | 否 |
| 收藏文章 | 是 | 是 | 是 |
| 管理用户角色 | 是 | 否 | 否 |
| 管理所有文章 | 是 | 否 | 否 |

### 4.2 权限校验原则

权限必须由后端校验，不能只依赖前端隐藏按钮。

文章访问判断顺序：

1. 用户是否已登录。
2. 文章是否存在。
3. 管理员是否正在访问。
4. 如果不是管理员，文章是否已发布。
5. 当前用户角色是否在文章允许角色列表中。

文章编辑判断：

```text
管理员：可以编辑任意文章
普通用户：只能编辑自己的文章
```

评论和收藏只能针对当前用户有权限查看的已发布文章。

## 5. 核心数据模型

### 5.1 用户表 `users`

```text
id
username
password_hash
nickname
avatar_url
status              ACTIVE / DISABLED
created_at
updated_at
```

### 5.2 角色表 `roles`

```text
id
code                ADMIN / OWNER / USER
name
description
```

### 5.3 用户角色表 `user_roles`

```text
user_id
role_id
```

虽然当前每个账号通常只有一个角色，但使用关联表可以为以后扩展多角色保留空间。

### 5.4 文章表 `posts`

```text
id
title
slug
summary
content_markdown
status              DRAFT / PUBLISHED / ARCHIVED
author_id
cover_image_url
created_at
updated_at
published_at
```

文章正文只保存 Markdown 原文。展示时再转换为 HTML，并经过安全过滤。

### 5.5 文章允许角色表 `post_allowed_roles`

```text
post_id
role_id
```

这个表支持一篇文章被多个角色同时查看。

例如：

```text
文章 A -> OWNER
文章 A -> ADMIN
```

表示只有站长和管理员可以查看文章 A。

### 5.6 标签表 `tags`

```text
id
name
created_at
```

### 5.7 文章标签关系表 `post_tags`

```text
post_id
tag_id
```

### 5.8 评论表 `comments`

第一版采用平面评论，不做无限级嵌套回复。

```text
id
post_id
user_id
content
status              VISIBLE / HIDDEN
created_at
updated_at
```

后续如果确实需要楼中楼，再增加 `parent_id`。

### 5.9 收藏表 `post_favorites`

```text
user_id
post_id
created_at
```

`user_id + post_id` 建立唯一约束，避免重复收藏。

### 5.10 文件表 `files`

```text
id
original_name
stored_name
storage_path
content_type
file_size
uploader_id
created_at
```

图片文件保存在服务器指定目录，数据库保存文件元数据。文章 Markdown 中只引用系统生成的安全访问地址。

## 6. 页面规划

### 6.1 登录页 `/login`

- 用户名
- 密码
- 登录按钮
- 登录错误提示
- 不提供注册入口

### 6.2 首页 `/`

- 当前用户可见的已发布文章
- 文章标题、摘要、作者、标签、更新时间
- 搜索框
- 标签筛选
- 收藏状态
- 新建文章入口

### 6.3 文章详情页 `/posts/{slug}`

- 标题
- 作者
- 发布时间
- 标签
- Markdown 渲染后的正文
- 文章目录
- 代码块高亮
- 收藏/取消收藏
- 评论列表
- 发表评论
- 作者本人显示编辑入口

### 6.4 文章编辑页 `/posts/new` 和 `/posts/{id}/edit`

- 标题
- 摘要
- Markdown 编辑区
- Markdown 预览区
- 标签
- 可见角色复选框
- 保存草稿
- 发布
- 上传图片

### 6.5 我的文章 `/me/posts`

- 当前用户创建的文章
- 草稿、已发布、已归档状态
- 编辑、删除、发布操作

### 6.6 管理后台 `/admin`

管理员可以看到：

- 所有文章
- 所有草稿
- 所有用户
- 所有评论
- 用户角色管理
- 文章权限调整

普通用户访问后台地址时必须返回无权限页面。

## 7. Markdown 和图片处理

### 7.1 Markdown 处理流程

```text
用户输入 Markdown
        |
        v
保存原始 Markdown
        |
        v
展示时解析为 HTML
        |
        v
过滤危险标签、属性和链接
        |
        v
返回页面
```

### 7.2 图片上传

第一版支持：

- PNG
- JPG/JPEG
- GIF
- WebP

上传时限制：

- 单个文件大小
- 文件扩展名
- 实际 MIME 类型
- 文件名重命名
- 图片保存目录不能直接暴露为可执行目录

编辑器上传成功后，自动插入 Markdown：

```markdown
![图片说明](/files/202609/example-image.png)
```

## 8. 计划中的页面接口

这里的接口是后续开发约定，不代表现在开始实现。

### 认证

```text
POST /login
POST /logout
GET  /me
```

### 文章

```text
GET    /
GET    /posts/{slug}
GET    /me/posts
GET    /posts/new
POST   /posts
GET    /posts/{id}/edit
POST   /posts/{id}
POST   /posts/{id}/publish
POST   /posts/{id}/archive
POST   /posts/{id}/delete
```

### 评论和收藏

```text
POST   /posts/{id}/comments
POST   /comments/{id}/delete
POST   /posts/{id}/favorite
POST   /posts/{id}/unfavorite
```

### 管理员

```text
GET    /admin/users
POST   /admin/users
POST   /admin/users/{id}/roles
POST   /admin/users/{id}/disable
GET    /admin/posts
GET    /admin/comments
POST   /admin/comments/{id}/hide
```

## 9. 安全设计

- 使用 BCrypt 或 Argon2 保存密码。
- 禁止通过接口注册账号。
- 管理员创建账号时只接收密码，不在日志中打印密码。
- 使用 HttpOnly、Secure、SameSite Cookie 保存会话。
- 使用 CSRF 防护。
- 登录失败进行频率限制，避免简单暴力破解。
- Markdown 禁止执行 JavaScript 和危险 HTML。
- 所有文章、评论、收藏操作都在服务端做权限校验。
- 普通用户不能通过修改 URL 访问无权限文章。
- 上传文件限制类型、大小和路径。
- 不在异常页面暴露数据库、文件路径和堆栈信息。
- 生产环境必须使用 HTTPS。
- 定期备份数据库和图片目录。

## 10. 项目目录规划

```text
src/main/java/.../blog
├── config                  安全、MVC、文件上传配置
├── controller              页面控制器
├── service                 业务逻辑
├── repository              数据访问
├── entity                  数据库实体
├── dto                     表单和页面数据对象
├── security                用户详情、权限和登录处理
├── markdown                Markdown 解析和安全过滤
└── exception               业务异常和全局异常处理

src/main/resources
├── templates               Thymeleaf 页面
├── static                  CSS、JavaScript、图标
├── db/migration             Flyway 数据库迁移脚本
└── application.yml         应用配置

storage
└── uploads                 文章图片和附件
```

## 11. 开发阶段规划

### 阶段一：基础能力

- 创建 Spring Boot 项目
- 配置 MySQL 和 Flyway
- 创建用户、角色和文章表
- 完成登录、退出和会话管理
- 初始化三个角色和第一个管理员账号

### 阶段二：文章功能

- 文章列表
- 文章详情
- Markdown 编辑和预览
- 草稿和发布
- 文章权限组合配置
- 只能编辑自己的文章

### 阶段三：互动功能

- 收藏和取消收藏
- 评论和删除自己的评论
- 管理员隐藏或删除评论
- 标签和搜索

### 阶段四：图片和体验

- 图片上传
- Markdown 图片引用
- 代码高亮
- 目录生成
- 移动端样式
- 深色模式

### 阶段五：部署和维护

- Docker 部署
- 数据库初始化和迁移
- 图片目录备份
- 日志和错误处理
- 管理员操作记录

## 12. 第一版验收标准

第一版完成后，应满足：

1. 未登录用户不能查看任何 Blog 内容。
2. 系统没有注册页面和注册接口。
3. 管理员可以看到所有文章和草稿。
4. 站长和普通用户可以创建、发布和管理自己的文章。
5. 用户不能编辑或删除别人的文章。
6. 文章可以选择多个允许查看的角色。
7. 用户只能查看自己被授权的文章。
8. 用户可以对可见文章发表评论和收藏。
9. 管理员可以管理所有评论和用户角色。
10. Markdown 能正确展示代码、链接、列表、表格和图片。
11. Markdown 中的危险脚本不能执行。
12. 文章和评论权限由后端校验。

## 13. 暂不实现的内容

第一版暂时不做：

- 用户注册
- 邮箱验证
- 找回密码
- 点赞系统
- 私信
- 关注用户
- 无限级评论回复
- 推荐算法
- 多租户
- 高并发缓存和分布式部署
- 对象存储和 CDN

这些功能以后有明确需求时再扩展。

## 14. 当前默认决策

如果没有进一步调整，后续开发将默认采用以下规则：

- 使用 Spring Boot MVC + Thymeleaf，不做前后端分离。
- 使用 MySQL 保存业务数据。
- 使用 Spring Security 的会话登录。
- `ADMIN` 可以查看和管理全部内容。
- `OWNER` 和 `USER` 当前拥有相同的发文、评论、收藏权限。
- `OWNER` 和 `USER` 只能修改、删除自己的文章。
- 文章可见角色支持任意组合。
- 评论采用平面结构，暂不支持楼中楼。
- 图片先存储在服务器本地目录。
- 暂不开发代码，先以本文档作为开发依据。

