package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameterType;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
    }

    public void parseRequestAndCopyToClipboard() {
        parseBaseInfo();
        addRowName();
        parsePath();
        parseQuery();
        parseHeaders();
        parseCookies();
        parseRequestBody();
        copyClipBoard();
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
        targetReqRes.request().parameters().stream()
                .filter((e) -> e.type() == HttpParameterType.URL)
                .forEach(e -> addTableData(e.type().name(),e.name(),e.value()));
    }

    private void parseHeaders(){
        targetReqRes.request().headers().stream()
                .filter((e)-> ! "Cookie".equalsIgnoreCase(e.name()))
                .forEach(e -> addTableData("Header",e.name(),e.value()));
    }

    private void parseCookies(){
        targetReqRes.request().parameters().stream()
                .filter((e)-> (e.type() == HttpParameterType.COOKIE))
                .forEach(e -> addTableData("Cookie",e.name(),e.value()));
    }

    private void parseRequestBody(){
        targetReqRes.request().parameters().stream()
                .filter((e)-> (e.type() != HttpParameterType.URL))
                .filter((e)-> (e.type() != HttpParameterType.COOKIE))
                .forEach(e -> addTableData(e.type().name(),e.name(),e.value()));
    }

    private void copyClipBoard(){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(resultString.toString());
        clipboard.setContents(stringSelection,null);
    }
}
