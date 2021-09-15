package realSelf;

import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

import java.util.regex.Pattern;

public class BriefWidget extends UIObject {

    public static Doctor getDoctorBrief(String html){
        Doctor doctor = new Doctor();
        String element = StringExtensions.getFirstSegment(html, NamePattern);
        doctor.Link = "https://www.realself.com/" + StringExtensions.valueOfAttribute(element, "href");
        String text = StringExtensions.extractHtmlText(element);
        doctor.FullName = text.substring(0, text.indexOf(",")-1);
        element = StringExtensions.getFirstSegment(html, SpecialityPatter);
        doctor.Speciality = StringExtensions.extractHtmlText(element);
        element = StringExtensions.getFirstSegment(html, AddressPattern);
        doctor.Address = StringExtensions.extractHtmlText(element);
        element = StringExtensions.getFirstSegment(html, ExperiencePattern);
        doctor.YearsExperience = StringExtensions.extractHtmlText(element);
        element = StringExtensions.getFirstSegment(html, PhonePattern);
        if(element != null){
            doctor.Phone = StringExtensions.extractHtmlText(element).replace("or call: ", "");
        }
        element = StringExtensions.getFirstSegment(html, StringExtensions.imagePattern);
        doctor.ThumnailLink = StringExtensions.valueOfAttribute(element, "src");

        return doctor;
    }

    public static final Pattern NamePattern = Pattern.compile("<a [^>]*?Headline[^>]*?>[^<]*</a>", Pattern.MULTILINE);
    public static final Pattern SpecialityPatter = Pattern.compile("<span [^>]*?secondary[^>]*?>[^<]*</span>", Pattern.MULTILINE);
    public static final Pattern AddressPattern = Pattern.compile("<a [^>]*?address[^>]*?>.*?</a>", Pattern.MULTILINE);
    public static final Pattern ExperiencePattern = Pattern.compile("<(li)[^>]*yearsExperience[^>]*>(?:(?>[^<]+)|<(?!t\\1\\b[^>]*>))*?</\\1>", Pattern.MULTILINE);
    public static final Pattern PhonePattern = Pattern.compile("<span[^>]*?phone[^>]*?>.*?</a>", Pattern.MULTILINE);

    public BriefWidget(WorkingContext context, By by, Integer index) {
        super(context, by, index);
    }
}
