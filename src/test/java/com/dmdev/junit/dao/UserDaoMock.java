package com.dmdev.junit.dao;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Answer1;

import java.util.HashMap;
import java.util.Map;

// т.к. класс UserDaoMock наследуется от UserDao, то можно его исп. везде, где нужен UserDao
// но классы, от которых наследуем Mock-классы (в примере это класс UserDao) не долж. быть final,
// иначе получим исключение - то есть Mockito играет роль некоей обертки, прокси
public class UserDaoMock extends UserDao {

    private Map<Integer, Boolean> answers = new HashMap<>();
    // private Answer1<Integer, Boolean> answer1;

    @Override
    public boolean delete(Integer userId) {
        return answers.getOrDefault(userId, false);
    }
}
