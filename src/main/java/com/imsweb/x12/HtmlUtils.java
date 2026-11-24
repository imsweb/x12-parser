package com.imsweb.x12;

/**
 * Minimal HTML encoding helper used by the toHtml() renderers. Safe for both element content and quoted attribute values.
 */
final class HtmlUtils {

    private HtmlUtils() {
    }

    /**
     * Builds the (unescaped) html id of a loop or segment: the parent's html id, if any, followed by this item's id and its zero-based index among
     * the repeats of that id, so that repeated siblings (and everything below them) get distinct ids.
     * @param parentHtmlId the html id of the parent, may be null or empty
     * @param id the id of the loop or segment
     * @param index the zero-based occurrence of this id within its parent
     * @return the html id, not yet escaped
     */
    static String htmlId(String parentHtmlId, String id, int index) {
        String own = id + "-" + index;
        return parentHtmlId == null || parentHtmlId.isEmpty() ? own : parentHtmlId + Separators.HTML_ID_SEPARATOR + own;
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
