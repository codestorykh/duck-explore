enterprise-platform-foundation/
├── pom.xml
├── enterprise-platform-dependencies/
│   └── pom.xml
└── enterprise-boot-starter-idempotency/
├── pom.xml
└── src/main/
├── java/com/company/platform/idempotency/
│   ├── annotation/
│   │   └── Idempotent.java
│   ├── aop/
│   │   └── IdempotencyAspect.java
│   ├── configuration/
│   │   ├── IdempotencyAutoConfiguration.java
│   │   └── IdempotencyProperties.java
│   ├── exception/
│   │   └── DuplicateTransactionException.java
│   └── service/
│       ├── IdempotencyKeyResolver.java
│       └── RedisIdempotencyStore.java
└── resources/
└── META-INF/spring/
└── org.springframework.boot.autoconfigure.AutoConfiguration.imports