package lemon.jug.web.util;

import java.io.File;

public class TaskFileHelper {

    public static String getFile(String name) {
        return String.format("%s%s%s", "", File.separator, name);
    }

}
