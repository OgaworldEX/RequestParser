package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;

public class RequestParser {

    private final MontoyaApi api;
    private final HttpRequest targetRequest;
    private final StringBuilder resultString;

    public RequestParser(MontoyaApi api,HttpRequest targetRequest){
        this.api = api;
        this.targetRequest = targetRequest;
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
        resultString.append(targetRequest.method());
        resultString.append("\n");
        resultString.append("Url");
        resultString.append("\t");
        resultString.append(targetRequest.url());
        resultString.append("\n");
        resultString.append("Version");
        resultString.append("\t");
        resultString.append(targetRequest.httpVersion());
        resultString.append("\n");
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
        String path = targetRequest.path().split("\\?")[0];
        String[] pathSegments = path.split("/");

        Arrays.stream(pathSegments)
                .filter((e)-> (!e.isEmpty()))
                .forEach(e -> addTableData("Path",e));
    }

    private void parseQuery(){
        targetRequest.parameters().stream()
                .filter((e) -> e.type() == HttpParameterType.URL)
                .forEach(e -> addTableData(e.type().name(),e.name(),e.value()));
    }

    private void parseHeaders(){
        targetRequest.headers().stream()
                .filter((e)-> ! "Cookie".equalsIgnoreCase(e.name()))
                .forEach(e -> addTableData("Header",e.name(),e.value()));
    }

    private void parseCookies(){
        targetRequest.parameters().stream()
                .filter((e)-> (e.type() == HttpParameterType.COOKIE))
                .forEach(e -> addTableData("Cookie",e.name(),e.value()));
    }

    private void parseRequestBody(){
        var params = targetRequest.parameters();
        for (HttpParameter param: params){
            if(!(param.type() == HttpParameterType.URL ||
                 param.type() == HttpParameterType.COOKIE)){
                addTableData(param.type().name().toUpperCase(),param.name(),param.value());
            }
        }
    }

    private void copyClipBoard(){
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(resultString.toString());
        clipboard.setContents(stringSelection,null);
    }
}
