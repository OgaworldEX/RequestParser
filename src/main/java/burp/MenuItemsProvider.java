package burp;

import burp.Parser.RequestParser;
import burp.Parser.ResponseParser;
import burp.Parser.SetCookieParser;
import burp.api.montoya.MontoyaApi;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

public class MenuItemsProvider implements ContextMenuItemsProvider
{
    private final MontoyaApi api;

    public MenuItemsProvider(MontoyaApi api) {
        this.api = api;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {

        List<Component> menuItemList = new ArrayList<>();

        //Save MenuItem (Request+Response)
        JMenuItem parseReqResMenuItem = new JMenuItem("Send to RequestParser(Request+Response)");

        if(!event.selectedRequestResponses().isEmpty()) {
            parseReqResMenuItem.addActionListener(e -> parseRequestResponseAndCopyToClipboard(event.selectedRequestResponses().get(0)));
        } else if(event.messageEditorRequestResponse().isPresent()){
            parseReqResMenuItem.addActionListener(e -> parseRequestResponseAndCopyToClipboard(event.messageEditorRequestResponse().get().requestResponse()));
        }else{
            return null;
        }
        menuItemList.add(parseReqResMenuItem);


        //Save MenuItem (Request)
        JMenuItem parseMenuItem = new JMenuItem("Send to RequestParser(Request)");

        if(!event.selectedRequestResponses().isEmpty()) {
            parseMenuItem.addActionListener(e -> parseRequestAndCopyToClipboard(event.selectedRequestResponses().get(0)));
        } else if(event.messageEditorRequestResponse().isPresent()){
            parseMenuItem.addActionListener(e -> parseRequestAndCopyToClipboard(event.messageEditorRequestResponse().get().requestResponse()));
        }else{
            return null;
        }
        menuItemList.add(parseMenuItem);

        return menuItemList;
    }

    private void parseRequestResponseAndCopyToClipboard(HttpRequestResponse httpRequestResponse) {
        var sb = new StringBuilder();

        var requestParser = new RequestParser(api,httpRequestResponse);
        sb.append((requestParser.getResultString()));

        sb.append("\n\n\n");

        var responseParser = new ResponseParser(api,httpRequestResponse);
        sb.append((responseParser.getResultString()));

        //var setCookieParser = new SetCookieParser(api,httpRequestResponse);
        //sb.append((setCookieParser.getResultString()));

        copyToClipboard(sb.toString());
    }

    private void parseRequestAndCopyToClipboard(HttpRequestResponse httpRequestResponse) {
        var sb = new StringBuilder();
        var requestParser = new RequestParser(api,httpRequestResponse);
        sb.append((requestParser.getResultString()));

        copyToClipboard(sb.toString());
    }

    private void copyToClipboard(String copyText) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(copyText);
        clipboard.setContents(stringSelection,null);
    }
}
