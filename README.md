# AI智能取名小程序

一款基于 AI 大模型的智能取名小程序，融合易经五行、现代审美与音韵学，为宝宝取一个寓意美好、朗朗上口的好名字。

## 技术栈

### 后端
- **Java 1.8**
- **SpringBoot 2.7.18**
- **MyBatisPlus 3.5.5**
- **MySQL 8.0**
- **Redis**
- **WebClient**（异步 AI 调用）

### 前端
- **UniApp** (Vue3)
- 支持：微信小程序、H5、App

## 快速开始

```bash
# 1. 数据库初始化
mysql -u root -p < database/init.sql

# 2. 后端启动
cd ai-naming-backend
mvn spring-boot:run

# 3. 前端启动
cd ai-naming-uniapp
npm install
npm run dev:h5
```
