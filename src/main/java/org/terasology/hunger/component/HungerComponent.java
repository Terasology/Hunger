/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.hunger.component;

import org.terasology.entitySystem.Component;

/**
 * @author UltimateBudgie <TheUltimateBudgie@gmail.com>
 */
public class HungerComponent implements Component {
    //General Hunger Settings
    /**
     * The maximum amount of food an entity can "contain".
     * The minimum is 0.
     */
    public float maxFoodCapacity = 100;

    public float lastCalculatedFood;

    public long lastCalculationTime;

    /**
     * The amount of food decreased at each foodDecayInterval (below)
     */
    public float foodDecayPerSecond = 0.01f;

    //Health loss settings
    /**
     * The entity will begin to lose health if their food capacity is < this threshold. Set to 0, if you do not want
     * the entity to lose health.
     */
    public float healthLossThreshold = 1;

    /**
     * The entity will stop regenerating health if their food capacity is < this threshold.
     */
    public float healthStopRegenThreshold = 50;

    /**
     * The amount of health decreased at every healthDecreaseInterval(below)
     */
    public int healthDecreaseAmount = 15;

    /**
     * The interval (in milliseconds) at which healthDecreaseAmount (above) is applied to the component
     */
    public int healthDecreaseInterval = 30000;

    public long nextHealthDecreaseTick;
}
