package com.etoitau.pixeldungeon.actors.skills;

/**
 * Created by Moussa on 20-Jan-17.
 */
public class Hunting extends PassiveSkillA3 {


    {
        name = "Hunting";
        image = 74;
        tier = 3;
    }

    @Override
    public int hunting() {
        return level;
    }

    @Override
    protected boolean upgrade() {
        return true;
    }


    @Override
    public String info() {
        return "Creates food with time.\n"
                + costUpgradeInfo();
    }
}
