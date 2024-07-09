package burp;

import burp.api.montoya.MontoyaApi;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
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

        //Save MenuItem
        JMenuItem parseMenuItem = new JMenuItem("Send to RequestParser");

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

    private void parseRequestAndCopyToClipboard(HttpRequestResponse httpRequestResponse) {
        //api.logging().logToOutput("start");
        var requestParser = new RequestParser(api,httpRequestResponse.request());
        requestParser.parseRequestAndCopyToClipboard();
    }
}
