package burp.Parser;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;

public class ResponseParser {
    private final MontoyaApi api;
    private final HttpRequestResponse targetReqRes;
    private final StringBuilder resultString;

    public ResponseParser(MontoyaApi api, HttpRequestResponse targetRequestResponse){
        this.api = api;
        this.targetReqRes = targetRequestResponse;
        resultString = new StringBuilder();

        parseStatusLine();
        parseHeaders();
        //parseBody();
    }

    private void parseStatusLine(){
        resultString.append("Version");
        resultString.append("\t");
        resultString.append(targetReqRes.response().httpVersion());
        resultString.append("\n");
        resultString.append("StatusCode");
        resultString.append("\t");
        resultString.append(targetReqRes.response().statusCode());
        resultString.append("\n");
        resultString.append("Message");
        resultString.append("\t");
        resultString.append(targetReqRes.response().reasonPhrase());
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

    private void parseHeaders(){
        targetReqRes.response().headers()
                .forEach(e -> addTableData("Header",e.name(),e.value()));
    }

    public String getResultString() {
        return resultString.toString();
    }
}
