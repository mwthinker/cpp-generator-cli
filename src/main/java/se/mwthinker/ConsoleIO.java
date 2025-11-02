package se.mwthinker;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Stream;

public class ConsoleIO implements Closeable, Flushable {
    private final Terminal terminal;
    private final LineReader lineReader;
    private final PrintWriter writer;

    private enum PromptType {
        PREFIX,
        QUESTION,
        HINT,
        COLON
    }

    private enum MessageType {
        ERROR,
        SUCCESS,
        INFO,
        WARNING
    }

    public ConsoleIO() throws IOException {
        this.terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        this.lineReader = LineReaderBuilder.builder().terminal(terminal).build();
        this.writer = terminal.writer();
    }

    public boolean askYesNoQuestion(String question) {
        String prompt = styledPrompt(PromptType.QUESTION, question, " ", "(y/N)", ": ");
        String input = lineReader.readLine(prompt);
        return input.trim().equalsIgnoreCase("y") || input.trim().equalsIgnoreCase("yes");
    }

    public String askQuestion(String question) {
        String prompt = styledPrompt(PromptType.QUESTION, question);
        return lineReader.readLine(prompt);
    }

    public String askQuestion(String question, String description) {
        String prompt = styledPrompt(PromptType.QUESTION, question, description);
        return lineReader.readLine(prompt);
    }

    public void printInfo(String message) {
        printMessage(MessageType.INFO, message);
    }

    private void printMessage(MessageType messageType, String message) {
        switch (messageType) {
            case ERROR -> writer.println(styledError(message));
            case SUCCESS -> writer.println(styledSuccess(message));
            case INFO -> writer.println(styledInfo(message));
            case WARNING -> writer.println(styledWarning(message));
        }
    }

    public void printError(String message) {
        writer.println(styledError(message));
    }

    private String styledPrompt(PromptType promptType, String message, String... descriptions) {
        AttributedStringBuilder builder = new AttributedStringBuilder();
        if (promptType == PromptType.QUESTION) {
            builder.styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold(), "? ");
            builder.styled(AttributedStyle.DEFAULT.bold(), message);

            Stream.of(descriptions)
                    .forEach(p -> builder.styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT), p));
        } else {
            throw new UnsupportedOperationException("Prompt type not supported: " + promptType);
        }
        return builder.toAnsi();
    }

    private String styledError(String message) {
        return new AttributedStringBuilder()
                .styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold(), "✗ ")
                .styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.RED), message)
                .toAnsi();
    }

    private String styledSuccess(String message) {
        return new AttributedStringBuilder()
                .styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold(), "✓ ")
                .styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN), message)
                .toAnsi();
    }

    private String styledInfo(String message) {
        return new AttributedStringBuilder()
                .styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold(), "ℹ ")
                .styled(AttributedStyle.DEFAULT, message)
                .toAnsi();
    }

    private String styledWarning(String message) {
        return new AttributedStringBuilder()
                .styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW).bold(), "⚠ ")
                .styled(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW), message)
                .toAnsi();
    }

    @Override
    public void close() throws IOException {
        terminal.close();
    }

    @Override
    public void flush() {
        terminal.flush();
    }

}
