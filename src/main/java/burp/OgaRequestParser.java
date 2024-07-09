package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;

public class OgaRequestParser implements BurpExtension
{
    public final static String extensionName = "RequestParser";
    public final static String version = "0.9.0";

    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName(extensionName);
        api.userInterface().registerContextMenuItemsProvider(new MenuItemsProvider(api));

        api.logging().logToOutput(extensionName + " " + version + " Load ok");
    }

}
