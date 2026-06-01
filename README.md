# Marianistore Backend

Monolito robusto desarrollado en Spring Boot para la gestión y procesamiento de órdenes de Marianistore.

## 🚀 Tecnologías utilizadas
* Java 25
* Spring Boot 3.5.6
* Spring Data JPA (Hibernate)
* Spring Security & JWT
* MySQL (Base de datos)
* SDK Mercado Pago / Pasarela de pagos

## 🛠️ Arquitectura
El proyecto cuenta con 12 interfaces de repositorio JPA y una estructura desacoplada en controladores, servicios, DTOs y modelos para manejar:
* Autenticación y Usuarios
* Carrito de compras y Wishlist
* Procesamiento de Pedidos y Pagos