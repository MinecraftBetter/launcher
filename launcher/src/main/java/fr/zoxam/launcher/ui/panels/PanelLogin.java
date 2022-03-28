package fr.zoxam.launcher.ui.panels;



import fr.litarvan.openauth.Authenticator;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import fr.zoxam.launcher.ui.PanelManager;
import fr.zoxam.launcher.ui.panel.Panel;


public class PanelLogin extends Panel {


    @Override
    public void init(PanelManager panelManager) {
        super.init(panelManager);
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        MicrosoftAuthResult result = authenticator.
        /*GridPane loginPanel = new GridPane();
        GridPane mainPanel = new GridPane();
        GridPane bottomPanel = new GridPane();
        AtomicBoolean connectWithMicrosoft = new AtomicBoolean(false);

        loginPanel.setMaxWidth(400);
        loginPanel.setMinWidth(400);
        loginPanel.setMaxHeight(580);
        loginPanel.setMinHeight(580);

        GridPane.setVgrow(loginPanel, Priority.ALWAYS);
        GridPane.setHgrow(loginPanel, Priority.ALWAYS);
        GridPane.setValignment(loginPanel, VPos.CENTER);
        GridPane.setHalignment(loginPanel, HPos.CENTER);
        loginPanel.getRowConstraints().addAll(new RowConstraints());
        loginPanel.add(mainPanel, 0, 0);
        GridPane.setVgrow(mainPanel, Priority.ALWAYS);
        GridPane.setHgrow(mainPanel, Priority.ALWAYS);
        mainPanel.setStyle("-fx-background-color: #181818;");
        //mainPanel.setStyle(AriLibFX.setResponsiveBackground(""));//

        //if (connectWithMicrosoft.get()) {
            //openUrl("https://login.live.com/");
       // } else {
            //("https://www.minecraft.net/fr-fr/login");
        //}

        this.layout.getChildren().add(loginPanel);

        Label connectLabel = new Label("Se Connecter !");
        GridPane.setVgrow(connectLabel, Priority.ALWAYS);
        GridPane.setHgrow(connectLabel, Priority.ALWAYS);
        GridPane.setValignment(connectLabel, VPos.TOP);
        connectLabel.setTranslateY(27);
        connectLabel.setTranslateX(150);
        connectLabel.setStyle("-fx-text-fill: #ffff; -fx-font-size: 16px;");

        Separator connectSeparator = new Separator();
        GridPane.setVgrow(connectSeparator, Priority.ALWAYS);
        GridPane.setHgrow(connectSeparator, Priority.ALWAYS);
        GridPane.setValignment(connectSeparator, VPos.TOP);
        GridPane.setHalignment(connectSeparator, HPos.CENTER);
        connectSeparator.setTranslateY(60);
        connectSeparator.setMinWidth(325);
        connectSeparator.setMaxWidth(325);
        connectSeparator.setStyle("-fx-background-color: #ffff; -fx-opacity: 50%;");

        Label usernameLabel = new Label("Nom d'utilisateur/Adresse Mail");
        GridPane.setVgrow(usernameLabel, Priority.ALWAYS);
        GridPane.setHgrow(usernameLabel, Priority.ALWAYS);
        GridPane.setValignment(usernameLabel, VPos.TOP);
        GridPane.setHalignment(usernameLabel, HPos.LEFT);
        usernameLabel.setStyle("-fx-text-fill: #ffff; -fx-font-size: 14px;");
        usernameLabel.setTranslateY(110);
        usernameLabel.setTranslateX(37.5);

        TextField usernameField = new TextField();
        GridPane.setVgrow(usernameField, Priority.ALWAYS);
        GridPane.setHgrow(usernameField, Priority.ALWAYS);
        GridPane.setValignment(usernameField, VPos.TOP);
        GridPane.setHalignment(usernameField, HPos.LEFT);
        usernameField.setStyle("-fx-background-color: #1e1e1e; -fx-font-size: 16px; -fx-text-fill: #e5e5e5");
        usernameField.setMaxWidth(325);
        usernameField.setMaxHeight(40);
        usernameField.setTranslateY(140);
        usernameField.setTranslateX(37.5);

        Separator usernameSeparator = new Separator();
        GridPane.setVgrow(usernameSeparator, Priority.ALWAYS);
        GridPane.setHgrow(usernameSeparator, Priority.ALWAYS);
        GridPane.setValignment(usernameSeparator, VPos.TOP);
        GridPane.setHalignment(usernameSeparator, HPos.CENTER);
        usernameSeparator.setTranslateY(181);
        usernameSeparator.setMinWidth(325);
        usernameSeparator.setMaxWidth(325);
        usernameSeparator.setStyle("-fx-background-color: #ffff; -fx-opacity: 40%;");

        Label passwordLabel = new Label("Mot de passe");
        GridPane.setVgrow(passwordLabel, Priority.ALWAYS);
        GridPane.setHgrow(passwordLabel, Priority.ALWAYS);
        GridPane.setValignment(passwordLabel, VPos.TOP);
        GridPane.setHalignment(passwordLabel, HPos.LEFT);
        passwordLabel.setStyle("-fx-text-fill: #ffff; -fx-font-size: 14px;");
        passwordLabel.setTranslateY(200);
        passwordLabel.setTranslateX(37.5);

        TextField passwordField = new PasswordField();
        GridPane.setVgrow(passwordField, Priority.ALWAYS);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        GridPane.setValignment(passwordField, VPos.TOP);
        GridPane.setHalignment(passwordField, HPos.LEFT);
        passwordField.setStyle("-fx-background-color: #1e1e1e; -fx-font-size: 16px; -fx-text-fill: #e5e5e5");
        passwordField.setMaxWidth(325);
        passwordField.setMaxHeight(40);
        passwordField.setTranslateY(230);
        passwordField.setTranslateX(37.5);

        Separator passwordSeparator = new Separator();
        GridPane.setVgrow(passwordSeparator, Priority.ALWAYS);
        GridPane.setHgrow(passwordSeparator, Priority.ALWAYS);
        GridPane.setValignment(passwordSeparator, VPos.TOP);
        GridPane.setHalignment(passwordSeparator, HPos.CENTER);
        passwordSeparator.setTranslateY(271);
        passwordSeparator.setMinWidth(325);
        passwordSeparator.setMaxWidth(325);
        passwordSeparator.setStyle("-fx-background-color: #ffff; -fx-opacity: 40%;");

        Label forgotPasswordLabel = new Label("Mot de passe oublié ?");
        GridPane.setVgrow(forgotPasswordLabel, Priority.ALWAYS);
        GridPane.setHgrow(forgotPasswordLabel, Priority.ALWAYS);
        GridPane.setValignment(forgotPasswordLabel, VPos.CENTER);
        GridPane.setHalignment(forgotPasswordLabel, HPos.LEFT);
        forgotPasswordLabel.setTranslateX(37.5);
        forgotPasswordLabel.setStyle("-fx-text-fill: #69a7ed; -fx-font-size: 12px;");
        forgotPasswordLabel.setUnderline(true);
        forgotPasswordLabel.setOnMouseEntered(e->this.layout.setCursor(Cursor.HAND));
        forgotPasswordLabel.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        forgotPasswordLabel.setOnMouseClicked(event ->{
            if (connectWithMicrosoft.get()){
                openUrl("https://account.live.com/ResetPassword.aspx");
            }else{
                openUrl("https://www.minecraft.net/fr-fr/password/forgot");
            }
        });

        Button connectButton = new Button("Se connecter");
        GridPane.setVgrow(connectButton, Priority.ALWAYS);
        GridPane.setHgrow(connectButton, Priority.ALWAYS);
        GridPane.setValignment(connectButton, VPos.CENTER);
        GridPane.setHalignment(connectButton, HPos.LEFT);
        connectButton.setTranslateX(37.5);
        connectButton.setTranslateY(80);
        connectButton.setMinWidth(325);
        connectButton.setMinHeight(50);
        connectButton.setStyle("-fx-background-color: #81807f; -fx-opacity: 50%; -fx-border-radius: 0px; -fx-background-insets: 0;" +
                " -fx-opacity: 100%;-fx-font-size: 14px; -fx-text-fill: #ffff;");
        connectButton.setOnMouseEntered(e->this.layout.setCursor(Cursor.HAND));
        connectButton.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        connectButton.setOnMouseClicked(event ->{
            if (connectWithMicrosoft.get()) {
                //todo microsoft
            }else{
                try {
                    AuthenticationResponse response = Auth.authenticate(usernameField.getText(),passwordField.getText());
                    Main.logger.log("============[Auth]============");
                    Main.logger.log("Access Token: "+ response.getAccessToken());
                    Main.logger.log("Account name: "+ response.getSelectedProfile().getName());
                    Main.logger.log("Account id:"+ response.getSelectedProfile().getUUID());
                    Main.logger.log("==============================");
                } catch (RequestException | AuthenticationUnavailableException ex) {
                    ex.printStackTrace();
                }
            }
        });
        Separator chooseConnectSeparator = new Separator();
        GridPane.setVgrow(chooseConnectSeparator, Priority.ALWAYS);
        GridPane.setHgrow(chooseConnectSeparator, Priority.ALWAYS);
        GridPane.setValignment(chooseConnectSeparator, VPos.CENTER);
        GridPane.setHalignment(chooseConnectSeparator, HPos.CENTER);
        chooseConnectSeparator.setTranslateY(160);
        chooseConnectSeparator.setMinWidth(325);
        chooseConnectSeparator.setMaxWidth(325);
        chooseConnectSeparator.setStyle("-fx-opacity: 30%;");

        Button chooseConnexion = new Button("CONNEXION AVEC");
        GridPane.setVgrow(chooseConnexion, Priority.ALWAYS);
        GridPane.setHgrow(chooseConnexion, Priority.ALWAYS);
        GridPane.setValignment(chooseConnexion, VPos.CENTER);
        GridPane.setHalignment(chooseConnexion, HPos.CENTER);
        chooseConnexion.setTranslateY(160);
        chooseConnexion.setStyle("-fx-background-color: #181818; -fx-text-fill: #5e5e5e; -fx-font-size: 14px;");
        chooseConnexion.setUnderline(true);

        Image logoImageMicrosoft = new Image(Main.class.getResource("/microsoft.png").toExternalForm());
        ImageView imageViewMicrosoft = new ImageView(logoImageMicrosoft);
        Image logoImageMojang = new Image(Main.class.getResource("/mojang.png").toExternalForm());
        ImageView imageViewMojang = new ImageView(logoImageMojang);

        Button microsoftButton = new Button("Microsoft");
        GridPane.setVgrow(microsoftButton, Priority.ALWAYS);
        GridPane.setHgrow(microsoftButton, Priority.ALWAYS);
        GridPane.setValignment(microsoftButton, VPos.CENTER);
        GridPane.setHalignment(microsoftButton, HPos.LEFT);
        microsoftButton.setTranslateX(127);
        microsoftButton.setTranslateY(215);
        microsoftButton.setMinWidth(140);
        microsoftButton.setMinHeight(40);
        microsoftButton.setStyle("-fx-background-color: #34aa2f; -fx-border-radius: 0px; -fx-background-insets: 0;-fx-font-size: 14px; -fx-text-fill: #ffff;");
        microsoftButton.setGraphic(imageViewMicrosoft);
        microsoftButton.setOnMouseEntered(e->this.layout.setCursor(Cursor.HAND));
        microsoftButton.setOnMouseExited(event -> this.layout.setCursor(Cursor.DEFAULT));
        microsoftButton.setOnMouseClicked(event ->{
            if (connectWithMicrosoft.get()) {
                connectWithMicrosoft.set(false);
                usernameLabel.setText("Nom d'utilisateur/Adresse Mail");
                microsoftButton.setGraphic(imageViewMicrosoft);
                microsoftButton.setText("Microsoft");
            }else{
                connectWithMicrosoft.set(true);
                usernameLabel.setText("Adresse mail");
                microsoftButton.setGraphic(imageViewMojang);
                microsoftButton.setText("Mojang");
            }
        });


        mainPanel.getChildren().addAll(connectLabel, connectSeparator, usernameLabel, usernameField, usernameSeparator,
                passwordSeparator, passwordField, passwordLabel, forgotPasswordLabel,connectButton,chooseConnectSeparator,
                chooseConnexion,microsoftButton);
    }

    private void openUrl(String url){
        try{
            Desktop.getDesktop().browse(new URI(url));
        }catch (IOException | URISyntaxException e){
            Main.logger.warn(e.getMessage());
        }*/
    }
}
