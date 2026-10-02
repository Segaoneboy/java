package ru.test.domain.exception;

public enum ErrorCode {
    FILE_NOT_FOUND("Файл не найден"),
    FILE_READ_ERROR("Ошибка чтения файла"),
    WRONG_FIELD_COUNT("Неверное количество колонок в строке"),
    BAD_NUMBER("Ошибка формата числа (возраст, ID, и т.д)"),
    INVALID_HEADER("Неверный формат заголовка CSV файла"),
    UNKNOWN_ERROR("Неизвестная ошибка");

    private final String defaultMessage;

    ErrorCode(String defaultMessage){
        this.defaultMessage = defaultMessage;
    }
    public String getDefaultMessage(){
        return defaultMessage;
    }
}
