/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
