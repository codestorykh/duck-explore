# Null-Safety Standard with JSpecify

## 1. Overview
Our services use **JSpecify** (`org.jspecify.annotations`) to eliminate `NullPointerException` (NPE) bugs at compile time.

We follow a **Null-Marked by Default** architecture. All classes, method parameters, and return types within our packages are strictly **non-null** unless explicitly declared with `@Nullable`.

---

## 2. Allowed Annotations
Do **not** import any of the following legacy annotations:
* ❌ `javax.annotation.Nullable` / `javax.annotation.Nonnull` (JSR-305)
* ❌ `org.springframework.lang.Nullable` / `NonNull`
* ❌ `org.jetbrains.annotations.*`
* ❌ `edu.umd.cs.findbugs.annotations.*`

**Always use:**
* ✅ `org.jspecify.annotations.NullMarked`
* ✅ `org.jspecify.annotations.Nullable`
* ✅ `org.jspecify.annotations.NonNull` (Only when overriding an unannotated scope)

---

## 3. Engineering Conventions

### Rule 1: Set Package Defaults
Every new sub-package must include a `package-info.java` containing `@NullMarked`:
```java
@NullMarked
package com.duck.explore;

import org.jspecify.annotations.NullMarked;