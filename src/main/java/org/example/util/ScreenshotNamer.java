package org.example.util;

/**
 * Utility class responsible solely for generating screenshot file names (SRP).
 *
 * <p>The naming pattern {@code {ClassName}_{methodName}_{timestamp}.png} satisfies
 * the screenshot naming requirement and makes each file uniquely identifiable per
 * test execution, even when the same test is re-run multiple times.</p>
 *
 * <p>Used by {@link org.example.ScreenshotWatcher} when saving failure screenshots
 * to {@code target/screenshots/}.</p>
 */
public final class ScreenshotNamer {

    /** Prevent instantiation — this is a static utility class. */
    private ScreenshotNamer() {}

    /**
     * Builds a screenshot file name from the test class name, method name, and the
     * current epoch timestamp in milliseconds.
     *
     * <p>The timestamp component ensures uniqueness across multiple runs of the same
     * test, preventing earlier screenshots from being overwritten.</p>
     *
     * <p>Example output:
     * {@code LoginTest_validLoginNavigatesToProductsPage_1712345678901.png}</p>
     *
     * @param className  the simple name of the test class (e.g. {@code LoginTest})
     * @param methodName the name of the test method (e.g. {@code validLoginNavigatesToProductsPage})
     * @return a filename string in the format {@code className_methodName_timestamp.png}
     */
    public static String buildName(String className, String methodName) {
        return className + "_" + methodName + "_" + System.currentTimeMillis() + ".png";
    }
}
