package burp.Parser;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameterType;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class RequestParser {

    private final MontoyaApi api;
    private final HttpRequestResponse targetReqRes;
    private final StringBuilder resultString;

    public RequestParser(MontoyaApi api, HttpRequestResponse targetRequestResponse){
        this.api = api;
        this.targetReqRes = targetRequestResponse;
        resultString = new StringBuilder();

        parseBaseInfo();
        addRowName();
        parsePath();
        parseQuery();
        parseHeaders();
        parseCookies();
        parseRequestBody();
    }

    private void parseBaseInfo(){
        resultString.append("Method");
        resultString.append("\t");
        resultString.append(targetReqRes.request().method());
        resultString.append("\n");
        resultString.append("Url");
        resultString.append("\t");
        resultString.append(targetReqRes.request().url());
        resultString.append("\n");
        resultString.append("Version");
        resultString.append("\t");
        resultString.append(targetReqRes.request().httpVersion());
        resultString.append("\n");
        resultString.append("Notes");
        resultString.append("\t");
        if (Objects.nonNull(targetReqRes.annotations().notes())){
            resultString.append(targetReqRes.annotations().notes().replaceAll("[\n\r]", ""));
        }
        resultString.append("\n");
    }

    private void addTableData(String type,String name,String value){
        resultString.append(type);
        resultString.append("\t");
        resultString.append(name);
        resultString.append("\t");
        resultString.append(value);
        resultString.append("\n");
    }

    private void addTableData(String type,String value){
        addTableData(type,"-",value);
    }

    private void addRowName(){
        addTableData("TYPE","NAME","VALUE");
    }

    private void parsePath(){
        String path = targetReqRes.request().path().split("\\?")[0];
        String[] pathSegments = path.split("/");

        Arrays.stream(pathSegments)
                .filter((e)-> (!e.isEmpty()))
                .forEach(e -> addTableData("Path",e));
    }

    private void parseQuery(){
        String charset = getRequestCharset();

        targetReqRes.request().parameters().stream()
                .filter((e) -> e.type() == HttpParameterType.URL)
                .forEach(e -> {
                    String decodedName  = decodeValue(e.name(), charset);
                    String decodedValue = decodeValue(e.value(), charset);
                    addTableData(e.type().name(), decodedName, decodedValue);
                });
    }

    private void parseHeaders(){
        targetReqRes.request().headers().stream()
                .filter((e)-> ! "Cookie".equalsIgnoreCase(e.name()))
                .forEach(e -> addTableData("Header",e.name(),e.value()));
    }

    private void parseCookies(){
        String charset = getRequestCharset();

        targetReqRes.request().parameters().stream()
                .filter((e)-> (e.type() == HttpParameterType.COOKIE))
                .forEach(e -> {
                    String decodedName  = decodeValue(e.name(), charset);
                    String decodedValue = decodeValue(e.value(), charset);
                    addTableData("Cookie", decodedName, decodedValue);
                });
    }

    private void parseRequestBody(){
        String charset = getRequestCharset();

        targetReqRes.request().parameters().stream()
                .filter((e)-> (e.type() != HttpParameterType.URL))
                .filter((e)-> (e.type() != HttpParameterType.COOKIE))
                .forEach(e -> {
                    String decodedName  = decodeValue(e.name(), charset);
                    String decodedValue = decodeValue(e.value(), charset);
                    addTableData(e.type().name(), decodedName, decodedValue);
                });
    }

    private String getRequestCharset() {
        return targetReqRes.request().headers().stream()
                .filter(h -> h.name().equalsIgnoreCase("Content-Type"))
                .map(h -> {
                    String v = h.value().toLowerCase();
                    int index = v.indexOf("charset=");
                    if (index != -1) {
                        return v.substring(index + "charset=".length()).trim();
                    }
                    return "utf-8";
                })
                .findFirst()
                .orElse("utf-8");
    }

    private String decodeValue(String value, String charset) {
        try {
            return java.net.URLDecoder.decode(value, charset);
        } catch (Exception e) {
            return value; // デコード失敗時はそのまま
        }
    }

    public String getResultString() {
        return resultString.toString();
    }
}
