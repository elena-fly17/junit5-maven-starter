package com.dmdev.junit.extension;

import com.dmdev.junit.service.UserService;
import lombok.Getter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;

public class PostProcessingExtension implements TestInstancePostProcessor {

    // в параметрах метода было написано Object o, но мы заменили о на testInstance, т.к. этот объект,
    // который приходит в параметрах метода, и есть объект нашего класса - я так понимаю,
    // нашего тестового класса (нашего класса с тестами)
    // этот extension (TestInstancePostProcessor) используется спрингом для внедрения зависимостей
    // когда вызывается метод ниже, как раз в этом колбэке мы приходим в точку, когда только-только
    // создался объект нашего тестового класса, и в нем есть поля, в которые мы хотим заинджектить
    // зависимости
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext)
            throws Exception {

        System.out.println("post processing extension");

        // берем объект нашего тестового класса и получаем о нем всю информацию (все поля)
        var declaredFields = testInstance.getClass().getDeclaredFields();

        // далее мы можем пройтись по каждому из этих полей и выполнить проверку - например, если поле
        // нашего тестового класса содержит аннотацию геттера ломбока, то мы в это поле заинджектим
        // какое-то значение - то есть мы в поле нашего тестового класса, который только что создался для
        // выполнения тестов инджектим вновь созданный объект UserService
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Getter.class)) {
                declaredField.set(testInstance, new UserService(null));
            }
        }
    }
}
