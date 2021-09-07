/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.hunger.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/** This event is triggered after the Activate event has been consumed upon eating of a food item */
public class FoodConsumedEvent implements Event {
    private EntityRef instigator;
    private EntityRef target;

    public FoodConsumedEvent(ActivateEvent event) {
        this.instigator = event.getInstigator();
        this.target = event.getTarget();
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getTarget() {
        return target;
    }
}
