package com.dmdev.junit.service;

import com.dmdev.junit.TestBase;
import com.dmdev.junit.dao.UserDao;
import com.dmdev.junit.dto.User;
import com.dmdev.junit.extension.ConditionalExtension;
import com.dmdev.junit.extension.GlobalExtension;
import com.dmdev.junit.extension.PostProcessingExtension;
import com.dmdev.junit.extension.ThrowableExtension;
import com.dmdev.junit.extension.UserServiceParamResolver;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

// выполнен статический импорт
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

// эта аннотация означает, что будут запущены только unit-тесты - т.е. тесты отдельных методов
@Tag("fast")
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// эта аннотация позволяет нам задать порядок выполнения наших тестов - на практике злоупотреблять ею не
// стоит и следует писать тесты так, чтобы их результат не зависел от порядка их выполнения - но тем не
// менее, если уже очень необходимо, то порядок их выполнения можно задать
@TestMethodOrder(MethodOrderer.DisplayName.class)
// аннотация исп. для передачи в нее всех классов, реализующих и-с Extension, а параметр Resolver,
// который мы исп. для внедрения зависимости внутрь метода prepare как раз исп. для настройки
// внедрения этой зависимости и реализует и-с Extension, поэтому указываем класс, который мы
// использовали для этого
// то есть с помощью этой аннотации мы добавляем какой-то функционал к нашему тесту, какое-то поведение
// (напр., можно внедрить в метод какую-то зависимость)
// вообще же extension (экстеншены, расширения) в Junit позволяют нам добавлять и настраивать какой-то
// свой функционал - модель расширения является частью модуля Jupiter, который позволяет вам расширять
// основные функции JUnit 5 с помощью гибких и мощных расширений
@ExtendWith({
        UserServiceParamResolver.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        MockitoExtension.class
        // ThrowableExtension.class
        // GlobalExtension.class
})
public class UserServiceTest extends TestBase {

    // это константы
    // константам можно присвоить значение только 1 раз
    // Константа объявляется также, как и переменная, но вначале идет ключевое слово final
    // Как правило, константы имеют имена в верхнем регистре
    // Константы позволяют задать переменные, которые не долж. больше изменяться.
    // Напр., если у нас есть переменная для хранения числа pi, то можем объявить ее константой,
    // т.к. ее з-е постоянно.
    // создали эти константы, чтобы было удобно переиспользовать этих юзеров
    // в разных тестах, а не создавать их каждый раз в тестовом методе заново
    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

    @Captor
    private ArgumentCaptor<Integer> argumentCaptor;

    // обозначаем таким образом, что userDao это мок
    // lenient = true - это означает, что мы хотим, чтобы Junit 5 не выбрасывал нам исключения и
    // предупреждения, если мы используем в методе с аннотацией @BeforeEach какие-то stub-ы, которые
    // не используются во всех методах нашего тестового класса - а ситуация, когда они не используются
    // происходит тогда, когда мы пишем в методе с аннотацией @BeforeEach какой-нибудь stub, а потом
    // в каком-нибудь тестовом методе нашего тестового класса пишем еще какой-нибудь stub - например, у нас
    // есть такой stub в методе throwExceptionIfDatabaseIsNotAvailable - в строке ниже lenitnt будет
    // относиться ко всем stabum мока userDao
    @Mock(lenient = true)
    private UserDao userDao;

    // эту ан-ю ставим над классами, которые хотим тестировать и в которые хотим заинджектить моки
    @InjectMocks
    private UserService userService;

