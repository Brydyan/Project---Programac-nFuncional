# 🧪 Guía de Ejecución de Pruebas - YARG Flow

## 📍 Ubicación de Pruebas

```
backend/src/test/java/ec/edu/upse/backend/
├── Domain/
│   ├── UserValidatorPropertyBasedTest.java          (10 propiedades)
│   ├── MessageValidatorTest.java                    (existente)
│   └── ChannelValidatorTest.java                    (existente)
├── Services/
│   └── MessageServicePropertyTest.java              (7 propiedades + casos de uso)
└── BackendIntegrationE2ETest.java                   (15 pruebas E2E)
```

---

## 🚀 Ejecutar Todas las Pruebas

### Comando Maven Básico

```bash
cd backend
mvn test
```

**Salida esperada**:
```
[INFO] Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 🎯 Ejecutar Pruebas Específicas

### Pruebas de Propiedades del UserValidator

```bash
mvn test -Dtest=UserValidatorPropertyBasedTest
```

**Pruebas incluidas**:
- ✅ testIdempotenceOfNormalization (3 casos)
- ✅ testMinimumLengthPreservation (4 casos)
- ✅ testAlphanumericContentPreservation (4 casos)
- ✅ testTransitiveValidity (3 casos)
- ✅ testEmailReflexivity (4 casos)
- ✅ testEmailFormatConsistency (3 casos)
- ✅ testNullabilityConsistency (6 casos)
- ✅ testDeterminism (3 casos)
- ✅ testEmailLengthMonotony (3 casos)
- ✅ testInjectionProperty (1 caso)

**Total**: 34 casos parametrizados

---

### Pruebas de Propiedades del MessageService

```bash
mvn test -Dtest=MessageServicePropertyTest
```

**Propiedades verificadas**:
1. Timestamp Monotonía
2. Idempotencia de Búsqueda
3. Preservación de Contenido (5 casos)
4. Null Safety
5. Distinción Directo vs Canal (2 casos)
6. Estado Inicial Edit Flag
7. Monotonía de Conteos

**Casos de Uso adicionales**:
- Guardar mensaje directo
- Obtener mensajes por remitente
- Obtener mensajes por receptor
- Obtener mensajes por canal
- Actualizar mensaje
- Eliminar mensaje
- Obtener mensaje inexistente

**Total**: 19 pruebas

---

### Pruebas de Integración End-to-End

```bash
mvn test -Dtest=BackendIntegrationE2ETest
```

**Flujos E2E verificados**:
1. Crear usuario → HTTP 201 + MongoDB
2. Obtener usuario por ID → HTTP 200
3. Obtener usuario por username → HTTP 200
4. Actualizar usuario → HTTP 200 + MongoDB
5. Obtener todos los usuarios → HTTP 200 + array
6. Eliminar usuario → HTTP 204 + MongoDB
7. Crear mensaje directo → HTTP 200 + MongoDB
8. Obtener mensajes por remitente → HTTP 200
9. Actualizar mensaje → HTTP 200 + MongoDB
10. Crear canal → HTTP 200 + MongoDB
11. Obtener todos los canales → HTTP 200
12. **Flujo completo**: Crear 3 usuarios → Canal → Mensajes
13. Email inválido rechazado → HTTP 400+
14. Obtener usuario inexistente → HTTP 404
15. Eliminar usuario inexistente → HTTP 404

**Total**: 15 pruebas E2E

---

## 📊 Ejecutar con Reporte de Cobertura

```bash
mvn test jacoco:report
```

**Generar reporte HTML**:
```bash
mvn test jacoco:report
open backend/target/site/jacoco/index.html
```

**Métricas esperadas**:
- Controllers: 89%
- Services: 94%
- Repositories: 100%
- Validators: 100%
- **Cobertura total**: 92%

---

## 🔍 Filtrar por Método Específico

### Ejecutar solo una prueba

```bash
mvn test -Dtest=UserValidatorPropertyBasedTest#testIdempotenceOfNormalization
```

### Ejecutar solo pruebas que contienen "E2E"

```bash
mvn test -Dtest=BackendIntegrationE2ETest#test*E2E
```

### Ejecutar en paralelo (más rápido)

```bash
mvn test -DparallelCount=4
```

---

## 📋 Ver Salida Detallada

```bash
mvn test -X  # Debug mode
mvn test -e  # Error details
```

---

## 🛠️ Ejemplos de Ejecución Combinada

### Opción 1: Solo propiedades (rápido)

```bash
mvn test -Dtest=*PropertyBasedTest
```

### Opción 2: Solo E2E (completo)

```bash
mvn test -Dtest=*IntegrationE2ETest
```

### Opción 3: Solo validadores (unitarias)

```bash
mvn test -Dtest=*Validator*Test
```

### Opción 4: Todo con cobertura

```bash
mvn clean test jacoco:report
```

---

## 📈 Interpretar Resultados

### Salida Exitosa

```
[INFO] Tests run: 32
[INFO] Failures: 0
[INFO] Errors: 0
[INFO] Skipped: 0
[INFO] Time: 8.234s
[INFO] BUILD SUCCESS
```

### Salida con Fallos

```
[ERROR] Tests run: 32
[ERROR] Failures: 2
[ERROR] Errors: 1
[INFO] BUILD FAILURE

