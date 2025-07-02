package com.example.helloWorld.service.impl;

import com.example.helloWorld.model.exception.BowlingIllegalArgumentException;
import com.example.helloWorld.model.dto.Frame;
import com.example.helloWorld.service.BowlingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
@Slf4j
public class BowlingServiceImpl implements BowlingService {

    private Frame[] frames = new Frame[10];;

    private final static int START_KEGEL_MENGE=10;
    private Scanner keyboardInput = new Scanner(System.in);


    @Override
    public void startBowling() {

        for (int frameIndex=0; frameIndex < 10; frameIndex++ ) {

           Frame frame = new Frame();
           var restKegelMenge = START_KEGEL_MENGE;
           for (int wurfIndex=0; wurfIndex < 2; wurfIndex++) {

               var treffer = getGetroffeneKegelVonBenutzer();

               try {
                   checkWurfKorrekt(treffer, restKegelMenge);
               } catch (BowlingIllegalArgumentException exception) {
                   log.warn(exception.getMessage());
                   wurfIndex--;
                   continue; //Bei Benutzerfehleingabe, starte erneut Wurfabfrage
               }


               if (isStrike(treffer, wurfIndex)) {
                   frame.setOpenStrike(true); //calc later
                   break; //close loop

               } else if (isSpare(treffer, wurfIndex, restKegelMenge)) {
                   frame.setOpenSpare(true);

               } else {
                   if (wurfIndex == 0) {
                       frame.setWurf1(treffer);
                   } else {
                       frame.setWurf2(treffer);
                       frame.setSumme(getPreviousSumme(frameIndex) + frame.getWurf1() + frame.getWurf2());
                   }
               }

               restKegelMenge -= treffer;

            }
           frames[frameIndex] = frame;

           calcSpareBonusForPreviousFrame(frameIndex, frame.getSumme());
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

        Frame previousFrame = frames[frameIndex-1];
        if (!previousFrame.getOpenStrike()) {
            return;
        }

        //-calc----
        for (int i=0; i<=frameIndex; i++ ) {
            if (frames[i].getOpenStrike()) {
                var previousSumme = getPreviousSumme(i);

                if ((frameIndex-i) == 1) {
                    frames[i].setSumme(previousSumme+10+frames[frameIndex].getSumme());
                }

                if ((frameIndex-i) == 2) {
                    frames[i].setSumme(previousSumme+20+frames[frameIndex].getSumme());
                }

                if ((frameIndex-i) >= 3) {
                    frames[i].setSumme(previousSumme+30);
                }

                frames[i].setOpenStrike(false);
            }
        }

        currentFrame.setSumme(getPreviousSumme(frameIndex)+currentFrame.getSumme());
    }

    private void calcSpareBonusForPreviousFrame(int frameIndex, int treffer) {
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
        previousFrame.setSumme(getPreviousSumme(frameIndex-1)+10+treffer);
        previousFrame.setOpenSpare(false);

        Frame currentFrame = frames[frameIndex];
        currentFrame.setSumme(getPreviousSumme(frameIndex)+treffer);

    }

    private int getPreviousSumme(int frameIndex) {
        var previousSumme=0;
        if (frameIndex > 0) {
            previousSumme += frames[frameIndex -1].getSumme();
        }
        return previousSumme;
    }

    private boolean isStrike(int treffer, int wurfIndex) {
        return  ((wurfIndex == 0) && (treffer == START_KEGEL_MENGE));
    }

    private boolean isSpare(int treffer, int wurfIndex, int restMenge) {
        return  ((wurfIndex == 1) && (treffer == restMenge));
    }

    private int getGetroffeneKegelVonBenutzer() {
        log.info("Bitte geben Sie die getroffenen Kegel ein:");
        var getoffeneKegel = keyboardInput.nextInt();
        return getoffeneKegel;
    }

    private void checkWurfKorrekt(int treffer, int restKegelMenge) throws BowlingIllegalArgumentException {
        if (treffer <=0) {
            throw new BowlingIllegalArgumentException("Minimale Kegeltrefferzahl 0");
        }

        if (treffer > START_KEGEL_MENGE) {
            throw new BowlingIllegalArgumentException("Maximale Kegeltrefferzahl 10");
        }

        if (treffer > restKegelMenge) {
            throw new BowlingIllegalArgumentException("Es können nur maximal %d geroffen werden".formatted(restKegelMenge));
        }
    }

    private void printFrames(int maxFrameLimit) {
        var columnLineMessage = "         ";
        var columnDivertLine =  "---------";
        var wurfLineMessage =   "Wurf:    ";
        var sumLineMessage =    "Summe:  ";

        for (int frameIndex=0; frameIndex<=maxFrameLimit; frameIndex++) {
            columnLineMessage += " Frame%d|".formatted(frameIndex + 1);
            columnDivertLine +=  "-------+";
            wurfLineMessage += getWurfLinePrint(frameIndex);
            sumLineMessage += getSumLinePrint(frameIndex);
        }

        log.info(columnLineMessage);
        log.info(columnDivertLine);
        log.info(wurfLineMessage);
        log.info(sumLineMessage);
    }

    private String getWurfLinePrint(int frameIndex) {
        if (frames[frameIndex].getStrike()) {
            return " X     |";
        } else if (frames[frameIndex].getSpare()) {
            return " %d  / |".formatted(frames[frameIndex].getWurf1());
        } else {
            return " %d %d |".formatted(frames[frameIndex].getWurf1(), frames[frameIndex].getWurf2());
        }
    }

    private String getSumLinePrint(int frameIndex) {
        if (frames[frameIndex].getOpenStrike() || frames[frameIndex].getOpenSpare()) {
            return "     - |";
        } else {
            return "    %d |".formatted(frames[frameIndex].getSumme());
        }
    }
}
