package com.dmdev.junit.extension;

import com.dmdev.junit.dao.UserDao;
import com.dmdev.junit.service.UserService;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

// если хотим внедрить какую-то зависимость не просто через конструктор в классе (как показано в
// классе UserServiceTest, там есть такой конструктор), а передать/внедрить нужную зависимость прямо в
// тестовый метод, для этого создаем такой класс, как ниже - а затем в нужном нам тестовом методе
// попросим Junit дать нам нужную зависимость, передав ее в параметры метода - в нашем примере это метод
// prepare в классе UserServiceTest
public class UserServiceParamResolver implements ParameterResolver {

    // ParameterContext - это объект, который говорит нам всю информацию о параметре, который мы
    // в последующем либо успешно заинджекттим, вернув true, либо нет, вернув false
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        // строка ниже означает, что если параметр равен UserService.class, то это отличный вариант
        // для того, чтобы мы заинджектили ему свой (мы это делаем в методе ниже)
        return parameterContext.getParameter().getType() == UserService.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        // мы можем закешировать UserService - для этого есть готов. инструмент - Store
        // NameSpace - это по сути ключ для нашего Store - можно Store воспринимать как HashMap
        // кодом ниже говорим, что хотим, чтобы для методов, требующих UserService, возвращался
        // всегда один и тот же Store

        // ЗАКОММЕНТИРОВАННЫЙ КОД
//        var store = extensionContext
//                .getStore(ExtensionContext.Namespace.create(UserService.class));

        // получаем у Store по ключу значение - в данном случае ключом является UserService.class,
        // а значением - запуск механизма, который вернет нам UserService
        // кажд. раз будет возвращаться один и тот же самый UserService - мы его закешировали

        // ЗАКОММЕНТИРОВАННЫЙ КОД
        // return store.getOrComputeIfAbsent(UserService.class, it -> new UserService());

        // также мы можем сделать операции не на классе UserService в целом, а на методе -
        // и тогда для каждого метода создавался заново бы UserService - вариант кода для этого:
        var store = extensionContext
                .getStore(ExtensionContext.Namespace.create(extensionContext.getTestMethod()));
        return store.getOrComputeIfAbsent(UserService.class, it -> new UserService(new UserDao()));
        // кроме того, нужно попросить UserServoce для какого-нибудь метода -
        // сделали это в классе UserServiceTest - в методе usersEmptyIfNoUserAdded
        // (передав UserService в параметры метода)
    }
}
