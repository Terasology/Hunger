// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.hunger.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.utilities.modifiable.ModifiableValue;


/**
 * This component uses the ModifiableValue type Any modifications to the data members of this component must be via
 * ModifiableValue modifiers ModifiableValue.getValue can be used to get the value of the data members
 */
public class FoodComponent implements Component {
    /**
     * The amount of hunger this food restores when used.
     */
    public ModifiableValue filling;
}
