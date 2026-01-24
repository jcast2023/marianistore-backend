# TechShop - Backend API 🛒
Sistema de gestión de e-commerce desarrollado con Spring Boot, enfocado en la seguridad y la gestión de pedidos.

## 🚀 Tecnologías Principales
* **Java 17** y **Spring Boot 3.x**
* **Spring Security** con **JWT** (JSON Web Tokens)
* **MySQL** (Persistencia de datos)
* **Maven** (Gestión de dependencias)
* **Docker** (Contenerización lista para la nube)

## 🛡️ Características de Seguridad Implementadas
* **Autenticación y Autorización:** Implementación de roles (`ADMIN`, `USER`).
* **Blindaje de Pedidos:** Los usuarios solo pueden acceder a sus propios pedidos mediante validación de identidad en el Service Layer.
* **Filtros JWT:** Interceptores de seguridad para proteger rutas críticas.

## 📄 Funcionalidades Destacadas
* Gestión completa de productos y categorías.
* Sistema de carrito y procesamiento de pedidos.
* Generación de facturas y reportes.
* Manejo global de excepciones.

## 🛠️ Cómo ejecutar el proyecto
1. Clonar el repositorio.
2. Configurar la base de datos en `src/main/resources/application.properties`.
3. Ejecutar `./mvnw spring-boot:run` en la terminal.