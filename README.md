# TechShop - Backend API 🛒

Sistema de gestión de e-commerce desarrollado con Spring Boot, enfocado en la seguridad, gestión de pedidos y reportes automatizados.

## 🚀 Tecnologías Principales

* **Java 21 (LTS)**
* **Spring Boot 3.5.6**
* **Spring Security** con **JWT** (JSON Web Tokens)
* **MySQL** (Persistencia de datos)
* **OpenPDF & ZXing** (Generación de Facturas, QR y Códigos de Barras)
* **Maven** (Gestión de dependencias)

## 🛡️ Características de Seguridad Implementadas

* **Autenticación y Autorización:** Implementación de roles (`ADMIN`, `USER`).
* **Blindaje de Pedidos:** Los usuarios solo acceden a sus propios pedidos mediante validación de identidad en el Service Layer.
* **Filtros JWT:** Interceptores de seguridad para proteger rutas críticas y validación de tokens Bearer.

## 📄 Funcionalidades Destacadas

* **Dashboard de Ventas:** Endpoint de estadísticas con el Top 5 de productos más vendidos.
* **Procesamiento de Pedidos:** Gestión de stock automatizada y validación de ítems.
* **Facturación Electrónica:** Generación de facturas en PDF con códigos QR dinámicos y códigos de barras.
* **Gestión de Carrito:** Persistencia de ítems y conversión fluida a Pedido.

## 🛠️ Cómo ejecutar el proyecto

1. Clonar el repositorio.
2. Configurar la base de datos en `src/main/resources/application.properties`.
3. Ejecutar `./mvnw spring-boot:run` en la terminal.