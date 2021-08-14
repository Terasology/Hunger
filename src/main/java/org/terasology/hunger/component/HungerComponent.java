// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.hunger.component;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

public class HungerComponent implements Component<HungerComponent> {
    //General Hunger Settings
    /**
     * The maximum amount of food an entity can "contain".
     * The minimum is 0.
     */
    @Replicate
    public float maxFoodCapacity = 100;

    /**
     * The food level of an entity after the last calculation.
     */
    @Replicate
    public float lastCalculatedFood;

    /**
     * The ingame time in MS when the last food level calculation happened.
     */
    @Replicate
    public long lastCalculationTime;

    /**
     * The amount of food decreased at each foodDecayInterval (below)
     */
    @Replicate
    public float foodDecayPerSecond = 0.01f;

    //Health loss settings
    /**
     * The entity will begin to lose health if their food capacity is < this threshold. Set to 0, if you do not want
     * the entity to lose health.
     */
    @Replicate
    public float healthLossThreshold = 15;

    /**
     * The entity will stop regenerating health if their food capacity is < this threshold.
     */
    @Replicate
    public float healthStopRegenThreshold = 50;

    /**
     * The amount of health decreased at every healthDecreaseInterval(below)
     */
    @Replicate
    public int healthDecreaseAmount = 15;

    @Override
    public void copyFrom(HungerComponent other) {
        this.maxFoodCapacity = other.maxFoodCapacity;
        this.lastCalculatedFood = other.lastCalculatedFood;
        this.lastCalculationTime = other.lastCalculationTime;
        this.foodDecayPerSecond = other.foodDecayPerSecond;
        this.healthLossThreshold = other.healthLossThreshold;
        this.healthStopRegenThreshold = other.healthStopRegenThreshold;
        this.healthDecreaseAmount = other.healthDecreaseAmount;
    }
}
