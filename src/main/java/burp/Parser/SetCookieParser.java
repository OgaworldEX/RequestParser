package burp.Parser;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.message.HttpHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetCookieParser {
    private final MontoyaApi api;
    //private final HttpRequestResponse targetReqRes;
    private final StringBuilder resultString;

    public SetCookieParser(MontoyaApi api, HttpRequestResponse targetRequestResponse) {
        this.api = api;
        //this.targetReqRes = targetRequestResponse;
        resultString = new StringBuilder();

        parseCookies(targetRequestResponse.response());
    }

    public static class Cookie {
        public String name;
        public String value;
        public String path;
        public String domain;
        public String expires;
        public boolean secure;
        public boolean httpOnly;
    }

    public void parseCookies(HttpResponse response) {
        List<Cookie> cookieList = new ArrayList<>();

        List<HttpHeader> headers = response.headers();
        for (HttpHeader header : headers) {
            if (header.name().equalsIgnoreCase("Set-Cookie")) {
                String cookieString = header.value();
                Cookie cookie = parseSingleCookie(cookieString);
                cookieList.add(cookie);
            }
        }

        cookieList.forEach(c -> {
            addTableData("Set-Cookie", c.name, c.value);
            addTableData("Set-Cookie.attr", "Domain", Objects.toString(c.domain, ""));
            addTableData("Set-Cookie.attr", "Path", Objects.toString(c.path, ""));
            addTableData("Set-Cookie.attr", "Expires", Objects.toString(c.expires, ""));
            addTableData("Set-Cookie.attr", "Secure", String.valueOf(c.secure));
            addTableData("Set-Cookie.attr", "HttpOnly", String.valueOf(c.httpOnly));
        });

    }

    private Cookie parseSingleCookie(String cookieString) {
        Cookie cookie = new Cookie();
        String[] parts = cookieString.split(";");
        boolean first = true;

        for (String part : parts) {
            String[] kv = part.trim().split("=", 2);

            if (first) {
                // 1つ目はCookieのメインキー
                cookie.name = kv[0].trim();
                cookie.value = kv.length > 1 ? kv[1].trim() : "";
                first = false;
            } else {
                String key = kv[0].trim().toLowerCase();
                String value = kv.length > 1 ? kv[1].trim() : "";

                switch (key) {
                    case "path" -> cookie.path = value;
                    case "domain" -> cookie.domain = value;
                    case "expires" -> cookie.expires = value;
                    case "secure" -> cookie.secure = true;
                    case "httponly" -> cookie.httpOnly = true;
                }
            }
        }

        return cookie;
    }

    private void addTableData(String type,String name,String value){
        resultString.append(type);
        resultString.append("\t");
        resultString.append(name);
        resultString.append("\t");
        resultString.append(value);
        resultString.append("\n");
    }

    public String getResultString() {
        return resultString.toString();
    }
}
