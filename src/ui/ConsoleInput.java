package ui;

import java.util.Scanner;

/** Shared keyboard-input helpers for the console screens. */
final class ConsoleInput {
    private static final Scanner IN = new Scanner(System.in);

    private ConsoleInput() { }

    static String text(String prompt) {
        System.out.print(prompt);
        if (!IN.hasNextLine()) throw new IllegalStateException("Input closed");
        return IN.nextLine().trim();
    }

    static int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(text(prompt));
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }

    static double readDouble(String prompt, double min) {
        while (true) {
            try {
                double v = Double.parseDouble(text(prompt));
                if (v >= min) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a number of at least " + min + ".");
        }
    }

    static boolean yesNo(String prompt) {
        while (true) {
            String s = text(prompt + " (y/n): ").toLowerCase();
            if (s.equals("y")) return true;
            if (s.equals("n")) return false;
        }
    }
}
