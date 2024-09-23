package extension;


import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import javax.swing.*;

public class fuckspringauthview implements BurpExtension
{
    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName("fuckspringauth");
        api.logging().logToOutput(("####FUZZ SPRING AUTH###"));
        api.logging().logToOutput(("####write by xxscan####"));
//        api.logging().logToError("engine error");
        JPanel myUI = new JPanel();
        api.userInterface().registerSuiteTab("fuckspringauth",myUI);
        api.userInterface().registerContextMenuItemsProvider(new MyContextMenus(api));
        api.scanner().registerScanCheck(new MyScanCheck(api));

//        api.userInterface().registerContextMenuItemsProvider(new MyContextMenuItemsProvider(api));
    }
}