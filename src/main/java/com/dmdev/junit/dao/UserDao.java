package com.dmdev.junit.dao;

import lombok.SneakyThrows;

import java.sql.DriverManager;

public class UserDao {

    // @SneakyThrows исп. для бросания проверяемых исключений без их объявления в throws метода.
    @SneakyThrows
    public boolean delete(Integer userId) {
        // наша задача во время тестов проверить, вызывается этот метод или нет - поэтому здесь
        // пишем не обращение к реальн. БД, а аналогию - получим ли мы исключение, т.к. у нас
        // нет такой БД или не получим?..
        try (var connection = DriverManager
                        .getConnection("url", "username", "password")) {
            return true;
        }
    }
}
