package fr.zoxam.launcher.ui.panels;


import fr.litarvan.openauth.microsoft.AuthTokens;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.litarvan.openauth.microsoft.model.response.MinecraftProfile;
import fr.zoxam.launcher.ui.PanelManager;
import fr.zoxam.launcher.ui.panel.Panel;


public class PanelLogin extends Panel {

    public static MicrosoftAuthenticator authenticator;
    public static MinecraftProfile account = null;

    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        authenticator = new MicrosoftAuthenticator();

        //TODO: Check if we saved a token
        new Thread(PanelLogin::webConnect).start(); // Open the pop-up asynchronously, so it doesn't freeze the app
    }

    /**
     * Try to connect using a web pop-up
     */
    private static Boolean webConnect() {
        try {
            connected(authenticator.loginWithWebview());
            return true;
        } catch (MicrosoftAuthenticationException e) {
            e.printStackTrace(); /* Error (user closed the popup, ...) TODO: Handle this */
        }
        return false;
    }

    /**
     * Try to connect using a token
     */
    private static Boolean tokenConnect(AuthTokens token) {
        try {
            connected(authenticator.loginWithTokens(token));
            return true;
        } catch (MicrosoftAuthenticationException e) {
            e.printStackTrace(); /* Unknown error TODO: Handle this */
        }
        return false;
    }

    private static void connected(MicrosoftAuthResult result) {
        //TODO: Save the token
        account = result.getProfile();
        System.out.printf("Hello %1$s%n", account.getName());
    }
}
