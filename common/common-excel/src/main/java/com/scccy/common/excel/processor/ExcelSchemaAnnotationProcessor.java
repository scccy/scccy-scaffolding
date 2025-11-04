package com.scccy.common.excel.processor;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Excel Schema 注解处理器
 * <p>
 * 在编译时处理 @ExcelSchemaProperty 注解，检查类或字段是否有 @Schema 注解
 * 并提供编译时提示
 * </p>
 *
 * @author scccy
 */
@SupportedAnnotationTypes({"com.scccy.common.excel.annotation.ExcelSchemaProperty"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ExcelSchemaAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            
            for (Element element : elements) {
                if (element.getKind() == ElementKind.FIELD) {
                    VariableElement fieldElement = (VariableElement) element;
                    processField(fieldElement);
                } else if (element.getKind() == ElementKind.CLASS) {
                    TypeElement classElement = (TypeElement) element;
                    processClass(classElement);
                }
            }
        }
        return true;
    }

    private void processClass(TypeElement classElement) {
        processingEnv.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            "检测到类 " + classElement.getQualifiedName() + " 使用了 @ExcelSchemaProperty，" +
            "请确保字段上有 @Schema(description = \"...\") 注解"
        );
    }

    private void processField(VariableElement fieldElement) {
        // 检查字段是否有 @Schema 注解
        Schema schema = fieldElement.getAnnotation(Schema.class);
        if (schema == null) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "字段 " + fieldElement.getSimpleName() + " 使用了 @ExcelSchemaProperty，" +
                "但缺少 @Schema 注解，无法自动读取 description"
            );
            return;
        }

        String description = schema.description();
        if (description == null || description.isEmpty()) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.WARNING,
                "字段 " + fieldElement.getSimpleName() + " 的 @Schema 注解缺少 description 属性"
            );
        }
    }
}

