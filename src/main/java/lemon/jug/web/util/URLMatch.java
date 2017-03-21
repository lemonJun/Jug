package lemon.jug.web.util;

/**
 * Created by lemon on 16/1/22.
 */
public class URLMatch {
    private static AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * ant style path
     * @param url request url
     * @param pattern url pattern
     * @return
     */
    public static boolean match(String url, String pattern) {
        return antPathMatcher.match(pattern, url);
    }

    public static void main(String[] args) {
        System.out.println(antPathMatcher.match("/*", "/a"));
        System.out.println(antPathMatcher.matchStart("/**", "/a/index"));
    }
}
