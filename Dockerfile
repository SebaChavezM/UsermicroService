# Usamos una imagen base de OpenJDK para Java 17
FROM openjdk:21-jdk-slim

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo JAR generado por Maven al contenedor
COPY target/usermicroservice-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto que usa el microservicio
EXPOSE 8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
