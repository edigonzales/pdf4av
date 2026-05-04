package ch.so.agi.av.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;

class Pdf4AvCliTest {
    @Test
    void helpReturnsExitCodeZero() {
        int exitCode = new CommandLine(new Pdf4AvCli()).execute("--help");
        assertEquals(0, exitCode);
    }
}