    // пример внедрения зависимостей Junit 5 (тут немного др. механизм, это не Spring,
    // хотя если проект на спринг, то можно исп. его механизмы - ну, а если нет,
    // то для внедрения зависимостей можно исп. Junit)
    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    @BeforeAll
    void init() {
        System.out.println("Before all: " + this);
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each: " + this);
        // настройка lenient не для всех stub-od мока userDao, а только для одного конкретного
        // внимание: здесь исп. не очень хорош. вариант стаба, т.к. при нем идет реальный вызов
        // метода delete у userDao - и т.к. пока нет реальн. подключения к реальной БД, тест упадет -
        // но для lenient для конкр. стаба возможен только такой вариант
        lenient().when(userDao.delete(IVAN.getId())).thenReturn(true);

        // это условие действительно для всех тестов - т.к. оно прописано в методе с ан-й @BeforeEach
        // что оно означает: просим вернуть true, когда мы у userDao вызовем метод delete и передадим туда
        // id - имей в виду, что в этой строке вызов delete не происходит - а просто настраивается то,
        // что будет происх. при вызове
        // строка ниже - это мы написали stub
        doReturn(true).when(userDao).delete(IVAN.getId());
        // метод mock или proxy создает прокси-объект с таким же классом, который мы передали в него
        // в кач-ве параметра - далее этот прокси-объект можем исп. в UserService (передать туда)
        // в дан. случае userDao это мок-объект (ну, или spy) - тут сейчас вызван метод spy, но
        // вместо него может быть и метод mock
        // тут мы сначала создали мок-объект (userDao), потом заинджектили его в userService
//        this.userDao = Mockito.spy(new UserDao());
//        this.userService = new UserService(userDao);

        // до того, как внедрили эту зависимость через механизм Junit 5
        // для внедрения зависимостей (класс UserServiceParamResolver) она
        // была внедрена так, как в строке ниже - но теперь вместо этой строки
        // можем писать так, как выше
        // userService = new UserService(); // инициализируем поле userService
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable() {
        // метод doThrow пробросит исключение, когда у мока userDao вызовется метод delete с id Ивана
        // строка ниже - это мы написали stub
        doThrow(RuntimeException.class).when(userDao).delete(IVAN.getId());
        // если БД недоступна, то ожидаем получить исключение при вызове delete
        assertThrows(RuntimeException.class, () -> userService.delete(IVAN.getId()));
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
        // мы просим вернуть true, когда мы у userDao вызовем метод delete и передадим туда id
        // имей в виду, что в этой строке вызов delete не происходит - а просто настраивается то,
        // что будет происходить при вызове
        // Mockito.doReturn(true).when(userDao).delete(IVAN.getId());

        // а если нас не интересует конкретный id, то можем написать вот так:
        // Mockito.doReturn(true).when(userDao).delete(Mockito.any());

        // здесь идет реальный вызов метода delete и вылетает исключение, потому что у нас нет реальной
        // базы данных и реального подключения к ней - то есть тест падает
//        Mockito.when(userDao.delete(IVAN.getId()))
//                .thenReturn(true)
//                .thenReturn(false);

        // примеры для BDD
        // BDDMockito.given(userDao.delete(IVAN.getId())).willReturn(true);
        // либо 2 вариант:
        // BDDMockito.willReturn(true).given(userDao).delete(IVAN.getId());

        var deleteResult = userService.delete(IVAN.getId());
        System.out.println(userService.delete(IVAN.getId()));
        System.out.println(userService.delete(IVAN.getId()));

        // зманили эту строку на поле argumentCaptor с аннотацией @Captor в начале класса
        // var argumentCuptor = ArgumentCaptor.forClass(Integer.class);

        // в метод verify передаем мок - так мы проверяем, сколько раз вызвался метод у опр. мока
        // выше в методе prepare написано почему userDao является моком
        Mockito.verify(userDao, Mockito.times(3)).delete(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(IVAN.getId());

        assertThat(deleteResult).isTrue();
    }

    @Test
    // эта аннотация позволяет указывать, в каком порядке будут выполняться тесты -
    // в данном случае этот тест выполнится первым, а следующий тест ниже - вторым
    @Order(1)
    // с помощью этой аннотации настраиваем отображение тестовых методов,
    // чтобы они выполнялись в нужном порядке
    // можем передать в аннотацию люб. строку, чтобы улучшить отображение названий методов
    // и если мы пометим методы такими аннотациями, то методы будут вызываться
    // в алфавитном порядке относительно тех строк, которые мы передадим в аннотации
    @DisplayName("users will be empty if no user added")
    void usersEmptyIfNoUserAdded(UserService userService) throws IOException {
        if (true) {
            throw new RuntimeException();
        }
        System.out.println("Test 1: " + this);
        var users= userService.getAll();
        Assertions.assertTrue(users.isEmpty(), () -> "User list should be empty");
    }

    @Test
    @Order(2)
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETR);
        var users = userService.getAll();
        assertThat(users).hasSize(2);
    }

    // этот метод написан с использованием библиотеки AssertJ - ниже пример
    // этого же метода, но с использованием библиотеки Hamcrest
//    @Test
//    void usersConvertedToMapById() {
//        userService.add(IVAN, PETR);
//        Map<Integer, User> users = userService.getAllConvertedById();
//        // нижеприведенный метод assertAll бросает исключение, его можно исп. для реализации
//        // любого блока кода, потенциально вызывающего ошибку
//        // инфа: https://junit.org/junit5/docs/5.0.3/api/org/junit/jupiter/api/function/Executable.html
//        // благодаря этому методу, все лямбда-выражения, переданные в него, вызовутся, а без него
//        // если бы при выполнении первого лямбда-выражения возникла ошибка, то до выполнения второго
//        // дело бы не дошло - но благодаря использованию этого метода, будут проверены оба выражения
//        assertAll(
//                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
//                () -> assertThat(users).containsValues(IVAN, PETR)
//        );
//    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETR);
        Map<Integer, User> users = userService.getAllConvertedById();

