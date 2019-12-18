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
package com.etoitau.pixeldungeon.items.armor.glyphs;

import com.etoitau.pixeldungeon.actors.Char;
import com.etoitau.pixeldungeon.actors.buffs.Buff;
import com.etoitau.pixeldungeon.actors.buffs.Charm;
import com.etoitau.pixeldungeon.effects.Speck;
import com.etoitau.pixeldungeon.items.armor.Armor;
import com.etoitau.pixeldungeon.items.armor.Armor.Glyph;
import com.etoitau.pixeldungeon.levels.Level;
import com.etoitau.pixeldungeon.sprites.ItemSprite;
import com.etoitau.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabau.utils.GameMath;
import com.watabau.utils.Random;

/**
 * Chance of giving both attacker and defender charm debuff, defender's may not last as long
 */
public class Affection extends Glyph {

    private static final String TXT_AFFECTION = "%s of affection";

    private static ItemSprite.Glowing PINK = new ItemSprite.Glowing(0xFF4488);

    @Override
    public int proc(Armor armor, Char attacker, Char defender, int damage) {

        int level = (int) GameMath.gate(0, armor.effectiveLevel(), 6);

        if (Level.adjacent(attacker.pos, defender.pos) && Random.Int(level / 2 + 5) >= 4) {

            int duration = Random.IntRange(3, 7);

            Buff.affect(attacker, Charm.class, Charm.durationFactor(attacker) * duration).object = defender.id();
            attacker.sprite.centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);

            duration *= Random.Float(0.5f, 1);

            Buff.affect(defender, Charm.class, Charm.durationFactor(defender) * duration).object = attacker.id();
            defender.sprite.centerEmitter().start(Speck.factory(Speck.HEART), 0.2f, 5);
        }

        return damage;
    }

    @Override
    public String name(String weaponName) {
        return String.format(TXT_AFFECTION, weaponName);
    }

    @Override
    public Glowing glowing() {
        return PINK;
    }
}
