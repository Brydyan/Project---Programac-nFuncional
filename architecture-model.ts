/**
 * YARG Flow - Arquitectura Final del Sistema
 * Representación de componentes y flujos
 */

// ============================================
// 1. CAPA DE PRESENTACIÓN (Angular Frontend)
// ============================================
interface ClientLayer {
  framework: "Angular 20";
  language: "TypeScript 5.9";
  components: {
    auth: ["LoginComponent", "RegisterComponent", "ForgotPasswordComponent"];
    pages: ["AuthPage", "DashboardPage", "ChatPage", "ChannelsPage"];
    services: ["AuthService", "MessageService", "ChannelService", "UserService"];
  };
  communication: "HttpClient + RxJS + WebSocket";
}

// ============================================
// 2. CAPA DE ACCESO A RED
// ============================================
interface NetworkLayer {
  reverseProxy: "nginx";
  protocol: "HTTPS/HTTP";
  ports: {
    frontend: 80;
    backend: 8080;
    websocket: 8080;
  };
  loadBalancing: "nginx";
}

// ============================================
// 3. CAPA DE API (REST + WebSocket)
// ============================================
interface APILayer {
  framework: "Spring Boot 3.5.7";
  java_version: 21;
  
  restControllers: {
    UserController: ["POST /app/v1/user", "GET /app/v1/user", "GET /app/v1/user/{id}", "DELETE /app/v1/user/{id}"];
    MessageController: ["POST /app/v1/messages", "GET /app/v1/messages", "GET /app/v1/messages/sender/{id}", "PUT /app/v1/messages/{id}"];
    ChannelController: ["POST /app/v1/channels", "GET /app/v1/channels", "GET /app/v1/channels/{id}", "DELETE /app/v1/channels/{id}"];
    PresenceController: ["GET /app/v1/presence", "POST /app/v1/presence/{userId}"];
    SessionController: ["POST /app/v1/sessions", "GET /app/v1/sessions/{userId}"];
    NotificationController: ["GET /app/v1/notifications", "POST /app/v1/notifications/{userId}"];
    ContactController: ["GET /app/v1/contacts", "POST /app/v1/contacts"];
    ChannelMsgController: ["GET /app/v1/channel-messages/{channelId}"];
  };
  
  websocketServer: {
    endpoint: "/ws/messages";
    features: ["Real-time messaging", "Live presence", "Channel notifications"];
  };
}

// ============================================
// 4. CAPA DE LÓGICA DE NEGOCIO
// ============================================
interface BusinessLogicLayer {
  services: {
    UserService: {
      methods: ["save()", "getAllUsers()", "getUserById()", "updateUser()", "deleteUser()"];
      validation: "UserValidator";
    };
    MessageService: {
      methods: ["save()", "getAllMessages()", "getMessagesBySender()", "updateMessage()", "deleteMessage()"];
      validation: "MessageValidator";
    };
    ChannelService: {
      methods: ["save()", "getAllChannels()", "getChannelById()", "updateChannel()", "deleteChannel()"];
      validation: "ChannelValidator";
    };
    PresenceService: {
      methods: ["setPresence()", "getPresence()", "updatePresence()"];
    };
    SessionService: {
      methods: ["createSession()", "validateSession()", "endSession()"];
    };
    NotificationService: {
      methods: ["sendNotification()", "getNotifications()"];
    };
    ContactService: {
      methods: ["addContact()", "removeContact()", "getContacts()"];
    };
    ChannelMsgService: {
      methods: ["saveChannelMessage()", "getChannelMessages()"];
    };
  };

  validators: {
    UserValidator: {
      methods: ["normalizarUsername()", "normalizarEmail()"];
      rules: ["Username: solo alfanuméricos", "Email: formato válido"];
    };
    MessageValidator: {
      methods: ["validateContent()", "validateLength()"];
    };
    ChannelValidator: {
      methods: ["validateChannelName()", "validateMembers()"];
    };
  };
}