        // эта строка - это пример использования библиотеки Hamcrest
        MatcherAssert.assertThat(users, IsMapContaining.hasKey(IVAN.getId()));

        // это старый вариант кода - но его следовало бы удалить, а в видео не удалили
        // поэтому весь этот метод будет работать некорректно
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After each: " + this);
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After all: " + this);
    }

    // чтобы улучшить читабельность наших тестов и как-то их разделить, мы можем использовать так
    // называемые nested классы - например, у нас много тестов, которые связаны с логином - мы можем
    // создать внутренний класс специально для этих методов и перенести их туда
    // и теперь мы можем убрать (я просто закомментирую) аннотацию @Tag("login") над каждым из них и
    // поставить ее один раз над классом - и также этот класс необходимо пометить аннотацией @Nested
    // такой подход очень удобен для разграничения функционала
    @Nested
    @DisplayName("test user login functionality")
    @Tag("login")
    class LoginTest{

        @Test
        @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
        void checkLoginFunctionalityPerformance() {
            System.out.println(Thread.currentThread().getName());
            // первым параметром в строке ниже передаем таймаут,
            // вторым exetuble (вызов логин-функциональности)
            var result = assertTimeoutPreemptively(Duration.ofMillis(200L), () -> {
                System.out.println(Thread.currentThread().getName());
                // строка ниже приостанавливает выполнение потока на заданное время -
                // поэтому если запустим метод вместе с ней, он провалится,
                // т.к. время его выполнения будет больше, чем 2 с (т.к. мы приостановили поток
                // на 3 с) - а без этой строки ниже тест пройдет
                // Thread.sleep(300L);
                return userService.login("dummy", IVAN.getPassword());
            });
        }

        // @Test
        // @Tag("login")
        @RepeatedTest(value = 5, name = RepeatedTest.LONG_DISPLAY_NAME)
        void loginSuccessIfUserExists(RepetitionInfo repetitionInfo) {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());
            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
        }

        // этот метод проверяет, а выбросит ли метод исключение, если передадим null юзернейм и null пароль
        // (выброс исключения и будет правильным и ожидаемым поведением для метода)
        @Test
        // эта аннотация позволяет нам отфильтровывать наши тесты по некоторым тегам
        // например, мы помечаем тегом login наши методы, которые относятся к логину
        // и тогда мы можем запускать только те тесты, которые помечены определенным тегом
        // @Tag("login")
        // эту аннотацию также часто используют для flaky-тестов, она ставится вместо аннотации @Test -
        // в параметрах аннотации указываем, сколько раз мы хотим запустить тест, и даем нашему методу
        // какое-то уникальное название, чтобы было понятно, что это такое
        void throwExceptionIfUsernameOrPasswordIsNull() {
            // проверка одного параметра метода на предмет исключения - проверяем на null юзернейм
//        try {
//            userService.login(null, "dummy");
//            fail("login should throw exception on null username");
//        } catch (IllegalArgumentException ex) {
//            assertTrue(true);
//        }

            // но можно написать и по-другому - более кратко:
//            assertThrows(IllegalArgumentException.class,
//                    () -> userService.login(null, "dummy"));

            // еще можно проверить сразу несколько параметров метода - напр., проверяем
            // 2 варианта: когда в метод передается null вместо юзернейма, и когда передается
            // null вместо пароля
//        assertAll(
//                () -> assertThrows(IllegalArgumentException.class,
//                        () -> userService.login(null, "dummy")),
//                () -> assertThrows(IllegalArgumentException.class,
//                        () -> userService.login("dummy", null))
//        );

            // более того, т.к. ф-я assertThrows возвращает exception, то мы можем работат с ним дальше -
            // например, проверить, какое сообщение он содержит - например, если работаем сразу
            // с несколькими исключениями, можем настроить для каждого нужное сообщение
            // (настройка сообщения выполнена в классе UserService - в классе, работу которого проверяем)
            // и потом в методе-тесте проверить, какое сообщение содержит возвращенное нам исключение
            // и таким образом понять, действительно ли было выброшено нужное исключение - вот как это
            // может выглядеть:
            assertAll(
                    () -> {
                        // записываем в переменную полученное в рез-те выполнения assertThrows исключение
                        var exception = assertThrows(IllegalArgumentException.class,
                                () -> userService.login(null, "dummy"));
                        // проверяем, а равно ли сообщение в этом исключении тому сообщению
                        // в том исключении, которое нам нужно, чтобы было здесь выброшено
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> userService.login("dummy", null))
            );
        }

        @Test
        // @Tag("login")
        // ан-я позволяет в след. раз не запускать тест, исп. для flaky-тестов - и оставляем коммент -
        // напр., в комменте к ан-и ниже сообщаем, что это нестаб. тест, который нужно посмотреть
        @Disabled("flaky, need to see")
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);
            var maybeUser = userService.login(IVAN.getUsername(), "dummy");
            Assertions.assertTrue(maybeUser.isEmpty());
        }

        @Test
        // @Tag("login")
        void loginFailIfUserDoesNotExist() {
            userService.add(IVAN);
            var maybeUser = userService.login("dummy", IVAN.getPassword());
            Assertions.assertTrue(maybeUser.isEmpty());
        }

        // эта аннотация означает, что мы пишем параметризованный тест
        @ParameterizedTest(name = "{arguments} test")
        // @ArgumentsSource()
        // аннотации, приведенные ниже, являются как бы разновидностями/подвидами/что-то в этом роде
        // для аннотации, которая написана выше
        // @NullSource
        // @EmptySource
        // объединяет в себе две предыдущие
        // @NullAndEmptySource
