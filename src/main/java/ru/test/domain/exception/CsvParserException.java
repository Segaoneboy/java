package ru.test.domain.exception;

public class CsvParserException extends Exception{
    private final ErrorCode errorCode;
    private final int lineNumber;

    public CsvParserException(ErrorCode errorCode, int lineNumber, String details){
        super(details);
        this.errorCode = errorCode;
        this.lineNumber = lineNumber;
    }
    public CsvParserException(ErrorCode errorCode, String details){
        this(errorCode, 0, details);
    }
    public ErrorCode getErrorCode(){
        return errorCode;
    }
    public int getLineNumber(){
        return lineNumber;
    }

    public String getUserMsg(){
        StringBuilder sb = new StringBuilder();
        sb.append("Код ошибки: ").append(errorCode.name()).append("\n");
        sb.append("Описание: ").append(errorCode.getDefaultMessage()).append("\n");

        if(lineNumber > 0){
            sb.append("Строка в файле: ").append(lineNumber).append("\n");
        }
        if(getMessage() != null && !getMessage().isBlank()){
            sb.append("Детали: ").append(getMessage());
        }
        return sb.toString();
    }

    public void getLinenumber() {
    }
}
