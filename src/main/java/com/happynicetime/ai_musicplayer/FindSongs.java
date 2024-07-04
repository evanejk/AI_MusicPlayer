package com.happynicetime.ai_musicplayer;

import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class FindSongs extends SimpleFileVisitor<Path> {
    public static LinkedList<Path> songPaths = new LinkedList<>();
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attr) {
        try{
            //System.out.println(path.toString());
            Media media = new Media(path.toUri().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.dispose();
            songPaths.add(path);
        }catch(Exception ex){
            //ex.printStackTrace();
            System.out.println("not playable: "+path.toString());
        }
        
        return CONTINUE;
    }
}
