/*
 * Pixel Dungeon Echo
 * Copyright (C) 2019-2020 Kyle Chatman
 *
 * Based on:
 *
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.etoitau.pixeldungeon.actors.blobs;

import com.etoitau.pixeldungeon.items.scrolls.ScrollOfChallenge;
import com.watabau.noosa.audio.Sample;
import com.etoitau.pixeldungeon.Assets;
import com.etoitau.pixeldungeon.Dungeon;
import com.etoitau.pixeldungeon.DungeonTilemap;
import com.etoitau.pixeldungeon.Journal;
import com.etoitau.pixeldungeon.Journal.Feature;
import com.etoitau.pixeldungeon.actors.Actor;
import com.etoitau.pixeldungeon.actors.Char;
import com.etoitau.pixeldungeon.actors.buffs.Buff;
import com.etoitau.pixeldungeon.actors.buffs.FlavourBuff;
import com.etoitau.pixeldungeon.actors.hero.Hero;
import com.etoitau.pixeldungeon.actors.mobs.Mob;
import com.etoitau.pixeldungeon.effects.BlobEmitter;
import com.etoitau.pixeldungeon.effects.Flare;
import com.etoitau.pixeldungeon.effects.Wound;
import com.etoitau.pixeldungeon.effects.particles.SacrificialParticle;
import com.etoitau.pixeldungeon.items.scrolls.ScrollOfWipeOut;
import com.etoitau.pixeldungeon.scenes.GameScene;
import com.etoitau.pixeldungeon.ui.BuffIndicator;
import com.etoitau.pixeldungeon.utils.GLog;
import com.watabau.utils.Bundle;
import com.watabau.utils.Random;

public class SacrificialFire extends Blob {


    private static final String TXT_WORTHY = "\"Your sacrifice is worthy...\" ";
    private static final String TXT_BARELY = "\"Your sacrifice is barely worthy...\" ";
    private static final String TXT_UNWORTHY = "\"Your sacrifice is unworthy...\" ";
    private static final String TXT_REWARD = "\"Your sacrifice is worthy and so are you!\" ";

    protected int pos;

    private boolean claimed = false;

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        for (int i = 0; i < LENGTH; i++) {
            if (cur[i] > 0) {
                pos = i;
                break;
            }
        }
    }

    @Override
    protected void evolve() {


        volume = off[pos] = cur[pos];
        // if char standing on
        Char ch = Actor.findChar(pos);
        if (ch != null) {
            if (Dungeon.visible[pos] && ch.buff(Marked.class) == null) {
                ch.sprite.emitter().burst(SacrificialParticle.FACTORY, 20);
                Sample.INSTANCE.play(Assets.SND_BURNING);
            }
            Buff.prolong(ch, Marked.class, Marked.DURATION);
            if (ch == Dungeon.hero) {
                // if hero is in fire, beckon mobs
                ScrollOfChallenge.challengeMobs(pos);
            }
        }
        if (Dungeon.visible[pos]) {
            Journal.add(Feature.SACRIFICIAL_FIRE);
        }
    }

    @Override
    public void seed(int cell, int amount) {
        cur[pos] = 0;
        pos = cell;
        volume = cur[pos] = amount;
    }

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);

        emitter.pour(SacrificialParticle.FACTORY, 0.04f);
    }

    public static void sacrifice(Char ch) {


        Wound.hit(ch);

        SacrificialFire fire = (SacrificialFire) Dungeon.level.blobs.get(SacrificialFire.class);
        if (fire != null && !fire.claimed) {

            int exp = 0;
            String msg = TXT_UNWORTHY;
            if (ch instanceof Mob) {
                exp = ((Mob) ch).exp();
                if (exp > 0) {
                    exp *= Random.IntRange(1, 3);
                    msg = TXT_WORTHY;
                } else if (Random.Float() > 0.5) {
                    exp = ((Mob) ch).getEXP();
                    msg = TXT_BARELY;
                }

            } else if (ch instanceof Hero) {
                exp = ((Hero) ch).maxExp();
                msg = TXT_WORTHY;
            }

            if (exp > 0) {

                int volume = fire.volume - exp;
                if (volume > 0) {
                    fire.seed(fire.pos, volume);
                    GLog.w(msg);
                } else {
                    fire.seed(fire.pos, 0);
                    Journal.remove(Feature.SACRIFICIAL_FIRE);

                    GLog.w(TXT_REWARD);
                    GameScene.effect(new Flare(7, 32)
                            .color(0x66FFFF, true)
                            .show(ch.sprite.parent,
                                    DungeonTilemap.tileCenterToWorld(fire.pos),
                                    2f));
                    Dungeon.level.drop(new ScrollOfWipeOut(), fire.pos).sprite.drop();
                    fire.claimed = true;
                }
            } else {

                GLog.w(TXT_UNWORTHY);

            }
        }
    }

    @Override
    public String tileDesc() {
        return "Sacrificial fire burns here. Every creature touched by this fire is marked as an " +
                "offering for the spirits of the dungeon.";
    }

    public static class Marked extends FlavourBuff {

        public static final float DURATION = 5f;
        private boolean causeNight = false;

        @Override
        public int icon() {
            return BuffIndicator.SACRIFICE;
        }

        @Override
        public String toString() {
            return "Marked for sacrifice";
        }

        @Override
        public boolean attachTo(Char target) {
            // spawn more mobs like nighttime while hero marked
            if (target == Dungeon.hero && !Dungeon.nightMode) { Dungeon.nightMode = causeNight = true; }
            return super.attachTo(target);
        }

        @Override
        public void detach() {
            if (causeNight) {
                // if this buff caused it to be night, undo
                Dungeon.nightMode = false;
            }
            if (!target.isAlive()) {
                sacrifice(target);
            }
            super.detach();
        }

        public static void spreadFire(Char from, Char to) {
            if (from.buff(SacrificialFire.Marked.class) != null) {
                // if attacker marked, pass on mark
                Buff.prolong(to, SacrificialFire.Marked.class, SacrificialFire.Marked.DURATION);
            }
        }
    }

}

// spawn more mobs like nighttime
               // if (!Dungeon.nightMode) { Dungeon.nightMode = causeNight = true; }