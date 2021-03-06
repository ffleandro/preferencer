package com.github.preferencer.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;

/**
 * @author raunysouza
 */
public class CodeGeneration {
    private CodeGeneration() {
    }

    public static void addGeneratedAnnotation(ProcessingEnvironment processingEnvironment, TypeSpec.Builder builder) {
        Elements elements = processingEnvironment.getElementUtils();
        if (elements.getTypeElement(Generated.class.getCanonicalName()) != null) {
            builder.addAnnotation(AnnotationSpec.builder(Generated.class)
                    .addMember("value", "$S", SharedPreferenceProcessor.class.getCanonicalName()).build());
        }
    }

    public static void writeType(ProcessingEnvironment processingEnvironment, String packageName, TypeSpec typeSpec) throws IOException {
        JavaFile file = JavaFile.builder(packageName, typeSpec)
                .addFileComment("Generated file do not edit, generated by " +
                        SharedPreferenceProcessor.class.getCanonicalName())
                .indent("    ")
                .build();
        file.writeTo(processingEnvironment.getFiler());
    }
}
