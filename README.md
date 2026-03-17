# ⚙️ TechShop Backend - Spring Boot REST API

Sistema completo de comercio electrónico con autenticación JWT, gestión de pedidos, direcciones de envío, simulador de pagos y generación de facturas PDF con QR y códigos de barras.

## 🚀 Características Principales

### **Autenticación y Autorización**
- ✅ Registro de usuarios con encriptación BCrypt
- ✅ Login con generación de JWT tokens
- ✅ Roles: `USER` y `ADMIN`
- ✅ Protección de endpoints con Spring Security

### **Gestión de Productos**
- ✅ CRUD completo de productos
- ✅ Control de stock en tiempo real
- ✅ Actualización automática de inventario tras compra
- ✅ Top 5 productos más vendidos
- ✅ Validación de stock antes de confirmar pedido

### **Sistema de Pedidos**
- ✅ Creación de pedidos con múltiples ítems
- ✅ Estados: `PENDIENTE`, `PAGADO`, `ENVIADO`, `ENTREGADO`, `CANCELADO`
- ✅ Asociación con direcciones de envío
- ✅ Validación de pertenencia de dirección al usuario
- ✅ Registro de método de pago por pedido
- ✅ Historial completo de pedidos por usuario

### **Direcciones de Envío**
- ✅ CRUD de direcciones por usuario
- ✅ Múltiples direcciones por usuario
- ✅ Validación de propiedad (un usuario solo modifica sus direcciones)
- ✅ **Protección de integridad:** No se pueden eliminar direcciones con pedidos asociados
- ✅ Mensajes de error descriptivos

### **Sistema de Pagos Simulado**
- ✅ Tres métodos de pago: Tarjeta de Crédito, PayPal, Transferencia Bancaria
- ✅ Registro del método usado en cada pedido
- ✅ Cambio automático de estado a `PAGADO`
- ✅ Endpoint para procesar pago con método seleccionado

### **Generación de Facturas PDF**
- ✅ **QR Code** con enlace directo al pedido
- ✅ **Código de Barras** (Code 128) con ID del pedido
- ✅ Diseño profesional de 3 columnas:
  - Cliente (nombre, email)
  - Pedido (número, fecha, estado, método de pago)
  - Dirección de envío (completa)
- ✅ Tabla detallada de productos con cantidades y precios
- ✅ Total destacado
- ✅ Visualización inline en navegador o descarga directa

### **Dashboard Administrativo**
- ✅ Ventas totales en tiempo real
- ✅ Conteo de pedidos pendientes de envío
- ✅ Productos con stock crítico
- ✅ Total de productos en catálogo

---

## 🛠️ Tecnologías Utilizadas

| Categoría | Tecnología | Versión |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.5.6 |
| **Lenguaje** | Java | 21 |
| **Seguridad** | Spring Security + JWT | 6.x |
| **ORM** | Spring Data JPA / Hibernate | 6.x |
| **Base de Datos** | MySQL | 8.0 |
| **Validación** | Jakarta Validation | 3.x |
| **Generación PDF** | OpenPDF (iText fork) | 1.3.30 |
| **QR/Códigos de Barras** | ZXing | 3.5.1 |
| **Documentación** | Swagger UI / OpenAPI | 3.0 |
| **Contenedores** | Docker + Docker Compose | Latest |
| **Build Tool** | Maven | 3.8+ |

---

### **1. Clonar el Repositorio**
```bash
git clone https://github.com/jcast2023/e-commerce
cd techshop-backend
```
---
## 🔐 Seguridad

### **Flujo de Autenticación**
```
1. Usuario → POST /api/auth/register → Contraseña encriptada con BCrypt
2. Usuario → POST /api/auth/login → Recibe JWT token
3. Cliente incluye token en cada request:
   Header: Authorization: Bearer {token}
4. JwtAuthenticationFilter valida token
5. Si válido → Request procesado
   Si inválido → 401 Unauthorized
```

## 🐳 Docker

### **Requisitos**

- Docker Desktop instalado ([Descargar](https://www.docker.com/products/docker-desktop/))

---

#

## 🔗 Frontend

Este backend se integra con el frontend Angular:

👉 **[TechShop Frontend - Angular 19](https://github.com/jcast2023/techshop-frontend)**

---

## 🛡️ Buenas Prácticas Implementadas

- ✅ **Arquitectura en capas:** Controller → Service → Repository
- ✅ **DTOs** para transferencia de datos
- ✅ **Validación** con Jakarta Validation (`@Valid`, `@NotNull`, etc.)
- ✅ **Manejo de excepciones** centralizado
- ✅ **Transacciones** con `@Transactional`
- ✅ **Inyección de dependencias** vía constructor
- ✅ **Configuración externalizada** en `application.properties`
- ✅ **Logging** con SLF4J
- ✅ **Separación de concerns** (lógica de negocio en servicios)
- ✅ **Validación de permisos** a nivel de método (`@PreAuthorize`)

---


## 👨‍💻 Autor

**Julio Castillo**


---



