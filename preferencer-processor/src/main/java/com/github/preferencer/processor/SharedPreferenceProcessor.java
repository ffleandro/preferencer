package com.github.preferencer.processor;

import com.github.preferencer.SharedPreference;
import com.github.preferencer.processor.exception.ProcessingException;
import com.github.preferencer.processor.generator.Generator;
import com.github.preferencer.processor.generator.SharedPreferenceGenerator;
import com.github.preferencer.processor.model.Preference;
import com.github.preferencer.processor.model.SharedPreferenceClass;
import com.google.auto.service.AutoService;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;


/**
 * @author raunysouza
 */
@AutoService(Processor.class)
public class SharedPreferenceProcessor extends AbstractProcessor {

    private Messager messager;
    private Generator generator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        generator = new SharedPreferenceGenerator();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(SharedPreference.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(SharedPreference.class)) {
                TypeElement typeElement = (TypeElement) element;
                if (!isValidClass(typeElement)) {
                    return true;
                }

                SharedPreferenceClass sharedPreferenceClass = new SharedPreferenceClass();
                sharedPreferenceClass.setSourceElement(typeElement);
                sharedPreferenceClass.setInterface(typeElement.getKind() == ElementKind.INTERFACE);
                SharedPreference annotation = typeElement.getAnnotation(SharedPreference.class);
                sharedPreferenceClass.setUseDefault(annotation.useDefault());
                getAllPreferences(typeElement, sharedPreferenceClass);

                if (sharedPreferenceClass.getPreferences().isEmpty()) {
                    warn(element, "Class %s is annotated with @SharedPreference but has no field", typeElement.getSimpleName());
                }

                // No errors, generate
                generator.generate(sharedPreferenceClass, processingEnv);
            }
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        } catch (Exception e) {
            error(null, e.getMessage());
        }

        return true;
    }

    private boolean isValidClass(TypeElement element) {
        // Check whether its a public class
        if (!element.getModifiers().contains(Modifier.PUBLIC)) {
            error(element, "The class %s is not public", element.getQualifiedName().toString());
            return false;
        }

        // Check whether its a abstract class
        if (!element.getModifiers().contains(Modifier.ABSTRACT)) {
            error(element, "Only Abstract class supported and class %s isn't",
                    element.getQualifiedName().toString());
            return false;
        }

        return true;
    }

    private void getAllPreferences(TypeElement typeElement, SharedPreferenceClass sharedPreferenceClass) throws ProcessingException {
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD && element.getModifiers().contains(Modifier.ABSTRACT)) {

                ExecutableElement executableElement = (ExecutableElement) element;
                String name = executableElement.getSimpleName().toString();
                if (name.startsWith("get") || name.startsWith("is")) {
                    Preference preference = new Preference();
                    preference.setMethodElement(executableElement);

                    name = name.replaceAll("(get|is)", "");
                    preference.setName(name);

                    com.github.preferencer.Preference annotation = executableElement.getAnnotation(com.github.preferencer.Preference.class);
                    if (annotation != null) {
                        preference.setDefaultValue(annotation.defaultValue());

                        if (!StringUtils.isEmpty(annotation.name())) {
                            preference.setName(annotation.name());
                        }
                    }

                    preference.setType(executableElement.getReturnType());

                    sharedPreferenceClass.addPreference(preference);
                }
            }
        }
    }

    private void error(Element element, String message, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(message, args),
                element
        );
    }

    private void warn(Element element, String message, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.WARNING,
                String.format(message, args),
                element
        );
    }
}