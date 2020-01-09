/*
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
package com.etoitau.pixeldungeon.items.food;

import com.etoitau.pixeldungeon.actors.Actor;
import com.etoitau.pixeldungeon.actors.Char;
import com.etoitau.pixeldungeon.actors.buffs.Hunger;
import com.etoitau.pixeldungeon.actors.mobs.npcs.RatKing;
import com.etoitau.pixeldungeon.scenes.CellSelector;
import com.etoitau.pixeldungeon.sprites.ItemSpriteSheet;

public class Pasty extends Food {

    {
        name = "pasty";
        image = ItemSpriteSheet.PASTY;
        energy = Hunger.STARVING; // 360


    }

    @Override
    public String info() {
        return "This is authentic Cornish pasty with traditional filling of beef and potato.";
    }

    @Override
    public int price() {
        return 20 * quantity;
    }


}
