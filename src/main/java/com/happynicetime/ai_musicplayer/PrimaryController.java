package com.happynicetime.ai_musicplayer;

import static com.happynicetime.ai_musicplayer.AI.currentBrain;
import static com.happynicetime.ai_musicplayer.AI.goodBrain;
import static com.happynicetime.ai_musicplayer.AI.mutatedBrain;
import static com.happynicetime.ai_musicplayer.App.mediaPlayer;
import static com.happynicetime.ai_musicplayer.App.pc;
import static com.happynicetime.ai_musicplayer.App.songPlay;
import static com.happynicetime.ai_musicplayer.App.songsBefore;
import static com.happynicetime.ai_musicplayer.App.stage;
import static com.happynicetime.ai_musicplayer.FindSongs.songPaths;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.EqualizerBand;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.robot.Robot;
import javafx.util.Duration;

public class PrimaryController {

    static void resetBass() {
        bassAmount = 0;
        setupEqualizer();
    }
    static void checkBassBoost() {
        pc.bassBoostCheck.setSelected(true);
        setupEqualizer();
    }
    @FXML private void findSongFast(){
                
        int findIndex = 0;
        String findThisString = findSongText.getText();
        if(beforeSearchString.equals(findThisString)){
            findIndex = beforeSearchIndex + 1;
        }
        Path foundSongPath = null;
        for(;findIndex < songPaths.size();findIndex++){
            if(songPaths.get(findIndex).toString().toLowerCase().contains(findThisString.toLowerCase())){
                foundSongPath = songPaths.get(findIndex);
                break;
            }
        }
        if(foundSongPath != null){
            for(int i = 0;i < songsBefore.length - 2;i++){
                songsBefore[i] = songsBefore[i + 2];
            }
            //add current song to songsBefore
            songsBefore[songsBefore.length - 2] = songPlay;
            songsBefore[songsBefore.length - 1] = (int) (1000 * mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
            songPlay = findIndex;
            beforeSearchIndex = findIndex;
            beforeSearchString = findThisString;
            //play song
            mediaPlayer.stop();
            mediaPlayer.dispose();
            playingSongPath = foundSongPath;
            Media media = new Media(foundSongPath.toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(finishedSongRunnableForFindSongFast);
            setupEqualizer();
            mediaPlayer.play();
            setSongPlayingLabel();
            pauseSongButton.setText("Pause");
            paused = false;
        }else{
            beforeSearchIndex = 0;
            beforeSearchString = "";
        }
    }
    @FXML private Button pauseSongButton;
    static boolean paused = false;
    @FXML private void pauseSong(){
        staticPauseSong();
    }
    static void staticPauseSong(){
        if(paused){
            mediaPlayer.play();
            paused = false;
            pc.pauseSongButton.setText("Pause");
        }else{
            mediaPlayer.pause();
            paused = true;
            pc.pauseSongButton.setText("Play");
        }
    }
    static Path playingSongPath;
    private static void playNextSong() {
        for(int i = 0;i < songsBefore.length - 2;i++){
            songsBefore[i] = songsBefore[i + 2];
        }
        //add current song to songsBefore
        songsBefore[songsBefore.length - 2] = songPlay;
        songsBefore[songsBefore.length - 1] = (int) (1000 * mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
        //find next song to play
        songPlay = AI.currentBrain.compute(songsBefore);
        playingSongPath = FindSongs.songPaths.get(songPlay);
        Media media = new Media(playingSongPath.toUri().toString());
        mediaPlayer.stop();
        mediaPlayer.dispose();
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(finishedSongRunnable);
        setupEqualizer();
        mediaPlayer.play();
    }
    @FXML private Label songPlayingLabel;
    @FXML private TextField findSongText;
    static Runnable finishedSongRunnable = () -> {
        currentBrain.songPlays++;
        System.out.println("finished song "+playingSongPath.toString());
        playNextSong();
        App.pc.setSongPlayingLabel();
    };
    static Runnable finishedSongRunnableForFindSongFast = () -> {
        playNextSong();
        App.pc.setSongPlayingLabel();
    };
    static void staticSkipSong(){
        currentBrain.songSkips++;
        if(currentBrain.songSkips >= 4){
            if(currentBrain == mutatedBrain){
                //check if improved //if improved set as new good brain
                if(mutatedBrain.songPlays > goodBrain.songPlays){//if mutation had more plays
                    System.out.println("mutated network won and became good network");
                    goodBrain = mutatedBrain;//set as good brain
                }else{
                    System.out.println("good network won");
                }
                System.out.println("switching to good network");
                currentBrain = goodBrain;
                //reset brain stats
                currentBrain.songPlays = 0;
                currentBrain.songSkips = 0;
                //make new mutated brain
                mutatedBrain = currentBrain.getMutatedCopy();
            }else if(currentBrain == goodBrain){
                System.out.println("switching to mutated network");
                currentBrain = mutatedBrain;
            }
        }
        playNextSong();
        pc.setSongPlayingLabel();
        pc.pauseSongButton.setText("Pause");
        paused = false;
    }
    @FXML private void skipSong() {
        staticSkipSong();
    }
    String beforeSearchString = "";
    int beforeSearchIndex = 0;
    @FXML private void findSong() {
        int findIndex = 0;
        String findThisString = findSongText.getText();
        if(beforeSearchString.equals(findThisString)){
            findIndex = beforeSearchIndex + 1;
        }
        boolean foundSomething = false;
        for(;findIndex < songPaths.size();findIndex++){
            if(songPaths.get(findIndex).toString().toLowerCase().contains(findThisString.toLowerCase())){
                foundSomething = true;
                break;
            }
        }
        if(foundSomething){
            beforeSearchIndex = findIndex;
            beforeSearchString = findThisString;
            //System.out.println("found "+songPaths.get(findIndex).toString());
            //found something at findIndex in songPaths
            //songPlayingLabel.setText("loading...");
            skipUntilFound(findThisString);
        }else{
            beforeSearchIndex = 0;
            beforeSearchString = "";
        }
        
    }
    void missingArgs() {
        songPlayingLabel.setText("AI_MusicPlayer.jar [path to music folder]");
    }

    @FXML private ProgressBar progressBar;
    void setSongPlayingLabel() {
        songPlayingLabel.setText(playingSongPath.getFileName().toString());
        //also setup loading bar
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue)->{
            progressBar.setProgress(mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
        });
    }

    private void skipUntilFound(String findThisString) {
        int totalSkipsToFindSong = 0;
        playingSongPath = Paths.get("");
        boolean didntPlaySong = false;
        while(!playingSongPath.toString().toLowerCase().contains(findThisString.toLowerCase())){
            totalSkipsToFindSong++;
            currentBrain.songSkips++;
            if(currentBrain.songSkips >= 4){
                if(currentBrain == mutatedBrain){
                    //check if improved //if improved set as new good brain
                    if(mutatedBrain.songPlays > goodBrain.songPlays){//if mutation had more plays
                        goodBrain = mutatedBrain;//set as good brain
                    }
                    currentBrain = goodBrain;
                    //reset brain stats
                    currentBrain.songPlays = 0;
                    currentBrain.songSkips = 0;
                    //make new mutated brain
                    mutatedBrain = currentBrain.getMutatedCopy();
                }else if(currentBrain == goodBrain){
                    currentBrain = mutatedBrain;
                }
            }
            for(int i = 0;i < songsBefore.length - 2;i++){
                songsBefore[i] = songsBefore[i + 2];
            }
            //add current song to songsBefore
            songsBefore[songsBefore.length - 2] = songPlay;
            if(didntPlaySong){
                songsBefore[songsBefore.length - 1] = 0;
            }else{
                songsBefore[songsBefore.length - 1] = (int) (1000 * mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
                didntPlaySong = true;
            }
            //find next song to play
            songPlay = AI.currentBrain.compute(songsBefore);
            playingSongPath = songPaths.get(songPlay);
        }
        System.out.println("Took "+totalSkipsToFindSong+" skips to find song.");
        if(currentBrain == goodBrain){
            System.out.println("good network found song");
        }else if(currentBrain == mutatedBrain){
            System.out.println("mutated network found song");
        }
        try{
            Media media = new Media(playingSongPath.toUri().toString());
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(finishedSongRunnable);
            mediaPlayer.play();
            setSongPlayingLabel();
            setupEqualizer();
            pauseSongButton.setText("Pause");
            paused = false;
        }catch(MediaException ex){
            System.out.println("MediaException error trying to play file: "+playingSongPath.toString());
        }
    }

    @FXML private void seek() {
        //System.out.println("clicked on progress bar");
        Robot robot = new Robot();
        double clickedX = robot.getMouseX() - stage.getX();
        double seekTo = (clickedX - progressBar.getLayoutX()) / progressBar.getWidth();
        //System.out.println(seekTo);
        if(seekTo >= 0 && seekTo <= 1){
            mediaPlayer.seek(new Duration(mediaPlayer.getTotalDuration().toMillis() * seekTo));
        }
    }
    @FXML private CheckBox bassBoostCheck;
    @FXML private void bassBoostClicked() {
        setupEqualizer();
    }
    static double bassAmount = .5d;
    @FXML private void bassUp(){
      bassAmount += .1d;
      if(bassAmount > 5){
          bassAmount = 5;
      }     
      setupEqualizer();
    }
    @FXML private void bassDown(){
      bassAmount -= .1d;
      if(bassAmount < 0){
          bassAmount = 0;
      }
      setupEqualizer();
    }
    static void staticBassUp(){
      bassAmount += .25d;
      if(bassAmount > 5){
          bassAmount = 5;
      }     
      setupEqualizer();
    }
    static void staticBassDown(){
      bassAmount -= .25d;
      if(bassAmount < 0){
          bassAmount = 0;
      }
      setupEqualizer();
    }
    static void setupEqualizer() {
        mediaPlayer.setAudioSpectrumNumBands(10);
        AudioEqualizer audioEqualizer = mediaPlayer.getAudioEqualizer();
        ObservableList<EqualizerBand> bands = audioEqualizer.getBands();
        EqualizerBand band0 = bands.get(0);
        EqualizerBand band1 = bands.get(1);
        EqualizerBand band2 = bands.get(2);
        EqualizerBand band3 = bands.get(3);
        EqualizerBand band4 = bands.get(4);
        EqualizerBand band5 = bands.get(5);
        EqualizerBand band6 = bands.get(6);
        EqualizerBand band7 = bands.get(7);
        EqualizerBand band8 = bands.get(8);
        EqualizerBand band9 = bands.get(9);
        if(pc.bassBoostCheck.isSelected()){
            //turn off
            //System.out.println("turn bass on");
            band0.setGain(-8d       * bassAmount);
            band1.setGain(9.6d      * bassAmount);
            band2.setGain(9.6d      * bassAmount);
            band3.setGain(5.6d      * bassAmount);
            band4.setGain(1.6d      * bassAmount);
            band5.setGain(-4d       * bassAmount);
            band6.setGain(-8d       * bassAmount);
            band7.setGain(-10.3d      * bassAmount);
            band8.setGain(-11.2d      * bassAmount);
            band9.setGain(-11.2d      * bassAmount);
            
        }else{
            //turn on
            //System.out.println("turn bass off");
            band0.setGain(0d);
            band1.setGain(0d);
            band2.setGain(0d);
            band3.setGain(0d);
            band4.setGain(0d);
            band5.setGain(0d);
            band6.setGain(0d);
            band7.setGain(0d);
            band8.setGain(0d);
            band9.setGain(0d);

        }
    }
}
