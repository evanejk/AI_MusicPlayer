package com.happynicetime.ai_musicplayer;

import static com.happynicetime.ai_musicplayer.PrimaryController.finishedSongRunnable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static boolean missingArgs = false;
    static MediaPlayer mediaPlayer;
    static PrimaryController pc;
    static int[] songsBefore;
    static int songPlay;
    static Stage stage;
    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"),420,420);
        stage.setScene(scene);
        stage.show();
        App.stage = stage;
        //test mediaplayer
        //Media media = new Media(Paths.get("C:\\Users\\incur\\Music\\Bjork\\Debut\\03 Venus as a Boy.mp3").toUri().toString());
        //MediaPlayer mediaPlayer = new MediaPlayer(media);
        //mediaPlayer.play();
        
        pc = (PrimaryController) fxmlLoader.getController();
        if(missingArgs){
            pc.missingArgs();
            return;
        }
        //find songs and save them to FindSongs.songPaths
        Files.walkFileTree(Paths.get(args[0]), new FindSongs());
        //load ai
        AI.load();
        //make up songs listened to before
        songsBefore = new int[AI.currentBrain.layer1.length];
        for(int i = 0;i < songsBefore.length;i++){
            songsBefore[i] = ThreadLocalRandom.current().nextInt(0, FindSongs.songPaths.size());
            i++;
            songsBefore[i] = 0;
        }
        //use before songs for brain input
        songPlay = AI.goodBrain.compute(songsBefore);
        //use brain output to play next song
        Media media = new Media(FindSongs.songPaths.get(songPlay).toUri().toString());
        PrimaryController.playingSongPath = FindSongs.songPaths.get(songPlay);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(finishedSongRunnable);
        PrimaryController.checkBassBoost();
        //PrimaryController.setupEqualizer();
        mediaPlayer.play();
        pc.setSongPlayingLabel();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    private static FXMLLoader fxmlLoader;
    private static Parent loadFXML(String fxml) throws IOException {
        fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    static String[] args;
    public static void main2(String[] argss) {
        if(argss.length == 0){
            missingArgs = true;
        }
        args = argss;
        launch();
    }
    @Override
    public void stop(){
        if(missingArgs == false){
            AI.save();
        }
    }
}