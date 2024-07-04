module com.happynicetime.ai_musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.happynicetime.ai_musicplayer to javafx.fxml;

    exports com.happynicetime.ai_musicplayer;
    requires artifactABC;

}
