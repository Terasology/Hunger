// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.hunger.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.logic.common.ActivateEvent;

/**
 * This event is triggered after the Activate event has been consumed upon eating of a food item
 */
public class FoodConsumedEvent implements Event {
    private final EntityRef instigator;
    private final EntityRef target;

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
