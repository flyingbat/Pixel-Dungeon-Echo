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
package com.etoitau.pixeldungeon.items.weapon.missiles;

import com.watabau.noosa.audio.Sample;
import com.etoitau.pixeldungeon.Assets;
import com.etoitau.pixeldungeon.Dungeon;
import com.etoitau.pixeldungeon.actors.Actor;
import com.etoitau.pixeldungeon.actors.Char;
import com.etoitau.pixeldungeon.actors.buffs.Buff;
import com.etoitau.pixeldungeon.actors.buffs.Paralysis;
import com.etoitau.pixeldungeon.actors.hero.Hero;
import com.etoitau.pixeldungeon.effects.CellEmitter;
import com.etoitau.pixeldungeon.effects.particles.BlastParticle;
import com.etoitau.pixeldungeon.effects.particles.SmokeParticle;
import com.etoitau.pixeldungeon.items.Item;
import com.etoitau.pixeldungeon.levels.Level;
import com.etoitau.pixeldungeon.levels.Terrain;
import com.etoitau.pixeldungeon.scenes.GameScene;
import com.etoitau.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabau.utils.Random;

import java.util.ArrayList;

public class BombArrow extends Arrow {

    {
        name = "bomb arrow";
        image = ItemSpriteSheet.BombArrow;

        stackable = true;
    }

    public BombArrow() {
        this(1);
    }

    public BombArrow(int number) {
        super();
        quantity = number;
    }

    @Override
    public Item random() {
        quantity = Random.Int(1, 3);
        return this;
    }

    @Override
    public String desc() {
        return
                "An arrow with an attached bomb. Keep your distance..";
    }


    @Override
    public int price() {
        return quantity * 15;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (Dungeon.hero.belongings.bow != null) {
            if (actions.contains(AC_THROW) == false)
                actions.add(AC_THROW);
        } else
            actions.remove(AC_THROW);
        actions.remove(AC_EQUIP);

        return actions;
    }


    @Override
    protected void onThrow(int cell) {
        if (Level.pit[cell]) {
            super.onThrow(cell);
        } else {
            Sample.INSTANCE.play(Assets.SND_BLAST, 2);

            if (Dungeon.visible[cell]) {
                CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
            }

            boolean terrainAffected = false;
            for (int c: Level.aroundNine(cell)) {
                if (c >= 0 && c < Level.LENGTH) {
                    if (Dungeon.visible[c]) {
                        CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
                    }

                    if (Level.flamable[c]) {
                        Level.set(c, Terrain.EMBERS);
                        GameScene.updateMap(c);
                        terrainAffected = true;
                    }

                    Char ch = Actor.findChar(c);
                    if (ch != null) {
                        int dmg = Random.Int(1 + Dungeon.depth, 10 + Dungeon.depth * 2) - Random.Int(ch.dr());
                        if (dmg > 0) {
                            ch.damage(dmg, this);
                            if (ch.isAlive()) {
                                Buff.prolong(ch, Paralysis.class, 2);
                            }
                        }
                    }
                }
            }

            if (terrainAffected) {
                Dungeon.observe();
            }
        }
    }
}
