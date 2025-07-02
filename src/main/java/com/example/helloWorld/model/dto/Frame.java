package com.example.helloWorld.model.dto;

import lombok.Data;

@Data
public class Frame {
    private int wurf1 = 0;
    private int wurf2 = 0;
    private int summe = 0;

    private Boolean openStrike = false;
    private Boolean strike = false;

    private Boolean openSpare = false;
    private Boolean spare = false;

    public void setOpenStrike(Boolean openStrike) {
        this.openStrike = openStrike;

        if (openStrike == true) {
            this.setStrike(openStrike);
        }
    }

    public void setOpenSpare(Boolean openSpare) {
        this.openSpare = openSpare;

        if (openSpare == true) {
            this.setSpare(openSpare);
        }
    }
}
