# Función de Validación de Nombre de Usuario y Correo Electrónico

## Descripción General
Se añadió validación en tiempo real para verificar duplicados de alias (username) y correo electrónico durante el registro, mostrando retroalimentación clara al usuario.

---

## Cambios en el Backend

### 1. **UserAvailabilityController.java** (NUEVO)
- **Ubicación:** `backend/src/main/java/ec/edu/upse/backend/Controller/UserAvailabilityController.java`
- **Endpoints:**
  - `GET /app/v1/user/available/username/{username}` – Verifica si el alias está disponible
  - `GET /app/v1/user/available/email/{email}` – Verifica si el correo está disponible
- **Respuesta:** `{ "available": true/false }`
- **Dependencias:** Usa `UserService` para verificar disponibilidad

---

### 2. **UserService.java** (ACTUALIZADO)
- **Archivo:** `backend/src/main/java/ec/edu/upse/backend/Service/UserService.java`
- **Nuevos Métodos:**
  - `isUsernameAvailable(String username): boolean`
  - `isEmailAvailable(String email): boolean`
- **Cambios importantes en save():**
  - Verifica duplicados antes de guardar
  - Lanza `IllegalArgumentException` si existe duplicado

---

### 3. **Estado de Build:** ✅ ÉXITO
- Maven compila sin errores
- Se genera el artefacto final correctamente

---

## Cambios en el Frontend

### 1. **UserAvailabilityService.ts** (NUEVO)
- **Ubicación:** `Frontend-PF/src/app/Service/UserAvailabilityService.ts`
- **Características:**
  - Métodos para consumir el backend y validar disponibilidad
  - Caché interno con TTL de 5 minutos
  - Manejo de errores seguro
  - Devuelve `false` para entradas vacías o inválidas

---

### 2. **RegisterComponent.ts** (ACTUALIZADO)
- **Archivo:** `Frontend-PF/src/app/Components/auth/register/register.ts`
- **Nuevas Propiedades:**
  - `usernameAvailability`
  - `emailAvailability`
- **Nuevos Métodos:**
  - `onAliasBlur()` – Verifica alias al perder foco
  - `onEmailBlur()` – Verifica correo al perder foco
- **Mejora en onRegisterSubmit():**
  - Bloquea envío si alias o correo no están disponibles

---

### 3. **register.html** (ACTUALIZADO)
- En el campo de correo se añadió:
  - `(blur)="onEmailBlur()"`
  - Mensajes dinámicos:
    - "Verificando disponibilidad..."
    - "Este correo ya está registrado."
    - "✓ Correo disponible"
- En el campo de alias se añadió:
  - `(blur)="onAliasBlur()"`
  - Mensajes dinámicos equivalentes

---

### 4. **register.scss** (ACTUALIZADO)
- Nuevas clases:
  - `.success-message`
  - `.info-message`

---

### 5. **Build del Frontend:** ✅ ÉXITO
- Transferencia estimada: 76.26 kB
- Advertencia menor de tamaño CSS

---

## Flujo para el Usuario

1. Usuario ingresa alias
2. Sale del campo → se muestra:
   - "Verificando disponibilidad…"
   - Luego verde si está disponible
   - O rojo si ya existe
3. Usuario ingresa correo
4. Sale del campo → mismo flujo que alias
5. Usuario presiona "Registrar cuenta"
   - Si alias/correo están en uso: el formulario se bloquea
   - Si están libres: registro exitoso

---

## Ejemplos de API

### Verificar alias
```http
GET /app/v1/user/available/username/john_doe
Response: { "available": true }
```

### Verificar correo
```http
GET /app/v1/user/available/email/user@example.com
Response: { "available": false }
```

### Registrar usuario
```http
POST /app/v1/user
{
  "email": "test@example.com",
  "nombre": "Test User",
  "alias": "testuser",
  "password": "...",
  "day": "12",
  "month": "03",
  "year": "1990"
}
```

---

## Archivos Modificados / Creados

| Archivo | Tipo | Estado |
|--------|------|--------|
| Controller/UserAvailabilityController.java | Nuevo | ✅ |
| Service/UserService.java | Editado | ✅ |
| UserAvailabilityService.ts | Nuevo | ✅ |
| register.ts | Editado | ✅ |
| register.html | Editado | ✅ |
| register.scss | Editado | ✅ |

---

## Lista de Pruebas

- [ ] Alias nuevo → debe mostrar ✓
- [ ] Alias duplicado → debe mostrar error
- [ ] Correo nuevo → debe mostrar ✓
- [ ] Correo duplicado → debe mostrar error
- [ ] Registro exitoso sin duplicados
- [ ] Registro falla si alias/correo están usados
- [ ] Caché funciona (segunda consulta más rápida)

---

## Estrategia de Caché

- TTL: 5 minutos
- Keys:  
  - `username:{alias}`  
  - `email:{email}`
- Eliminación automática de entradas vencidas
- Método `clearCache()` disponible

---

## Manejo de Errores

| Caso | Frontend | Backend |
|------|----------|---------|
| Error de red | Asume no disponible | N/A |
| Entrada vacía | Devuelve false | N/A |
| Duplicado | Error visual | Excepción |
| JSON inválido | Error general | 400/500 |

---

## Seguridad

- Normalización de datos
- Prevención básica de enumeración
- Validaciones robustas en backend

---

## Mejoras Futuras

1. Debounce de 500 ms
2. Rate-limiting
3. Logs de intentos fallidos
4. CAPTCHA opcional
5. Reemplazar blur por async validators
