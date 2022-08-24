package com.dmdev.junit.extension;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ConditionalExtension implements ExecutionCondition {

    // как видим, в этом методе имеем доступ к ExtensionContext (он приходит в параметрах метода)
    // в этом методе определяем, стоит вызывать тест или нет - напр., если передаем
    // какое-нибудь проперти (оно не равно null, т.е. передали его, увидели его в нашем приложении),
    // тогда пропускаем тест - в противном случае тест выполняем - еще нужно дополнительно в идее
    // в VM options указать эту переменую/проперти skip - это делается там, где настраивали
    // подключение к БД, чтобы было у каждого на компе свое индивидуальное со своими индивид. параметрами
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {

        return System.getProperty("skip") != null
                ? ConditionEvaluationResult.disabled("test is skipped")
                : ConditionEvaluationResult.enabled("enabled by default");
    }
}
