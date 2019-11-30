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
package com.etoitau.pixeldungeon.mechanics;

import com.etoitau.pixeldungeon.actors.Actor;
import com.etoitau.pixeldungeon.actors.hero.Hero;
import com.etoitau.pixeldungeon.actors.mobs.npcs.SummonedPet;
import com.etoitau.pixeldungeon.levels.Level;

public class Ballistica {

    public static int[] trace = new int[Math.max(Level.WIDTH, Level.HEIGHT)];
    public static int distance;

    public static int cast(int from, int to, boolean magic, boolean hitChars) {

        int w = Level.WIDTH;

        int x0 = from % w;
        int x1 = to % w;
        int y0 = from / w;
        int y1 = to / w;

        int dx = x1 - x0;
        int dy = y1 - y0;

        int stepX = dx > 0 ? +1 : -1;
        int stepY = dy > 0 ? +1 : -1;

        dx = Math.abs(dx);
        dy = Math.abs(dy);

        int stepA;
        int stepB;
        int dA;
        int dB;

        if (dx > dy) {

            stepA = stepX;
            stepB = stepY * w;
            dA = dx;
            dB = dy;

        } else {

            stepA = stepY * w;
            stepB = stepX;
            dA = dy;
            dB = dx;

        }

        distance = 1;
        trace[0] = from;

        int cell = from;

        int err = dA / 2;
        while (cell != to || magic) {

            cell += stepA;

            err += dB;
            if (err >= dA) {
                err = err - dA;
                cell = cell + stepB;
            }

            trace[distance++] = cell;

            if (!Level.passable[cell] && !Level.avoid[cell]) {
                return trace[--distance - 1];
            }

            if (Level.losBlocking[cell] || (hitChars && Actor.findChar(cell) != null)) {
                return cell;
            }
        }

        trace[distance++] = cell;

        return to;
    }

    public static int castMaiden(int from, int to) {

        int w = Level.WIDTH;

        int x0 = from % w;
        int x1 = to % w;
        int y0 = from / w;
        int y1 = to / w;

        int dx = x1 - x0;
        int dy = y1 - y0;

        int stepX = dx > 0 ? +1 : -1;
        int stepY = dy > 0 ? +1 : -1;

        dx = Math.abs(dx);
        dy = Math.abs(dy);

        int stepA;
        int stepB;
        int dA;
        int dB;

        if (dx > dy) {

            stepA = stepX;
            stepB = stepY * w;
            dA = dx;
            dB = dy;

        } else {

            stepA = stepY * w;
            stepB = stepX;
            dA = dy;
            dB = dx;

        }

        distance = 1;
        trace[0] = from;

        int cell = from;

        int err = dA / 2;
        while (cell != to) {

            cell += stepA;

            err += dB;
            if (err >= dA) {
                err = err - dA;
                cell = cell + stepB;
            }

            trace[distance++] = cell;

            if (!Level.passable[cell] && !Level.avoid[cell]) {
                return trace[--distance - 1];
            }

            if (Level.losBlocking[cell] || (Actor.findChar(cell) != null && !(Actor.findChar(cell) instanceof Hero) && !(Actor.findChar(cell) instanceof SummonedPet))) {
                return cell;
            }
        }

        trace[distance++] = cell;

        return to;
    }


    public static int cast(int from, int to, int skipChars) {

        int w = Level.WIDTH;

        int x0 = from % w;
        int x1 = to % w;
        int y0 = from / w;
        int y1 = to / w;

        int dx = x1 - x0;
        int dy = y1 - y0;

        int stepX = dx > 0 ? +1 : -1;
        int stepY = dy > 0 ? +1 : -1;

        dx = Math.abs(dx);
        dy = Math.abs(dy);

        int stepA;
        int stepB;
        int dA;
        int dB;

        if (dx > dy) {

            stepA = stepX;
            stepB = stepY * w;
            dA = dx;
            dB = dy;

        } else {

            stepA = stepY * w;
            stepB = stepX;
            dA = dy;
            dB = dx;

        }

        distance = 1;
        trace[0] = from;

        int cell = from;

        int err = dA / 2;
        while (cell != to && skipChars > 0) {

            skipChars--;
            cell += stepA;

            err += dB;
            if (err >= dA) {
                err = err - dA;
                cell = cell + stepB;
            }

            trace[distance++] = cell;

            if (!Level.passable[cell]) {
                return trace[--distance - 1];
            }

            if (Level.losBlocking[cell]) {
                return cell;
            }

        }

        trace[distance++] = cell;

        return to;
    }
}