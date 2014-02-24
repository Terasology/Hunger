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
public class ThirstComponent implements Component {
    //General Thirst Settings
    /**
     * The maximum amount of Water an entity can "contain".
     * The minimum is 0.
     */
    public float maxWaterCapacity = 100;

    public float lastCalculatedWater;

    public long lastCalculationTime;

    /**
     * The decay of thirst under normal movement conditions
     */
    public float normalDecayPerSecond = 0.05f;
    /**
     * The decay of thirst under sprint movement conditions
     */
    public float sprintDecayPerSecond = 0.2f;

    /**
     * Current decay of thirst
     */
    public float waterDecayPerSecond = normalDecayPerSecond;

    /**
     * If the entity's water capacity is < to this threshold, sprinting is disabled!
     */
    public float sprintLossThreshold = 50;
}
