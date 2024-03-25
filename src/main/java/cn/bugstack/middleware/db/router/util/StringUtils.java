package cn.bugstack.middleware.db.router.util;

/**
 * 输入的字符串从中划线命名（middle-score）转换为驼峰命名（camelCase）。
 */
public class StringUtils {

    public static String middleScoreToCamelCase(String input){
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == '-') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpperCase = false;
                } else {
                    result.append(currentChar);
                }
            }
        }
        return result.toString();
    }

}
