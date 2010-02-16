/**
 * 
 */
package org.fusesource.hawtjni.ui;

class HtmlBuilder {
    StringBuilder sb = new StringBuilder();
    
    public HtmlBuilder build() {
        p("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
        p("  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        p("<html>");
        head();
        body();
        p("</html>");
        return this;
    }

    protected void body() {
        p(" <body " + bodyAttributes() + ">");
        bodyContent();
        p(" </body>");
    }

    protected String bodyAttributes() {
        return "";
    }

    protected void bodyContent() {
    }

    protected void head() {
        p(" <head>");
        headContent();
        p(" </head>");
    }

    protected void headContent() {
    }

    protected void p(String value) {
        sb.append(value);
        sb.append('\n');
    }

    protected String esc(String content) {
        StringBuilder sb = new StringBuilder();
        char[] chars = content.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch(c) {
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '&':
                sb.append("&amp;");
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }
    protected String pre(String id, String content) {
        return "<pre id=\""+id+"\">"+content+"</pre>\n";
    }
    protected String div(String id, String content) {
        return "<div id=\""+id+"\">"+content+"</div>\n";
    }    
    protected String div(String id, String content, String clazz) {
        return "<div id=\""+id+"\" class=\""+clazz+"\">"+content+"</div>\n";
    }
    
    @Override
    public String toString() {
        return sb.toString();
    }
}