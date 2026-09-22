# Blog

一个面向个人知识记录的 Markdown Blog。文章支持草稿、发布、归档，以及按 admin、owner、user 三类角色设置可见范围。

## 当前版本

第一版 MVP 使用 Spring Boot、Thymeleaf、Spring Security 和 JPA：

- 本地开发默认使用 H2 文件数据库，数据保存在 data/。
- 生产环境使用 Docker Compose、MySQL 和挂载到宿主机的上传目录。
- 没有开放注册，账号由管理员在数据库或后台维护。
- Markdown 会在服务端渲染，并经过 HTML 白名单清洗。

## 本地开发

要求：JDK 21、Git。项目已包含 Maven Wrapper，不需要单独安装 Maven。

    .\mvnw.cmd test
    .\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.seed.enabled=true --app.seed.admin-username=admin --app.seed.admin-password=请替换为强密码"

开发完成后可以执行：

    .\mvnw.cmd package -DskipTests

默认访问 http://localhost:8080。种子账号只用于本地首次验证，正式环境不要把密码写进命令历史或提交到 Git。

## 服务器部署

服务器项目目录为 /opt/blog。首次部署或更新代码后：

    cd /opt/blog
    cp .env.example .env
    vi .env
    docker compose up -d --build
    docker compose logs -f app

先在 .env 中填写强密码，再启动服务。若要创建首批账号，可临时设置 APP_SEED_ENABLED=true 及三类账号密码；启动成功后改回 false，再执行 docker compose up -d。

## Git 流程

- main 保持可部署，功能开发优先使用 feature/<name> 分支。
- 提交前运行 .\mvnw.cmd test 和 .\mvnw.cmd package -DskipTests。
- 本地验证通过后推送 GitHub；服务器只通过 Git 拉取已提交代码，再重新构建容器。
- .env、密钥、数据库文件、上传文件和构建产物不会提交到仓库。

详细产品范围和后续规划见 PROJECT_DESIGN.md。