// ============================================
// 5. CAPA DE ACCESO A DATOS
// ============================================
interface DataAccessLayer {
  orm: "Spring Data MongoDB";
  repositories: [
    "UserRepository extends MongoRepository<UserEntity, String>",
    "MessageRepository extends MongoRepository<MessageEntity, String>",
    "ChannelRepository extends MongoRepository<ChannelEntity, String>",
    "PresenceRepository extends MongoRepository<PresenceEntity, String>",
    "SessionRepository extends MongoRepository<SessionEntity, String>",
    "NotificationRepository extends MongoRepository<NotificationEntity, String>",
    "ContactRepository extends MongoRepository<ContactEntity, String>",
    "ChannelMsgRepository extends MongoRepository<ChannelMsgEntity, String>"
  ];
  queryMethods: ["findAll()", "findById()", "findBy*()", "save()", "delete()"];
}

// ============================================
// 6. ENTIDADES (Domain Model)
// ============================================
interface DomainModel {
  entities: {
    UserEntity: {
      fields: ["id: String", "username: String", "email: String", "password: String", "displayName: String", "status: String", "createdAt: Instant", "preferences: Map"];
      collection: "Users";
    };
    MessageEntity: {
      fields: ["id: String", "senderId: String", "receiverId: String", "channelId: String", "content: String", "timestamp: Instant", "edited: boolean", "deleted: boolean"];
      collection: "Messages";
    };
    ChannelEntity: {
      fields: ["id: String", "name: String", "description: String", "createdBy: String", "members: List<String>", "createdAt: Instant"];
      collection: "Channels";
    };
    PresenceEntity: {
      fields: ["id: String", "userId: String", "status: String (ONLINE/OFFLINE/AWAY)", "lastSeen: Instant"];
      collection: "Presences";
    };
    SessionEntity: {
      fields: ["id: String", "userId: String", "token: String", "createdAt: Instant", "expiresAt: Instant"];
      collection: "Sessions";
    };
    NotificationEntity: {
      fields: ["id: String", "userId: String", "message: String", "type: String", "read: boolean", "createdAt: Instant"];
      collection: "Notifications";
    };
    ContactEntity: {
      fields: ["id: String", "userId: String", "contactId: String", "addedAt: Instant"];
      collection: "Contacts";
    };
    ChannelMsgEntity: {
      fields: ["id: String", "channelId: String", "senderId: String", "content: String", "timestamp: Instant"];
      collection: "ChannelMessages";
    };
  };
}

// ============================================
// 7. BASES DE DATOS
// ============================================
interface DatabaseLayer {
  primary: {
    type: "MongoDB Atlas (Cloud)";
    database: "YARGFlow";
    uri: "mongodb+srv://ali_user:***@serveryargflow.hbcosog.mongodb.net/YARGFlow";
    collections: 8;
    features: ["Escalabilidad horizontal", "Replicación automática", "Backups diarios"];
  };
  
  cache: {
    type: "Redis";
    port: 6379;
    purpose: ["Session storage", "User presence cache", "Message cache", "Rate limiting"];
    ttl: "Configurable por tipo de dato";
  };
}

// ============================================
// 8. INFRAESTRUCTURA (Docker)
// ============================================
interface InfrastructureLayer {
  containerization: "Docker + Docker Compose";
  
  services: {
    backend: {
      dockerfile: "backend/Dockerfile";
      port: 8080;
      image: "Custom Java 21 Spring Boot";
      dependencies: ["mongodb", "redis"];
    };
    
    frontend: {
      dockerfile: "Frontend-PF/Dockerfile";
      port: 80;
      image: "Custom nginx + Angular";
      dependencies: ["backend"];
    };
    
    mongodb: {
      image: "mongo:latest";
      port: 27017;
      volume: "./backend/mongo-data";
      credentials: "root:mi_clave_secreta";
    };
    
    redis: {
      image: "redis:alpine";
      port: 6379;
      volume: "./backend/redis-data";
    };
  };
  
  volumeManagement: {
    mongoData: "Persistencia de MongoDB",
    redisData: "Persistencia de Redis"
  };
}

