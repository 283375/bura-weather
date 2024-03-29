/*
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura

import com.davidtakac.bura.wind.WindDirection
import org.junit.Assert.*
import org.junit.Test

class WindDirectionTest {
    @Test
    fun `cardinal direction`() {
        assertEquals(WindDirection.Compass.N, WindDirection(0.0).compass)
        assertEquals(WindDirection.Compass.ESE, WindDirection(112.0).compass)
        assertEquals(WindDirection.Compass.SW, WindDirection(225.0).compass)
        assertEquals(WindDirection.Compass.NNW, WindDirection(338.0).compass)
    }

    @Test
    fun equals() {
        assertEquals(WindDirection(0.0), WindDirection(0.0))
    }
}