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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.hunger.component.DrinkComponent;
import org.terasology.hunger.component.ThirstComponent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class ThirstAuthoritySystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private Time time;

    @ReceiveEvent(components = {ThirstComponent.class})
    public void onPlayerRespawn(OnPlayerSpawnedEvent event, EntityRef player) {
        ThirstComponent thirst = player.getComponent(ThirstComponent.class);
        thirst.lastCalculatedWater = thirst.maxWaterCapacity;
        thirst.lastCalculationTime = time.getGameTimeInMs();
        player.saveComponent(thirst);
    }

    @ReceiveEvent
    public void onPlayerFirstSpawn(OnPlayerSpawnedEvent event, EntityRef player) {
        if (!player.hasComponent(ThirstComponent.class)) {
            ThirstComponent thirst = new ThirstComponent();
            thirst.lastCalculatedWater = thirst.maxWaterCapacity;
            thirst.lastCalculationTime = time.getGameTimeInMs();
            player.addComponent(thirst);
        }
    }

    @ReceiveEvent
    public void beforeRemoval(BeforeDeactivateComponent event, EntityRef entity, ThirstComponent thirst) {
        thirst.lastCalculatedWater = HungerAndThirstUtils.getThirstForEntity(entity);
        thirst.lastCalculationTime = time.getGameTimeInMs();
        entity.saveComponent(thirst);
    }

    @ReceiveEvent
    public void drinkConsumed(ActivateEvent event, EntityRef item, DrinkComponent drink) {
        float filling = drink.filling;
        EntityRef instigator = event.getInstigator();
        ThirstComponent thirst = instigator.getComponent(ThirstComponent.class);
        if (thirst != null) {
            thirst.lastCalculatedWater = Math.min(thirst.maxWaterCapacity, HungerAndThirstUtils.getThirstForEntity(instigator) + filling);
            thirst.lastCalculationTime = time.getGameTimeInMs();
            instigator.saveComponent(thirst);
        }
    }

    @ReceiveEvent
    public void characterMoved(CharacterMoveInputEvent event, EntityRef character, ThirstComponent thirst) {
        final float expectedDecay = event.isRunning() ? thirst.sprintDecayPerSecond : thirst.normalDecayPerSecond;
        if (expectedDecay != thirst.waterDecayPerSecond) {
            // Recalculate current thirst and apply new decay
            thirst.lastCalculatedWater = HungerAndThirstUtils.getThirstForEntity(character);
            thirst.lastCalculationTime = time.getGameTimeInMs();
            thirst.waterDecayPerSecond = expectedDecay;
            character.saveComponent(thirst);
        }
    }
}