// ============================================
// 9. FLUJOS DE OPERACIÓN
// ============================================
interface OperationalFlows {
  userAuthentication: {
    steps: [
      "1. Usuario ingresa credenciales en LoginComponent",
      "2. FormBuilder valida formato (email, contraseña requerida)",
      "3. HttpClient.post('/app/v1/user', credentials)",
      "4. UserController.createUser() recibe solicitud",
      "5. UserService.save() valida y normaliza datos",
      "6. UserValidator normaliza username y email",
      "7. UserRepository.save() persiste en MongoDB",
      "8. Respuesta 200 OK con UserEntity",
      "9. Frontend guarda token en localStorage",
      "10. Redirige a dashboard"
    ];
    errorHandling: ["404 Not Found", "400 Bad Request", "401 Unauthorized"];
  };
  
  messageFlow: {
    directMessage: {
      steps: [
        "1. Usuario A abre chat con Usuario B",
        "2. Digita mensaje y presiona enviar",
        "3. POST /app/v1/messages { senderId, receiverId, content }",
        "4. MessageController.create() recibe",
        "5. MessageService.save() valida contenido",
        "6. MessageRepository.save() en MongoDB",
        "7. WebSocket notifica Usuario B en tiempo real",
        "8. Ambos ven el mensaje instantáneamente"
      ];
    };
    
    channelMessage: {
      steps: [
        "1. Usuario publica en canal",
        "2. POST /app/v1/channel-messages",
        "3. ChannelMsgService.save()",
        "4. Persistencia en MongoDB (ChannelMessages collection)",
        "5. WebSocket broadcast a todos los miembros del canal",
        "6. Todos ven el mensaje en tiempo real"
      ];
    };
  };
  
  presenceTracking: {
    steps: [
      "1. Usuario se conecta/desconecta",
      "2. WebSocket evento CONNECT/DISCONNECT",
      "3. PresenceService.setPresence(userId, status)",
      "4. Guarda en Redis con TTL",
      "5. Broadcast a contactos del usuario",
      "6. Frontend actualiza estado (online/offline/away)"
    ];
  };
}

// ============================================
// 10. CONFIGURACIÓN Y PROPIEDADES
// ============================================
interface Configuration {
  spring: {
    application_name: "backend";
    port: 8080;
    security: "Spring Security configured";
    websocket: "Enabled";
  };
  
  mongodb: {
    database: "YARGFlow";
    uri: "mongodb+srv://ali_user:AYRR3005abav@serveryargflow.hbcosog.mongodb.net/YARGFlow?retryWrites=true&w=majority";
    retryWrites: true;
    writeConcern: "majority";
  };
  
  redis: {
    host: "redis (Docker service)";
    port: 6379;
  };
  
  security: {
    cors: "Configurado (necesita setup)",
    authentication: "JWT ready (implementar)",
    csrf: "Protection enabled";
  };
}

// ============================================
// 11. MÉTRICAS DE COMPLETITUD
// ============================================
interface CompletionStatus {
  backend_api: "100% - 8 Controllers, 8 Services, 8 Entities";
  frontend_auth: "80% - Login, Register, ForgotPassword (falta OAuth)";
  database_design: "100% - 8 Collections diseñadas";
  testing: "50% - Tests básicos en Validators";
  websocket: "0% - Dependencias incluidas, no implementado";
  security: "40% - Spring Security configurado, JWT pendiente";
  docker: "90% - Docker Compose completo";
  cicd: "0% - No configurado";
}

// ============================================
// 12. PRÓXIMAS FASES
// ============================================
const roadmap: string[] = [
  "✅ Fase 1: API REST (COMPLETO)",
  "⚠️ Fase 2: Autenticación JWT",
  "⚠️ Fase 3: WebSocket Real-time",
  "⚠️ Fase 4: Frontend Dashboard",
  "❌ Fase 5: Tests 80%+",
  "❌ Fase 6: OAuth2 Integration",
  "❌ Fase 7: CI/CD Pipeline",
  "❌ Fase 8: Documentación OpenAPI/Swagger"
];

export {
  ClientLayer,
  NetworkLayer,
  APILayer,
  BusinessLogicLayer,
  DataAccessLayer,
  DomainModel,
  DatabaseLayer,
  InfrastructureLayer,
  OperationalFlows,
  Configuration,
  CompletionStatus,
  roadmap
};
