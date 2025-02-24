package parser;

public class SyntaxException extends RuntimeException {
    private final char unexpectedCharacter;
    private final int unexpectedCharacterIndex;

    public SyntaxException(char unexpectedCharacter, int unexpectedCharacterIndex) {
        this("Unexpected character \"" + unexpectedCharacter + "\" at " + unexpectedCharacterIndex, unexpectedCharacter, unexpectedCharacterIndex);
    }

    public SyntaxException(String message, char unexpectedCharacter, int unexpectedCharacterIndex) {
        super(message);
        this.unexpectedCharacter = unexpectedCharacter;
        this.unexpectedCharacterIndex = unexpectedCharacterIndex;
    }

    public char getUnexpectedCharacter() {
        return unexpectedCharacter;
    }

    public int getUnexpectedCharacterIndex() {
        return unexpectedCharacterIndex;
    }
}
