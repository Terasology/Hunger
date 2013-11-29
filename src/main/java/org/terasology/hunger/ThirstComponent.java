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
package org.terasology.hunger;

import org.terasology.entitySystem.Component;

/**
 * @author UltimateBudgie <TheUltimateBudgie@gmail.com>
 */
public class ThirstComponent implements Component {
    //General Thirst Settings
    /**
     * The maximum amount of Water an entity can "contain".
     * The minimum is 0.
     */
    public float maxWaterCapacity = 100;

    /**
     * The current amount of Water the entity has
     */
    public float currentWaterCapacity = maxWaterCapacity;

    /**
     * The amount of Water decreased at each waterDecreaseInterval (below)
     */
    public float waterDecreaseAmount = 5;

    /**
     * The interval (in milliseconds) at which waterDecreaseAmount (above) is applied to the component
     */
    public int waterDecreaseInterval = 10000;

    //Sprint related settings.
    /**
     * Whether or not sprinting will affect the decrease rate of thirst
     */
    public boolean sprintDecrease;

    /**
     * The amount to multiply the waterDecreaseAmount while sprinting
     */
    public float sprintMultiplier = 1.5f;

    /**
     * Whether or not to enable loss of sprint when thirst goes below sprintLossThreshold
     */
    public boolean sprintLoss = true;

    /**
     * If the entity's water capacity is <= to this threshold, sprinting is disabled!
     */
    public float sprintLossThreshold = 0;
}
