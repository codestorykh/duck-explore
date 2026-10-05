# Enterprise Engineering Standard: Null-Safety with JSpecify & NullAway

## 1. Architectural Philosophy
We follow a Null-Marked by Default model:

* ✅ `All scopes are non-null by default:` Unless declared otherwise, any variable, parameter, or return type is strictly assumed non-null.
* ✅ `Opt-in Nullability:` We only use `@Nullable` when `null` represents a valid domain state (e.g., an optional database field, cache miss, or optional payload parameter).
* ✅ `Compile-Time Enforcement:` Relying on runtime `NullPointerException` (NPE) is unacceptable in transaction-critical microservices. Null safety must be verified during compilation via NullAway.
 

## 2. Core JSpecify Annotation Standards
All engineers must import annotations exclusively from `org.jspecify.annotations.*`:

* ✅ `org.jspecify.annotations.NullMarked`: Establishes that all enclosed types are non-null by default.
* ✅ `org.jspecify.annotations.NullUnmarked`: Opts out of null-safety analysis (used only for legacy integrations).
* ✅ `org.jspecify.annotations.Nullable`: Explicitly documents and allows `null` for a variable, field, parameter, or return.
* ✅ `org.jspecify.annotations.NonNull`: (Only when overriding an unannotated scope)

Strict Prohibitions (Banned Imports)

Do not import any of these legacy annotations:
* ❌ javax.annotation.Nullable / Nonnull (JSR-305)
* ❌ org.springframework.lang.Nullable / NonNull
* ❌ org.jetbrains.annotations.*
* ❌ edu.umd.cs.findbugs.annotations.*
* ❌ android.support.annotation.*

## 3. Best Practices for Implementation

### A. DTOs and Java Records
DTOs receive input from external boundaries (HTTP/JSON, Kafka, MQ).
* Never declare collections as `@Nullable`. Return an empty collection instead (`List.of()`, `Set.of()`).
* Optional fields must be marked `@Nullable`.

```java
package com.duck.explore.dto;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record TransactionPostingRequest(
    String transactionId,                   // Required (Non-null)
    BigDecimal amount,                      // Required (Non-null)
    String currency,                        // Required (Non-null)
    @Nullable String narration,             // Optional
    @Nullable String externalRefCode,       // Optional
    List<String> tags                       // Non-null container, empty list if omitted
) {
    public TransactionPostingRequest {
        // Defensive assignment: ensure collections are never null
        tags = (tags == null) ? List.of() : List.copyOf(tags);
    }
}
```
### B. Service Layer Patterns
1. Method Signatures
Place @Nullable directly on the type being modified:
```java
// Method can return null if record is not found
public @Nullable TransactionRecord findById(String transactionId) {
    return repository.find(transactionId);
}

// Method accepts an optional fallback code
public TransactionRecord process(TransactionPostingRequest req, @Nullable String fallbackCode) {
    // NullAway enforces a null-check before dereferencing fallbackCode
    String code = (fallbackCode != null) ? fallbackCode.trim() : "DEFAULT";
    return new TransactionRecord(req.transactionId(), code);
}
```
2. Safe Dereferencing
When handling @Nullable references:
```java
// Option A: Standard null guard (smart-cast by NullAway)
if (request.narration() != null) {
    log.info("Length: {}", request.narration().length()); // Safe
}

// Option B: Ternary fallback
String ref = (request.externalRefCode() != null) ? request.externalRefCode() : "N/A";

// Option C: Fast-fail assertion for guaranteed invariants
String id = Objects.requireNonNull(request.transactionId(), "transactionId cannot be null");
```

## 4. Production Maven Build Configuration
Add this setup to your service `pom.xml`. It guarantees that NullAway compiles in JSpecify-strict mode and prevents IDE compiler crashes.

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.release>21</maven.compiler.release>
    <jspecify.version>1.0.0</jspecify.version>
    <nullaway.version>0.12.2</nullaway.version>
    <errorprone.version>2.36.0</errorprone.version>
</properties>

<dependencies>
    <!-- JSpecify Standard Annotations -->
    <dependency>
        <groupId>org.jspecify</groupId>
        <artifactId>jspecify</artifactId>
        <version>${jspecify.version}</version>
    </dependency>

    <!-- Runtime Bean Validation (Jakarta @NotNull, @Valid for REST boundaries) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <release>${maven.compiler.release}</release>
                <fork>true</fork>
                <compilerArgs>
                    <arg>-XDcompilePolicy=simple</arg>
                    <arg>--should-stop=ifError=FLOW</arg>
                    <!-- NullAway JSpecify Configuration -->
                    <arg>-Xplugin:ErrorProne -Xep:NullAway:ERROR -XepOpt:NullAway:AnnotatedPackages=com.duck.explore -XepOpt:NullAway:JSpecifyMode=true -XepOpt:NullAway:TreatGeneratedAsUnannotated=true</arg>
                    
                    <!-- JDK Internal Exports for Error Prone AST Inspection -->
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
                </compilerArgs>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.google.errorprone</groupId>
                        <artifactId>error_prone_core</artifactId>
                        <version>${errorprone.version}</version>
                    </path>
                    <path>
                        <groupId>com.uber.nullaway</groupId>
                        <artifactId>nullaway</artifactId>
                        <version>${nullaway.version}</version>
                    </path>
                    <path>
                        <groupId>org.jspecify</groupId>
                        <artifactId>jspecify</artifactId>
                        <version>${jspecify.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## 5. Developer Workflow & IDE Setup

IntelliJ IDEA Configuration

To prevent internal compiler errors and synchronize IDE checks with Maven:

* Open Settings / Preferences (Ctrl + Alt + S / Cmd + ,).
* Navigate to Build, Execution, Deployment $\rightarrow$ Build Tools $\rightarrow$ Maven $\rightarrow$ Runner
* Enable: "Delegate IDE build/run actions to Maven".
* Navigate to Editor $\rightarrow$ Inspections $\rightarrow$ Search @NotNull/@Nullable problems
  * Add `org.jspecify.annotations.Nullable` to Nullable.
  * Add `org.jspecify.annotations.NullMarked` to NullMarked / NonNull defaults.

Build Commands
```ssh
# Verify all null-safety checks locally
./mvnw clean test-compile -DskipTests

# Run application locally
./mvnw spring-boot:run
```

## 6. Null-Safety Decision Tree
Use this quick guide when writing or refactoring Java methods:
```plaintext
Is this field / return / parameter guaranteed to be present?
  ├── YES ──▶ Do NOT annotate (it is non-null by default via @NullMarked)
  └── NO
        ├── Is it a Collection (List, Set, Map)?
        │     └── Return an empty collection instead: List.of() or Collections.emptyList()
        ├── Is it an Optional domain lookup in a Service/Repository?
        │     └── Return Optional<T>
        └── Is it a DTO field, method argument, or internal utility result?
              └── Mark as @Nullable T
```


### Reference

https://github.com/uber/NullAway/wiki/Error-Messages