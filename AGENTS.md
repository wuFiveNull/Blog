# Blog 项目协作说明

- 开始开发前先阅读 PROJECT_DESIGN.md。
- 技术栈默认使用 Spring Boot MVC、Thymeleaf、MySQL、Spring Security。
- 本地开发，GitHub 管理版本，服务器 /opt/blog 只通过 Git 拉取。
- main 保持可部署，功能使用 feature/* 分支。
- 不提交 *.pem、*.key、.env、密码、日志、上传文件和 target。
- 不修改或覆盖服务器已有的 /opt/my-blog、/app/blog。
- 后端必须校验文章、评论、收藏和管理操作权限，不能只依赖前端隐藏按钮。
- 未经明确要求，不在服务器直接编辑源代码，也不执行强制覆盖或删除操作。
