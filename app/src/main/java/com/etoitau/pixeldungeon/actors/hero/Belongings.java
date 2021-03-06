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
package com.etoitau.pixeldungeon.actors.hero;

import android.util.Log;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.etoitau.pixeldungeon.Badges;
import com.etoitau.pixeldungeon.Dungeon;
import com.etoitau.pixeldungeon.TimeMachine;
import com.etoitau.pixeldungeon.items.Ankh;
import com.etoitau.pixeldungeon.items.AnkhCracked;
import com.etoitau.pixeldungeon.items.Item;
import com.etoitau.pixeldungeon.items.KindOfWeapon;
import com.etoitau.pixeldungeon.items.armor.Armor;
import com.etoitau.pixeldungeon.items.bags.Bag;
import com.etoitau.pixeldungeon.items.bags.Keyring;
import com.etoitau.pixeldungeon.items.keys.IronKey;
import com.etoitau.pixeldungeon.items.keys.Key;
import com.etoitau.pixeldungeon.items.keys.SkeletonKey;
import com.etoitau.pixeldungeon.items.rings.Ring;
import com.etoitau.pixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.etoitau.pixeldungeon.items.wands.Wand;
import com.etoitau.pixeldungeon.items.weapon.missiles.Bow;
import com.etoitau.pixeldungeon.utils.GLog;
import com.watabau.utils.Bundle;
import com.watabau.utils.Random;

public class Belongings implements Iterable<Item> {

    public static final int BACKPACK_SIZE = 18; // Took one out for bow

    private Hero owner;

    public Bag backpack;
    public Keyring keys;

    public KindOfWeapon weapon = null;
    public Armor armor = null;
    public Ring ring1 = null;
    public Ring ring2 = null;
    public Bow bow = null;


    public Belongings(Hero owner) {
        this.owner = owner;

        backpack = new Bag() {{
            name = "backpack";
            size = BACKPACK_SIZE;
        }};
        backpack.owner = owner;

        keys = new Keyring();
    }

    private static final String KEYS = "keyring";
    private static final String WEAPON = "weapon";
    private static final String ARMOR = "armor";
    private static final String RING1 = "ring1";
    private static final String RING2 = "ring2";
    private static final String BOW = "bow";

    public void storeInBundle(Bundle bundle) {
        backpack.storeInBundle(bundle);

        keys.storeInBundle(bundle, KEYS);

        bundle.put(WEAPON, weapon);
        bundle.put(ARMOR, armor);
        bundle.put(RING1, ring1);
        bundle.put(RING2, ring2);
        bundle.put(BOW, bow);
    }

