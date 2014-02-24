/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.hunger;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.hunger.component.HungerComponent;
import org.terasology.hunger.component.ThirstComponent;
import org.terasology.registry.CoreRegistry;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public final class HungerAndThirstUtils {
    private HungerAndThirstUtils() {
    }

    public static float getHungerForEntity(EntityRef entity) {
        HungerComponent hunger = entity.getComponent(HungerComponent.class);
        if (hunger == null) {
            return 0;
        }

        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();
        float foodDecay = hunger.foodDecayPerSecond * (gameTime - hunger.lastCalculationTime) / 1000f;
        return Math.max(0, hunger.lastCalculatedFood - foodDecay);
    }

    public static float getThirstForEntity(EntityRef entity) {
        ThirstComponent thirst = entity.getComponent(ThirstComponent.class);
        if (thirst == null) {
            return 0;
        }

        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();
        float waterDecay = thirst.waterDecayPerSecond * (gameTime - thirst.lastCalculationTime) / 1000f;
        return Math.max(0, thirst.lastCalculatedWater - waterDecay);
    }
}
