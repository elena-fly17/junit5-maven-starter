package com.dmdev.junit;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;

// это класс по настройке запуска наших тестов
public class TestLauncher {

    public static void main(String[] args) {

        var launcher = LauncherFactory.create();

        // создаем Listener - listeners прослушивают интересующие нас события
        // (напр., есть прослушиватели для добавления опр. журналов, обработки событий UI и др.
        // листенер SummaryGeneratingListener генерирует статистику по тестам
        var summaryGeneratingListener = new SummaryGeneratingListener();

        // LauncherDiscoveryRequest исп. для создания запроса, который
        // будет отправлен в Junit TestEngine для обнаружения тестовых случаев -
        // с помощью LauncherDiscoveryRequest выполняется настройка/конфигурация
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
                // указываем, где реквест будет искать тесты - в данном случае указываем класс,
                // в котором написаны тесты - хотя есть и другие варианты селекторов -
                // в строке ниже пример с указанием класса, но эта строка закомментирована так как
                // еще ниже мы сделали селектор с указанием пакета и пока что будем пользоваться им
                // .selectors(DiscoverySelectors.selectClass(UserServiceTest.class))
                // например, вместо класса можно указать пакет, в котором лежит класс с тестами
                .selectors(DiscoverySelectors.selectPackage("com.dmdev.junit.service"))
                // указываем, что хотим запускать только тесты, помеченные тегом login
                // запускаем метод main в этом классе - отработают только тесты с тегом login
                .filters(
                        TagFilter.includeTags("login")
                )
                .build();
        // передаем в launcher для выполнения созданный нами request с настройками и listener -
        launcher.execute(request, summaryGeneratingListener);

        // выводим статистику из listener в консоль
        try (var writer = new PrintWriter(System.out)) {
            summaryGeneratingListener.getSummary().printTo(writer);
        }
    }
}
