package burp.Parser;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ResponseParser {
    private final MontoyaApi api;
    private final HttpRequestResponse targetReqRes;

    private final StringBuilder headerBuf = new StringBuilder();
    private final StringBuilder cookieBuf = new StringBuilder();
    private final StringBuilder bodyBuf   = new StringBuilder();

    public ResponseParser(MontoyaApi api, HttpRequestResponse targetReqRes){
        this.api = api;
        this.targetReqRes = targetReqRes;

        parseStatusLine();
        parseHeaders();
        parseCookies();
        parseBody();
    }

    public String getResultString() {
        StringBuilder sb = new StringBuilder();
        sb.append(headerBuf);
        sb.append(cookieBuf);
        sb.append(bodyBuf);
        return sb.toString();
    }

    private void parseStatusLine(){
        headerBuf.append("Version\t").append(targetReqRes.response().httpVersion()).append("\n");
        headerBuf.append("StatusCode\t").append(targetReqRes.response().statusCode()).append("\n");
        headerBuf.append("Message\t").append(targetReqRes.response().reasonPhrase()).append("\n");
    }

    private void addHeader(String name, String value){
        headerBuf.append("Header").append("\t")
                .append(name).append("\t")
                .append(value).append("\n");
    }

    private void parseHeaders(){
        targetReqRes.response().headers()
                .forEach(e -> addHeader(e.name(), e.value()));
    }

    private void parseCookies() {
        SetCookieParser scp = new SetCookieParser(api, targetReqRes);
        cookieBuf.append(scp.getResultString());
    }

    private String getResponseCharset() {
        return targetReqRes.response().headers().stream()
                .filter(h -> h.name().equalsIgnoreCase("Content-Type"))
                .map(h -> {
                    String v = h.value();
                    int idx = v.toLowerCase().indexOf("charset=");
                    if (idx != -1) {
                        return v.substring(idx + "charset=".length()).trim();
                    }
                    return "UTF-8";
                })
                .findFirst()
                .orElse("UTF-8");
    }

    private String decodeResponseBody(HttpResponse response) {
        String charset = getResponseCharset();
        try {
            byte[] raw = response.body().getBytes();
            return new String(raw, Charset.forName(charset));
        } catch (Exception e) {
            return new String(response.body().getBytes(), StandardCharsets.UTF_8);
        }
    }

    private void parseBody() {
        HttpResponse response = targetReqRes.response();

        String body = decodeResponseBody(response);

        String contentType = response.headers().stream()
                .filter(h -> h.name().equalsIgnoreCase("Content-Type"))
                .map(h -> h.value().toLowerCase())
                .findFirst()
                .orElse("");

        if (contentType.contains("json")) {
            bodyBuf.append(parseJsonAsTableFormat(body));
        } else if (contentType.contains("text/plain")) {
            bodyBuf.append("Body\ttext/plain\t").append(body).append("\n");
        } else if (contentType.contains("javascript")) {
            bodyBuf.append("Body\tjavascript\t(JavaScript)\n");
        } else if (contentType.contains("text/html")) {
            bodyBuf.append("Body\ttext/html\t(HTML)\n");
        } else {
            bodyBuf.append("Body\tbinary?\t(BINARY?)\n");
        }
    }

    private String parseJsonAsTableFormat(String json) {
        StringBuilder sb = new StringBuilder();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);

            extractJson("", root, sb);
            return sb.toString();

        } catch (Exception e) {
            return json;
        }
    }

    private void extractJson(String prefix, com.fasterxml.jackson.databind.JsonNode node, StringBuilder sb) {

        if (node.isObject()) {

            node.fields().forEachRemaining(entry -> {
                String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                extractJson(key, entry.getValue(), sb);
            });

        } else if (node.isArray()) {

            int index = 0;
            for (com.fasterxml.jackson.databind.JsonNode item : node) {
                String key = prefix + "[" + index + "]";
                extractJson(key, item, sb);
                index++;
            }

        } else {
            sb.append("JSON").append("\t")
                    .append(prefix).append("\t")
                    .append(node.asText())
                    .append("\n");
        }
    }
}
