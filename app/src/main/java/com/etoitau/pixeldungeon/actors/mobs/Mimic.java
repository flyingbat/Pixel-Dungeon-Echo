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
package com.etoitau.pixeldungeon.actors.mobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.watabau.noosa.audio.Sample;
import com.etoitau.pixeldungeon.Assets;
import com.etoitau.pixeldungeon.Dungeon;
import com.etoitau.pixeldungeon.actors.Actor;
import com.etoitau.pixeldungeon.actors.Char;
import com.etoitau.pixeldungeon.actors.hero.HeroClass;
import com.etoitau.pixeldungeon.effects.CellEmitter;
import com.etoitau.pixeldungeon.effects.Pushing;
import com.etoitau.pixeldungeon.effects.Speck;
import com.etoitau.pixeldungeon.items.Gold;
import com.etoitau.pixeldungeon.items.Item;
import com.etoitau.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.etoitau.pixeldungeon.levels.Level;
import com.etoitau.pixeldungeon.scenes.GameScene;
import com.etoitau.pixeldungeon.sprites.MimicSprite;
import com.watabau.utils.Bundlable;
import com.watabau.utils.Bundle;
import com.watabau.utils.Random;

public class Mimic extends Mob {

    private int level;

    {
        name = "mimic";
        spriteClass = MimicSprite.class;
    }

    public ArrayList<Item> items;

    private static final String LEVEL = "level";
    private static final String ITEMS = "items";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ITEMS, items);
        bundle.put(LEVEL, level);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        // Error:(67, 95) java: incompatible types: java.util.Collection<com.watabau.utils.Bundlable> cannot be converted to java.util.Collection<? extends com.etoitau.pixeldungeon.items.Item>
        //items = new ArrayList<Item>( (Collection<? extends Item>) bundle.getCollection( ITEMS ) );

        // This works
        Collection<Bundlable> tmp = bundle.getCollection(ITEMS);

        items = new ArrayList<Item>();

        for (Bundlable item : tmp) {
            items.add((Item) item);
        }
        adjustStats(bundle.getInt(LEVEL));
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(HT / 10, HT / 4);
    }

    @Override
    public int attackSkill(Char target) {
        return 9 + level;
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (enemy == Dungeon.hero && Random.Int(3) == 0 && Dungeon.hero.heroSkills.passiveA1.lootBonus(100) == 0) { // <--- Rogue bandit if present
            Gold gold = new Gold(Random.Int(Dungeon.gold / 10, Dungeon.gold / 2));
            if (gold.quantity() > 0) {
                Dungeon.gold -= gold.quantity();
                Dungeon.level.drop(gold, Dungeon.hero.pos).sprite.drop();
            }
        }
        return super.attackProc(enemy, damage);
    }

    public void adjustStats(int level) {
        this.level = level;

        HT = (3 + level) * 4;
        EXP = 2 + 2 * (level - 1) / 5;
        defenseSkill = attackSkill(null) / 2;

        enemySeen = true;
    }

    @Override
    public void die(Object cause) {

        super.die(cause);

        if (items != null) {
            for (Item item : items) {
                Dungeon.level.drop(item, pos).sprite.drop();
            }
        }
    }

    @Override
    public boolean reset() {
        state = WANDERING;
        return true;
    }

    @Override
    public String description() {
        return
                "Mimics are magical creatures which can take any shape they wish. In dungeons they almost always " +
                        "choose a shape of a treasure chest, because they know how to beckon an adventurer.";
    }

    public static Mimic spawnAt(int pos, List<Item> items) {
        Char ch = Actor.findChar(pos);
        if (ch != null) {
            List<Integer> candidates = Level.aroundCell(pos, 1, Level.NEIGHBOURS8, true);

            if (candidates.size() > 0) {
                int newPos = candidates.get(0);
                Actor.addDelayed(new Pushing(ch, ch.pos, newPos), -1);

                ch.pos = newPos;
                // FIXME
                if (ch instanceof Mob) {
                    Dungeon.level.mobPress((Mob) ch);
                } else {
                    Dungeon.level.press(newPos, ch);
                }
            } else {
                return null;
            }
        }

        Mimic m = new Mimic();
        m.items = new ArrayList<Item>(items);
        m.adjustStats(Dungeon.depth);
        m.HP = m.HT;
        m.pos = pos;
        m.state = m.HUNTING;
        GameScene.add(m, 1);

        m.sprite.turnTo(pos, Dungeon.hero.pos);

        if (Dungeon.visible[m.pos]) {
            CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10);
            Sample.INSTANCE.play(Assets.SND_MIMIC);
        }

        return m;
    }

    private static final HashSet<Class<?>> IMMUNITIES = new HashSet<Class<?>>();

    static {
        IMMUNITIES.add(ScrollOfPsionicBlast.class);
    }

    @Override
    public HashSet<Class<?>> immunities() {
        return IMMUNITIES;
    }
}
