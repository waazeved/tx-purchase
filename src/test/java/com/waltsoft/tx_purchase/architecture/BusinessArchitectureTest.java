package com.waltsoft.tx_purchase.architecture;


import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class BusinessArchitectureTest {

    public static final String BUSINESS_PACKAGE = "com.waltsoft.tx_purchase.business";

    private static final Set<String> classesThatAreException = makeClassThatIsException(
            "exchange_rate.api.ExchangeRateApi"
    );

    private static Set<String> makeClassThatIsException(String... subPackageAndClassNames) {
        Set<String> exceptions = new HashSet<>();
        for (String subPackageAndClassName : subPackageAndClassNames) {
            String rootPackageSubPackageClassName = BUSINESS_PACKAGE + "." + subPackageAndClassName;
            exceptions.add(rootPackageSubPackageClassName);
        }
        return exceptions;
    }

    @Test
    void allClassesInBusinessPackageMustBePackagePrivateExceptOneServiceInterface() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BUSINESS_PACKAGE);

        List<JavaClass> publicClasses = importedClasses.stream()
                .filter(clazz -> clazz.getModifiers().contains(JavaModifier.PUBLIC))
                .filter(clazz -> !classesThatAreException.contains(clazz.getName()))
                .filter(clazz -> !clazz.isAssignableTo(Exception.class))
                .toList();

        StringBuilder errorMessages = new StringBuilder();

        Map<String, List<JavaClass>> publicClassesByPackage = publicClasses.stream()
                .collect(Collectors.groupingBy(JavaClass::getPackageName));

        publicClassesByPackage.forEach((packageName, classesInPackage) -> {

            List<JavaClass> publicInterfaces = classesInPackage.stream()
                    .filter(JavaClass::isInterface)
                    .toList();

            List<JavaClass> forbiddenPublicTypes = classesInPackage.stream()
                    .filter(clazz -> !clazz.isInterface())
                    .toList();

            if (publicInterfaces.size() > 1) {
                errorMessages.append(String.format(
                        "❌ Error in package [%s]: Only 1 public interface is allowed per package. Found %d public interfaces: %s%n",
                        packageName, publicInterfaces.size(), publicInterfaces.stream().map(JavaClass::getSimpleName).collect(Collectors.joining(", "))
                ));
            }

            if (publicInterfaces.size()==1) {
                JavaClass singleInterface = publicInterfaces.getFirst();
                if (!singleInterface.getSimpleName().endsWith("Service")) {
                    errorMessages.append(String.format(
                            "❌ Error in package [%s]: The single public interface must end with 'Service'. Found: '%s'%n",
                            packageName, singleInterface.getSimpleName()
                    ));
                }
            }

            for (JavaClass forbiddenType : forbiddenPublicTypes) {
                String classType = forbiddenType.isRecord() ? "Record":"Class";
                errorMessages.append(String.format(
                        "❌ Invalid public %s in [%s]: '%s'. Classes and Records must be non-public (package-private).%n",
                        classType, packageName, forbiddenType.getSimpleName()
                ));
            }
        });

        if (!errorMessages.isEmpty()) {
            Assertions.fail("Architecture validation failed for 'business' package:%n" + errorMessages);
        }
    }

    @Test
    void publicServiceInterfacesMustNotExtendAnyOtherInterface() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BUSINESS_PACKAGE);

        List<JavaClass> publicServiceInterfaces = importedClasses.stream()
                .filter(JavaClass::isInterface)
                .filter(clazz -> clazz.getModifiers().contains(JavaModifier.PUBLIC))
                .filter(clazz -> clazz.getSimpleName().endsWith("Service"))
                .filter(clazz -> !classesThatAreException.contains(clazz.getName()))
                .toList();

        StringBuilder errorMessages = new StringBuilder();

        for (JavaClass publicServiceInterface : publicServiceInterfaces) {

            Set<JavaClass> extendedInterfaces = publicServiceInterface.getInterfaces().stream()
                    .map(JavaType::toErasure)
                    .collect(Collectors.toSet());

            if (!extendedInterfaces.isEmpty()) {
                String parentNames = extendedInterfaces.stream()
                        .map(JavaClass::getSimpleName)
                        .collect(Collectors.joining(", "));

                errorMessages.append(String.format(
                        "❌ Error in [%s]: Public service interface '%s' cannot extend other interfaces. Found parent(s): [%s]%n",
                        publicServiceInterface.getPackageName(), publicServiceInterface.getSimpleName(), parentNames
                ));
            }
        }

        if (!errorMessages.isEmpty()) {
            Assertions.fail("Architecture validation failed: Public services are forbidden from extending other interfaces:%n" + errorMessages);
        }
    }
}