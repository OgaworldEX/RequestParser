package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

public class OgaRequestParser implements BurpExtension
{
    public final static String extensionName = "RequestParser";
    public final static String version = "1.1.0";

    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName(extensionName);
        api.userInterface().registerContextMenuItemsProvider(new MenuItemsProvider(api));

        api.logging().logToOutput(extensionName + " " + version + " Load ok!");
        api.logging().logToOutput("  (●•ө•●)");
    }
}
