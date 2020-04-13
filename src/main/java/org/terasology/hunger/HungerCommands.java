/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.hunger.component.HungerComponent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;

@RegisterSystem
@Share(HungerCommands.class)
public class HungerCommands extends BaseComponentSystem {

    @In
    private Time time;

    /**
     * A command for testing the hunger level for an entity.
     *
     * @param client The entity who is checking it's hunger level.
     * @return Returns a message for the client informing them about their food level if they have one.
     */
    @Command(shortDescription = "Checks your hunger/food level.", runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String showHunger(@Sender EntityRef client) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        if (character.hasComponent(HungerComponent.class)) {
            HungerComponent hunger = character.getComponent(HungerComponent.class);
            return "Current Food Level: " + HungerUtils.getHungerForEntity(character) + "/" + hunger.maxFoodCapacity;
        } else {
            return "You don't have a hunger level.";
        }
    }

    /**
     * A command for modifying your hunger level.
     *
     * @param newFood The new hunger level for the client. This has to be above 0 and below the max food capacity.
     * @param client  The client which is changing it's hunger level.
     * @return Returns a message for the client telling him about their new hunger level if they have one.
     */
    @Command(shortDescription = "Sets your current hunger level.", runOnServer = true, requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setHunger(@Sender EntityRef client, @CommandParam(value = "FoodLevel") float newFood) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        if (!character.hasComponent(HungerComponent.class)) {
            return "You don't have a hunger level.";
        }
        HungerComponent hunger = character.getComponent(HungerComponent.class);
        if (newFood < 0) {
            hunger.lastCalculatedFood = 0;
            hunger.lastCalculationTime = time.getGameTimeInMs();
            character.saveComponent(hunger);
            return "Food level cannot be below 0. Setting to 0.";
        }
        if (newFood > hunger.maxFoodCapacity) {
            hunger.lastCalculatedFood = hunger.maxFoodCapacity;
            hunger.lastCalculationTime = time.getGameTimeInMs();
            character.saveComponent(hunger);
            return "Food level cannot be above Max Food Capacity. Setting to Max(" + hunger.maxFoodCapacity + ")";
        }
        hunger.lastCalculatedFood = newFood;
        hunger.lastCalculationTime = time.getGameTimeInMs();
        character.saveComponent(hunger);
        return "Food level successfully set to: " + newFood;
    }

    /**
     * A command for changing your maximum food level.
     *
     * @param newMax The new maximum food level. Has to be above 0.
     * @param client The client which is changing it's food level.
     * @return Returns a message for the client telling him wether the command was succesful.
     */
    @Command(shortDescription = "Sets your max food level.", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setMaxHunger(@Sender EntityRef client, @CommandParam(value = "MaxFoodLevel") float newMax) {
        EntityRef character = client.getComponent(ClientComponent.class).character;
        if (!character.hasComponent(HungerComponent.class)) {
            return "You don't have a hunger level.";
        }
        HungerComponent hunger = character.getComponent(HungerComponent.class);
        if (newMax <= 0) {
            hunger.maxFoodCapacity = 100;
            character.saveComponent(hunger);
            return "Max Food Level cannot be below or equal to 0. Setting to default (100)";
        }
        hunger.maxFoodCapacity = newMax;
        character.saveComponent(hunger);
        return "Max Food Level successfully set to: " + newMax;
    }
}
