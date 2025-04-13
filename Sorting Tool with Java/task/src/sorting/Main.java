package sorting;

import java.io.*; // Import necessary I/O classes
import java.util.*;

public class Main {
    // Default values (can be overridden by command-line args)
    private static String dataType = "word";
    private static String sortingType = "natural";
    private static String inputFilePath = null; // Default to null, indicating standard input
    private static String outputFilePath = null; // Default to null, indicating standard output

    public static void main(final String[] args) {

        // --- Argument Parsing ---
        List<String> invalidArgs = new ArrayList<>(); // To store unrecognized arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-sortingType":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        sortingType = args[i + 1];
                        i++; // Skip the next argument as it's the value
                    } else {
                        System.err.println("No sorting type defined!"); // Error to console
                        return; // Exit if argument is missing value
                    }
                    break;
                case "-dataType":
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        dataType = args[i + 1];
                        i++; // Skip the next argument
                    } else {
                        System.err.println("No data type defined!"); // Error to console
                        return; // Exit
                    }
                    break;
                case "-inputFile": // New argument
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        inputFilePath = args[i + 1];
                        i++; // Skip the next argument
                    } else {
                        System.err.println("No input file defined!"); // Error to console
                        return; // Exit
                    }
                    break;
                case "-outputFile": // New argument
                    if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                        outputFilePath = args[i + 1];
                        i++; // Skip the next argument
                    } else {
                        System.err.println("No output file defined!"); // Error to console
                        return; // Exit
                    }
                    break;
                default:
                    // Collect unrecognized arguments
                    invalidArgs.add(args[i]);
                    break;
            }
        }

        // Report any invalid arguments found
        if (!invalidArgs.isEmpty()) {
            for (String arg : invalidArgs) {
                System.err.println("\"" + arg + "\" is not a valid parameter. It will be skipped."); // Error to console
            }
        }

        // --- Input Source Setup ---
        Scanner scanner = null;
        try {
            if (inputFilePath != null) {
                // Read from the specified file
                scanner = new Scanner(new File(inputFilePath));
            } else {
                // Read from standard input (console)
                scanner = new Scanner(System.in);
            }

            // --- Output Destination Setup ---
            PrintStream originalOut = System.out; // Keep original console output stream
            PrintStream outputStream = System.out; // Default to console output

            try {
                if (outputFilePath != null) {
                    // Create a PrintStream for the output file
                    outputStream = new PrintStream(new FileOutputStream(outputFilePath));
                    // Redirect standard output to the file stream
                    System.setOut(outputStream);
                }

                // --- Processing ---
                // Process based on data type using the configured scanner
                switch (dataType.toLowerCase()) {
                    case "long":
                        processLongs(sortingType, scanner);
                        break;
                    case "line":
                        processLines(sortingType, scanner);
                        break;
                    case "word":
                        processWords(sortingType, scanner);
                        break;
                    default:
                        // Use System.err for errors so they always go to console
                        System.err.println("Error: Invalid data type specified (" + dataType + "). Use 'long', 'line', or 'word'.");
                        break;
                }

            } catch (FileNotFoundException e) {
                // Handle file not found for output file
                System.err.println("Error creating or writing to output file '" + outputFilePath + "': " + e.getMessage());
            } finally {
                // --- Cleanup Output ---
                if (outputStream != System.out) {
                    outputStream.close(); // Close the file stream
                }
                // Restore original standard output ONLY if it was changed
                if (System.out != originalOut) {
                    System.setOut(originalOut);
                }
                // Optional: Confirm completion on console even if output went to file
                if (outputFilePath != null) {
                    System.out.println("Processing complete. Results written to " + outputFilePath);
                }
            }

        } catch (FileNotFoundException e) {
            // Handle file not found for input file
            System.err.println("Error reading input file '" + inputFilePath + "': " + e.getMessage());
        } finally {
            // --- Cleanup Input ---
            if (scanner != null) {
                scanner.close(); // Close the scanner (file or System.in)
            }
        }
    }

    // --- Processing Methods (Modified for Error Output) ---
    // Note: These methods now use System.out for results (which might be redirected to a file)
    // and System.err for errors/warnings (which always go to the console).

    private static void processLongs(String sortingType, Scanner scanner) {
        List<Long> numbers = new ArrayList<>();
        Map<Long, Integer> frequencyMap = new HashMap<>();
        long totalCount = 0;

        while (scanner.hasNext()) {
            if (scanner.hasNextLong()) {
                long number = scanner.nextLong();
                numbers.add(number);
                frequencyMap.put(number, frequencyMap.getOrDefault(number, 0) + 1);
                totalCount++;
            } else {
                // Consume the non-long token
                String invalidInput = scanner.next();
                // Print skipped input message to standard error (console)
                System.err.println("\"" + invalidInput + "\" is not a long. It will be skipped.");
            }
        }
        // Scanner is closed in main's finally block

        if (numbers.isEmpty()) {
            // Use System.err for informational messages that aren't primary results
            System.err.println("No valid numbers were found in the input.");
            return;
        }

        // Print total count to standard output (file or console)
        System.out.println("Total numbers: " + totalCount + ".");

        if ("natural".equalsIgnoreCase(sortingType)) {
            Collections.sort(numbers);
            System.out.print("Sorted data: "); // To standard output
            for (int i = 0; i < numbers.size(); i++) {
                System.out.print(numbers.get(i) + (i == numbers.size() - 1 ? "" : " "));
            }
            System.out.println(); // Newline

        } else if ("byCount".equalsIgnoreCase(sortingType)) {
            long finalTotalCount = totalCount; // Need final variable for lambda
            frequencyMap.entrySet().stream()
                    .sorted(Map.Entry.<Long, Integer>comparingByValue()
                            .thenComparing(Map.Entry.comparingByKey()))
                    // Print frequency results to standard output
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() + " time(s), " +
                            String.format("%.0f%%", (double) entry.getValue() * 100 / finalTotalCount)));
        } else {
            // Print invalid sorting type message to standard error
            System.err.println("Invalid sorting type for long: " + sortingType + ". Use 'natural' or 'byCount'.");
        }
    }

    private static void processWords(String sortingType, Scanner scanner) {
        List<String> words = new ArrayList<>();
        Map<String, Integer> frequencyMap = new HashMap<>();
        long totalCount = 0;

        while (scanner.hasNext()) { // Reads word by word (delimited by whitespace)
            String word = scanner.next();
            words.add(word);
            frequencyMap.put(word, frequencyMap.getOrDefault(word, 0) + 1);
            totalCount++;
        }
        // Scanner is closed in main's finally block

        if (words.isEmpty()) {
            System.err.println("No words were found in the input.");
            return;
        }

        System.out.println("Total words: " + totalCount + "."); // To standard output

        if ("natural".equalsIgnoreCase(sortingType)) {
            Collections.sort(words);
            System.out.print("Sorted data: "); // To standard output
            for (int i = 0; i < words.size(); i++) {
                System.out.print(words.get(i) + (i == words.size() - 1 ? "" : " "));
            }
            System.out.println(); // Newline

        } else if ("byCount".equalsIgnoreCase(sortingType)) {
            long finalTotalCount = totalCount;
            frequencyMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue()
                            .thenComparing(Map.Entry.comparingByKey()))
                    // Print frequency results to standard output
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() + " time(s), " +
                            String.format("%.0f%%", (double) entry.getValue() * 100 / finalTotalCount)));
        } else {
            // Print invalid sorting type message to standard error
            System.err.println("Invalid sorting type for word: " + sortingType + ". Use 'natural' or 'byCount'.");
        }
    }

    private static void processLines(String sortingType, Scanner scanner) {
        List<String> lines = new ArrayList<>();
        Map<String, Integer> frequencyMap = new HashMap<>();
        long totalCount = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lines.add(line);
            frequencyMap.put(line, frequencyMap.getOrDefault(line, 0) + 1);
            totalCount++;
        }

        if (lines.isEmpty()) {
            System.err.println("No lines were found in the input.");
            return;
        }

        System.out.println("Total lines: " + totalCount + "."); // To standard output

        if ("natural".equalsIgnoreCase(sortingType)) {
            Collections.sort(lines);
            System.out.println("Sorted data:"); // To standard output
            for (String line : lines) {
                System.out.println(line);
            }

        } else if ("byCount".equalsIgnoreCase(sortingType)) {
            long finalTotalCount = totalCount;
            frequencyMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue()
                            .thenComparing(Map.Entry.comparingByKey()))
                    // Print frequency results to standard output
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() + " time(s), " +
                            String.format("%.0f%%", (double) entry.getValue() * 100 / finalTotalCount)));
        } else {
            // Print invalid sorting type message to standard error
            System.err.println("Invalid sorting type for line: " + sortingType + ". Use 'natural' or 'byCount'.");
        }
    }
}