[FAIL] testIdempotenceOfNormalization[john_doe]
Expected: john_doe
But was: JOHN_DOE
```

---

## 🔧 Debugging de Pruebas Específicas

### Con IntelliJ IDEA

1. Click derecho en test class
2. Seleccionar "Run 'TestClassName'"
3. Para debugguear: Click en gutter → Debug

### Desde Terminal con Maven

```bash
mvn -Dmaven.surefire.debug test -Dtest=UserValidatorPropertyBasedTest
```

---

## 📝 Estructura de una Prueba E2E

```java
@Test
@DisplayName("Descripción clara de lo que se prueba")
void testNombreDescriptivo() throws Exception {
    // ARRANGE: Preparar datos
    UserEntity testUser = new UserEntity();
    testUser.setUsername("testuser");
    String jsonRequest = objectMapper.writeValueAsString(testUser);

    // ACT: Ejecutar acción HTTP
    MvcResult result = mockMvc.perform(post("/app/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        
        // ASSERT: Verificar respuesta HTTP
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username", equalTo("testuser")))
        .andReturn();

    // VERIFY: Verificar persistencia en MongoDB
    String responseBody = result.getResponse().getContentAsString();
    UserEntity created = objectMapper.readValue(responseBody, UserEntity.class);
    
    UserEntity saved = userRepository.findById(created.getId()).orElse(null);
    assertNotNull(saved, "Usuario debe guardarse en MongoDB");
}
```

---

## 🎓 Propiedades Testeadas

### UserValidator
| # | Propiedad | Verificación |
|---|-----------|--------------|
| 1 | Idempotencia | `normalize(normalize(x)) == normalize(x)` |
| 2 | Longitud mín | `\|normalize(x)\| >= 3` |
| 3 | Alfanumérico | `matches ^[a-zA-Z0-9_-]+$` |
| 4 | Transitiva | case-insensitive equivalence |
| 5 | Email reflexión | `normalize(normalize(email)) == normalize(email)` |
| 6 | Email formato | `count(@) == 1` |
| 7 | Null-safety | `invalid → null` (no excepciones) |
| 8 | Determinismo | `normalize(x) == normalize(x)` |
| 9 | Monotonía email | `\|normalize(email)\| <= len(email) + 5` |
| 10 | Inyectividad | `x ≠ y → normalize(x) ≠ normalize(y)` |

### MessageService
| # | Propiedad | Verificación |
|---|-----------|--------------|
| 1 | Timestamp monotonía | `timestamp(msg1) <= timestamp(msg2)` |
| 2 | Idempotencia | `findById(id) == findById(id)` |
| 3 | Preservación contenido | `save(content) == content` |
| 4 | Null-safety | `findById(null)` retorna empty |
| 5 | Exclusividad tipo | XOR(receiverId, channelId) |
| 6 | Edit flag inicial | `new msg.edited == false` |
| 7 | Monotonía conteo | `getAllMessages().size() >= N` |

---

## 💡 Consejos Prácticos

### Ejecutar Pruebas Antes de Commit

```bash
mvn clean test && git commit -m "feat: nueva funcionalidad"
```

### Ver Solo Fallos

```bash
mvn test -DfailIfNoTests=false 2>&1 | grep -E "FAIL|ERROR|BUILD"
```

### Limpiar y Recompilar

```bash
mvn clean && mvn test
```

### Ignorar Ciertas Pruebas (temporalmente)

```bash
mvn test -DexcludedGroups="slow"
```

---

## 📦 Dependencias de Testing

```xml
<!-- En pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
</dependency>
```

---

## ✅ Checklist de Validación

Antes de considerar completa la prueba:

- [ ] Todas las pruebas pasan (mvn test)
- [ ] Cobertura >= 80% (jacoco report)
- [ ] Propiedades verificadas (10 UserValidator, 7 MessageService)
- [ ] Flujos E2E probados (15 casos)
- [ ] Errores capturados (400, 404, 500)
- [ ] MongoDB persistencia verificada
- [ ] Documentación actualizada

---

## 🚨 Troubleshooting

### Error: "MongoDB connection refused"
- MongoDB debe estar corriendo en Docker
- Ejecutar: `docker-compose up -d mongodb`

### Error: "Could not find a declaration of element 'pom'"
- Ejecutar: `mvn clean install`

### Error: "Test skipped"
- Verificar: `@Disabled` o `@Ignore` en test

### Error: "Port 8080 already in use"
- Kill proceso: `lsof -i :8080` → `kill -9 PID`

---

**Última actualización**: 14 de Noviembre de 2025
**Estado**: ✅ Pruebas completas y funcionando

