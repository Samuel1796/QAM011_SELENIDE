package org.example.util;

/**
 * Utility class responsible solely for generating screenshot file names (SRP).
 *
 * <p>The naming pattern {@code {ClassName}_{methodName}_{timestamp}.png} satisfies
 * Requirement 9.2 and makes screenshots uniquely identifiable per test execution.</p>
 */
public final class ScreenshotNamer {

    /** Prevent instantiation of this utility class. */
    private ScreenshotNamer() {}

    /**
     * Builds a screenshot file name from the test class name, method name, and
     * the current epoch timestamp in milliseconds.
     *
     * <p>Example output: {@code LoginTest_validLoginNavigatesToProductsPage_1712345678901.png}</p>
     *
     * @param className  the simple name of the test class (e.g. {@code LoginTest})
     * @param methodName the name of the test method (e.g. {@code validLoginNavigatesToProductsPage})
     * @return a filename string in the format {@code className_methodName_timestamp.png}
     */
    public static String buildName(String className, String methodName) {
        return className + "_" + methodName + "_" + System.currentTimeMillis() + ".png";
    }
}