    public void restoreFromBundle(Bundle bundle) {
        backpack.clear();
        backpack.restoreFromBundle(bundle);

        keys.clear();
        keys.restoreFromBundle(bundle, KEYS);

        weapon = (KindOfWeapon) bundle.get(WEAPON);
        if (weapon != null) {
            weapon.activate(owner);
        }

        armor = (Armor) bundle.get(ARMOR);

        ring1 = (Ring) bundle.get(RING1);
        if (ring1 != null) {
            ring1.activate(owner);
        }

        ring2 = (Ring) bundle.get(RING2);
        if (ring2 != null) {
            ring2.activate(owner);
        }

        bow = (Bow) bundle.get(BOW);
        if (bow != null) {
            bow.activate(owner);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Item> T getItem(Class<T> itemClass) {

        for (Item item : this) {
            if (itemClass.equals(item.getClass())) {
                return (T) item;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Key> T getKey(Class<T> kind, int depth) {

        for (Item item : keys) {
            if (item.getClass() == kind && ((Key) item).depth == depth) {
                return (T) item;
            }
        }

        return null;
    }

    public void countDoorKeys() {

        IronKey.curDepthQuantity = 0;

        for (Item key : keys) {
            if ((key instanceof IronKey || key instanceof SkeletonKey) && ((Key)key).depth == Dungeon.depth) {
                IronKey.curDepthQuantity++;
            }
        }
    }

    public void identify() {
        for (Item item : this) {
            item.identify();
        }
    }

    public void observe() {
        if (weapon != null) {
            weapon.identify();
            Badges.validateItemLevelAquired(weapon);
        }
        if (armor != null) {
            armor.identify();
            Badges.validateItemLevelAquired(armor);
        }
        if (ring1 != null) {
            ring1.identify();
            Badges.validateItemLevelAquired(ring1);
        }
        if (ring2 != null) {
            ring2.identify();
            Badges.validateItemLevelAquired(ring2);
        }
        for (Item item : backpack) {
            item.cursedKnown = true;
        }
    }

    public void uncurseEquipped() {
        ScrollOfRemoveCurse.uncurse(owner, armor, weapon, ring1, ring2);
    }

    public Item randomUnequipped() {
        return Random.element(backpack.items);
    }


    public void resurrect() {
        Iterator<Item> it = backpack.iterator();
        Ankh ankh = null;
        AnkhCracked ankhCracked = null;

        while (it.hasNext()) {
            Item item = it.next();
            if (item.getClass() == Ankh.class) {
                ankh = (Ankh) item;
            } else if (item.getClass() == AnkhCracked.class) {
                ankhCracked = (AnkhCracked) item;
            }

            // Char.die leads to buffs being removed, and wands charge via a buff
            // but it doesn't clear the wand's Charger
            if (item instanceof Wand) {
                Wand wand = (Wand) item;
                // clear Charger
                wand.stopCharging();
                // add new Charger and charging buff
                wand.charge(owner);
                // set wands to full charge
                wand.curCharges = wand.maxCharges;
            }

        }

        if (ankh != null) {
            ankh.detach(backpack);
        } else if (ankhCracked != null) {
            ankhCracked.detach(backpack);
        }

        // will turn off TimeMachine if no ankhs left, or replace lost AnkhTimer if there are
        TimeMachine.updateStatus();

        // remove curses from equipped items
        if (weapon != null) {
            weapon.cursed = false;
            weapon.activate(owner);
        }

        if (armor != null) {
            armor.cursed = false;
        }

        if (ring1 != null) {
            ring1.cursed = false;
            ring1.activate(owner);
        }
        if (ring2 != null) {
            ring2.cursed = false;
            ring2.activate(owner);
        }
    }

    public int charge(boolean full) {

        int count = 0;

        for (Item item : this) {
            if (item instanceof Wand) {
                Wand wand = (Wand) item;
                if (wand.curCharges < wand.maxCharges) {
                    wand.curCharges = full ? wand.maxCharges : wand.curCharges + 1;
                    count++;

                    wand.updateQuickslot();
                }
            }
        }

        return count;
    }

    public int discharge() {

        int count = 0;

        for (Item item : this) {
            if (item instanceof Wand) {
                Wand wand = (Wand) item;
                if (wand.curCharges > 0) {
                    wand.curCharges--;
                    count++;

                    wand.updateQuickslot();
                }
            }
        }

        return count;
    }

    @Override
    public Iterator<Item> iterator() {
        return new ItemIterator();
    }

    private class ItemIterator implements Iterator<Item> {

        private int index = 0;

        private Iterator<Item> backpackIterator = backpack.iterator();

        private Item[] equipped = {weapon, armor, ring1, ring2};
        private int backpackIndex = equipped.length;

        @Override
        public boolean hasNext() {

            for (int i = index; i < backpackIndex; i++) {
                if (equipped[i] != null) {
                    return true;
                }
            }

            return backpackIterator.hasNext();
        }

        @Override
        public Item next() {

            while (index < backpackIndex) {
                Item item = equipped[index++];
                if (item != null) {
                    return item;
                }
            }

            return backpackIterator.next();
        }

        @Override
        public void remove() {
            switch (index) {
                case 0:
                    equipped[0] = weapon = null;
                    break;
                case 1:
                    equipped[1] = armor = null;
                    break;
                case 2:
                    equipped[2] = ring1 = null;
                    break;
                case 3:
                    equipped[3] = ring2 = null;
                    break;
                default:
                    backpackIterator.remove();
            }
        }
    }
}
