package com.imsweb.x12;

/**
 * Minimal HTML encoding helper used by the toHtml() renderers. Safe for both element content and quoted attribute values.
 */
final class HtmlUtils {

    private HtmlUtils() {
    }

    /**
     * Escapes the characters that are significant in HTML text and (single or double) quoted attribute values.
     * @param value the raw value, may be null
     * @return the escaped value, never null
     */
    static String escape(String value) {
        if (value == null || value.isEmpty())
            return "";

        StringBuilder sb = null;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            String replacement;
            switch (c) {
                case '&':
                    replacement = "&amp;";
                    break;
                case '<':
                    replacement = "&lt;";
                    break;
                case '>':
                    replacement = "&gt;";
                    break;
                case '"':
                    replacement = "&quot;";
                    break;
                case '\'':
                    replacement = "&#39;";
                    break;
                default:
                    replacement = null;
            }
            if (replacement != null) {
                if (sb == null)
                    sb = new StringBuilder(value.length() + 16).append(value, 0, i);
                sb.append(replacement);
            }
            else if (sb != null)
                sb.append(c);
        }
        return sb == null ? value : sb.toString();
    }
}