//        @ValueSource(strings = {
//                "Ivan", "Petr"
//        })
        // @EnumSource
        // в аннотацию ниже передаем название метода, принимающего все стримы аргументов -
        // создаем этот метод ниже - он долж. быть статич. - метод этот выносим из вложен. класса
        // во внеш. класс, т.к. он статический, а во влож. классах не может быть статич. методов
        // касательно названия метода, переданного в аннотацию: часть до решетки означает
        // путь к этому классу, часть после решетки - название метода - пришлось прибегнуть
        // к этому способу написания, т.к. мы вынесли этот созданный нами метод за пределы влож. класса,
        // поэтому он перестал быть виден для этой ан-и, а ан-я перестала быть видна для него
        @MethodSource("com.dmdev.junit.service.UserServiceTest#getArgumentsForLoginTest")
        // для использования этой ан-и создаем в тестовой директории папку resources, в ней файл
        // @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
//        @CsvSource({
//                "Ivan, 123",
//                "Petr, 111"
//        })
        // пишем тест, которым закрываем сразу множество тест-кейсов - в примере ниже проверим
        // логин-функциональность большинством тест-кейсов - т.е. когда и существующий юзер,
        // и несуществующий, и проблема с юзернеймом, а пароль правильный, и наоборот
        // в метод ниже передаем username и password - откуда они берутся? - мы просто просим Junit 5
        // предоставить их - он сделает это через свой механизм внедрения зависимостей
        // в параметрах метода ниже user - это наш ожидаемый юзер
        // в строке ниже мы переопределяем название именно нашего login теста параметризованного - то есть
        // то название, которое будет отображаться при запуске тестов внизу в левом углу экрана
        @DisplayName("login param test")
        void loginParameterizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, PETR);
            // чтобы подставить юзернейм и пароль, мы долж. предоставить какой-то класс или
            // функциональность, которая предоставит эти з-я - сделаем это с помощью
            // Junit-механизма внедрения зависимостей - для этого доб. ан-ю @ArgumentSource -
            // но тогда нам нужно будет написать свой ArgumentProveder - но мы будем использовать уже
            // существующие аргумент-провайдеры, поэтому аннотацию @ArgumentSource() закомментируем,
            // и вместо нее напишем аннотацию @NullSource, которая реализует NullArgumentProvider,
            // и еще 2 другие аннотации после нее, которые тоже реализуют соответствующие
            // аргумент-провайдеры
            var maybeUser = userService.login(username, password);
            // сравниваем юзера, полученного по юзернейму и паролю, с юзером, приходящим в метод
            assertThat(maybeUser).isEqualTo(user);
        }
    }
    static Stream<Arguments> getArgumentsForLoginTest() {
        // покрываем сразу 4 тестовых случая, которые выше во внеш. классе описаны с помощью
        // отдельных методов
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)), // передаем в метод правильн. юзернейм, пароль, и существующего юзера
                Arguments.of("Petr", "111", Optional.of(PETR)), // то же самое, что и выше, только для другого юзера
                Arguments.of("Petr", "dummy", Optional.empty()), // передан неправильный пароль
                Arguments.of("dummy", "123", Optional.empty()) // передан неправильный юзернейм
        );
    }
}
