package com.dmdev.junit.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.io.IOException;

public class ThrowableExtension implements TestExecutionExceptionHandler {

    // в этом методе имеем доступ к ExtensionContext (он приходит в параметрах метода)
    // это позволяет получить много различ. инфы, касающейся выполняемого теста -
    // можно перейти в ExtensionContext и см., какие методы он предоставляет для получения этой инфы
    // в данном примере мы смотрим, какое исключение получили - если оно является IOException, то мы
    // пробрасываем его дальше - в противном случае ничего не делаем - то есть получается, что если мы
    // встречаем, к примеру, RuntimeException, то мы типа говорим, что все в порядке, наш тест не упал,
    // продолжаем дальше его выполнение - и интересует нас только IOException
    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable)
            throws Throwable {

        if (throwable instanceof IOException) {
            throw throwable;
        }
    }
}
