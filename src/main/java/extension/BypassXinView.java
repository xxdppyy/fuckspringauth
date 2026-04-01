package extension;


import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import javax.swing.*;

public class BypassXinView implements BurpExtension
{
    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName("Bypass-Xin");
        api.logging().logToOutput(("####Bypass-Xin###"));
        api.logging().logToOutput(("####write by Xin####"));

        BypassLogPanel logPanel = new BypassLogPanel(api);
        api.userInterface().registerSuiteTab("Bypass-Xin", logPanel);

        MyContextMenus contextMenus = new MyContextMenus(api, logPanel);
        api.userInterface().registerContextMenuItemsProvider(contextMenus);
        api.scanner().registerScanCheck(new MyScanCheck(api, contextMenus));
    }
}