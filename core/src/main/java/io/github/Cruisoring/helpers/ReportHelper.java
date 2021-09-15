package io.github.Cruisoring.helpers;

import java.util.Arrays;
import java.util.Objects;

public class ReportHelper {
    public final static String PercentageAscii = "&#37";
    public static final String[] failuresIndicators = new String[] {"fail", "exception", "wrong", "error"};
    public static final String[] successIndicators = new String[] {"success", "succeed", "pass", "accepted"};
    public static final String TICKED = "<font size=4 color='green'><b>&#10004;</b></font>";
    public static final String CROSSED = "<font size=4 color='red'><b>&#10008;</b></font>";
//    private static final Logger log = Logger.getLogger(ReportHelper.class);

//    /**
//     * Helper method to save the current screenshot as embedded image of the test report.
//     * @param format   Descriptive message of the sceenshot.
//     * @param args      Arguments to compose the log.
//     */
//    public static void saveScreenShot(String format, Object... args) {
//        String imageAsMessage = null;
//        try {
//            WebDriver driver = TestHelper.getDriver();
//            String base64Image = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
//            String title = tryFormatString(format, args);
//            String message = String.format("<div><p>%s</p><img src='data:image/png;base64, %s'/></div>", title, base64Image);
//            Reporter.addStepLog(message);
//        }catch (Exception ex){
//            System.out.println(tryFormatString("Failed to save image as text: %s", imageAsMessage));
//        }
//    }

    /**
     * Compose log with given format and arguments and save it as step log of the test report.
     * @param format    Format of the log, or simply the whole message if no arguments provided.
     * @param args      Arguments to compose the log.
     */
    public static void reportAsStepLog(String format, Object... args){
        try {
            if(format.contains("img")){
                Logger.D(tryFormatString(format, args));
            } else if (args.length == 0){
                System.out.println(format);
                Logger.D(format);
            } else {
                System.out.println(args.length == 0 ? format : tryFormatString(format, args));

                //Check if the format contains some pre-formatted HTML tags/attributes
                if (format.matches("([^<]*<[^<|%]*%\\w[^<|>]*>.*)|(.*(?:'|\")%\\w(?:'|\").*)|(^<.*>$)")) {
                    Logger.D(tryFormatString(format, args));
                } else {
                    String color = "blue";
                    if (StringHelper.containsAnyIgnoreCase(format, failuresIndicators))
                        color = "red";
                    else if (StringHelper.containsAnyIgnoreCase(format, successIndicators))
                        color = "green";

                    String reportFormat = format.replaceAll("(%\\w)",
                            String.format("<font color=%s><b>$1</b></font>", color));
                    Logger.D(tryFormatString(reportFormat, args));
                }
            }
        }catch (Exception ex){}
    }

    /**
     * Compose log with given format and arguments and save it as normal log4j of the test report.
     * @param format    Format of the log, or simply the whole message if no arguments provided.
     * @param args      Arguments to compose the log.
     */
    public static void log(String format, Object... args){
        System.out.println(args.length == 0 ? format : tryFormatString(format, args));
    }

    /**
     * Try to call String.format() and refrain potential IllegalFormatException
     * @param format    template to compose a string with given arguments
     * @param args      arguments to be applied to the above template
     * @return          string formatted with the given or exceptional template.
     */
    public static String tryFormatString(String format, Object... args) {
        Objects.requireNonNull(format);
        try {
            String formatted = String.format(format, args);
            formatted = formatted.replaceAll(PercentageAscii, "%");
            return formatted;
        } catch (Exception e) {
            String[] argStrings = Arrays.stream(args).map(arg -> arg.toString()).toArray(size -> new String[size]);
            return String.format("MalFormated format: '%s'\nargs: '%s'", format, String.join(", ", argStrings));
        }
    }
}
