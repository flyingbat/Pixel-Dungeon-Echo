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
package com.etoitau.pixeldungeon.ui;

import com.watabau.noosa.ColorBlock;
import com.watabau.noosa.ui.Component;

public class HealthBar extends Component {

    private static final int COLOR_BG = 0xFFCC0000;
    private static final int COLOR_LVL = 0xFF00EE00;

    private static final int HEIGHT = 2;

    private ColorBlock hpBg;
    private ColorBlock hpLvl;

    private float level;

    @Override
    protected void createChildren() {
        hpBg = new ColorBlock(1, 1, COLOR_BG);
        add(hpBg);

        hpLvl = new ColorBlock(1, 1, COLOR_LVL);
        add(hpLvl);

        height = HEIGHT;
    }

    @Override
    protected void layout() {

        hpBg.x = hpLvl.x = x;
        hpBg.y = hpLvl.y = y;

        hpBg.size(width, HEIGHT);
        hpLvl.size(width * level, HEIGHT);

        height = HEIGHT;
    }

    public void level(float value) {
        level = value;
        layout();
    }
}
