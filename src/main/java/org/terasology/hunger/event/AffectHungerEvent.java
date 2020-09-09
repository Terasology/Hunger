// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.hunger.event;

import org.terasology.engine.entitySystem.event.AbstractValueModifiableEvent;

/**
 * This event is sent out by the {@link org.terasology.hunger.HungerAuthoritySystem} to allow for other systems to
 * modify hunger decay.
 */
public class AffectHungerEvent extends AbstractValueModifiableEvent {
    public AffectHungerEvent(float baseValue) {
        super(baseValue);
    }
}
