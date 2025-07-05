package com.example.bowling.service.impl;

import com.example.bowling.model.exception.BowlingIllegalArgumentException;
import com.example.bowling.model.dto.Frame;
import com.example.bowling.service.BowlingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.shell.table.*;
import java.util.Scanner;

@Service
@Slf4j
public class BowlingServiceImpl implements BowlingService {

    private Frame[] frames = new Frame[10];

    private final static int START_PINS =10;
    private Scanner keyboardInput = new Scanner(System.in);


    @Override
    public void startBowling() {

        log.info("Start up Bowling Service");

        for (int frameIndex=0; frameIndex < 10; frameIndex++ ) {

           Frame frame = new Frame();
           var restPins = START_PINS;
           for (int rollIndex=0; rollIndex < 2; rollIndex++) {

               var pinfall = getPinfall();

               try {
                   checkPinfall(pinfall, restPins);
               } catch (BowlingIllegalArgumentException exception) {
                   log.warn(exception.getMessage());
                   rollIndex--;
                   continue; //Bei Benutzerfehleingabe, starte erneut Wurfabfrage
               }


               if (isStrike(pinfall, rollIndex)) {
                   frame.setOpenStrike(true); //calc later
                   break; //close loop

               } else if (isSpare(pinfall, rollIndex, restPins)) {
                   frame.setOpenSpare(true);

               } else {
                   if (rollIndex == 0) {
                       frame.setRoll1(pinfall);
                   } else {
                       frame.setRoll2(pinfall);
                       frame.setScore(getPreviousScore(frameIndex) + frame.getRoll1() + frame.getRoll2());
                   }
               }

               restPins -= pinfall;

            }
           frames[frameIndex] = frame;

           calcSpareBonusForPreviousFrame(frameIndex, frame.getScore());
           calcStrikeBonusForPreviousFrames(frameIndex);

           printFrames(frameIndex);
        }

    }

    private void calcStrikeBonusForPreviousFrames(int frameIndex) {
        //-check---
        //Erstes Frame muss nicht berechnet werden
        if (frameIndex == 0) {
            return;
        }

        //Nur geschlossene Strike-Folge berechnen
        Frame currentFrame = frames[frameIndex];
        if (currentFrame.getOpenStrike()) {
            return;
        }

        //Offener Strike, der berechnet werden muss
        Frame previousFrame = frames[frameIndex-1];
        if (!previousFrame.getOpenStrike()) {
            return;
        }

        //-calc----
        for (int i=0; i<=frameIndex; i++ ) {
            if (frames[i].getOpenStrike()) {
                var previousScore = getPreviousScore(i);

                if ((frameIndex-i) == 1) {
                    frames[i].setScore(previousScore+10+frames[frameIndex].getScore());
                }

                if ((frameIndex-i) == 2) {
                    frames[i].setScore(previousScore+20+frames[frameIndex].getScore());
                }

                if ((frameIndex-i) >= 3) {
                    frames[i].setScore(previousScore+30);
                }

                frames[i].setOpenStrike(false);
            }
        }

        currentFrame.setScore(getPreviousScore(frameIndex)+currentFrame.getScore());
    }

    private void calcSpareBonusForPreviousFrame(int frameIndex, int pinfall) {
        //-check---
        //Erstes Frame muss nicht berechnet werden
        if (frameIndex == 0) {
            return;
        }

        Frame previousFrame = frames[frameIndex-1];
        if (!previousFrame.getOpenSpare()) {
            return;
        }

        //-calc----
        previousFrame.setScore(getPreviousScore(frameIndex-1)+10+pinfall);
        previousFrame.setOpenSpare(false);

        Frame currentFrame = frames[frameIndex];
        currentFrame.setScore(getPreviousScore(frameIndex)+pinfall);

    }

    private int getPreviousScore(int frameIndex) {
        var previousScore=0;
        if (frameIndex > 0) {
            previousScore += frames[frameIndex -1].getScore();
        }
        return previousScore;
    }

    private boolean isStrike(int pinfall, int rollIndex) {
        return  ((rollIndex == 0) && (pinfall == START_PINS));
    }

    private boolean isSpare(int pinfall, int rollIndex, int restPins) {
        return  ((rollIndex == 1) && (pinfall == restPins));
    }

    private int getPinfall() {
        System.out.print("Kegeltreffer:");
        return keyboardInput.nextInt();
    }

    private void checkPinfall(int pinfall, int restPins) throws BowlingIllegalArgumentException {
        if (pinfall <=0) {
            throw new BowlingIllegalArgumentException("Minimal erlaubte Kegeltreffer 0");
        }

        if (pinfall > START_PINS) {
            throw new BowlingIllegalArgumentException("Maximale erlaubte Kegeltreffer 10");
        }

        if (pinfall > restPins) {
            throw new BowlingIllegalArgumentException("Es können nur maximal %d getroffen werden".formatted(restPins));
        }
    }

    private void printFrames(int maxFrameLimit) {

        String[][] shellTableData = new String[3][1 + maxFrameLimit + 1];

        //Set header
        shellTableData[0][0] = "Frame";
        shellTableData[1][0] = "Wurf";
        shellTableData[2][0] = "Punkte";

        //Set values
        for (int frameIndex=0; frameIndex<=maxFrameLimit; frameIndex++) {
            shellTableData[0][frameIndex + 1] = String.valueOf(frameIndex + 1);
            shellTableData[1][frameIndex + 1] = getRollLinePrint(frameIndex);
            shellTableData[2][frameIndex + 1] = getScoreLinePrint(frameIndex);
        }

        String tableStringPrint = getTablePrint(maxFrameLimit, shellTableData);
        log.info(tableStringPrint);
    }

    private static String getTablePrint(int maxFrameLimit, String[][] shellTableData) {
        TableModel model = new ArrayTableModel(shellTableData);
        TableBuilder tableBuilder = new TableBuilder(model)
                .addHeaderAndVerticalsBorders(BorderStyle.oldschool);

        for (int col = 0; col <= maxFrameLimit +1; col++) {
            tableBuilder.on(CellMatchers.column(col))
                    .addAligner(SimpleHorizontalAligner.center);
        }
        Table table = tableBuilder.build();

        return  "\n" + table.render(120);
    }

    private String getRollLinePrint(int frameIndex) {
        if (frames[frameIndex].getStrike()) {
            return " X     ";
        } else if (frames[frameIndex].getSpare()) {
            return " %d  / ".formatted(frames[frameIndex].getRoll1());
        } else {
            return " %d %d ".formatted(frames[frameIndex].getRoll1(), frames[frameIndex].getRoll2());
        }
    }

    private String getScoreLinePrint(int frameIndex) {
        if (frames[frameIndex].getOpenStrike() || frames[frameIndex].getOpenSpare()) {
            return "     - ";
        } else {
            return "    %d ".formatted(frames[frameIndex].getScore());
        }
    }
}
