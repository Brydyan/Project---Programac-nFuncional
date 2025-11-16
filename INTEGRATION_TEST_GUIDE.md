# Pruebas de Integración: Validación de Alias y Correo

## Instrucciones Iniciales

### Requisitos
- Docker Desktop en ejecución
- Puerto 8081 libre (backend)
- Puerto 80 libre (frontend)

### Levantar Servicios
```bash
cd c:\xampp\htdocs\Project---Programac-nFuncional-1
docker-compose up -d
```

Servicios disponibles:
- Frontend → http://localhost/
- Backend → http://localhost:8081
- MongoDB → localhost:27017
- Redis → localhost:6379

---

# Escenarios de Prueba

## Escenario 1: Registro con alias y correo disponibles
1. Abrir http://localhost/
2. Ir a “No tengo cuenta”
3. Escribir email: `test1@example.com`
4. Escribir alias: `testuser123`
5. Salir del campo alias
6. Debe aparecer **✓ Alias disponible**

---

## Escenario 2: Email disponible
1. Salir del campo correo
2. Debe aparecer **✓ Correo disponible**

---

## Escenario 3: Registro completo
1. Contraseña: `SecurePass123!@#`
2. Fecha: 15/01/1990
3. Registrar
4. Debe mostrar:
   - “Registro exitoso”
   - Redirigir a login
   - Crear usuario en MongoDB

---

## Escenario 4: Alias duplicado
1. Ingresar alias usado previamente
2. Debe mostrar:
   - “Este alias ya está registrado.”

---

## Escenario 5: Correo duplicado
1. Cambiar correo a uno usado previamente
2. Debe mostrar:
   - “Este correo ya está registrado.”

---

## Escenario 6: Botón deshabilitado si hay duplicados
El botón debe:
- Estar gris
- No responder al click

---

## Escenario 7: Caché de 5 minutos
1. Consultar alias A
2. Consultar alias A otra vez → **instantáneo**
3. Cambiar alias → llamada nueva
4. Volver a alias A → **respuesta desde caché**

---

## Escenario 8: Entrada inválida
Si alias está vacío:
- No debe hacer llamada a backend

---

## Escenario 9: Error de red
- Backend caído
- Availability devuelve `false`
- Se muestra error rojo

---

# Pruebas con DevTools

## Network
```
GET /available/username/testuser123 → 200 OK
GET /available/email/test1@example.com → 200 OK
```

## Console
Sin:
- Errores
- CORS
- 404

## Application
- Token guardado después de login
- No se guarda info de presencia aún

---

# Pruebas con curl / Postman

### Alias
```bash
curl -X GET http://localhost:8081/app/v1/user/available/username/testuser123
```

### Email
```bash
curl -X GET http://localhost:8081/app/v1/user/available/email/test1@example.com
```

### Registro
```bash
curl -X POST "http://localhost:8081/app/v1/user" \
  -H "Content-Type: application/json" \
  -d '{ ... }'
```

---

# Verificación en MongoDB
```javascript
use app_db
db.users.find()
```

---

# Métricas de Rendimiento

| Acción | Duración | Notas |
|--------|----------|-------|
| Chequeo sin caché | 50–200ms | Latencia |
| Chequeo en caché | < 10ms | Instantáneo |
| Registro | 200–500ms | DB + backend |

---

# Reseteo Rápido

### Reset completo
```bash
docker-compose down -v
docker-compose up -d
```

### Reset solo MongoDB
```bash
docker exec mongo-container rm -rf /data/db/*
```

---

# Problemas Comunes

| Problema | Solución |
|----------|----------|
| 404 al llamar availability | Backend caído / mal puerto |
| Disponibilidad queda en “Verificando…” | Error en consola |
| Caché no funciona | Refrescar cache del navegador |
| Botón no se deshabilita | Hard reload (Ctrl+Shift+R) |

---

# Criterios de Éxito

- Todos los escenarios funcionan
- Mensajes correctos (verde/rojo)
- Caché funcionando
- Sin errores en consola
- Usuarios creados en MongoDB
