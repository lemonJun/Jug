package lemon.jug.web.connector;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;

public class WebUtils {

    public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";
    public static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";
    public static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";
    public static final String INCLUDE_PATH_INFO_ATTRIBUTE = "javax.servlet.include.path_info";
    public static final String INCLUDE_QUERY_STRING_ATTRIBUTE = "javax.servlet.include.query_string";

    /**
     * Standard Servlet 2.4+ spec request attributes for forward URI and paths.
     * <p>If forwarded to via a RequestDispatcher, the current resource will see its
     * own URI and paths. The originating URI and paths are exposed as request attributes.
     */
    public static final String FORWARD_REQUEST_URI_ATTRIBUTE = "javax.servlet.forward.request_uri";
    public static final String FORWARD_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.forward.context_path";
    public static final String FORWARD_SERVLET_PATH_ATTRIBUTE = "javax.servlet.forward.servlet_path";
    public static final String FORWARD_PATH_INFO_ATTRIBUTE = "javax.servlet.forward.path_info";
    public static final String FORWARD_QUERY_STRING_ATTRIBUTE = "javax.servlet.forward.query_string";

    /**
     * Standard Servlet 2.3+ spec request attributes for error pages.
     * <p>To be exposed to JSPs that are marked as error pages, when forwarding
     * to them directly rather than through the servlet container's error page
     * resolution mechanism.
     */
    public static final String ERROR_STATUS_CODE_ATTRIBUTE = "javax.servlet.error.status_code";
    public static final String ERROR_EXCEPTION_TYPE_ATTRIBUTE = "javax.servlet.error.exception_type";
    public static final String ERROR_MESSAGE_ATTRIBUTE = "javax.servlet.error.message";
    public static final String ERROR_EXCEPTION_ATTRIBUTE = "javax.servlet.error.exception";
    public static final String ERROR_REQUEST_URI_ATTRIBUTE = "javax.servlet.error.request_uri";
    public static final String ERROR_SERVLET_NAME_ATTRIBUTE = "javax.servlet.error.servlet_name";

    /**
     * Prefix of the charset clause in a content type String: ";charset="
     */
    public static final String CONTENT_TYPE_CHARSET_PREFIX = ";charset=";

    /**
     * Default character encoding to use when {@code request.getCharacterEncoding}
     * returns {@code null}, according to the Servlet spec.
     * @see ServletRequest#getCharacterEncoding
     */
    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";

    /**
     * Standard Servlet spec context attribute that specifies a temporary
     * directory for the current web application, of type {@code java.io.File}.
     */
    public static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";

    /**
     * HTML escape parameter at the servlet context level
     * (i.e. a context-param in {@code web.xml}): "defaultHtmlEscape".
     */
    public static final String HTML_ESCAPE_CONTEXT_PARAM = "defaultHtmlEscape";

    /**
     * Use of response encoding for HTML escaping parameter at the servlet context level
     * (i.e. a context-param in {@code web.xml}): "responseEncodedHtmlEscape".
     * @since 4.1.2
     */
    public static final String RESPONSE_ENCODED_HTML_ESCAPE_CONTEXT_PARAM = "responseEncodedHtmlEscape";

    /**
     * Web app root key parameter at the servlet context level
     * (i.e. a context-param in {@code web.xml}): "webAppRootKey".
     */
    public static final String WEB_APP_ROOT_KEY_PARAM = "webAppRootKey";

    /** Default web app root key: "webapp.root" */
    public static final String DEFAULT_WEB_APP_ROOT_KEY = "webapp.root";

    /** Name suffixes in case of image buttons */
    public static final String[] SUBMIT_IMAGE_SUFFIXES = { ".x", ".y" };

    /** Key for the mutex session attribute */
    public static final String SESSION_MUTEX_ATTRIBUTE = WebUtils.class.getName() + ".MUTEX";

    /**
     * Return an appropriate request object of the specified type, if available,
     * unwrapping the given request as far as necessary.
     * @param request the servlet request to introspect
     * @param requiredType the desired type of request object
     * @return the matching request object, or {@code null} if none
     * of that type is available
     */
    @SuppressWarnings("unchecked")
    public static <T> T getNativeRequest(ServletRequest request, Class<T> requiredType) {
        if (requiredType != null) {
            if (requiredType.isInstance(request)) {
                return (T) request;
            } else if (request instanceof ServletRequestWrapper) {
                return getNativeRequest(((ServletRequestWrapper) request).getRequest(), requiredType);
            }
        }
        return null;
    }

}
