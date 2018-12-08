package io.github.Cruisoring.helpers;

import org.testng.annotations.Test;

public class LoggerTest {

    @Test
    public void displayColors() throws Exception {

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int number = i*16 + j;
                String code = String.valueOf(number);
                String digit = String.format("%04d", number);
                String codeWithColor = String.format("\033[%sm %s%s", code, digit, "\u001B[0m");
                System.out.print(codeWithColor);
            }
            System.out.println();
        }

        Logger.V("Verbose");
        Logger.D("Debug");
        Logger.I("Info");
        Logger.W("Warning");
        Logger.E("Error");
    }
}