package com.duck.explore.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@Slf4j
public class ArchitectureVerificationRunner {

    public static void main(String[] args) {
        log.info("Running Whole-Project Architecture Policy Engine...");

        // 1. Import all compiled classes in the root package, ignoring test classes
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.duck.explore");

        // 2. Define Policy Rules
        // Controller -> Service -> Repository (Strict unidirectional flow)
        ArchRule layerRule = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .layer("Service").definedBy("..service..")
                .layer("Repository").definedBy("..repository..")
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

        //3. Domain Entity & Security Guardrails
        // JPA Entities must NEVER be used or returned in Controllers
        ArchRule entityLeakRule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..domain.entity..")
                .because("Entities leak internal DB state; Controllers must expose DTOs only");

        //4. Transaction & Framework Misuse Guardrails
        // Only @Service classes should manage transactions
        ArchRule transactionalMethodsMustBeInService = methods()
                .that().areAnnotatedWith(Transactional.class)
                .should().beDeclaredInClassesThat().resideInAPackage("..service..")
                .because("@Transactional on Controllers or Repositories causes long-running locks and connection leaks");

        // Mandate Constructor Injection over Field Injection
        ArchRule noFieldAutowired = noFields()
                .should().beAnnotatedWith(Autowired.class)
                .because("Field injection prevents immutability and makes unit testing harder; use constructor injection");

        //5. Third-Party Library & Legacy Code Bans
        // Prevent usage of obsolete or insecure date/time and logging utilities
        ArchRule noJodaTimeOrJavaUtilDate = noClasses()
                .should().dependOnClassesThat().resideInAnyPackage("org.joda.time..", "java.util.Date")
                .because("Modern services must use java.time (JSR-310) for timezone safety");

        ArchRule noSystemOut = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
                .because("Always use SLF4J / logback structured logging");


        // 6. Execute Evaluations
        try {
            layerRule.check(importedClasses);
            entityLeakRule.check(importedClasses);
            transactionalMethodsMustBeInService.check(importedClasses);
            noFieldAutowired.check(importedClasses);
            noJodaTimeOrJavaUtilDate.check(importedClasses);
            noSystemOut.check(importedClasses);

            log.info("All architectural integrity policies passed!");
        } catch (AssertionError error) {
            log.error("[ARCHITECTURE BREACH DETECTED]\n{}", error.getMessage());
            System.exit(1);
        }
    }
} 
