/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.happynicetime.ai_musicplayer;

import static com.happynicetime.ai_musicplayer.PrimaryController.staticSkipSong;
import static com.happynicetime.ai_musicplayer.PrimaryController.staticPauseSong;
import com.melloware.jintellitype.JIntellitype;
import javafx.application.Platform;

public class Main {
    public static void main(String[] args) {
        JIntellitype.getInstance().addIntellitypeListener((int aCommand) -> {
            //System.out.println("Intellitype command: "+aCommand);
            switch (aCommand) {
                case JIntellitype.APPCOMMAND_MEDIA_NEXTTRACK:
                    //System.out.println("skip pressed");
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            if(PrimaryController.paused){
                                PrimaryController.staticBassUp();
                                //System.out.println("bass up");
                            }else{
                                staticSkipSong();
                                //System.out.println("skip");
                            }
                        }
                    });
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_PLAY_PAUSE:
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            staticPauseSong();
                        }
                    });
                    break;
                case JIntellitype.APPCOMMAND_MEDIA_PREVIOUSTRACK:
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            if(PrimaryController.paused){
                                PrimaryController.staticBassDown();
                                //System.out.println("bass down");                        
                            }else{
                                PrimaryController.resetBass();
                            }
                        }
                    });
                    break;
                case JIntellitype.APPCOMMAND_VOLUME_UP:
                    System.out.println("volume up");
                    break;
                case JIntellitype.APPCOMMAND_VOLUME_DOWN:
                    System.out.println("volume down");
                    break;                    
            }
        });
        
        App.main2(args);
        Platform.runLater(new Runnable() {
            @Override public void run() {
              JIntellitype.getInstance().cleanUp();    
            }
        });
        System.exit(0);
    }
}
