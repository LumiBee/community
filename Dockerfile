# 使用一个轻量级的 Java 17 运行环境作为基础镜像
FROM eclipse-temurin:17-jdk-jammy

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装网络诊断工具
RUN apt-get update && apt-get install -y \
    iputils-ping \
    telnet \
    netcat-openbsd \
    dnsutils \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 将 Maven 构建出的 JAR 包作为参数
ARG JAR_FILE=target/*.jar

# 将 JAR 包复制到镜像中，并重命名为 app.jar
COPY ${JAR_FILE} app.jar

# 容器启动时执行的命令
ENTRYPOINT ["java","-jar","/app.jar"]
