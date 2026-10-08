package com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.SpecialItemInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Item cards with an effect (ERA, Artillery, Search, Draw). They are played from the hand for free, as many per turn
 * as you like. Names are placeholders. What the two effect values mean:
 * <ul>
 *   <li>ERA_PROTECTION: primary = percent less CHEMICAL damage</li>
 *   <li>ARTILLERY_STRIKE: primary = true damage to each target, secondary = how many targets (Legendary may also pick face-down ones)</li>
 *   <li>SEARCH_*: primary = how many cards to take</li>
 *   <li>DRAW_CARDS: primary = how many cards to draw</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public enum SpecialItem implements SpecialItemInterface {

    /*==================== ERA (attached; steady chemical resistance) ====================*/

    LIGHT_ERA("Light ERA Tiles", "Attach to one of your vehicles. It takes 20% less chemical damage until it is destroyed", CardLevel.COMMON, ItemType.SPECIAL, 1, SpecialItemEffect.ERA_PROTECTION, 20.0, 0.0),
    ERA_BLOCK_KIT("ERA Block Kit", "Attach to one of your vehicles. It takes 25% less chemical damage until it is destroyed", CardLevel.UNCOMMON, ItemType.SPECIAL, 1, SpecialItemEffect.ERA_PROTECTION, 25.0, 0.0),
    HEAVY_ERA("Heavy ERA Package", "Attach to one of your vehicles. It takes 35% less chemical damage until it is destroyed", CardLevel.RARE, ItemType.SPECIAL, 1, SpecialItemEffect.ERA_PROTECTION, 35.0, 0.0),
    COMPOSITE_ERA("Composite ERA Array", "Attach to one of your vehicles. It takes 40% less chemical damage until it is destroyed", CardLevel.EPIC, ItemType.SPECIAL, 1, SpecialItemEffect.ERA_PROTECTION, 40.0, 0.0),
    ADVANCED_ERA("Advanced ERA Suite", "Attach to one of your vehicles. It takes 50% less chemical damage until it is destroyed", CardLevel.LEGENDARY, ItemType.SPECIAL, 1, SpecialItemEffect.ERA_PROTECTION, 50.0, 0.0),

    /*==================== ARTILLERY (true damage; Legendary also hits face-down vehicles) ====================*/

    MORTAR_STRIKE("Mortar Strike", "Deal 20 true damage to 1 face-up enemy vehicle", CardLevel.COMMON, ItemType.SPECIAL, 1, SpecialItemEffect.ARTILLERY_STRIKE, 20.0, 1.0),
    HOWITZER_FIRE_MISSION("Howitzer Fire Mission", "Deal 25 true damage to 1 face-up enemy vehicle", CardLevel.UNCOMMON, ItemType.SPECIAL, 1, SpecialItemEffect.ARTILLERY_STRIKE, 25.0, 1.0),
    ROCKET_SALVO("Rocket Salvo", "Deal 30 true damage to each of up to 2 face-up enemy vehicles", CardLevel.RARE, ItemType.SPECIAL, 1, SpecialItemEffect.ARTILLERY_STRIKE, 30.0, 2.0),
    HEAVY_ROCKET_BARRAGE("Heavy Rocket Barrage", "Deal 35 true damage to each of up to 2 face-up enemy vehicles", CardLevel.EPIC, ItemType.SPECIAL, 1, SpecialItemEffect.ARTILLERY_STRIKE, 35.0, 2.0),
    STRATEGIC_BOMBARDMENT("Strategic Bombardment", "Deal 40 true damage to each of up to 3 enemy vehicles, even face-down ones (they are turned face up)", CardLevel.LEGENDARY, ItemType.SPECIAL, 1, SpecialItemEffect.ARTILLERY_STRIKE, 40.0, 3.0),

    /*==================== SEARCH (find cards of one kind in your deck) ====================*/

    SUPPLY_REQUEST("Supply Request", "Search your deck for 1 resource card, show it, and put it in your hand", CardLevel.COMMON, ItemType.SPECIAL, 1, SpecialItemEffect.SEARCH_RESOURCES, 1.0, 0.0),
    LOGISTICS_REQUEST("Logistics Request", "Search your deck for up to 2 resource cards, show them, and put them in your hand", CardLevel.RARE, ItemType.SPECIAL, 1, SpecialItemEffect.SEARCH_RESOURCES, 2.0, 0.0),
    QUARTERMASTER_DEPOT("Quartermaster Depot", "Search your deck for up to 3 resource cards, show them, and put them in your hand", CardLevel.LEGENDARY, ItemType.SPECIAL, 1, SpecialItemEffect.SEARCH_RESOURCES, 3.0, 0.0),

    ARMOR_REQUISITION("Armor Requisition", "Search your deck for 1 tank, show it, and put it in your hand", CardLevel.COMMON, ItemType.SPECIAL, 1, SpecialItemEffect.SEARCH_TANKS, 1.0, 0.0),
    ARMORED_REINFORCEMENTS("Armored Reinforcements", "Search your deck for up to 2 tanks, show them, and put them in your hand", CardLevel.RARE, ItemType.SPECIAL, 1, SpecialItemEffect.SEARCH_TANKS, 2.0, 0.0),
    ARMOR_DIVISION_TRANSFER("Armor Division Transfer", "Search your deck for up to 3 tanks, show them, and put them in your hand", CardLevel.LEGENDARY, ItemType.SPECIAL, 1, SpecialItemEffect.SEARCH_TANKS, 3.0, 0.0),

    SUPPORT_REQUEST("Support Request", "Search your deck for 1 support vehicle (not a tank), show it, and put it in your hand", CardLevel.COMMON, ItemType.SPECIAL, 1, SpecialItemEffect.SEARCH_SUPPORT, 1.0, 0.0),
    SPECIALIST_CALL_UP("Specialist Call-Up", "Search your deck for up to 2 support vehicles (not tanks), show them, and put them in your hand", CardLevel.RARE, ItemType.SPECIAL, 1, SpecialItemEffect.SEARCH_SUPPORT, 2.0, 0.0),
    JOINT_TASK_FORCE("Joint Task Force", "Search your deck for up to 3 support vehicles (not tanks), show them, and put them in your hand", CardLevel.LEGENDARY, ItemType.SPECIAL, 1, SpecialItemEffect.SEARCH_SUPPORT, 3.0, 0.0),

    /*==================== DRAW ====================*/

    FIELD_REPORT("Field Report", "Draw 1 card", CardLevel.COMMON, ItemType.SPECIAL, 1, SpecialItemEffect.DRAW_CARDS, 1.0, 0.0),
    INTEL_BRIEFING("Intel Briefing", "Draw 1 card", CardLevel.UNCOMMON, ItemType.SPECIAL, 1, SpecialItemEffect.DRAW_CARDS, 1.0, 0.0),
    STAFF_PLANNING("Staff Planning", "Draw 2 cards", CardLevel.RARE, ItemType.SPECIAL, 1, SpecialItemEffect.DRAW_CARDS, 2.0, 0.0),
    REINFORCEMENT_ORDERS("Reinforcement Orders", "Draw 2 cards", CardLevel.EPIC, ItemType.SPECIAL, 1, SpecialItemEffect.DRAW_CARDS, 2.0, 0.0),
    TOTAL_MOBILIZATION("Total Mobilization", "Draw 3 cards", CardLevel.LEGENDARY, ItemType.SPECIAL, 1, SpecialItemEffect.DRAW_CARDS, 3.0, 0.0);

    private final String itemName;
    private final String itemDescription;
    private final CardLevel cardLevel;
    private final ItemType itemType;
    private final Integer count;
    private final SpecialItemEffect specialItemEffect;
    private final Double primaryEffectValue;
    private final Double secondaryEffectValue;
}
