package io.github.Cruisoring.helpers;

import io.github.cruisoring.repository.Repository;
import io.github.cruisoring.tuple.Tuple;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StringExtensions {
    public static final String RegexSpecialCharacters = "[\\^$.|?*+(){}";
    public static final Character[] WindowsSpecialCharacters = new Character[] {'"', '*', '<', '>', ':', '/', '\\', '|', '?', };
    public static final Pattern escapeCharsPattern = Pattern.compile("(\\[|\\\\|\\^|\\$|\\.|\\||\\?|\\*|\\+|\\(|\\)|\\{|\\})");
    public static final Function<Object, String[]> defaultToStringForms = o -> new String[]{o.toString()};
    public static final BiPredicate<String, String> contains = (s, k) -> StringUtils.contains(s, k);
    public static final BiPredicate<String, String> containsIgnoreCase = (s, k) -> StringUtils.containsIgnoreCase(s, k);

    public static final String leafTablePatternString = "<(table)\\b[^>]*>(?:(?>[^<]+)|<(?!t\\1\\b[^>]*>))*?</\\1>";
    public static final Pattern simpleTablePattern = Pattern.compile(leafTablePatternString, Pattern.MULTILINE);
    public static final Pattern SimpleListItemPattern = Pattern.compile(leafTablePatternString.replace("table", "li"), Pattern.MULTILINE);
    public static final Pattern tableHeadPattern = Pattern.compile(leafTablePatternString.replace("table", "thead"), Pattern.MULTILINE);
    public static final Pattern tableHeadCellPattern = Pattern.compile(leafTablePatternString.replace("table", "th"), Pattern.MULTILINE);
    public static final Pattern tableBodyPattern = Pattern.compile(leafTablePatternString.replace("table", "tbody"), Pattern.MULTILINE);
    public static final Pattern tableRowPattern = Pattern.compile(leafTablePatternString.replace("table", "tr"), Pattern.MULTILINE);
    public static final Pattern tableCellPattern = Pattern.compile(leafTablePatternString.replace("table", "td"), Pattern.MULTILINE);
    public static final Pattern anyTableCellPattern = Pattern.compile(leafTablePatternString.replace("table", "[td|th]"), Pattern.MULTILINE);
    public static final Pattern LinkPattern = Pattern.compile(leafTablePatternString.replace("table", "a"), Pattern.MULTILINE);
    public static final Pattern svgPattern = Pattern.compile("<svg[^>]*>[\\s\\S]*?</svg>");
    public static final Pattern imagePattern = Pattern.compile("<img[^>]*?>");

    public static final String NewLine = System.getProperty("line.separator");
    public static final String attributeValueEnclosingChars = "\"'";
    public static final String attributePatternTemplate = "%s%s([^%c]*?)%s";

    public static final Map<String, Pattern> attributeRetrievePatterns = new HashMap<>();

    public static Repository<String, Pattern> LiteralTextPatterns = new Repository<String, Pattern>(literalText -> Pattern.compile(Pattern.quote(literalText)));


    public static List<String> getUrls(URL baseUrl, String outerHtml){
        Objects.requireNonNull(baseUrl);
        Objects.requireNonNull(outerHtml);

        List<String> hrefs = StringExtensions.getSegments(outerHtml, StringExtensions.LinkPattern)
                .stream()
                .map(e -> StringExtensions.valueOfAttribute(e, "href"))
                .distinct()
                .filter(href -> href != null)
                .collect(Collectors.toList());

        List<String> urls = hrefs.stream()
                .map(href -> getUrl(baseUrl, href)).collect(Collectors.toList());

        return urls;
    }

    public static String getUrl(URL baseUrl, String href){
        try {
            URL url = new URL(baseUrl, href);
            return url.toString();
        }catch (Exception ex){
            Logger.E(ex);
            return null;
        }
    }

    public static Pattern getAttributePattern(String leadingKey, String enclosingChars){
        String mapKey = leadingKey + enclosingChars;
        if (attributeRetrievePatterns.containsKey(mapKey))
            return attributeRetrievePatterns.get(mapKey);

        List<String> subs = new ArrayList<>();
        for(int i=0; i<enclosingChars.length(); i++){
            char ch = enclosingChars.charAt(i);
            String splitter = String.format((RegexSpecialCharacters.indexOf(ch) == -1) ? "%s" : "\\" + "%s", ch);
            String sub = String.format(attributePatternTemplate, leadingKey, splitter, ch, splitter);
            subs.add(sub);
        }
        subs.add(String.format("%s([^%s]*)[%s ]", leadingKey, enclosingChars, enclosingChars));
        String patternString = String.join("|", subs);
        Pattern pattern = Pattern.compile(patternString);
        attributeRetrievePatterns.put(mapKey, pattern);
        return pattern;
    }

    public static Pattern getAttributePattern(String leadingKey){
        return getAttributePattern(leadingKey, attributeValueEnclosingChars);
    }

    public static String valueOfAttribute(String template, String attributeName){
        attributeName = attributeName.endsWith("=") ? attributeName : attributeName+"=";
        Pattern pattern = getAttributePattern(attributeName);
        Matcher matcher = pattern.matcher(template);
        if (matcher.find()) {
            for (int i = matcher.groupCount(); i>0; i--) {
                String result = matcher.group(i);
                if(result != null) return result;
            }
        }
        return null;
    }

    public static String escapeRegexChars(String template){
        Matcher matcher = escapeCharsPattern.matcher(template);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
            sb.append("\\" + matcher.group(1));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String replaceAll(String template, Map<String, String> tokens) {
        // Create pattern of the format "%(token0|token1|...)%"
        Object[] quotedKeys = tokens.keySet().stream().map(s -> escapeRegexChars(s)).toArray();
        String patternString = String.format("(%s)", StringUtils.join(quotedKeys, '|'));
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(template);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
            String replacement = tokens.get(matcher.group(1));
            sb.append(replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String removeAllCharacters(String template, Character... chars){
        if(template == null) return template;

        int templateLen = template.length();
        if(templateLen==0 || chars == null || chars.length == 0)
            return template;

        Character[] sorted = ArrayUtils.getSorted(chars);
        int sortedLength = sorted.length;
        Character min = sorted[0];
        Character max = sorted[sortedLength-1];
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<templateLen; i ++){
            Character ch = Character.valueOf(template.charAt(i));
            if(ch.compareTo(min)==0 || ch.compareTo(max)==0)
                continue;
            else if(ch.compareTo(min)<0 || ch.compareTo(max)>0) {
                sb.append(ch);
                continue;
            } else {
                boolean matched = false;
                for(int j=1; j<sortedLength-2; j++){
                    Character token = sorted[j];
                    int withToken = ch.compareTo(token);
                    if(withToken == 0) {
                        matched = true;
                        break;
                    }
                    else if(withToken < 0){
                        break;
                    }
                }
                if(!matched)
                    sb.append(ch);
            }
        }
        return sb.toString();
    }


    public static String removeAllChars(String template, char... chars){
        return removeAllCharacters(template, toCharacters(chars));
    }

    public static String removeAllReserved(String template, String reserved){
        return removeAllChars(template, reserved.toCharArray());
    }

    public static Character[] toCharacters(char... chars){
        int length = chars.length;
        Character[] result = new Character[length];
        for(int i=0; i< length; i++){
            result[i] = Character.valueOf(chars[i]);
        }
        return result;
    }


    public static List<String> sortedListOf(Collection<String> strings, Comparator<String> comparator){
        List<String> list = (strings instanceof List) ? (List<String>)strings : new ArrayList<>(strings);
        Collections.sort(list, comparator);
        return list;
    }

    public static String getText(String context, String pattern){
        String segment0 = getSegments(context, pattern).get(0);

        String text = StringExtensions.extractHtmlText(segment0).trim();

        return text;

    }

    public static List<String> getInnerText(String template, String tagname, boolean trim){
        Pattern pattern = Pattern.compile("<td[^>]*>[\\s\\S]*?</td>".replaceAll("td", tagname));
        List<String> segments = getSegments(template, pattern);
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            String text = StringEscapeUtils.unescapeHtml4(segments.get(i));
            text = text.replaceAll("<[^>]*>", "");
            texts.add(trim ? text.trim() : text);
        }
        return texts;
    }

    public static String extractHtmlText(String template) {

        if (template == null) return null;

        String result = template.replaceAll("\\b(?!value)([-|_|\\w]+)=\\\"[^*]*?\\\"", "");
        result = result.replaceAll("<svg[^>]*>[\\s\\S]*?</svg>", "");
        result = result.replaceAll("<input\\W+[^>]*value=\\\"([^\"]*)\\\"[^\\>]*>", "$1");
        result = result.replaceAll("<!\\[CDATA\\[(.*?)\\]\\]>", "$1");
        String unescaped = StringEscapeUtils.unescapeHtml4(result);
        result = unescaped.replaceAll("<[^>]*>", "");

        return result;
    }

    public static String getFirstSegment(String template, Pattern pattern){
        Matcher matcher = pattern.matcher(template);
        if(matcher.find()){
            return matcher.group();
        } else {
            return null;
        }
    }

    public static List<String> getSegments(String template, Pattern pattern) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = pattern.matcher(template);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }

    public static List<String> getTexts(String template, Pattern pattern, boolean trim) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = pattern.matcher(template);
        while (m.find()) {
            String element = m.group();
            String text = extractHtmlText(element);
            allMatches.add(trim ? text.trim() : text);
        }
        return allMatches;
    }



    private static final Boolean matchAny(BiPredicate<String, String> matcher, String context, String[] keys) {
        if (context == null) return false;
        return Arrays.stream(keys).anyMatch(k -> matcher.test(context, k));
    }

    public static Boolean containsAll(String context, Function<Object, String[]> toStringForms, Object... keys) {
        if (context == null) return false;

        return Arrays.stream(keys)
                .filter(o -> o != null)
                .allMatch(o -> matchAny(contains, context, toStringForms.apply(o)));
    }

    public static Boolean containsAll(String context, Object... keys) {
        return containsAll(context, defaultToStringForms, keys);
    }

    public static Boolean containsAllIgnoreCase(String context, Function<Object, String[]> toStringForms, Object... keys) {
        if (context == null) return false;

        return Stream.of(keys)
                .filter(o -> o != null)
                .allMatch(o ->
                        matchAny(containsIgnoreCase, context,
                                toStringForms.apply(o)));
    }

    public static Boolean containsAllIgnoreCase(String context, Object... keys) {
        return containsAllIgnoreCase(context, defaultToStringForms, keys);
    }

    public static Boolean containsAny(String context, Function<Object, String[]> toStringForms, Object... keys) {
        if (context == null) {
            return false;
        }
        return Arrays.stream(keys).filter(o -> o != null)
                .anyMatch(o -> matchAny(contains, context, toStringForms.apply(o)));
    }

    public static Boolean containsAny(String context, Object... keys) {
        return containsAny(context, defaultToStringForms, keys);
    }

    public static Boolean containsAny(String context, String... keys) {
        return containsAny(context, defaultToStringForms, (Object[]) keys);
    }

    public static Boolean containsAnyIgnoreCase(String context, Function<Object, String[]> toStringForms, Object... keys) {
        if (context == null) {
            return false;
        }
        return Arrays.stream(keys).filter(o -> o != null).parallel()
                .anyMatch(o -> matchAny(containsIgnoreCase, context, toStringForms.apply(o)));
    }

    public static Boolean containsAnyIgnoreCase(String context, Object... keys) {
        return containsAnyIgnoreCase(context, defaultToStringForms, keys);
    }

    public static int indexOfAny(String context, int fromIndex, Object... keys) {
        if (keys.length == 0)
            return -1;

        return Arrays.stream(keys).map(k -> context.indexOf(k.toString(), fromIndex))
                .filter(i -> i != -1).min(Integer::min).orElse(-1);
    }

    public static List<String> sortedListByLength(Collection<String> strings){
        return sortedListOf(strings, (l, r) -> Integer.compare(l.length(), r.length()));
    }

    public static List<String> sortedListByLengthDesc(Collection<String> strings){
        return sortedListOf(strings, (l, r) -> Integer.compare(r.length(), l.length()));
    }

    public static String firstMatch(String template, Collection<String> keys, BiPredicate<String, String> predicate) {
        if(template == null)
            return null;
        return keys.stream()
                .filter(k -> k != null && predicate.test(template, k))
                .findFirst().orElse(null);
    }

    public static String firstContains(String template, Collection<String> keys) {
        return firstMatch(template, keys, String::contains);
    }

    public static String firstStartsWith(String template, Collection<String> keys) {
//        keys = sortedListByLengthDesc(keys);
        return firstMatch(template, keys, String::startsWith);
    }

    public static String firstEndsWith(String template, Collection<String> keys) {
//        keys = sortedListByLengthDesc(keys);
        return firstMatch(template, keys, String::endsWith);
    }

    public static int asInt(String text, int defaultValue){
        try {
            return Integer.valueOf(text);
        }catch (Exception ex){
            return defaultValue;
        }
    }

    public static float asFloat(String text, float defaultValue){
        try {
            return Float.valueOf(text);
        }catch (Exception ex){
            return defaultValue;
        }
    }

    /**
     * Find indexes of all occurance of concerned pattern in the given text context.
     * @param context   Text context to be searched.
     * @param pattern   Pattern to be searched.
     * @return          A list of indexes.
     */
    public static List<Integer> allIndexesOf(String context, Pattern pattern){
        Objects.requireNonNull(context);
        Objects.requireNonNull(pattern);

        Matcher matcher = pattern.matcher(context);
        List<Integer> indexes = new ArrayList<>();
        while (matcher.find()){
            indexes.add(matcher.start());
        }
        return indexes;
    }

    /**
     * FInd index of first occurance of concerned pattern in the given text context.
     * @param context       Text context to be searched.
     * @param patternString Pattern string to be searched.
     * @return  <tt>-1</tt> if not matched, otherwise the index of the first match.
     */
    public static int indexOfPatter(String context, String patternString){
        Objects.requireNonNull(context);
        Objects.requireNonNull(patternString);

        try {
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(context);
            return matcher.find() ? matcher.start() : -1;
        }catch (Exception ex){
            return -1;
        }
    }

    /**
     * Find indexes of all occurance of concerned literalText in the given text context.
     * @param context      Text context to be searched.
     * @param literalText   Literal text to be find from the given template.
     * @return              A list of indexes if nothing is wrong, otherwise null.
     */
    public static List<Integer> allIndexesOf(String context, String literalText){
        Objects.requireNonNull(context);

        try {
            if (StringUtils.isEmpty(literalText)) {
                throw new IllegalArgumentException("literalText shall not be empty to get meaningful indexes");
            }

            Pattern pattern = LiteralTextPatterns.apply(literalText);
            return  allIndexesOf(context, pattern);
        }catch (Exception e){
            Logger.W(e);
            return null;
        }
    }

    /**
     * Get the HTML segments with start tag pattern from the given text context.
     * @param context   Text context to perform the searching.
     * @param containerPattern Pattern to locate the container first, shall be simple one?
     * @param pattern   Pattern text of the start tag
     * @return          Segments with paired tags as a list.
     */
    public static List<String> getSegments(String context, Pattern containerPattern, String pattern){
        List<String> containerContexts = getSegmentsByLeadingPattern(context, containerPattern);

        List<String> segments = containerContexts.stream()
                .flatMap(ctx -> getSegments(ctx, pattern).stream())
                .collect(Collectors.toList());
        return segments;
    }

    public static List<String> getSegmentsByLeadingPattern(String context, Pattern pattern){
        Set<String> leadingTags = new HashSet<String>(getSegments(context, pattern));
        List<String> segments = leadingTags.stream()
                .map(leading -> getSegments(context, (String)leading))
                .flatMap(list -> list.stream())
                .collect(Collectors.toList());
        return segments;
    }

    /**
     * Get the HTML segments with start tag pattern from the given text context.
     * @param context   Text context to perform the searching.
     * @param pattern   Pattern text of the start tag
     * @return          Segments with paired tags as a list.
     */
    public static List<String> getSegments(String context, String pattern){
        Objects.requireNonNull(context);
        Objects.requireNonNull(pattern);
        if(!pattern.startsWith("<")){
            throw new IllegalArgumentException("pattern must take a form of <tagName...");
        }

        List<String> result = new ArrayList<>();
        List<Integer> segmentIndexes = allIndexesOf(context, pattern);
        if(segmentIndexes.isEmpty()){
            return result;
        }

        String tagName, startTagKey, endTag;
        int space = indexOfPatter(pattern, "\\s");
        tagName = space != -1 ? pattern.substring(1, space) : pattern.substring(1);
        startTagKey = "<" + tagName;
        endTag = "</" + tagName;
        int endTagSize = endTag.length()+1;

        List<Integer> startIndexes = allIndexesOf(context, startTagKey);
        List<Integer> endIndexes = allIndexesOf(context, endTag);
        segmentIndexes.add(context.length());

        List<Integer[][]> rangedStartEndIndexes = IntStream.range(0, segmentIndexes.size()-1).boxed()
                .map(index -> Tuple.create(segmentIndexes.get(index), segmentIndexes.get(index+1)))
                .map(tuple -> new Integer[][]{
                        startIndexes.stream()
                                .filter(i -> i >= tuple.getFirst() && i < tuple.getSecond())
                                .toArray(size -> new Integer[size]),
                        endIndexes.stream().
                                filter(i -> i >= tuple.getFirst() && i < tuple.getSecond())
                                .toArray(size -> new Integer[size])
                })
                .collect(Collectors.toList());

        for (Integer[][] pairedIndexes : rangedStartEndIndexes) {
            Integer[] starts = pairedIndexes[0];
            Integer[] ends = pairedIndexes[1];
            Set<Integer> both = new TreeSet<>(Arrays.asList(starts));
            both.addAll(Arrays.asList(ends));
            int depth = 0;
            Iterator<Integer> iterator = both.iterator();
            while(iterator.hasNext()) {
                Integer next = iterator.next();
                if(org.apache.commons.lang3.ArrayUtils.contains(starts, next)){
                    depth--;
                }else {
                    depth++;
                }
                if(depth == 0){
                    String segment = context.substring(starts[0], next+endTagSize);
                    result.add(segment);
                    break;
                }
            }
        }

        return result;
    }

    public static String getFirstSegmentByLeadingTag(String context, String pattern){
        List<String> segments = getSegments(context, pattern);

        return segments.isEmpty() ? null : segments.get(0);
    }
}
