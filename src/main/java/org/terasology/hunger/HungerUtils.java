// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.hunger;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.hunger.component.HungerComponent;

public final class HungerUtils {
    private HungerUtils() {
    }

    /**
     * Get the current hunger level for an entity.
     *
     * @param entity - The entity who's health you want to receive.
     * @return The hunger of an entity as a float.
     */
    public static float getHungerForEntity(EntityRef entity) {
        HungerComponent hunger = entity.getComponent(HungerComponent.class);
        if (hunger == null) {
            return 0;
        }

        long gameTime = CoreRegistry.get(Time.class).getGameTimeInMs();
        float foodDecay = hunger.foodDecayPerSecond * (gameTime - hunger.lastCalculationTime) / 1000f;
        return Math.max(0, hunger.lastCalculatedFood - foodDecay);
    }

}
